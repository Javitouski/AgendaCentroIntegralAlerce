package prog.android.centroalr.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotifHelper {

    public static final String CHANNEL_ID = "CITAS_CHANNEL";

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios del Centro Integral",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones previas a actividades y citas");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
