package prog.android.centroalr.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.R;
import prog.android.centroalr.controller.LoginController;
import prog.android.centroalr.model.AuthModel;

// 1. Implementa la interfaz de la Vista
public class LogInActivity extends AppCompatActivity implements LoginView {

    // Referencias a la UI (Vista)
    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private View loadingOverlay;

    // 2. Referencia al Controlador y al Modelo
    private LoginController controller;
    private AuthModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // 3. Inicializar MVC
        model = new AuthModel();
        controller = new LoginController(this, model);

        // UI refs
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loadingOverlay = findViewById(R.id.loadingOverlay);


        // 4. Delegar eventos al Controlador
        forgotPasswordTextView.setOnClickListener(v ->
                controller.onForgotPasswordClicked());

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
            controller.onLoginClicked(email, password);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 5. Delegar chequeo al Controlador
        controller.checkUserLoggedIn();
    }

    // --- Implementación de los métodos de LoginView ---

    @Override
    public void showEmailError(String message) {
        emailInputLayout.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        passwordInputLayout.setError(message);
    }

    @Override
    public void clearErrors() {
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    @Override
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            loginButton.setEnabled(true);
        }
    }

    @Override
    public void showLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToMainApp() {
        Intent i = new Intent(this, AgendMensActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void navigateToPasswordRecovery() {
        startActivity(new Intent(this, PasswordRecoveryActivity.class));
    }

    // --- Implementación de los NUEVOS métodos de LoginView ---

    @Override
    public Context getContext() {
        // Devuelve el "contexto" de la app para que el Controller encuentre MyApplication
        return getApplicationContext();
    }

    @Override
    public void onLoginSuccessNavigate() {
        // El Controller nos dice que naveguemos.
        // Llamamos al método que ya tenías para esto.
        navigateToMainApp();
    }

    @Override
    public void onLoginFailure(String message) {
        // El Controller nos pasa un mensaje de error.
        // Llamamos al método que ya tenías para mostrarlo.
        showLoginError(message);
    }
}

