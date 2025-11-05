package prog.android.centroalr.controller;

import android.text.TextUtils;
import android.util.Patterns;

import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnLoginResultListener;
import prog.android.centroalr.view.LoginView;

public class LoginController implements OnLoginResultListener {

    private LoginView view;
    private AuthModel model;

    public LoginController(LoginView view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    /**
     * Llamado desde la Activity cuando el usuario está en onStart.
     */
    public void checkUserLoggedIn() {
        if (model.isUserLoggedIn()) {
            view.navigateToMainApp();
        }
    }

    /**
     * Llamado desde la Activity cuando se pulsa "Olvidé contraseña".
     */
    public void onForgotPasswordClicked() {
        view.navigateToPasswordRecovery();
    }

    /**
     * Llamado desde la Activity cuando se pulsa "Iniciar Sesión".
     */
    public void onLoginClicked(String email, String password) {
        view.clearErrors();
        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            view.showEmailError("Ingresa tu correo");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Formato de correo inválido");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            view.showPasswordError("Ingresa tu contraseña");
            hasError = true;
        } else if (password.length() < 6) {
            view.showPasswordError("Mínimo 6 caracteres");
            hasError = true;
        }

        if (hasError) return;

        view.showLoading(true);
        model.login(email, password, this); // 'this' es el OnLoginResultListener
    }

    // -- Callbacks del Modelo --

    @Override
    public void onLoginSuccess() {
        view.showLoading(false);
        view.navigateToMainApp();
    }

    @Override
    public void onLoginFailure(String error) {
        view.showLoading(false);
        view.showLoginError(error);
    }
}