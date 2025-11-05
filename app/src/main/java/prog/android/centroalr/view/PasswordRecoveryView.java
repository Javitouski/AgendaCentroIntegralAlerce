package prog.android.centroalr.view;

// Contrato de lo que la Vista (Activity) de Recuperación debe poder hacer
public interface PasswordRecoveryView {
    void showEmailError(String message);
    void clearEmailError();
    void showLoading(boolean isLoading);
    void showSuccessMessage(String message);
    void showRecoverError(String message);
    void navigateBackToLogin();
}