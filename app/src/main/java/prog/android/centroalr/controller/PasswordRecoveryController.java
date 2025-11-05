package prog.android.centroalr.controller;

import android.text.TextUtils;
import android.util.Patterns;

import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnRecoverResultListener;
import prog.android.centroalr.view.PasswordRecoveryView;

public class PasswordRecoveryController implements OnRecoverResultListener {

    private PasswordRecoveryView view;
    private AuthModel model;

    public PasswordRecoveryController(PasswordRecoveryView view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    /**
     * Llamado desde la Activity cuando se pulsa "Recibir código".
     */
    public void onRecoverClicked(String email) {
        view.clearEmailError();

        if (TextUtils.isEmpty(email)) {
            view.showEmailError("El correo no puede estar vacío");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Formato de correo inválido");
            return;
        }

        view.showLoading(true);
        model.recoverPassword(email, this); // 'this' es el OnRecoverResultListener
    }

    // -- Callbacks del Modelo --

    @Override
    public void onRecoverSuccess() {
        view.showLoading(false);
        view.showSuccessMessage("Correo enviado. Revisa tu bandeja de entrada.");
        view.navigateBackToLogin();
    }

    @Override
    public void onRecoverFailure(String error) {
        view.showLoading(false);
        view.showRecoverError(error);
    }
}