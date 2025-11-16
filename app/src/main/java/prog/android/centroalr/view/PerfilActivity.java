package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout; // Importar LinearLayout
import android.widget.TextView; // Importar TextView

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.MyApplication; // Importar
import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario; // Importar

public class PerfilActivity extends AppCompatActivity {

    // Variable para el usuario
    private Usuario usuarioActual;

    // (Variables para tus vistas de botones)
    private LinearLayout btnPersonalInfo, btnChangePassword, btnManageRoles, btnYourActivities, btnCreateUsers, btnMantenedores;
    private TextView txtGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // --- Cargar Perfil de Usuario (Como en las otras pantallas) ---
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        // Chequeo de seguridad
        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // === Encontrar Vistas ===
        View back = findViewById(R.id.btnBack);
        txtGreeting = findViewById(R.id.txtGreeting);

        // Botones Comunes
        btnPersonalInfo = findViewById(R.id.btnPersonalInfo);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnYourActivities = findViewById(R.id.btnYourActivities);

        // Botones de Admin
        btnManageRoles = findViewById(R.id.btnManageRoles);
        btnCreateUsers = findViewById(R.id.btnCreateUsers);
        btnMantenedores = findViewById(R.id.btnMantenedores); // <-- 1. Encontrar el nuevo botón

        // === Configurar UI y Listeners ===

        // Poner el saludo
        if (txtGreeting != null && usuarioActual.getNombre() != null) {
            txtGreeting.setText("¡Hola, " + usuarioActual.getNombre().split(" ")[0] + "!"); // Saluda con el primer nombre
        }

        // Listener de Volver
        if (back != null) {
            back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        // Listeners de Botones Comunes
        if (btnPersonalInfo != null) {
            btnPersonalInfo.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, InfUsuarioActivity.class))
            );
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, ChangePasswordActivity.class))
            );
        }

        if (btnYourActivities != null) {
            btnYourActivities.setOnClickListener(v ->
                    startActivity(new Intent(PerfilActivity.this, AgndSemActivity.class))
            );
        }

        // --- LÓGICA DE PERMISOS (Botones de Admin) ---

        // Usamos el mismo permiso para todos los botones de admin
        if (usuarioActual.tienePermiso("PUEDE_GESTIONAR_MANTENEDORES")) {

            // Botón 1: Gestionar Usuarios/Permisos (Ya creado)
            if (btnManageRoles != null) {
                btnManageRoles.setVisibility(View.VISIBLE);
                btnManageRoles.setOnClickListener(v ->
                        startActivity(new Intent(PerfilActivity.this, GestionUsuariosActivity.class))
                );
            }

            // Botón 2: Crear Nuevo Usuario (Ya creado)
            if (btnCreateUsers != null) {
                btnCreateUsers.setVisibility(View.VISIBLE);
                btnCreateUsers.setOnClickListener(v ->
                        startActivity(new Intent(PerfilActivity.this, CrearUsuarioActivity.class))
                );
            }

            // Botón 3: Administrar Datos (Nuevo)
            if (btnMantenedores != null) {
                btnMantenedores.setVisibility(View.VISIBLE); // <-- 2. Hacerlo visible
                btnMantenedores.setOnClickListener(v ->
                        // Esto fallará hasta que creemos la Activity en el siguiente paso
                        startActivity(new Intent(PerfilActivity.this, MantenedoresHubActivity.class)) // <-- 3. Conectarlo
                );
            }

        } else {
            // El usuario NO es admin, ocultar TODOS los botones de admin
            if (btnManageRoles != null) {
                btnManageRoles.setVisibility(View.GONE);
            }
            if (btnCreateUsers != null) {
                btnCreateUsers.setVisibility(View.GONE);
            }
            if (btnMantenedores != null) {
                btnMantenedores.setVisibility(View.GONE);
            }
        }
    }
}