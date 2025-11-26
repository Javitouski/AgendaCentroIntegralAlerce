package prog.android.centroalr.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class CrearUsuarioActivity extends AppCompatActivity {

    private static final String TAG = "CrearUsuarioActivity";

    // Vistas del Layout (¡CORREGIDO!)
    private TextInputEditText etNombre, etEmail, etPassword;
    private AutoCompleteTextView autoCompleteRol;
    private Button btnCrearUsuario;
    private View loadingOverlay;

    // Servicios de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variable para guardar el rol seleccionado
    private String rolSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias a las vistas (¡CORREGIDO!)
        etNombre = findViewById(R.id.etNombre);
        // etApellido = findViewById(R.id.etApellido); // ELIMINADO
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        autoCompleteRol = findViewById(R.id.autoCompleteRol);
        btnCrearUsuario = findViewById(R.id.btnCrearUsuario);

        // Configurar el botón de volver
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Configurar el botón de crear
        btnCrearUsuario.setOnClickListener(v -> validarYCrearUsuario());

        // Configurar el menú desplegable de Roles
        setupDropdownRoles();
        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                // Obtenemos el tamaño exacto de las barras del sistema (arriba y abajo)
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Aplicamos ese tamaño como "relleno" (padding) al contenedor principal
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                return insets;
            });
        }
    }

    private void setupDropdownRoles() {
        // Opciones fijas para los roles
        String[] roles = new String[]{"admin", "usuario"}; // Asegúrate que 'admin' coincida con tu lógica de permisos

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        autoCompleteRol.setAdapter(adapter);

        // Guardar la selección del usuario
        autoCompleteRol.setOnItemClickListener((parent, view, position, id) -> {
            rolSeleccionado = roles[position];
        });
    }

    private void validarYCrearUsuario() {
        // 1. Obtener datos del formulario (¡CORREGIDO!)
        String nombre = etNombre.getText().toString().trim();
        // String apellido = etApellido.getText().toString().trim(); // ELIMINADO
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 2. Validar campos (¡CORREGIDO!)
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Nombre es requerido");
            etNombre.requestFocus();
            return;
        }
        // Validación de apellido ELIMINADA
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email es requerido");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Contraseña requerida (mín. 6 caracteres)");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(rolSeleccionado)) {
            autoCompleteRol.setError("Debe seleccionar un rol");
            autoCompleteRol.requestFocus();
            return;
        }

        // 3. Deshabilitar botón y mostrar carga
        btnCrearUsuario.setEnabled(false);

        // --- INICIO DEL PROCESO DE 2 PASOS ---

        // Paso A: Crear el usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // ¡Éxito! Usuario creado en Authentication.
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        // Proceder al Paso B (¡CORREGIDO!)
                        crearPerfilEnFirestore(uid, nombre, email, rolSeleccionado);
                    } else {
                        falloAlCrear("No se pudo obtener el UID del usuario creado.");
                    }
                })
                .addOnFailureListener(e -> {
                    // Falló el Paso A
                    Log.e(TAG, "Error al crear usuario en Auth", e);
                    falloAlCrear("Error al crear usuario: " + e.getMessage());
                });
    }

    // Paso B: Crear el perfil en Firestore (¡CORREGIDO!)
    private void crearPerfilEnFirestore(String uid, String nombre, String email, String rol) {

        // 1. Crear el mapa de datos básicos del perfil (¡CORREGIDO!)
        Map<String, Object> perfilUsuario = new HashMap<>();
        perfilUsuario.put("nombre", nombre);
        // perfilUsuario.put("apellido", apellido); // ELIMINADO
        perfilUsuario.put("email", email);
        perfilUsuario.put("rol", rol);
        perfilUsuario.put("uid", uid);

        // 2. ¡CRÍTICO! Añadir el mapa de permisos si el rol es "usuario"
        if (rol.equals("usuario")) {
            Map<String, Object> permisos = new HashMap<>();
            permisos.put("PUEDE_CREAR_ACTIVIDAD", false);
            permisos.put("PUEDE_MODIFICAR_ACTIVIDAD", false);
            permisos.put("PUEDE_CANCELAR_ACTIVIDAD", false);
            permisos.put("PUEDE_REAGENDAR_ACTIVIDAD", false);
            permisos.put("PUEDE_ADJUNTAR_ARCHIVOS", false);
            permisos.put("PUEDE_GESTIONAR_MANTENEDORES", false);

            perfilUsuario.put("permisos", permisos);
        }

        // 3. Guardar el documento en Firestore usando el UID como ID del documento
        db.collection("usuarios").document(uid).set(perfilUsuario)
                .addOnSuccessListener(aVoid -> {
                    // ¡ÉXITO TOTAL!
                    Toast.makeText(this, "Usuario creado exitosamente.", Toast.LENGTH_SHORT).show();
                    finish(); // Volver a la pantalla de Perfil
                })
                .addOnFailureListener(e -> {
                    // Falló el Paso B
                    Log.e(TAG, "Error al crear perfil en Firestore", e);
                    falloAlCrear("Usuario creado en Auth, pero falló al guardar perfil en DB.");
                });
    }

    // Helper para manejar fallos
    private void falloAlCrear(String mensaje) {
        btnCrearUsuario.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}