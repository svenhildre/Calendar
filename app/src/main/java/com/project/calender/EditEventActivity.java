package com.project.calender;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private TextView eventTimeTextView, selectedTimeTextView, reminderTextView, reminderStatusTextView;
    private ImageButton updateEventButton, deleteEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String eventId;

    private int selectedHour, selectedMinute;
    private boolean isReminderEnabled = false;
    private int selectedReminder = -1;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private boolean isAlarmSet = false;

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
        reminderTextView = findViewById(R.id.reminderTextView);
        reminderStatusTextView = findViewById(R.id.ReminderStatus);

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
        checkAlarmStatus();

        // Etkinliği sil
        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEvent();
            }
        });

        // ReminderTextView'a tıklandığında popup_reminder.xml aç
        reminderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReminderPopup();
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
                                isReminderEnabled = event.isReminderEnabled();
                                selectedReminder = event.getSelectedReminder();
                                updateReminderStatusText();
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

        // Etkinlik adı, zamanı ve açıklaması zorunlu olduğu için kontrol ediyoruz
        if (name.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", name);
        eventMap.put("time", time);
        eventMap.put("description", description);
        eventMap.put("reminderEnabled", isReminderEnabled);
        eventMap.put("selectedReminder", selectedReminder); // Değişiklik burada

        db.collection("users").document(currentUserID).collection("events").document(eventId)
                .update(eventMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditEventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                        if (isReminderEnabled) {
                            setAlarm();
                        } else {
                            cancelAlarm();
                        }
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
    private void showReminderPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_reminder, null);

        // Popup bileşenleri örnekleniyor
        final Switch reminderSwitch = popupView.findViewById(R.id.reminderSwitch);
        final RadioGroup reminderRadioGroup = popupView.findViewById(R.id.reminderRadioGroup);

        // Mevcut reminder ayarlarını popup bileşenlerine yükle
        reminderSwitch.setChecked(isReminderEnabled);
        if (isReminderEnabled) {
            reminderRadioGroup.check(getReminderRadioButtonId(selectedReminder));
        }

        // Popup oluşturuluyor
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Popup dışında tıklama olayını kapat
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.reminder)));
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Reminder switch değişikliğini takip et
        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isReminderEnabled = isChecked;
                updateReminderStatusText();
                if (isReminderEnabled) {
                    setAlarm();
                } else {
                    cancelAlarm();
                }
            }
        });

        // Reminder seçeneği değişikliğini takip et
        reminderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                selectedReminder = getSelectedReminderFromRadioButtonId(checkedId);
                updateReminderStatusText();
            }
        });
    }

    // Reminder seçeneğine göre ilgili RadioButton ID'sini döndürür
    private int getReminderRadioButtonId(int reminder) {
        switch (reminder) {
            case 5:
                return R.id.radio5Minutes;
            case 15:
                return R.id.radio15Minutes;
            case 30:
                return R.id.radio30Minutes;
            case 60:
                return R.id.radio1Hour;
            case 1440:
                return R.id.radio1Day;
            case 10080:
                return R.id.radio1Week;
            default:
                return R.id.radioBeforeEvent;
        }
    }

    // RadioButton ID'sine göre seçilen reminder değerini döndürür
    private int getSelectedReminderFromRadioButtonId(int radioButtonId) {
        switch (radioButtonId) {
            case R.id.radio5Minutes:
                return 5;
            case R.id.radio15Minutes:
                return 15;
            case R.id.radio30Minutes:
                return 30;
            case R.id.radio1Hour:
                return 60;
            case R.id.radio1Day:
                return 1440;
            case R.id.radio1Week:
                return 10080;
            default:
                return -1;
        }
    }

    // Reminder durumunu günceller ve ReminderStatus TextView'ına yazar
    private void updateReminderStatusText() {
        if (isReminderEnabled) {
            String reminderText;
            switch (selectedReminder) {
                case 5:
                    reminderText = "5 minutes";
                    break;
                case 15:
                    reminderText = "15 minutes";
                    break;
                case 30:
                    reminderText = "30 minutes";
                    break;
                case 60:
                    reminderText = "1 hour";
                    break;
                case 1440:
                    reminderText = "1 day";
                    break;
                case 10080:
                    reminderText = "1 week";
                    break;
                default:
                    reminderText = "Before event";
                    break;
            }
            reminderStatusTextView.setText(reminderText);
        } else {
            reminderStatusTextView.setText("Disabled");
        }
    }
    private void setAlarm() {
        if (isAlarmSet) {
            cancelAlarm();
        }

        int reminderInMilliseconds = selectedReminder * 60 * 1000;
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("eventName", eventNameEditText.getText().toString());
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + reminderInMilliseconds, alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + reminderInMilliseconds, alarmIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + reminderInMilliseconds, alarmIntent);
        }

        isAlarmSet = true;
    }

    private void cancelAlarm() {
        if (isAlarmSet) {
            alarmManager.cancel(alarmIntent);
            isAlarmSet = false;
        }
    }

    private void checkAlarmStatus() {
        if (isReminderEnabled && !isAlarmSet) {
            setAlarm();
        } else if (!isReminderEnabled && isAlarmSet) {
            cancelAlarm();
        }
    }
}
