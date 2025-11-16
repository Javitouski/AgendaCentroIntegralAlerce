package prog.android.centroalr.model;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.FirebaseNetworkException;
// PASO 1: Importar FirebaseUser
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
                        // PASO 2: Obtener el usuario y pasarlo al listener
                        FirebaseUser user = mAuth.getCurrentUser();
                        listener.onLoginSuccess(user);
                    } else {
                        String errorTraducido = getErrorEnEspanol(task.getException());

                        listener.onLoginFailure(errorTraducido);
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
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseNetworkException){
                            listener.onRecoverFailure("Error de conexión. Revisa tu conexión a internet");
                        }
                        else {
                            listener.onRecoverSuccess();
                        }
                    }
                });
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    // PASO 3: Simplificar el método logout.
    public void logout() {
        mAuth.signOut();
        // El listener se elimina, ya no es necesario aquí.
    }

    public void resetPassword(String oobCode, String newPassword, OnPasswordResetListener listener) {
        mAuth.confirmPasswordReset(oobCode, newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onResetSuccess();
                    } else {
                        String errorTraducido = getErrorEnEspanol(task.getException());
                        listener.onResetFailure(errorTraducido);
                    }
                });
    }

    // Traduce los códigos de error comunes de FirebaseAuthException y FirebaseNetworkException al español.
    private String getErrorEnEspanol(Exception exception) {

        // Primero, revisamos si es un error de red
        if (exception instanceof FirebaseNetworkException) {
            return "Error de conexión. Revisa tu conexión a internet.";
        }

        // Si no, revisamos si es un error de autenticación
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();

            switch (errorCode) {
                // Agrupamos todos los errores de login en uno solo.
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_INVALID_EMAIL":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_MISMATCH":
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_USER_DISABLED":
                    return "Correo o contraseña incorrectos. Por favor, verifica tus datos.";

                case "ERROR_EXPIRED_ACTION_CODE":
                    return "El enlace de recuperación ha expirado. Por favor, solicita uno nuevo.";

                case "ERROR_INVALID_ACTION_CODE":
                    return "El enlace es inválido o ya fue utilizado.";

                case "ERROR_TOO_MANY_REQUESTS":
                    return "Se han realizado demasiados intentos. Intenta más tarde.";

                case "ERROR_WEAK_PASSWORD":

                    return "La contraseña es demasiado débil. Debe tener al menos 6 caracteres.";

                case "ERROR_EMAIL_ALREADY_IN_USE":

                    return "Este correo electrónico ya está en uso por otra cuenta.";

                // Ocultamos errores internos
                case "ERROR_OPERATION_NOT_ALLOWED":
                case "ERROR_NO_SUCH_PROVIDER":
                case "ERROR_INVALID_CUSTOM_TOKEN":
                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                default:
                    return "Ocurrió un error inesperado. Inténtalo de nuevo.";
            }
        }
        // Si no es un error de Firebase, devuelve un mensaje genérico
        return "Ocurrió un error inesperado.";
    }
}