/*
 * Copyright (c) 2018. Gianni Andrea Cavalli
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.medsdate.ui.dialogs

import android.R.attr
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.medsdate.R
import com.medsdate.data.db.model.MedicineEntry
import com.medsdate.ui.dialogs.DialogGalleryFragment.Companion.newInstance
import com.medsdate.ui.dialogs.DialogGalleryFragment.OnDialogGalleryListener
import com.medsdate.ui.viewmodel.MedsViewModel
import com.medsdate.utils.Utility.loadImage
import java.io.IOException
import java.util.*


class BottomSheetDialogMedicineFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var wantCancelable = false
    private var listener: OnDialogMedicineListener? = null
    private var mMedicineId = DEFAULT_TASK_ID
    private var dialogView: View? = null
    private var mImage: ImageView? = null
    private var myCalendar: Calendar? = null
    private var date: OnDateSetListener? = null
    private var mDay: TextView? = null
    private var mMonth: TextView? = null
    private var mYear: TextView? = null
    private var mName: EditText? = null
    private var mQuantity: TextView? = null
    private var currentQuantity = 1
    private var imageName: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mMedicineId = arguments!!.getInt(EXTRA_TASK_ID, DEFAULT_TASK_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.isCancelable = wantCancelable
        // Inflate the layout for this fragment
        dialogView = inflater.inflate(R.layout.bottomsheet_dialog_medicine, container, false)
        dialogView!!.findViewById<View>(R.id.spinner_quantity).setOnClickListener(this)
        dialogView!!.findViewById<View>(R.id.txt_date_expire).setOnClickListener(this)
        dialogView!!.findViewById<View>(R.id.txt_cancel).setOnClickListener(this)
        dialogView!!.findViewById<View>(R.id.txt_save).setOnClickListener(this)
        dialogView!!.findViewById<View>(R.id.imageView).setOnClickListener(this)
        init()
        return dialogView
    }

    override fun onClick(view: View) {
        if (view.id == R.id.spinner_quantity) {
            showNumberPicker(currentQuantity)
        } else if (view.id == R.id.txt_date_expire) {
            dateChooser()
            val dpd = DatePickerDialog(context!!, date, myCalendar!!.get(Calendar.YEAR), myCalendar!![Calendar.MONTH],
                    myCalendar!![Calendar.DAY_OF_MONTH])
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        } else if (view.id == R.id.txt_cancel) {
            dismiss()
        } else if (view.id == R.id.txt_save) {
            if (mName!!.text.toString() != "" && mQuantity!!.text != "") {
                if (listener != null) {
                    this@BottomSheetDialogMedicineFragment.dialog!!.cancel()
                    val quantity = mQuantity!!.text.toString()
                    val medicineEntry = MedicineEntry(
                            "CATEGORY",
                            mName!!.text.toString(),
                            myCalendar!!.time,
                            if (quantity.contains("Sele")) 1 else quantity.toInt(),
                            imageName!!,
                            Date())
                    if (mMedicineId != DEFAULT_TASK_ID) {
                        medicineEntry.id = mMedicineId
                        listener!!.updateMedicine(medicineEntry)
                    } else {
                        listener!!.saveMedicine(medicineEntry)
                    }
                }
            } else {
                showAlertWithMessage(getString(R.string.error_insert_update))
            }
        } else if (view.id == R.id.imageView) {
            showImagePickerOptions(this.requireContext())

        }
    }

    private fun showImagePickerOptions(context: Context) {
        // setup the alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.lbl_set_profile_photo))

        // add a list
        val animals = arrayOf(context.getString(R.string.lbl_take_camera_picture),
                context.getString(R.string.lbl_choose_from_gallery),
                context.getString(R.string.lbl_choose_from_internal))
        builder.setItems(animals) { dialog: DialogInterface?, which: Int ->
            when (which) {
                0 -> onTakeCameraSelected()
                1 -> onChooseGallerySelected()
                2 -> internalImage()
            }
        }

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun internalImage() {
        newInstance(true, object : OnDialogGalleryListener {
            override fun setImageFromGallery(name: String?) {
                imageName = name
                Glide.with(context!!)
                        .asDrawable()
                        .load(loadImage(context!!, name!!))
                        .into(mImage!!)
                //mImage.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mImage!!.imageTintList = ColorStateList.valueOf(resources.getColor(android.R.color.transparent))
            }
        }).show(activity!!.supportFragmentManager, "DialogGalleryFragment")
    }

    private fun onChooseGallerySelected() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_IMAGE)
    }

    private fun onTakeCameraSelected() {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileName)
        if (takePictureIntent.resolveActivity(activity?.packageManager!!) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE)
        }
    }

    private fun init() {
        mQuantity = dialogView!!.findViewById(R.id.spinner_quantity)
        mName = dialogView!!.findViewById(R.id.txt_name_medicine)
        mDay = dialogView!!.findViewById(R.id.day_number_text)
        mMonth = dialogView!!.findViewById(R.id.month_text)
        mYear = dialogView!!.findViewById(R.id.year_text)
        mImage = dialogView!!.findViewById(R.id.imageView)
        if (mMedicineId != DEFAULT_TASK_ID) {
            val viewModel = ViewModelProviders.of(this).get(MedsViewModel::class.java)
            viewModel.load(mMedicineId).observe(viewLifecycleOwner, object : Observer<MedicineEntry?> {
                override fun onChanged(medicineEntry: MedicineEntry?) {
                    viewModel.load(mMedicineId).removeObserver(this)
                    populateUI(medicineEntry)
                }
            })
        } else {
            myCalendar = Calendar.getInstance()
            mDay?.text = myCalendar?.get(Calendar.DAY_OF_MONTH).toString()
            mMonth?.text = myCalendar?.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            mYear?.text = myCalendar?.get(Calendar.YEAR).toString()
        }
    }

    private fun populateUI(medicineEntry: MedicineEntry?) {
        if (medicineEntry == null) {
            return
        }
        mName!!.setText(medicineEntry.name)
        mQuantity!!.text = medicineEntry.quantity.toString()
        myCalendar = Calendar.getInstance()
        myCalendar?.time = medicineEntry.expireAt
        mDay!!.text = myCalendar?.get(Calendar.DAY_OF_MONTH).toString()
        mMonth!!.text = myCalendar?.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        mYear!!.text = myCalendar?.get(Calendar.YEAR).toString()
        if (medicineEntry.image != "") {
            Glide.with(context!!)
                    .asDrawable()
                    .load(loadImage(context!!, medicineEntry.image!!))
                    .into(mImage!!)
            //mImage.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mImage!!.imageTintList = ColorStateList.valueOf(resources.getColor(android.R.color.transparent))
        }
    }

    interface OnDialogMedicineListener {
        fun saveMedicine(medicineEntry: MedicineEntry)
        fun updateMedicine(medicineEntry: MedicineEntry)
    }

    private fun dateChooser() {
        myCalendar = Calendar.getInstance()
        date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar?.set(Calendar.YEAR, year)
            myCalendar?.set(Calendar.MONTH, monthOfYear)
            myCalendar?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
    }

    private fun updateLabel() {
        mDay!!.text = myCalendar!![Calendar.DAY_OF_MONTH].toString()
        mMonth!!.text = myCalendar!!.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        mYear!!.text = myCalendar!![Calendar.YEAR].toString()
    }

    private fun showNumberPicker(value: Int = currentQuantity) {
        val linearLayout = RelativeLayout(context)
        val aNumberPicker = NumberPicker(context)
        aNumberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        aNumberPicker.maxValue = 10
        aNumberPicker.value = value
        aNumberPicker.minValue = 1
        val params = RelativeLayout.LayoutParams(50, 50)
        val numPicerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        linearLayout.layoutParams = params
        linearLayout.addView(aNumberPicker, numPicerParams)
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Seleziona la quantità")
        alertDialogBuilder.setView(linearLayout)
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok"
                ) { dialog, id ->
                    currentQuantity = aNumberPicker.value
                    mQuantity!!.text = aNumberPicker.value.toString()
                }
                .setNegativeButton("Cancel"
                ) { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun showAlertWithMessage(message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton("Ok") { dialog: DialogInterface, i: Int -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data
                try {
                    // You can update this bitmap to your server
                    val bitmap = getBitmap(this.context?.contentResolver, uri)

                    // loading image
                    Glide.with(context!!)
                            .asDrawable()
                            .load(bitmap)
                            .into(mImage!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val EXTRA_TASK_ID = "extraTaskId"
        const val REQUEST_IMAGE = 0

        // Constant for default task id to be used when not in update mode
        private const val DEFAULT_TASK_ID = -1

        @JvmOverloads
        fun newInstance(cancelable: Boolean = true, listener: OnDialogMedicineListener? = null): BottomSheetDialogMedicineFragment {
            val fragment = BottomSheetDialogMedicineFragment()
            val args = Bundle()
            fragment.arguments = args
            fragment.wantCancelable = cancelable
            if (listener != null) {
                fragment.listener = listener
            }
            return fragment
        }

        fun newInstance(cancelable: Boolean, extra_id: Int, listener: OnDialogMedicineListener?): BottomSheetDialogMedicineFragment {
            val fragment = BottomSheetDialogMedicineFragment()
            fragment.wantCancelable = cancelable
            fragment.mMedicineId = extra_id
            if (listener != null) {
                fragment.listener = listener
            }
            return fragment
        }
    }
}