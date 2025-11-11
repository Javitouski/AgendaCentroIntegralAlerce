package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.R;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

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
        // (Si en tu proyecto se llama distinto, por ejemplo RestContraActivity,
        // cambia ChangePasswordActivity.class por ese nombre.)
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

        // (Opcional) Otros botones del layout para no dejar huecos sin respuesta:

        // Administrar Roles (placeholder)
        View manageRoles = findViewById(R.id.btnManageRoles);
        if (manageRoles != null) {
            manageRoles.setOnClickListener(v ->
                    Toast.makeText(this, "Administrar Roles: próximamente", Toast.LENGTH_SHORT).show()
            );
        }

        // Crear Usuarios (placeholder)
        View createUsers = findViewById(R.id.btnCreateUsers);
        if (createUsers != null) {
            createUsers.setOnClickListener(v ->
                    Toast.makeText(this, "Crear Usuarios: próximamente", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
