package com.project.calender;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton addEventButton;
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
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event event = eventListAdapter.getItem(position);
                if (event != null) {
                    Intent intent = new Intent(CalendarActivity.this, EditEventActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    startActivity(intent);
                }
            }
        });

        loadEvents(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
    }

    // Seçilen tarihe ait etkinlikleri Firebase Firestore veritabanından yükle
    private void loadEvents(int year, int month, int dayOfMonth) {
        db.collection("users").document(currentUserID).collection("events")
                .whereEqualTo("year", year)
                .whereEqualTo("month", month)
                .whereEqualTo("dayOfMonth", dayOfMonth)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        eventList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Event event = documentSnapshot.toObject(Event.class);
                            eventList.add(event);
                        }
                        eventListAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CalendarActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
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
            TextView reminderTextView = convertView.findViewById(R.id.reminderTextView);

            Event event = getItem(position);

            eventNameTextView.setText(event.getName());
            eventTimeTextView.setText(event.getTime());
            eventDescriptionTextView.setText(event.getDescription());

            String reminder = getReminderText(event.getSelectedReminder());
            reminderTextView.setText("Reminder: " + reminder);

            return convertView;
        }

        private String getReminderText(int reminder) {
            switch (reminder) {
                case 5:
                    return "5 minutes";
                case 10:
                    return "10 minutes";
                case 15:
                    return "15 minutes";
                case 30:
                    return "30 minutes";
                case 60:
                    return "1 hour";
                default:
                    return "None";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
    }
}
