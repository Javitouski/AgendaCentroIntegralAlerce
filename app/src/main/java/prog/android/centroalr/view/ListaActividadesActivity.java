package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.R;
import prog.android.centroalr.adapter.ActividadesAdapter;
import prog.android.centroalr.model.Actividad;

public class ListaActividadesActivity extends AppCompatActivity {

    private ImageView btnBackLista;
    private FloatingActionButton fabCrearActividadLista;
    private RecyclerView recyclerActividades;

    private FirebaseFirestore db;
    private ActividadesAdapter adapter;
    private TextView tvMensajeVacio;
    private View loadingOverlay;


    private static final String TAG = "ListaActividades";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_actividades);

        db = FirebaseFirestore.getInstance();

        initViews();
        initRecycler();
        initListeners();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cargarActividades();  // Se ejecuta al entrar y al volver de CrearActActivity
    }

    private void initViews() {
        btnBackLista = findViewById(R.id.btnBackLista);
        fabCrearActividadLista = findViewById(R.id.fabCrearActividadLista);
        recyclerActividades = findViewById(R.id.recyclerActividades);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }
    private void mostrarLoading(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }


    private void initRecycler() {
        recyclerActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadesAdapter(new ActividadesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Actividad actividad) {
                // Ahora abrimos la pantalla de detalle con el ID de la actividad
                if (actividad == null || actividad.getId() == null) {
                    Log.w(TAG, "Actividad clickeada sin ID, no se puede abrir detalle");
                    return;
                }

                Log.d(TAG, "Actividad clickeada: " + actividad.getId());

                Intent intent = new Intent(ListaActividadesActivity.this, DetActActivity.class);
                intent.putExtra("actividadId", actividad.getId());
                // Enviamos también el nombre como texto auxiliar
                intent.putExtra("event_text", actividad.getNombre());
                startActivity(intent);
            }
        });
        recyclerActividades.setAdapter(adapter);
    }

    private void initListeners() {

        btnBackLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fabCrearActividadLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaActividadesActivity.this, CrearActActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cargarActividades() {
        mostrarLoading(true);

        db.collection("actividades")
                .orderBy("fechaInicio")
                .get()
                .addOnSuccessListener(snapshot -> {
                    mostrarLoading(false);          // ⬅️ ocultar overlay
                    onActividadesCargadas(snapshot);
                })
                .addOnFailureListener(e -> {
                    mostrarLoading(false);          // ⬅️ también en error

                    Log.e(TAG, "Error al cargar actividades", e);
                    Toast.makeText(this, "Error al cargar actividades.", Toast.LENGTH_SHORT).show();

                    recyclerActividades.setVisibility(View.GONE);
                    tvMensajeVacio.setVisibility(View.VISIBLE);
                    tvMensajeVacio.setText("No se pudieron cargar las actividades. Intenta nuevamente.");
                });
    }



    private void onActividadesCargadas(@NonNull QuerySnapshot snapshot) {
        List<Actividad> lista = new ArrayList<>();

        if (snapshot.isEmpty()) {
            Log.d(TAG, "No se encontraron actividades");
        }

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Actividad actividad = new Actividad();

            actividad.setId(doc.getId());
            actividad.setNombre(doc.getString("nombre"));
            actividad.setDescripcion(doc.getString("descripcion"));

            Timestamp fechaInicio = doc.getTimestamp("fechaInicio");
            Timestamp fechaFin = doc.getTimestamp("fechaFin");
            actividad.setFechaInicio(fechaInicio);
            actividad.setFechaFin(fechaFin);

            Long cupoLong = doc.getLong("cupo");
            actividad.setCupo(cupoLong != null ? cupoLong.intValue() : 0);

            actividad.setEstado(doc.getString("estado"));

            DocumentReference lugarRef = doc.getDocumentReference("lugarId");
            DocumentReference tipoRef = doc.getDocumentReference("tipoActividadId");
            DocumentReference proyectoRef = doc.getDocumentReference("proyectoId");
            DocumentReference socioRef = doc.getDocumentReference("socioComunitarioId");
            DocumentReference oferenteRef = doc.getDocumentReference("oferenteId");

            actividad.setLugarId(lugarRef);
            actividad.setTipoActividadId(tipoRef);
            actividad.setProyectoId(proyectoRef);
            actividad.setSocioComunitarioId(socioRef);
            actividad.setOferenteId(oferenteRef);

            lista.add(actividad);
        }

        // Mostrar / ocultar mensaje vacío
        if (lista.isEmpty()) {
            recyclerActividades.setVisibility(View.GONE);
            tvMensajeVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerActividades.setVisibility(View.VISIBLE);
            tvMensajeVacio.setVisibility(View.GONE);
        }

        adapter.setData(lista);
    }

}
