package prog.android.centroalr.controller;

import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnLogoutListener;
import prog.android.centroalr.view.LogoutView;

public class LogoutController implements OnLogoutListener {

    private LogoutView view;
    private AuthModel model;

    public LogoutController(LogoutView view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    /**
     * Llamado desde la Activity cuando se pulsa "SALIR".
     */
    public void onLogoutClicked() {
        model.logout(this);
    }

    // -- Callback del Modelo --

    @Override
    public void onLogoutSuccess() {
        view.showLogoutSuccessMessage("Sesión cerrada correctamente.");
        view.navigateToLogin();
    }
}