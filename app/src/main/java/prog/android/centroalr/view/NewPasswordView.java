package prog.android.centroalr.view;

public interface NewPasswordView {
    void showNewPasswordError(String message);
    void showConfirmPasswordError(String message);
    void clearErrors();
    void showLoading(boolean isLoading);
    void showSuccess(String message);
    void showError(String message);
}