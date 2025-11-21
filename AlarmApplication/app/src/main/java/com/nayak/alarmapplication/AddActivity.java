package com.nayak.alarmapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText editText;
    private Button buttonSave, buttonCancel;

    private Alarm alarm;
    private boolean needRefresh; // ✅ fixed variable name
    private boolean isEdit = false;
    private int editId = -1;

    @RequiresApi(api = Build.VERSION_CODES.M) // ✅ corrected constant name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        timePicker = findViewById(R.id.timePicker);
        editText = findViewById(R.id.name);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);

        // Check if launched for edit
        if (getIntent() != null && getIntent().hasExtra("alarm_id")) {
            isEdit = true;
            editId = getIntent().getIntExtra("alarm_id", -1);
            int hour = getIntent().getIntExtra("alarm_hour", 0);
            int minute = getIntent().getIntExtra("alarm_minute", 0);
            String name = getIntent().getStringExtra("alarm_name");

            // Prepopulate UI
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            } else {
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);
            }
            editText.setText(name);
        }

        // ✅ Corrected 'new View.onClickListener()' to 'new View.OnClickListener()'
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, minute;

                // ✅ Handle time differently for Android versions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                } else {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                }

                String name = editText.getText().toString().trim();

                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                if (isEdit && editId != -1) {
                    // Update existing alarm
                    alarm = new Alarm(hour, minute, name, true);
                    alarm.setId(editId);
                    db.updateAlarm(alarm);
                } else {
                    // Insert new alarm
                    alarm = new Alarm(hour, minute, name, true);
                    long insertedId = db.addAlarm(alarm);
                    if (insertedId != -1) {
                        alarm.setId((int) insertedId);
                    }
                }

                needRefresh = true;
                finish(); // properly close the activity
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needRefresh = false;
                finish(); // ✅ correctly close without saving
            }
        });
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("needRefresh", true);
        this.setResult(RESULT_OK, data);
        super.finish();
    }
}
