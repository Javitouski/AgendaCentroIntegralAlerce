package prog.android.centroalr;

import android.content.pm.ApplicationInfo;
import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

// Importa modelo de Usuario
import prog.android.centroalr.model.Usuario;

// 🔔 Importación del canal de notificaciones
import prog.android.centroalr.notificaciones.NotifHelper;

public class MyApplication extends Application {

    private int contador;
    // Archivador global del usuario
    private Usuario usuarioActual;

    @Override
    public void onCreate() {
        super.onCreate();

        // Forzar SIEMPRE modo claro en toda la app,
        // ignorando el modo oscuro del teléfono.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // -------------------------
        // 🟩 Inicialización Firebase ------
        // -------------------------
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // Modo debug: usar AppCheck de depuración
            appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            // Modo release: usar Play Integrity
            appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }

        // ----------------------------------------------
        // 🔔 Crear canal de notificaciones (Android 8+)
        // ----------------------------------------------
        NotifHelper.crearCanal(this);
    }

    // ------------------------------------
    // Métodos del archivador de usuario
    // ------------------------------------
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return this.usuarioActual;
    }

    public void clearUsuarioActual() {
        this.usuarioActual = null;
    }
}
