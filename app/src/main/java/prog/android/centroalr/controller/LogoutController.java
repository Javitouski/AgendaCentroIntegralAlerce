package prog.android.centroalr.controller;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.model.AuthModel;
// ¡CORREGIDO! Ya no necesitamos OnLogoutListener aquí
import prog.android.centroalr.view.LogoutView;

// ¡CORREGIDO! El Controller ya NO implementa OnLogoutListener
public class LogoutController {
    private LogoutView logoutView;
    private AuthModel authModel;

    public LogoutController(LogoutView logoutView, AuthModel authModel) {
        this.logoutView = logoutView;
        this.authModel = authModel;
    }

    // --- Método que tu Activity llama ---

    public void onLogoutClicked() {
        try {
            // 1. Limpiamos el perfil global
            MyApplication myApp = (MyApplication) logoutView.getContext().getApplicationContext();
            myApp.clearUsuarioActual();

            // 2. Llamamos a tu método logout de AuthModel
            // (Tu método no usa listener, así que lo sacamos)
            authModel.logout(); // ¡CORREGIDO! Ahora se llama sin argumentos.
            // Sería ideal refactorizar AuthModel para que logout() no pida listener.

            // 3. Informamos a la Vista
            logoutView.onLogoutSuccess();

        } catch (Exception e) {
            // Si algo falla (ej. getContext es nulo), lo reportamos
            logoutView.onLogoutFailure(e.getMessage());
        }
    }
}