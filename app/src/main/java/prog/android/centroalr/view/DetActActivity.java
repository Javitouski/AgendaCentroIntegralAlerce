package prog.android.centroalr.view;

import android.content.Intent;
import android.content.res.ColorStateList; // Importante para el tint del botón
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Locale;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.controller.DetalleController;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.Usuario;

public class DetActActivity extends AppCompatActivity implements DetalleView {

    // UI References
    private ImageView btnBack;
    private TextView tvTitle, tvTipo, tvEstado, tvFecha, tvHora, tvLugar, tvCupo, tvDescripcion;

    // Menu References
    private FloatingActionButton fabMore;
    private LinearLayout menuAcciones;
    private View btnModificar, btnReagendar, btnCancelar;

    // Referencia al texto dentro del botón Cancelar para cambiarlo
    private TextView tvTextoBotonCancelar;

    private DetalleController controller;
    private Usuario usuarioActual;
    private String actividadId;

    // Formateadores
    private final Locale esCL = new Locale("es", "CL");
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE d 'de' MMMM", esCL);
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", esCL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_det_act);

        // 1. Usuario y Seguridad
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();
        if (usuarioActual == null) {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        // 2. Obtener ID
        if (getIntent().hasExtra("actividadId")) {
            actividadId = getIntent().getStringExtra("actividadId");
        } else {
            finish();
            return;
        }

        // 3. Inicializar
        initViews();
        initListeners();

        // 4. Cargar Controlador
        controller = new DetalleController(this);
        // NOTA: Ya no cargamos datos aquí, lo haremos en onResume para que se actualice siempre
    }

    // SOLUCIÓN 1: Usar onResume para recargar datos al volver de "Cancelar" o "Editar"
    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null && actividadId != null) {
            controller.cargarDetalle(actividadId);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tv_title);
        tvTipo = findViewById(R.id.tvTipo);
        tvEstado = findViewById(R.id.tvEstado);
        tvFecha = findViewById(R.id.tvFecha);
        tvHora = findViewById(R.id.tvHora);
        tvLugar = findViewById(R.id.tvLugar);
        tvCupo = findViewById(R.id.tvCupo);
        tvDescripcion = findViewById(R.id.tvDescripcion);

        fabMore = findViewById(R.id.fabMore);
        menuAcciones = findViewById(R.id.menuAcciones);

        btnModificar = findViewById(R.id.btn_modificar);
        btnReagendar = findViewById(R.id.btn_reagendar);
        btnCancelar = findViewById(R.id.btn_cancelar);

        // Texto dentro del botón cancelar
        tvTextoBotonCancelar = findViewById(R.id.tv_btn_cancelar_text);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Lógica de permisos
        boolean canModify = usuarioActual.tienePermiso("PUEDE_MODIFICAR_ACTIVIDAD");
        boolean canReagendar = usuarioActual.tienePermiso("PUEDE_REAGENDAR_ACTIVIDAD");
        boolean canCancel = usuarioActual.tienePermiso("PUEDE_CANCELAR_ACTIVIDAD");

        if (canModify || canReagendar || canCancel) {
            fabMore.setVisibility(View.VISIBLE);
            fabMore.setOnClickListener(v -> {
                menuAcciones.setVisibility(menuAcciones.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            btnModificar.setVisibility(canModify ? View.VISIBLE : View.GONE);
            btnReagendar.setVisibility(canReagendar ? View.VISIBLE : View.GONE);
            btnCancelar.setVisibility(canCancel ? View.VISIBLE : View.GONE);

            btnModificar.setOnClickListener(v -> abrirActivity(ModificarActActivity.class));
            btnReagendar.setOnClickListener(v -> abrirActivity(ReagActActivity.class));

            // Botón Cancelar/Reactivar gestionado por el controlador
            btnCancelar.setOnClickListener(v -> controller.gestionarBotonEstado());

        } else {
            fabMore.setVisibility(View.GONE);
        }
    }

    private void abrirActivity(Class<?> clase) {
        Intent intent = new Intent(this, clase);
        intent.putExtra("actividadId", actividadId);
        startActivity(intent);
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ (DetalleView) ---

    @Override
    public void mostrarCarga(boolean activa) {
        // Opcional: Bloquear interacción o mostrar loading
    }

    @Override
    public void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void mostrarDatosActividad(Actividad act) {
        tvTitle.setText(act.getNombre());
        tvDescripcion.setText(act.getDescripcion());
        tvCupo.setText("Cupo: " + act.getCupo());

        // Estado
        String estado = act.getEstado() != null ? act.getEstado() : "";
        if(!estado.isEmpty()) estado = estado.substring(0, 1).toUpperCase() + estado.substring(1);
        tvEstado.setText(estado);

        // Fechas
        if (act.getFechaInicio() != null) {
            tvFecha.setText(dateFmt.format(act.getFechaInicio().toDate()));
            String hIni = timeFmt.format(act.getFechaInicio().toDate());
            String hFin = act.getFechaFin() != null ? timeFmt.format(act.getFechaFin().toDate()) : "--:--";
            tvHora.setText(hIni + " - " + hFin);
        }
    }

    @Override
    public void mostrarNombreLugar(String nombre) {
        tvLugar.setText(nombre);
    }

    @Override
    public void mostrarNombreTipo(String nombre) {
        tvTipo.setText(nombre);
    }

    // SOLUCIÓN 2: Configuración visual correcta del botón
    @Override
    public void configurarBotonEstado(boolean esCancelada) {
        if (tvTextoBotonCancelar == null || btnCancelar == null) return;

        // Siempre texto blanco para buen contraste
        tvTextoBotonCancelar.setTextColor(Color.WHITE);

        if (esCancelada) {
            // Modo: REACTIVAR
            tvTextoBotonCancelar.setText("Reactivar Actividad");
            // Fondo Verde
            btnCancelar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#18990D")));
        } else {
            // Modo: CANCELAR
            tvTextoBotonCancelar.setText("Cancelar Actividad");
            // Fondo Rojo (para indicar peligro/cancelación)
            btnCancelar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
        }
    }

    @Override
    public void navegarACancelar(String actividadId) {
        Intent intent = new Intent(this, CancelActActivity.class);
        intent.putExtra("actividadId", actividadId);
        startActivity(intent);
    }
}