package prog.android.centroalr.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.R;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper flipper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el layout que creaste: activity_main.xml
        setContentView(R.layout.activity_main);

        // Vincular el ViewFlipper
        flipper = findViewById(R.id.mega_view_flipper);

        // Pedir permisos de notificaciones en Android 13+
        pedirPermisoNotificaciones();
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    public void showScreen(int index) {
        if (flipper != null && index >= 0 && index < flipper.getChildCount()) {
            flipper.setDisplayedChild(index);
        }
    }

    public void next(android.view.View v) {
        if (flipper != null) flipper.showNext();
    }

    public void previous(android.view.View v) {
        if (flipper != null) flipper.showPrevious();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Si no hay usuario autenticado, redirigir al login
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        }
    }
}
