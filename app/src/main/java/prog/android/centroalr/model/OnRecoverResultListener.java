package prog.android.centroalr.model;

// Interfaz para notificar el resultado de la Recuperación
public interface OnRecoverResultListener {
    void onRecoverSuccess();
    void onRecoverFailure(String error);
}