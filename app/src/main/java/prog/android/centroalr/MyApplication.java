package prog.android.centroalr;

import android.content.pm.ApplicationInfo;
import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

// 1. Asegúrate de que extienda de Application
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 2. SOLO el código de App Check va aquí
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
}