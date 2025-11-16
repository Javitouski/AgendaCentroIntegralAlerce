package prog.android.centroalr.controller;

import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import prog.android.centroalr.MyApplication;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.OnLoginResultListener;
import prog.android.centroalr.model.Usuario;
import prog.android.centroalr.view.LoginView;

public class LoginController implements OnLoginResultListener {
    private LoginView loginView;
    private AuthModel authModel;
    private FirebaseFirestore db;

    public LoginController(LoginView loginView, AuthModel authModel) {
        this.loginView = loginView;
        this.authModel = authModel;
        this.db = FirebaseFirestore.getInstance();
    }

    // --- Métodos que tu Activity llama ---

    public void onLoginClicked(String email, String password) {
        loginView.showLoading(true);
        authModel.login(email, password, this);
    }

    public void onForgotPasswordClicked() {
        // (Tu lógica para "olvidé contraseña" iría aquí)
        // Por ahora, le decimos a la vista que muestre un error
        loginView.onLoginFailure("Función 'Olvidé Contraseña' no implementada.");
    }

    public void checkUserLoggedIn(){
        // (Tu lógica para chequear si ya hay un usuario logueado)
        // if (authModel.isUserLoggedIn()) {
        //     ... (lógica para obtener el user y saltar el login)
        // }
    }

    // --- Métodos DEL LISTENER que el AuthModel llama ---

    @Override
    public void onLoginSuccess(FirebaseUser user) {
        loginView.showLoading(false); // <-- AÑADIR ESTA LÍNEA
        String uid = user.getUid();
        DocumentReference userRef = db.collection("usuarios").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                if (usuario != null) {
                    MyApplication myApp = (MyApplication) loginView.getContext().getApplicationContext();
                    myApp.setUsuarioActual(usuario);
                    loginView.onLoginSuccessNavigate();
                } else {
                    loginView.onLoginFailure("Error al leer el perfil de usuario.");
                }
            } else {
                loginView.onLoginFailure("Error: Perfil de usuario no encontrado.");
            }
        }).addOnFailureListener(e -> {
            loginView.onLoginFailure("Error de red al cargar el perfil.");
        });
    }

    @Override
    public void onLoginFailure(String message) {
        loginView.showLoading(false);
        loginView.onLoginFailure(message);
    }
}