package com.ecertic.signWallet.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.ui.quote.ListActivity;

/**
 * Created by Juan on 02/04/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    int MID = 0;

    @Override

    public void onReceive(Context context, Intent intent)
    {
        int count = intent.getIntExtra("Pend",0);
        if (count > 0) {

            //Toast.makeText(context,"Hi",Toast.LENGTH_SHORT).show();
            long when = System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notificationIntent = new Intent(context, ListActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            String pend = String.valueOf(count);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            android.support.v4.app.NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_notif)
                    .setContentTitle("Sign Wallet")
                    .setContentText("Tienes " + pend + " contrato(s) pendiente(s) de firmar").setSound(alarmSound)
                    .setAutoCancel(true).setWhen(when)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
            notificationManager.notify(MID, mNotifyBuilder.build());
            MID++;

        }
    }
}
