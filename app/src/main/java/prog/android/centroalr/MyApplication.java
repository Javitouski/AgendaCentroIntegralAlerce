package prog.android.centroalr;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import prog.android.centroalr.model.Usuario;

public class MyApplication extends Application {

    public static final String CHANNEL_ID = "agenda_notif_channel";

    private Usuario usuarioActual;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        crearCanalNotificaciones();
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager manager = getSystemService(NotificationManager.class);

            // Si el canal ya existe, no lo vuelve a crear
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {

                NotificationChannel canal = new NotificationChannel(
                        CHANNEL_ID,
                        "Notificaciones de Agenda",
                        NotificationManager.IMPORTANCE_HIGH
                );
                canal.setDescription("Avisos de actividades, recordatorios y eventos.");

                manager.createNotificationChannel(canal);
            }
        }
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void clearUsuarioActual() {
        this.usuarioActual = null;
    }
}
