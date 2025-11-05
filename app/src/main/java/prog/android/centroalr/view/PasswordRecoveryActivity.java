package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import prog.android.centroalr.R;
import prog.android.centroalr.controller.PasswordRecoveryController;
import prog.android.centroalr.model.AuthModel;

// 1. Implementa la interfaz de la Vista
public class PasswordRecoveryActivity extends AppCompatActivity implements PasswordRecoveryView {

    // Vistas
    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private MaterialButton sendCodeButton;
    private View loadingOverlay;
    private ImageButton backButton;


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
        loadingOverlay = findViewById(R.id.loadingOverlay);
        backButton = findViewById(R.id.backButton);

        // 4. Delegar evento al Controlador
        sendCodeButton.setOnClickListener(v -> {
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            controller.onRecoverClicked(email);
        });

        //5. Función Back Button.
        backButton.setOnClickListener(v->{
            finish();
        });
    }



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
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            sendCodeButton.setEnabled(false);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            sendCodeButton.setEnabled(true);
        }
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, "Si tu correo está registrado, recibirás un enlace en breve.", Toast.LENGTH_LONG).show();
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