package prog.android.centroalr.notificaciones;

import android.content.Context;
import android.content.Intent;

public class NotifHelper {

    public static void enviarNotificacion(Context context, String titulo, String mensaje) {
        Intent intent = new Intent(context, NotifReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensaje", mensaje);

        // Asegurar que se use applicationContext
        context.getApplicationContext().sendBroadcast(intent);
    }
}
