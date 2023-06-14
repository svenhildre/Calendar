package com.project.calender;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

// AddEventActivity.java

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText;
    private TextView eventTimeButton, selectedTimeTextView;
    private ImageButton saveEventButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Calendar selectedDate;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Firebase Firestore ve Authentication örnekleri oluşturuluyor
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // XML dosyasındaki bileşenler örnekleniyor
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventTimeButton = findViewById(R.id.eventTimeTextView);
        saveEventButton = findViewById(R.id.saveEventButton);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);

        // Seçilen tarihi almak için Calendar nesnesi oluşturuluyor
        selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(getIntent().getLongExtra("selectedDate", 0));

        // Etkinlik saatini seçmek için TimePickerDialog oluşturuluyor
        eventTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        // Yeni etkinliği kaydet
        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEvent();
            }
        });
    }

    // TimePickerDialog'u göstermek için method
    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;

                        // Seçilen saati etkinlik zamanı olarak ayarla
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        selectedTimeTextView.setText(time);
                    }
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    // Yeni etkinliği Firebase Firestore veritabanına kaydet
    private void saveEvent() {
        String name = eventNameEditText.getText().toString();
        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
        String description = eventDescriptionEditText.getText().toString();

        // Seçilen tarihi al
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int dayOfMonth = selectedDate.get(Calendar.DAY_OF_MONTH);

        // Etkinlik nesnesi oluştur
        Event event = new Event(null, name, time, description, year, month, dayOfMonth);

        // Firebase Firestore veritabanına etkinliği kaydet
        db.collection("users").document(currentUserID).collection("events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String eventId = documentReference.getId();
                        event.setEventId(eventId);
                        updateEvent(eventId, event);
                        Toast.makeText(AddEventActivity.this, "Event saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddEventActivity.this, "Error saving event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Etkinlik belgesinin eventId alanını güncelle
    private void updateEvent(String eventId, Event event) {
        db.collection("users").document(currentUserID).collection("events")
                .document(eventId)
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddEventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddEventActivity.this, "Error updating event", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
