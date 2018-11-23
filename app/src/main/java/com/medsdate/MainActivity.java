package com.medsdate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.medsdate.ui.main.MedicineFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MedicineFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }
}

   /* *//*
             Set the Floating Action Button (FAB) to its corresponding View.
             Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
             to launch the AddTaskActivity.
             *//*
    FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        // Create a new intent to start an AddTaskActivity
        Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
        startActivity(addTaskIntent);
        }
        });*/

/*
DialogWelcomeFragment.newInstance(true, true, new DialogWelcomeFragment.OnDialogWelcomeListener() {
@Override
public void startTrial() {
        Intent tutorialIntent = new Intent(OptionActivity.this, TrialWizardActivity.class);
        tutorialIntent.putExtra("url", String.format(ApiUtil.getInstance().trialUrl(),
        UUIDHandler.getDeviceId(getApplicationContext())));
        startActivity(tutorialIntent);
        }

@Override
public void startAccedi() {
        Intent loginIntent = new Intent(OptionActivity.this, LoginActivity.class);
        loginIntent.putExtra(LoginActivity.LOGIN_FORM_NEEDED, true);
        startActivity(loginIntent);
        }
        }).show(getSupportFragmentManager(), "DialogWelcomeFragment");*/
