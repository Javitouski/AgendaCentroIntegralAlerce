package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import prog.android.centroalr.LogInActivity;
import prog.android.centroalr.R;
import prog.android.centroalr.model.AuthModel; // Importa tu AuthModel
import prog.android.centroalr.controller.NewPasswordController; // Nuevo controlador


public class NewPasswordActivity extends AppCompatActivity implements NewPasswordView { // Implementa la nueva vista

    private TextInputLayout newPasswordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private MaterialButton createPasswordButton;
    private View backArrow;
//
    private NewPasswordController controller;
    private String oobCode; // Guardaremos el código de Firebase Dynamic Link aquí

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password); // Asegúrate de que el nombre coincida

        // Inicializa tus vistas
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        createPasswordButton = findViewById(R.id.createPasswordButton);
        backArrow = findViewById(R.id.back_arrow);

        // Inicializa tu MVC
        AuthModel model = new AuthModel(); // Puedes pasarle una instancia ya existente si quieres un Singleton
        controller = new NewPasswordController(this, model);

        // Manejar el botón de regreso
        backArrow.setOnClickListener(v -> finish());

        // Obtener el oobCode del Intent (viene de Firebase Dynamic Links)
        if (getIntent() != null && getIntent().getData() != null) {
            oobCode = getIntent().getData().getQueryParameter("oobCode");
            if (oobCode == null) {
                showError("Error: Código de restablecimiento no encontrado.");
                finish(); // Si no hay código, no podemos continuar
            } else {
                // Opcional: Podrías llamar a controller.verifyCode(oobCode) aquí
                // para mostrar un mensaje de "cargando" mientras se verifica.
            }
        } else {
            showError("Error: Acceso inválido a la pantalla de restablecimiento.");
            finish();
        }

        // Configurar el listener del botón
        createPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            controller.onNewPasswordEntered(oobCode, newPassword, confirmPassword);
        });
    }

    // --- Implementación de NewPasswordView ---

    @Override
    public void showNewPasswordError(String message) {
        newPasswordInputLayout.setError(message);
    }

    @Override
    public void showConfirmPasswordError(String message) {
        confirmPasswordInputLayout.setError(message);
    }

    @Override
    public void clearErrors() {
        newPasswordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
    }

    @Override
    public void showLoading(boolean isLoading) {
        createPasswordButton.setEnabled(!isLoading);
        // Opcional: Muestra/oculta un ProgressBar
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // 1. Crea un Intent para ir a la pantalla de Login
        Intent intent = new Intent(NewPasswordActivity.this, LogInActivity.class);

        // 2. Añade estas flags. Son cruciales.
        // FLAG_ACTIVITY_NEW_TASK: Inicia el Login en una nueva tarea.
        // FLAG_ACTIVITY_CLEAR_TASK: Borra la tarea anterior (Gmail -> NewPassword)
        // Esto hace que tu app se "reinicie" en el Login, y el botón "Atrás"
        // no devuelva al usuario a Gmail.

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // 3. Inicia la actividad de Login
        startActivity(intent);
        // 4. Finaliza esta actividad (NewPasswordActivity) para que desaparezca
        finish();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}