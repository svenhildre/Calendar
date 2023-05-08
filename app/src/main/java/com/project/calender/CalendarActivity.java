package com.project.calender;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ListView eventListView;
    private Button addEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private List<Event> eventList;
    private EventListAdapter eventListAdapter;

    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Firebase Firestore ve Authentication örnekleri oluşturuluyor
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // XML dosyasındaki bileşenler örnekleniyor
        calendarView = findViewById(R.id.calendarView);
        eventListView = findViewById(R.id.eventsListView);
        addEventButton = findViewById(R.id.addEventButton);

        // Etkinlik listesi için özel bir adaptör oluşturuluyor
        eventList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(this, eventList);
        eventListView.setAdapter(eventListAdapter);

        // Seçilen tarihi almak için Calendar nesnesi oluşturuluyor
        selectedDate = Calendar.getInstance();

        // CalendarView'da tarih seçildiğinde etkinlikleri yükle
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                selectedDate.set(year, month, dayOfMonth);
                loadEvents(year, month, dayOfMonth);
            }
        });

        // Yeni etkinlik eklemek için Add Event butonuna tıklandığında AddEventActivity başlatılır
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CalendarActivity.this, AddEventActivity.class);
                intent.putExtra("selectedDate", selectedDate.getTimeInMillis());
                startActivity(intent);
            }
        });
    }

    // Seçilen tarihe ait etkinlikleri Firebase Firestore veritabanından yükle
    private void loadEvents(int year, int month, int dayOfMonth) {
        db.collection("users").document(currentUserID).collection("events")
                .whereEqualTo("year", year)
                .whereEqualTo("month", month)
                .whereEqualTo("dayOfMonth", dayOfMonth)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            eventList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Event event = document.toObject(Event.class);
                                eventList.add(event);
                            }
                            eventListAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(CalendarActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Etkinlik listesi için özel bir adaptör sınıfı
    private class EventListAdapter extends ArrayAdapter<Event> {

        public EventListAdapter(Context context, List<Event> events) {
            super(context, 0, events);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_item, parent, false);
            }

            TextView eventNameTextView = convertView.findViewById(R.id.eventNameTextView);
            TextView eventTimeTextView = convertView.findViewById(R.id.eventTimeTextView);
            TextView eventDescriptionTextView = convertView.findViewById(R.id.eventDescriptionTextView);

            Event event = getItem(position);

            eventNameTextView.setText(event.getName());
            eventTimeTextView.setText(event.getTime());
            eventDescriptionTextView.setText(event.getDescription());

            return convertView;
        }
    }
}