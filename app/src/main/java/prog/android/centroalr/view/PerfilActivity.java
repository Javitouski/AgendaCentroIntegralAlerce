package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// PASO 1: Importar las clases que necesitamos
import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class PerfilActivity extends AppCompatActivity {

    // PASO 2: Declarar una variable para nuestro usuario
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // PASO 3: Cargar el perfil de usuario desde MyApplication
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        // PASO 4: CHEQUEO DE SEGURIDAD
        // Si por alguna razón el usuario es nulo (ej. Android limpió la memoria),
        // no dejamos que se quede en esta pantalla. Lo enviamos al Login.
        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Importante: detenemos la ejecución del onCreate aquí
        }

        // === Back (ImageView con id @+id/btnBack) ===
        View back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }

        // === Navegaciones solicitadas ===

        // 1) Información personal -> InfUsuarioActivity
        View personalInfo = findViewById(R.id.btnPersonalInfo);
        if (personalInfo != null) {
            personalInfo.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, InfUsuarioActivity.class))
            );
        }

        // 2) Cambiar contraseña -> ChangePasswordActivity
        View changePass = findViewById(R.id.btnChangePassword);
        if (changePass != null) {
            changePass.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, ChangePasswordActivity.class))
            );
        }

        // 3) Tus actividades -> AgndSemActivity
        View yourActivities = findViewById(R.id.btnYourActivities);
        if (yourActivities != null) {
            yourActivities.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, AgndSemActivity.class))
            );
        }

        // --- PASO 5: LÓGICA DE PERMISOS ---

        // Buscamos los botones de administrador
        View manageRoles = findViewById(R.id.btnManageRoles);
        View createUsers = findViewById(R.id.btnCreateUsers);

        // Verificamos el permiso (¡tu modelo Usuario.java ya maneja la lógica de "admin" o mapa de permisos!)
        if (usuarioActual.tienePermiso("PUEDE_GESTIONAR_MANTENEDORES")) {
            // El usuario SÍ tiene permiso (es admin)

            if (manageRoles != null) {
                manageRoles.setVisibility(View.VISIBLE); // Asegurarse de que sea visible
                manageRoles.setOnClickListener(v ->
                        Toast.makeText(this, "Administrar Roles: próximamente", Toast.LENGTH_SHORT).show()
                );
            }

            if (createUsers != null) {
                createUsers.setVisibility(View.VISIBLE); // Asegurarse de que sea visible
                createUsers.setOnClickListener(v ->
                        startActivity(new Intent(PerfilActivity.this, CrearUsuarioActivity.class))
                );
            }

        } else {
            // El usuario NO tiene permiso

            if (manageRoles != null) {
                manageRoles.setVisibility(View.GONE); // ¡Ocultamos el botón!
            }
            if (createUsers != null) {
                createUsers.setVisibility(View.GONE); // ¡Ocultamos el botón!
            }
        }
    }
}