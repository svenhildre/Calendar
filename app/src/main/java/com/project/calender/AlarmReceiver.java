package com.project.calender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Alarm tetiklendiğinde gerçekleştirilecek işlemler burada yer alır
        Toast.makeText(context, "Alarm Triggered", Toast.LENGTH_SHORT).show();

        // Alarm sesi çalınması için kod örneği
        String alarmSoundUriString = intent.getStringExtra("alarmSoundUri");
        if (alarmSoundUriString != null) {
            Uri alarmSoundUri = Uri.parse(alarmSoundUriString);
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSoundUri);
            ringtone.play();
        }
    }
}
