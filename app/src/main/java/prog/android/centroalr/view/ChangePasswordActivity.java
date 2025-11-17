package prog.android.centroalr.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import prog.android.centroalr.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etContrasenaActual;
    private EditText etContrasenaNueva;
    private EditText etContrasenaNuevaRepetida;
    private Button btnCrearContrasena;
    private ImageButton btnBack;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_contra); // Tu XML moderno

        auth = FirebaseAuth.getInstance();

        etContrasenaActual = findViewById(R.id.etContrasenaActual);
        etContrasenaNueva = findViewById(R.id.etContrasenaNueva);
        etContrasenaNuevaRepetida = findViewById(R.id.etContrasenaNuevaRepetida);
        btnCrearContrasena = findViewById(R.id.btnCrearContrasena);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnCrearContrasena.setOnClickListener(v -> cambiarContrasena());
    }

    private void cambiarContrasena() {
        final String actual = etContrasenaActual.getText().toString().trim();
        final String nueva = etContrasenaNueva.getText().toString().trim();
        final String nuevaRep = etContrasenaNuevaRepetida.getText().toString().trim();

        if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(nuevaRep)) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nueva.equals(nuevaRep)) {
            Toast.makeText(this, "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esPasswordValida(nueva)) {
            Toast.makeText(this, "La nueva contraseña no cumple los requisitos", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "No se pudo obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), actual);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> user.updatePassword(nueva)
                        .addOnSuccessListener(unused1 -> {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Contraseña actualizada correctamente",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(ChangePasswordActivity.this,
                                        "Error al actualizar: " + e.getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show()))
                .addOnFailureListener(e -> Toast.makeText(ChangePasswordActivity.this,
                        "Contraseña actual incorrecta",
                        Toast.LENGTH_LONG).show());
    }

    private boolean esPasswordValida(String pass) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\$%#\\+\\-]).{6,}$";
        return pass.matches(regex);
    }
}
