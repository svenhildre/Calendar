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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText;
    private TextView eventTimeTextView, selectedTimeTextView;
    private ImageButton updateEventButton, deleteEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String eventId;

    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Firebase Firestore ve Authentication örnekleri oluşturuluyor
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // XML dosyasındaki bileşenler örnekleniyor
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        updateEventButton = findViewById(R.id.updateEventButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);

        // EditEventActivity'ye geçiş yaparken eventID'yi al
        eventId = getIntent().getStringExtra("eventId");

        // Etkinliği yükle ve görüntüle
        loadEvent();

        // Zaman seçiciyi aç
        eventTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        // Etkinliği güncelle
        updateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEvent();
            }
        });

        // Etkinliği sil
        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEvent();
            }
        });
    }

    // Etkinliği yükle ve görüntüle
    private void loadEvent() {
        db.collection("users").document(currentUserID).collection("events").document(eventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Event event = documentSnapshot.toObject(Event.class);
                            if (event != null) {
                                eventNameEditText.setText(event.getName());
                                selectedTimeTextView.setText(event.getTime());
                                eventDescriptionEditText.setText(event.getDescription());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditEventActivity.this, "Error loading event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Zaman seçiciyi göster
    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                selectedHour = hourOfDay;
                selectedMinute = minute;

                // Seçilen saati ve dakikayı formatlayarak metin görüntüleyicide göster
                String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                selectedTimeTextView.setText(time);
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    // Etkinliği güncelle
    private void updateEvent() {
        String name = eventNameEditText.getText().toString();
        String time = selectedTimeTextView.getText().toString();
        String description = eventDescriptionEditText.getText().toString();

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", name);
        eventMap.put("time", time);
        eventMap.put("description", description);

        db.collection("users").document(currentUserID).collection("events").document(eventId)
                .update(eventMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditEventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditEventActivity.this, "Error updating event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Etkinliği sil
    private void deleteEvent() {
        db.collection("users").document(currentUserID).collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditEventActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditEventActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
