package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.controller.InfUsuarioController;
import prog.android.centroalr.model.Usuario;

public class InfUsuarioActivity extends AppCompatActivity implements InfUsuarioView {

    private EditText etNombre, etFuncion;
    private TextView tvPermisoSeleccionado;
    private ImageView btnBack;

    private MaterialButton btnSubirArchivo; // ← NUEVO

    private InfUsuarioController controller;
    private Usuario usuarioActual; // ← guardo el usuario para usar su ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inf_usuario);

        // 1. Inicializar Vistas
        initViews();

        // 2. Obtener usuario de la sesión global
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        // 3. Inicializar Controlador y pedir datos
        controller = new InfUsuarioController(this, usuarioActual);
        controller.cargarDatos();

        // 4. Configurar botón para subir archivo
        configurarBotonSubirArchivo();
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

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etFuncion = findViewById(R.id.etFuncion);
        tvPermisoSeleccionado = findViewById(R.id.tvPermisoSeleccionado);
        btnBack = findViewById(R.id.btnBack);
        btnSubirArchivo = findViewById(R.id.btnSubirArchivoInfUsuario); // ← NUEVO

        // Configurar botón volver
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        // Bloquear edición para que solo sea visual
        deshabilitarEdicion(etNombre);
        deshabilitarEdicion(etFuncion);
    }

    private void configurarBotonSubirArchivo() {
        if (btnSubirArchivo != null) {
            btnSubirArchivo.setOnClickListener(v -> {

                if (usuarioActual == null) {
                    Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Enviar ID del usuario a la pantalla de adjuntar archivos
                Intent i = new Intent(InfUsuarioActivity.this, AdjuntarArchivosActivity.class);
                i.putExtra("idUsuario", usuarioActual.getUid());
                startActivity(i);
            });
        }
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
