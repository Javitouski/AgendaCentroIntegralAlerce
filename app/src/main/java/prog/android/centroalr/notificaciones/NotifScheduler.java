package prog.android.centroalr.notificaciones;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotifScheduler {

    public static void programar(Context ctx, long triggerTime, String titulo, String mensaje) {

        Intent i = new Intent(ctx, NotifReceiver.class);
        i.putExtra("titulo", titulo);
        i.putExtra("mensaje", mensaje);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                (int) System.currentTimeMillis(),
                i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
    }
}
