package prog.android.centroalr.model;

import com.google.firebase.auth.FirebaseUser;

public interface OnLoginResultListener {
    // ¡CAMBIO CLAVE! Ahora pasamos el FirebaseUser
    void onLoginSuccess(FirebaseUser user);

    void onLoginFailure(String message);
}