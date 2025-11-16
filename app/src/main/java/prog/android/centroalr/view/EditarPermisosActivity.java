package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar; // Asegúrate de tener esta importación

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class EditarPermisosActivity extends AppCompatActivity {

    private static final String TAG = "EditarPermisosActivity";

    // Vistas
    private TextView tvNombreUsuario;
    private AutoCompleteTextView autoCompleteRol;
    private LinearLayout grupoPermisos;
    private Button btnGuardarPermisos;
    private ImageButton btnBack;
    private View loadingOverlay; // Vista de carga


    // CheckBoxes
    private CheckBox cbCrearActividad, cbModificarActividad, cbReagendarActividad,
            cbCancelarActividad, cbAdjuntarArchivos, cbGestionarMantenedores;

    // Firebase y Datos
    private FirebaseFirestore db;
    private String usuarioUid;
    private Usuario usuarioActual;
    private String rolSeleccionado;
    private final String[] ROLES = new String[]{"admin", "usuario"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_permisos);
        loadingOverlay = findViewById(R.id.loadingOverlay); // Correcto

        // Recibir el UID del Intent
        usuarioUid = getIntent().getStringExtra("usuario_uid");
        if (usuarioUid == null || usuarioUid.isEmpty()) {
            Toast.makeText(this, "Error: No se especificó un usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupDropdownRoles();

        btnBack.setOnClickListener(v -> finish());
        btnGuardarPermisos.setOnClickListener(v -> guardarCambios());

        cargarDatosUsuario();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        autoCompleteRol = findViewById(R.id.autoCompleteRol);
        grupoPermisos = findViewById(R.id.grupoPermisos);
        btnGuardarPermisos = findViewById(R.id.btnGuardarPermisos);

        // Checkboxes
        cbCrearActividad = findViewById(R.id.cbCrearActividad);
        cbModificarActividad = findViewById(R.id.cbModificarActividad);
        cbReagendarActividad = findViewById(R.id.cbReagendarActividad);
        cbCancelarActividad = findViewById(R.id.cbCancelarActividad);
        cbAdjuntarArchivos = findViewById(R.id.cbAdjuntarArchivos);
        cbGestionarMantenedores = findViewById(R.id.cbGestionarMantenedores);
    }

    private void setupDropdownRoles() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ROLES);
        autoCompleteRol.setAdapter(adapter);

        autoCompleteRol.setOnItemClickListener((parent, view, position, id) -> {
            rolSeleccionado = ROLES[position];
            actualizarVisibilidadPermisos(rolSeleccionado);
        });
    }

    private void cargarDatosUsuario() {
        showLoading(true); // <-- AÑADIDO
        db.collection("usuarios").document(usuarioUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false); // <-- AÑADIDO
                    if (documentSnapshot.exists()) {
                        usuarioActual = documentSnapshot.toObject(Usuario.class);
                        if (usuarioActual != null) {
                            poblarDatosEnUI();
                        } else {
                            Toast.makeText(this, "Error al leer datos del usuario.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false); // <-- AÑADIDO
                    Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void poblarDatosEnUI() {
        tvNombreUsuario.setText(String.format("%s (%s)", usuarioActual.getNombre(), usuarioActual.getEmail()));

        rolSeleccionado = usuarioActual.getRol();
        autoCompleteRol.setText(rolSeleccionado, false);

        actualizarVisibilidadPermisos(rolSeleccionado);

        if (rolSeleccionado.equals("usuario") && usuarioActual.getPermisos() != null) {
            Map<String, Boolean> permisos = usuarioActual.getPermisos(); // Correcto

            cbCrearActividad.setChecked( permisos.getOrDefault("PUEDE_CREAR_ACTIVIDAD", false) );
            cbModificarActividad.setChecked( permisos.getOrDefault("PUEDE_MODIFICAR_ACTIVIDAD", false) );
            cbReagendarActividad.setChecked( permisos.getOrDefault("PUEDE_REAGENDAR_ACTIVIDAD", false) );
            cbCancelarActividad.setChecked( permisos.getOrDefault("PUEDE_CANCELAR_ACTIVIDAD", false) );
            cbAdjuntarArchivos.setChecked( permisos.getOrDefault("PUEDE_ADJUNTAR_ARCHIVOS", false) );
            cbGestionarMantenedores.setChecked( permisos.getOrDefault("PUEDE_GESTIONAR_MANTENEDORES", false) );
        }
    }

    private void actualizarVisibilidadPermisos(String rol) {
        if (rol.equals("usuario")) {
            grupoPermisos.setVisibility(View.VISIBLE);
        } else { // "admin"
            grupoPermisos.setVisibility(View.GONE);
        }
    }

    private void guardarCambios() {
        showLoading(true); // <-- AÑADIDO
        // btnGuardarPermisos.setEnabled(false); // Esta línea está en showLoading

        Map<String, Object> updates = new HashMap<>();
        updates.put("rol", rolSeleccionado);

        if (rolSeleccionado.equals("usuario")) {
            Map<String, Object> nuevosPermisos = new HashMap<>();
            nuevosPermisos.put("PUEDE_CREAR_ACTIVIDAD", cbCrearActividad.isChecked());
            nuevosPermisos.put("PUEDE_MODIFICAR_ACTIVIDAD", cbModificarActividad.isChecked());
            nuevosPermisos.put("PUEDE_REAGENDAR_ACTIVIDAD", cbReagendarActividad.isChecked());
            nuevosPermisos.put("PUEDE_CANCELAR_ACTIVIDAD", cbCancelarActividad.isChecked());
            nuevosPermisos.put("PUEDE_ADJUNTAR_ARCHIVOS", cbAdjuntarArchivos.isChecked());
            nuevosPermisos.put("PUEDE_GESTIONAR_MANTENEDORES", cbGestionarMantenedores.isChecked());
            updates.put("permisos", nuevosPermisos);
        } else if (rolSeleccionado.equals("admin")) {
            updates.put("permisos", FieldValue.delete());
        }

        db.collection("usuarios").document(usuarioUid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false); // <-- AÑADIDO
                    Toast.makeText(this, "Permisos actualizados con éxito.", Toast.LENGTH_SHORT).show();
                    finish(); // Vuelve a la lista de usuarios
                })
                .addOnFailureListener(e -> {
                    showLoading(false); // <-- AÑADIDO
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // btnGuardarPermisos.setEnabled(true); // Esta línea está en showLoading
                });
    }

    /**
     * Helper para mostrar/ocultar el overlay de carga.
     * ¡CORREGIDO!
     */
    // @Override // <-- CORRECCIÓN 1: Quitar @Override
    public void showLoading(boolean isLoading) {
        if (loadingOverlay == null) {
            Log.e(TAG, "loadingOverlay es nulo.");
            return;
        }

        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            btnGuardarPermisos.setEnabled(false); // <-- CORRECCIÓN 2: Usar btnGuardarPermisos
        } else {
            loadingOverlay.setVisibility(View.GONE);
            btnGuardarPermisos.setEnabled(true); // <-- CORRECCIÓN 3: Usar btnGuardarPermisos
        }
    }
}