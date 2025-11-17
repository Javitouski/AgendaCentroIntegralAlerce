package prog.android.centroalr.controller;

import android.text.TextUtils;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnPasswordResetListener;
import prog.android.centroalr.view.NewPasswordView;

public class NewPasswordController implements OnPasswordResetListener {

    private NewPasswordView view;
    private AuthModel model;

    public NewPasswordController(NewPasswordView view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    public void onNewPasswordEntered(String oobCode, String newPassword, String confirmPassword) {
        view.clearErrors();

        boolean hasError = false;

        // 1. Validar Vacío
        if (TextUtils.isEmpty(newPassword)) {
            view.showNewPasswordError("Ingresa tu nueva contraseña");
            hasError = true;
        }
        // 2. Validar Requisitos de Seguridad (Regex)
        // Coincide con: Mayúscula, Minúscula, Número, Símbolo y Min 6 chars
        else if (!esPasswordValida(newPassword)) {
            view.showNewPasswordError("La contraseña no cumple con los requisitos de seguridad (Mayúscula, número, símbolo)");
            hasError = true;
        }

        // 3. Validar Confirmación
        if (TextUtils.isEmpty(confirmPassword)) {
            view.showConfirmPasswordError("Confirma tu nueva contraseña");
            hasError = true;
        }

        // 4. Validar Coincidencia
        if (!hasError && !newPassword.equals(confirmPassword)) {
            view.showConfirmPasswordError("Las contraseñas no coinciden");
            hasError = true;
        }

        if (hasError) return;

        view.showLoading(true);
        model.resetPassword(oobCode, newPassword, this);
    }

    // Método auxiliar de validación (Igual que en ChangePasswordActivity)
    private boolean esPasswordValida(String password) {
        // Regex: Al menos 1 Mayús, 1 Minús, 1 Dígito, 1 Símbolo, Min 6 chars
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\$%#\\+\\-]).{6,}$";
        return password.matches(regex);
    }

    // --- Callbacks del Modelo ---

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