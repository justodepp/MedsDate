package com.medsdate.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.medsdate.R
import com.medsdate.ui.main.GalleryImageAdapter
import java.io.IOException

class DialogGalleryFragment : DialogFragment() {
    private var wantCancelable: Boolean = false
    private var listener: OnDialogGalleryListener? = null
    private var dialogView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        this.isCancelable = wantCancelable
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        dialogView = inflater.inflate(R.layout.dialog_gallery, null)
        init()

        // Create the AlertDialog object and return it
        builder.setView(dialogView)
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(wantCancelable)
        return builder.create()
    }

    private fun init() {
        var images: Array<String?>? = arrayOfNulls(0)
        try {
            images = activity!!.assets.list("img_meds")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val listImages = images
        val gridView = dialogView!!.findViewById<GridView>(R.id.grid_gallery)
        val mAdapter = GalleryImageAdapter(context!!, listImages)
        gridView.adapter = mAdapter
        gridView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            listener!!.setImageFromGallery(listImages?.get(i))
            dismiss()
        }
    }

    interface OnDialogGalleryListener {
        fun setImageFromGallery(name: String?)
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(cancelable: Boolean = true, listener: OnDialogGalleryListener? = null): DialogGalleryFragment {
            val fragment = DialogGalleryFragment()
            val args = Bundle()
            fragment.arguments = args
            fragment.wantCancelable = cancelable
            if (listener != null) {
                fragment.listener = listener
            }
            return fragment
        }
    }
}