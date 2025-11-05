package prog.android.centroalr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.BuildConfig;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import prog.android.centroalr.controller.LoginController;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.view.LoginView;

// 1. Implementa la interfaz de la Vista
public class LogInActivity extends AppCompatActivity implements LoginView {

    // Referencias a la UI (Vista)
    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private Button loginButton;
    private TextView forgotPasswordTextView;

    // 2. Referencia al Controlador y al Modelo
    private LoginController controller;
    private AuthModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // 3. Inicializar MVC (ESTO FALTABA)
        model = new AuthModel();
        controller = new LoginController(this, model);

        // UI refs (ESTO FALTABA)
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // 4. Delegar eventos al Controlador (ESTO FALTABA)
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
        loginButton.setEnabled(!isLoading);
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
}