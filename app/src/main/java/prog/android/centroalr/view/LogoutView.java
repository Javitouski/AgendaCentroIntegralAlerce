package prog.android.centroalr.view;

// Contrato de lo que la Vista (Activity) que hace Logout debe poder hacer
public interface LogoutView {
    void showLogoutSuccessMessage(String message);
    void navigateToLogin();
}