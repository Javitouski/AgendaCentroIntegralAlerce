package prog.android.centroalr.model;

// Interfaz para notificar el resultado del Login
public interface OnLoginResultListener {
    void onLoginSuccess();
    void onLoginFailure(String error);
}