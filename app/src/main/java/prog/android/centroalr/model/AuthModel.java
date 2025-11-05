package prog.android.centroalr.model;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthModel {

    private FirebaseAuth mAuth;

    public AuthModel() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Verifica si hay un usuario actualmente logueado.
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Intenta iniciar sesión en Firebase.
     * Notifica el resultado usando el listener.
     */
    public void login(String email, String password, OnLoginResultListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onLoginSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Error al iniciar sesión";
                        listener.onLoginFailure(error);
                    }
                });
    }

    /**
     * Envía un correo de recuperación de contraseña.
     * Notifica el resultado usando el listener.
     */
    public void recoverPassword(String email, OnRecoverResultListener listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onRecoverSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Error al enviar correo";
                        listener.onRecoverFailure(error);
                    }
                });
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout(OnLogoutListener listener) {
        mAuth.signOut();
        listener.onLogoutSuccess();
    }

    public void resetPassword(String oobCode, String newPassword, OnPasswordResetListener listener) {
        mAuth.confirmPasswordReset(oobCode, newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onResetSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Error al restablecer contraseña.";
                        listener.onResetFailure(error);
                    }
                });
    }
}