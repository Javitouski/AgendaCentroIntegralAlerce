package prog.android.centroalr;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import prog.android.centroalr.controller.PasswordRecoveryController;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.view.PasswordRecoveryView;

// 1. Implementa la interfaz de la Vista
public class PasswordRecoveryActivity extends AppCompatActivity implements PasswordRecoveryView {

    // Vistas
    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private MaterialButton sendCodeButton;

    // 2. Referencia al Controlador y Modelo
    private PasswordRecoveryController controller;
    private AuthModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_contrase_a);

        // 3. Inicializar MVC
        model = new AuthModel(); // El mismo modelo, nueva instancia
        controller = new PasswordRecoveryController(this, model);

        // UI refs
        emailEditText = findViewById(R.id.emailEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        sendCodeButton = findViewById(R.id.sendCodeButton);

        // 4. Delegar evento al Controlador
        sendCodeButton.setOnClickListener(v -> {
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            controller.onRecoverClicked(email);
        });
    }

    // --- Implementación de los métodos de PasswordRecoveryView ---

    @Override
    public void showEmailError(String message) {
        emailInputLayout.setError(message);
    }

    @Override
    public void clearEmailError() {
        emailInputLayout.setError(null);
    }

    @Override
    public void showLoading(boolean isLoading) {
        sendCodeButton.setEnabled(!isLoading);
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showRecoverError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateBackToLogin() {
        finish(); // Cierra esta actividad y vuelve a Login
    }
}