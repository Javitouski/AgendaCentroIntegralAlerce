package prog.android.centroalr.controller;

import android.text.TextUtils;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnPasswordResetListener; // Asegúrate que sea el modelo correcto
import prog.android.centroalr.view.NewPasswordView; // Asegúrate que sea la vista correcta

public class NewPasswordController implements OnPasswordResetListener {

    private NewPasswordView view;
    private AuthModel model;

    public NewPasswordController(NewPasswordView view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    /**
     * Llamado cuando el usuario introduce y confirma la nueva contraseña.
     */
    public void onNewPasswordEntered(String oobCode, String newPassword, String confirmPassword) {
        view.clearErrors();

        boolean hasError = false;

        if (TextUtils.isEmpty(newPassword)) {
            view.showNewPasswordError("Ingresa tu nueva contraseña");
            hasError = true;
        } else if (newPassword.length() < 6) {
            view.showNewPasswordError("La contraseña debe tener al menos 6 caracteres");
            hasError = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            view.showConfirmPasswordError("Confirma tu nueva contraseña");
            hasError = true;
        }

        if (!newPassword.equals(confirmPassword)) {
            view.showConfirmPasswordError("Las contraseñas no coinciden");
            hasError = true;
        }

        if (hasError) return;

        view.showLoading(true);
        model.resetPassword(oobCode, newPassword, this);
    }

    // --- Implementación de OnPasswordResetListener (callbacks del Modelo) ---

    @Override
    public void onResetSuccess() {
        view.showLoading(false);
        view.showSuccess("Contraseña restablecida correctamente. Inicia sesión con tu nueva contraseña.");
    }

    @Override
    public void onResetFailure(String error) {
        view.showLoading(false);
        view.showError("Error al restablecer contraseña: " + error);
    }
}