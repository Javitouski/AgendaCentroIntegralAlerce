package prog.android.centroalr.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prog.android.centroalr.R;
import prog.android.centroalr.adapter.MantenedorAdapter;
import prog.android.centroalr.model.SimpleMantenedorItem;

/**
 * Activity CRUD para gestionar la colección "proyecto" en Firestore.
 */
public class MantenedorSociosActivity extends AppCompatActivity implements MantenedorAdapter.OnItemClickListener {

    private static final String TAG = "MantenedorSocios";
    // <-- CAMBIO 1:
    private final String COLECCION = "socioComunitario";
    // <-- CAMBIO 2: (Según tu BD, el campo es "nombre")
    private final String CAMPO_NOMBRE = "nombre";

    // Vistas
    private RecyclerView recyclerMantenedor;
    private ProgressBar progressBar;
    private TextView tvMensajeVacio;
    private ImageButton btnBack;
    private FloatingActionButton fabAgregar;
    private TextView tvTitulo; // <-- CAMBIO 3: Variable

    // Firebase y Adaptador
    private FirebaseFirestore db;
    private MantenedorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenedor_lugares); // Reutilizamos el MISMO LAYOUT

        db = FirebaseFirestore.getInstance();

        // Encontrar Vistas
        btnBack = findViewById(R.id.btnBack);
        recyclerMantenedor = findViewById(R.id.recyclerMantenedor);
        progressBar = findViewById(R.id.progressBar);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        fabAgregar = findViewById(R.id.fabAgregar);
        tvTitulo = findViewById(R.id.tvTitulo); // <-- CAMBIO 4: Encontrar

        // --- Configurar UI específica ---
        tvTitulo.setText("Gestionar Proyectos"); // <-- CAMBIO 4
        fabAgregar.setContentDescription("Añadir nuevo proyecto");
        tvMensajeVacio.setText("No hay proyectos creados.");
        // --- Fin Configurar UI ---

        // Configurar botón de volver
        btnBack.setOnClickListener(v -> finish());

        // Configurar botón de añadir
        fabAgregar.setOnClickListener(v -> mostrarDialogoEditarCrear(null));

        // Configurar la lista
        setupRecyclerView();
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
        cargarItems();
    }

    private void setupRecyclerView() {
        adapter = new MantenedorAdapter(this);
        recyclerMantenedor.setLayoutManager(new LinearLayoutManager(this));
        recyclerMantenedor.setAdapter(adapter);
    }

    // --- El resto del código es IDÉNTICO ---

    private void cargarItems() {
        mostrarLoading(true);

        db.collection(COLECCION)
                .orderBy(CAMPO_NOMBRE, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mostrarLoading(false);
                    List<SimpleMantenedorItem> lista = new ArrayList<>();

                    if (querySnapshot.isEmpty()) {
                        tvMensajeVacio.setVisibility(View.VISIBLE);
                        recyclerMantenedor.setVisibility(View.GONE);
                    } else {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String nombre = doc.getString(CAMPO_NOMBRE);
                            if (nombre != null) {
                                lista.add(new SimpleMantenedorItem(doc.getId(), nombre));
                            }
                        }
                        adapter.setItems(lista);
                        tvMensajeVacio.setVisibility(View.GONE);
                        recyclerMantenedor.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarLoading(false);
                    Log.e(TAG, "Error al cargar " + COLECCION, e);
                    tvMensajeVacio.setText("Error al cargar datos.");
                    tvMensajeVacio.setVisibility(View.VISIBLE);
                });
    }

    private void mostrarLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerMantenedor.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoEditarCrear(SimpleMantenedorItem item) {
        boolean esCrear = (item == null);
        String titulo = esCrear ? "Crear Nuevo Proyecto" : "Editar Proyecto";
        String nombreActual = esCrear ? "" : item.getNombre();

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(nombreActual);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setView(container)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (esCrear) {
                        guardarNuevoItem(nuevoNombre);
                    } else {
                        actualizarItem(item, nuevoNombre);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarNuevoItem(String nombre) {
        Map<String, Object> data = new HashMap<>();
        data.put(CAMPO_NOMBRE, nombre);
        // data.put("descripcion", ""); // Opcional

        db.collection(COLECCION).add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Ítem creado.", Toast.LENGTH_SHORT).show();
                    cargarItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarItem(SimpleMantenedorItem item, String nuevoNombre) {
        db.collection(COLECCION).document(item.getId())
                .update(CAMPO_NOMBRE, nuevoNombre)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ítem actualizado.", Toast.LENGTH_SHORT).show();
                    cargarItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void borrarItem(SimpleMantenedorItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Borrado")
                .setMessage("¿Estás seguro de que quieres borrar '" + item.getNombre() + "'?")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    db.collection(COLECCION).document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Ítem borrado.", Toast.LENGTH_SHORT).show();
                                cargarItems();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al borrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void onEditClick(SimpleMantenedorItem item) {
        mostrarDialogoEditarCrear(item);
    }

    @Override
    public void onDeleteClick(SimpleMantenedorItem item) {
        borrarItem(item);
    }
}