package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.adapter.ActividadesAdapter;
import prog.android.centroalr.controller.ActividadesController;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.Usuario;

public class ListaActividadesActivity extends AppCompatActivity implements ActividadesView {

    private ImageView btnBackLista;
    private FloatingActionButton fabCrearActividadLista;
    private RecyclerView recyclerActividades;
    private TextView tvMensajeVacio;
    private View loadingOverlay;

    // NUEVO: Referencia al Spinner
    private Spinner spinnerFiltroUsuarios;

    private ActividadesAdapter adapter;
    private Usuario usuarioActual;
    private ActividadesController controller;

    // Listas para manejar la selección del filtro
    private List<String> usuariosIds = new ArrayList<>();
    private String filtroActualId = null; // null = "Todos"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_actividades);

        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            irALogin();
            return;
        }

        initViews();
        initRecycler();
        initListeners();

        controller = new ActividadesController(this);

        // 1. Cargar el filtro de usuarios al iniciar
        cargarFiltroUsuarios();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar lista usando el filtro que esté seleccionado actualmente
        if (controller != null) {
            controller.cargarLista(filtroActualId);
        }
    }

    private void initViews() {
        btnBackLista = findViewById(R.id.btnBackLista);
        fabCrearActividadLista = findViewById(R.id.fabCrearActividadLista);
        recyclerActividades = findViewById(R.id.recyclerActividades);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Inicializar Spinner
        spinnerFiltroUsuarios = findViewById(R.id.spinnerFiltroUsuarios);
    }

    private void initRecycler() {
        recyclerActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadesAdapter(actividad -> {
            if (actividad != null && actividad.getId() != null) {
                Intent intent = new Intent(ListaActividadesActivity.this, DetActActivity.class);
                intent.putExtra("actividadId", actividad.getId());
                intent.putExtra("event_text", actividad.getNombre());
                startActivity(intent);
            }
        });
        recyclerActividades.setAdapter(adapter);
    }

    private void initListeners() {
        btnBackLista.setOnClickListener(v -> finish());

        if (usuarioActual.tienePermiso("PUEDE_CREAR_ACTIVIDAD")) {
            fabCrearActividadLista.setVisibility(View.VISIBLE);
            fabCrearActividadLista.setOnClickListener(v -> {
                startActivity(new Intent(ListaActividadesActivity.this, CrearActActivity.class));
            });
        } else {
            fabCrearActividadLista.setVisibility(View.GONE);
        }
    }

    // Método para llenar el Spinner y configurar su listener
    private void cargarFiltroUsuarios() {
        controller.cargarUsuariosParaFiltro((nombres, ids) -> {
            usuariosIds = ids; // Guardamos los IDs correspondientes

            // Adaptador simple para mostrar los nombres
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
            );
            spinnerFiltroUsuarios.setAdapter(spinnerAdapter);

            // Listener de selección
            spinnerFiltroUsuarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Obtenemos el ID real (o null si es "Todos")
                    filtroActualId = usuariosIds.get(position);
                    // Recargamos la lista aplicando el filtro
                    controller.cargarLista(filtroActualId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        });
    }

    private void irALogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =========================================================
    // IMPLEMENTACIÓN DE LA INTERFAZ (ActividadesView)
    // =========================================================

    @Override
    public void mostrarCarga(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void mostrarListaActividades(List<Actividad> actividades) {
        recyclerActividades.setVisibility(View.VISIBLE);
        tvMensajeVacio.setVisibility(View.GONE);
        adapter.setData(actividades);
    }

    @Override
    public void mostrarMensajeVacio() {
        recyclerActividades.setVisibility(View.GONE);
        tvMensajeVacio.setVisibility(View.VISIBLE);
        // Mensaje personalizado según si hay filtro o no
        if (filtroActualId != null) {
            tvMensajeVacio.setText("Este usuario no ha creado actividades.");
        } else {
            tvMensajeVacio.setText("No hay actividades registradas.");
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        recyclerActividades.setVisibility(View.GONE);
        tvMensajeVacio.setVisibility(View.VISIBLE);
        tvMensajeVacio.setText(mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}