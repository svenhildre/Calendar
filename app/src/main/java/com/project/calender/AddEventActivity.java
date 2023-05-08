package com.project.calender;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventTimeEditText, eventDescriptionEditText;
    private Button saveEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private Calendar selectedDate;

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
        eventTimeEditText = findViewById(R.id.eventTimeEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        saveEventButton = findViewById(R.id.saveEventButton);

        // Seçilen tarihi almak için Calendar nesnesi oluşturuluyor
        selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(getIntent().getLongExtra("selectedDate", 0));

        // Yeni etkinliği kaydet
        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEvent();
            }
        });
    }

    // Yeni etkinliği Firebase Firestore veritabanına kaydet
    private void saveEvent() {
        String name = eventNameEditText.getText().toString();
        String time = eventTimeEditText.getText().toString();
        String description = eventDescriptionEditText.getText().toString();

        // Seçilen tarihi al
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate.getTimeInMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Etkinlik nesnesi oluştur
        Event event = new Event(name, time, description, year, month, dayOfMonth);

        // Firebase Firestore veritabanına etkinliği kaydet
        db.collection("users").document(currentUserID).collection("events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
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
}
