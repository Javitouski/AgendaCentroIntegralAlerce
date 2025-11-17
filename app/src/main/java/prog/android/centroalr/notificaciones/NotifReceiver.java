package prog.android.centroalr.notificaciones;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import prog.android.centroalr.R;

public class NotifReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {

        String titulo = i.getStringExtra("titulo");
        String mensaje = i.getStringExtra("mensaje");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NotifHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // puedes cambiarlo
                        .setContentTitle(titulo)
                        .setContentText(mensaje)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
