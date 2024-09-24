package com.example.meetingscheduler;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MeetingSchedulerActivity extends Activity {

    private EditText editTextTitle, editTextParticipants;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button buttonSetMeeting = findViewById(R.id.buttonSetMeeting);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextParticipants = findViewById(R.id.editTextParticipants);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);

        buttonSetMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleMeeting();
            }
        });
    }

    private void scheduleMeeting() {
        String title = editTextTitle.getText().toString().trim();
        String participants = editTextParticipants.getText().toString().trim();
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1; // Months are 0-based
        int day = datePicker.getDayOfMonth();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        // Prepare meeting data to be saved
        ContentValues values = new ContentValues();
        values.put(MeetingProvider.KEY_TITLE, title);
        values.put(MeetingProvider.KEY_PARTICIPANTS, participants);
        values.put(MeetingProvider.KEY_DATE, year + "-" + month + "-" + day);
        values.put(MeetingProvider.KEY_TIME, hour + ":" + minute);

        // Insert the meeting data using the content provider
        Uri uri = getContentResolver().insert(MeetingProvider.CONTENT_URI, values);

        if (uri != null) {
            Toast.makeText(this, "Meeting set successful", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Failed to set meeting", Toast.LENGTH_SHORT).show();
        }
    }
}