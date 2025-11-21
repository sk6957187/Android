package com.nayak.alarmapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String activeAlarm = "";
    private ListView listView;

    public static final int REQUEST_CODE = 1;
    public static List<Alarm> alarmList = new ArrayList<>();
    private CustomAdapter customAdapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            db = new DatabaseHelper(this);

            MaterialButton button = findViewById(R.id.add);
            button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v){
                   Intent intent = new Intent(MainActivity.this, AddActivity.class);
                   startActivityForResult(intent, REQUEST_CODE);
               }
            });

            listView = findViewById(R.id.listView);
            List<Alarm> list = db.getAllAlarms();
            alarmList.addAll(list);
            customAdapter = new CustomAdapter(MainActivity.this, alarmList);
            listView.setAdapter(customAdapter);
        } catch (Throwable t) {
            // Write stack trace to external files directory so user can retrieve it via adb
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                String trace = sw.toString();
                File out = new File(getExternalFilesDir(null), "crash_trace.txt");
                FileWriter fw = new FileWriter(out, false);
                fw.write(trace);
                fw.close();
            } catch (IOException ioe) {
                // ignore secondary failures
            }
            Toast.makeText(this, "Startup error logged; see crash_trace.txt", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            boolean needRefresh = data.getExtras().getBoolean("needRefresh");
            if(needRefresh){
                alarmList.clear();
                List<Alarm> list = db.getAllAlarms();
                alarmList.addAll(list);
                customAdapter.notifyDataSetChanged();
            }
        }
    }
}