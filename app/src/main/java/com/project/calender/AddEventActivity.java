package com.project.calender;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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

    private EditText eventNameEditText, eventDescriptionEditText;
    private TextView eventTimeButton, selectedTimeTextView, reminderTextView, reminderStatusTextView;
    private ImageButton saveEventButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Calendar selectedDate;
    private int selectedHour, selectedMinute;
    private boolean isReminderEnabled = false;
    private int selectedReminder = -1;

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
        reminderTextView = findViewById(R.id.reminderTextView);
        reminderStatusTextView = findViewById(R.id.ReminderStatus);

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

        // ReminderTextView'a tıklandığında popup_reminder.xml aç
        reminderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReminderPopup();
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

        // Etkinlik adı, zamanı ve açıklaması zorunlu olduğu için kontrol ediyoruz
        if (name.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Seçilen tarihi al
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int dayOfMonth = selectedDate.get(Calendar.DAY_OF_MONTH);

        // Etkinlik nesnesi oluştur
        Event event = new Event(null, name, time, description, year, month, dayOfMonth);

        // Reminder bilgilerini etkinlik nesnesine ekle
        event.setReminderEnabled(isReminderEnabled);
        event.setSelectedReminder(selectedReminder);

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
                        setAlarm(event);
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

    // Reminder popup'ını göstermek için method
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
                    reminderText = "Before the event";
                    break;
            }
            reminderStatusTextView.setText(reminderText);
        } else {
            reminderStatusTextView.setText("No reminder");
        }
    }

    // AlarmManager kullanarak alarm kurma methodu
    private void setAlarm(Event event) {
        if (isReminderEnabled && selectedReminder > 0) {
            long eventTimeInMillis = event.getEventTimeInMillis();

            // Özel alarm sesi için Uri oluştur
            Uri alarmSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soviet);

            // Alarm zamanını hesapla
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(eventTimeInMillis - (selectedReminder * 60000));

            // Alarm için PendingIntent oluştur
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("name", event.getName());
            intent.putExtra("alarmSoundUri", alarmSoundUri.toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // AlarmManager nesnesini al
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Alarmı kur
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }

}
