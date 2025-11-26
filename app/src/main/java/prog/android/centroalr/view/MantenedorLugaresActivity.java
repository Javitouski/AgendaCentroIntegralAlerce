package prog.android.centroalr.view;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Necesario para el layout del dialog
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

public class MantenedorLugaresActivity extends AppCompatActivity implements MantenedorAdapter.OnItemClickListener {

    private static final String TAG = "MantenedorLugares";
    private final String COLECCION = "lugares";
    private final String CAMPO_NOMBRE = "descripcion";

    private RecyclerView recyclerMantenedor;
    private ProgressBar progressBar;
    private TextView tvMensajeVacio;
    private ImageButton btnBack;
    private FloatingActionButton fabAgregar;

    private FirebaseFirestore db;
    private MantenedorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenedor_lugares);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerMantenedor = findViewById(R.id.recyclerMantenedor);
        progressBar = findViewById(R.id.progressBar);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        fabAgregar = findViewById(R.id.fabAgregar);

        btnBack.setOnClickListener(v -> finish());
        fabAgregar.setOnClickListener(v -> mostrarDialogoEditarCrear(null));

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
                            // LEER CAPACIDAD
                            Long cap = doc.getLong("capacidad");
                            int capacidad = (cap != null) ? cap.intValue() : 0;

                            if (nombre != null) {
                                lista.add(new SimpleMantenedorItem(doc.getId(), nombre, capacidad));
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

    // --- DIÁLOGO PERSONALIZADO CON 2 CAMPOS (NOMBRE Y CAPACIDAD) ---
    private void mostrarDialogoEditarCrear(SimpleMantenedorItem item) {
        boolean esCrear = (item == null);
        String titulo = esCrear ? "Crear Nuevo Lugar" : "Editar Lugar";
        String nombreActual = esCrear ? "" : item.getNombre();
        String capacidadActual = (esCrear || item.getCapacidad() == 0) ? "" : String.valueOf(item.getCapacidad());

        // Contenedor vertical
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Margenes
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_margin); // Asegúrate de tener este dimen o usa 50
        // Si no tienes el dimen, usa un valor fijo en px, ej: 50
        layout.setPadding(50, 40, 50, 10);

        // Input Nombre
        final EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del lugar (Ej: Oficina 1)");
        inputNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputNombre.setText(nombreActual);
        layout.addView(inputNombre);

        // Input Capacidad
        final EditText inputCapacidad = new EditText(this);
        inputCapacidad.setHint("Aforo máximo (personas)");
        inputCapacidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputCapacidad.setText(capacidadActual);
        layout.addView(inputCapacidad);

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = inputNombre.getText().toString().trim();
                    String nuevaCapacidadStr = inputCapacidad.getText().toString().trim();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int nuevaCapacidad = 0;
                    if (!nuevaCapacidadStr.isEmpty()) {
                        try {
                            nuevaCapacidad = Integer.parseInt(nuevaCapacidadStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Capacidad inválida", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (esCrear) {
                        guardarNuevoItem(nuevoNombre, nuevaCapacidad);
                    } else {
                        actualizarItem(item, nuevoNombre, nuevaCapacidad);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarNuevoItem(String nombre, int capacidad) {
        Map<String, Object> data = new HashMap<>();
        data.put(CAMPO_NOMBRE, nombre);
        data.put("capacidad", capacidad); // Guardamos el entero

        db.collection(COLECCION).add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Lugar creado.", Toast.LENGTH_SHORT).show();
                    cargarItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarItem(SimpleMantenedorItem item, String nuevoNombre, int nuevaCapacidad) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(CAMPO_NOMBRE, nuevoNombre);
        updates.put("capacidad", nuevaCapacidad);

        db.collection(COLECCION).document(item.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lugar actualizado.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(this, "Lugar borrado.", Toast.LENGTH_SHORT).show();
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