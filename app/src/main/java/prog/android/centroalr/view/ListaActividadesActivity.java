package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import prog.android.centroalr.R;
import prog.android.centroalr.adapter.ActividadesAdapter;
import prog.android.centroalr.controller.ActividadesController;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.MyApplication;
import prog.android.centroalr.model.Usuario;

public class ListaActividadesActivity extends AppCompatActivity implements ActividadesView {

    private ImageView btnBackLista;
    private FloatingActionButton fabCrearActividadLista;
    private RecyclerView recyclerActividades;
    private TextView tvMensajeVacio;
    private View loadingOverlay;

    private ActividadesAdapter adapter;
    private Usuario usuarioActual;
    private ActividadesController controller;

    private static final String TAG = "ListaActividades";

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

        // Inicializamos el controlador
        controller = new ActividadesController(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            controller.cargarLista();
        }
    }

    private void initViews() {
        btnBackLista = findViewById(R.id.btnBackLista);
        fabCrearActividadLista = findViewById(R.id.fabCrearActividadLista);
        recyclerActividades = findViewById(R.id.recyclerActividades);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void initRecycler() {
        recyclerActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadesAdapter(new ActividadesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Actividad actividad) {
                if (actividad != null && actividad.getId() != null) {
                    Intent intent = new Intent(ListaActividadesActivity.this, DetActActivity.class);
                    intent.putExtra("actividadId", actividad.getId());
                    intent.putExtra("event_text", actividad.getNombre());
                    startActivity(intent);
                }
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

    private void irALogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =========================================================
    // IMPLEMENTACIÓN CORREGIDA DE LA INTERFAZ (ActividadesView)
    // =========================================================

    @Override
    public void mostrarCarga(boolean mostrar) {
        // Este método maneja tanto mostrar como ocultar
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
        tvMensajeVacio.setText("No hay actividades disponibles.");
    }

    @Override
    public void mostrarError(String mensaje) {
        recyclerActividades.setVisibility(View.GONE);
        tvMensajeVacio.setVisibility(View.VISIBLE);
        tvMensajeVacio.setText(mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}