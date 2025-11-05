package prog.android.centroalr.model;

// Interfaz para notificar el resultado del Logout (aunque signOut es síncrono,
// es buena práctica para mantener consistencia si luego añades más lógica)
public interface OnLogoutListener {
    void onLogoutSuccess();
}