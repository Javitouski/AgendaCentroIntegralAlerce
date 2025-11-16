package prog.android.centroalr.view;

import android.content.Context;

// Contrato de lo que la Vista (Activity) de Login debe poder hacer
public interface LoginView {
    void showEmailError(String message);
    void showPasswordError(String message);
    void clearErrors();
    void showLoading(boolean isLoading);
    void showLoginError(String message);
    void navigateToMainApp();
    void navigateToPasswordRecovery();
    void onLoginSuccessNavigate();
    void onLoginFailure(String message);
    Context getContext();
}