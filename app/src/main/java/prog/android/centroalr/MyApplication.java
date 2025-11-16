package prog.android.centroalr;

import android.content.pm.ApplicationInfo;
import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

// Importa modelo de Usuario
import prog.android.centroalr.model.Usuario;

public class MyApplication extends Application {

    // Añade la variable "global" para guardar el perfil
    // Esta será el "archivador"
    private Usuario usuarioActual;

    @Override
    public void onCreate() {
        super.onCreate();

        // Código de App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }
    }

    // Añade los métodos para acceder al "archivador"

    /**
     * Método para GUARDAR el perfil del usuario en el almacén global.
     * El LoginController usará esto.
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /**
     * Método para LEER el perfil del usuario desde el almacén global.
     * Todas las demás pantallas usarán esto para chequear permisos.
     */
    public Usuario getUsuarioActual() {
        return this.usuarioActual;
    }

    /**
     * Método para LIMPIAR el perfil al cerrar sesión.
     */
    public void clearUsuarioActual() {
        this.usuarioActual = null;
    }
}