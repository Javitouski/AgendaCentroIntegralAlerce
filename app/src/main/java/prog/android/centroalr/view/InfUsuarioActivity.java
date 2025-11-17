package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.controller.InfUsuarioController;
import prog.android.centroalr.model.Usuario;

public class InfUsuarioActivity extends AppCompatActivity implements InfUsuarioView {

    private EditText etNombre, etFuncion;
    private TextView tvPermisoSeleccionado;
    private ImageView btnBack;

    private InfUsuarioController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inf_usuario);

        // 1. Inicializar Vistas
        initViews();

        // 2. Obtener usuario de la sesión global
        MyApplication myApp = (MyApplication) getApplicationContext();
        Usuario usuario = myApp.getUsuarioActual();

        // 3. Inicializar Controlador y pedir datos
        controller = new InfUsuarioController(this, usuario);
        controller.cargarDatos();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etFuncion = findViewById(R.id.etFuncion);
        tvPermisoSeleccionado = findViewById(R.id.tvPermisoSeleccionado);
        btnBack = findViewById(R.id.btnBack);

        // Configurar botón volver
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        // Bloquear edición para que solo sea visual (opcional)
        deshabilitarEdicion(etNombre);
        deshabilitarEdicion(etFuncion);
    }

    private void deshabilitarEdicion(EditText et) {
        if (et != null) {
            et.setFocusable(false);
            et.setClickable(false);
            et.setCursorVisible(false);
            et.setFocusableInTouchMode(false);
        }
    }

    // --- Métodos de la Interfaz MVP ---

    @Override
    public void mostrarInformacion(Usuario usuario) {
        if (etNombre != null) {
            etNombre.setText(usuario.getNombre());
        }

        if (etFuncion != null) {
            // Capitalizar rol (ej: "admin" -> "Admin")
            String rol = usuario.getRol();
            if (rol != null && !rol.isEmpty()) {
                String rolFormateado = rol.substring(0, 1).toUpperCase() + rol.substring(1);
                etFuncion.setText(rolFormateado);
            } else {
                etFuncion.setText("Usuario");
            }
        }

        if (tvPermisoSeleccionado != null) {
            // Mostrar algo relevante en permisos, o el mismo rol
            tvPermisoSeleccionado.setText("Rol: " + usuario.getRol());
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}