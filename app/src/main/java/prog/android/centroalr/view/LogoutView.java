package prog.android.centroalr.view;

import android.content.Context;

// Contrato de lo que la Vista (Activity) que hace Logout debe poder hacer
public interface LogoutView {
    void showLogoutSuccessMessage(String message);
    void navigateToLogin();
    void onLogoutSuccess();
    void onLogoutFailure(String message);
    Context getContext();
}