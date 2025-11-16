package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton; // Importar ImageButton
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.R;
import prog.android.centroalr.adapter.UsuarioAdapter;
import prog.android.centroalr.model.Usuario;

/**
 * Activity para que un Admin vea la lista de todos los usuarios
 * y seleccione uno para editar sus permisos.
 * Implementa la interfaz del adaptador para recibir clics.
 */
public class GestionUsuariosActivity extends AppCompatActivity implements UsuarioAdapter.OnUsuarioClickListener {

    private static final String TAG = "GestionUsuariosActivity";

    // Vistas
    private RecyclerView recyclerUsuarios;
    private ProgressBar progressBar;
    private TextView tvMensajeVacio;
    private ImageButton btnBack; // Corregido a ImageButton

    // Firebase y Adaptador
    private FirebaseFirestore db;
    private UsuarioAdapter adapter;
    private List<Usuario> listaUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Encontrar Vistas
        btnBack = findViewById(R.id.btnBack);
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        progressBar = findViewById(R.id.progressBar);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);

        // Configurar botón de volver
        btnBack.setOnClickListener(v -> finish());

        // Configurar la lista
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargamos los usuarios cada vez que la pantalla se muestra
        // (Esto asegura que la lista se refresque si volvemos de editar)
        cargarUsuarios();
    }

    private void setupRecyclerView() {
        // Inicializamos el adaptador y le pasamos "this" (esta Activity)
        // como el listener para los clics.
        adapter = new UsuarioAdapter(this);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsuarios.setAdapter(adapter);
    }

    private void cargarUsuarios() {
        mostrarLoading(true);
        listaUsuarios.clear(); // Limpiar la lista antes de cargar

        // Consultar la colección "usuarios", ordenados por nombre
        db.collection("usuarios")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mostrarLoading(false);
                    if (querySnapshot.isEmpty()) {
                        tvMensajeVacio.setVisibility(View.VISIBLE);
                        recyclerUsuarios.setVisibility(View.GONE);
                    } else {
                        // Convertir cada documento en un objeto Usuario
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Usuario usuario = doc.toObject(Usuario.class);
                            if (usuario != null) {
                                // Guardamos el UID que es el ID del documento
                                // ¡Importante! Asegúrate que tu modelo Usuario.java tenga un método "setUid(String uid)"
                                usuario.setUid(doc.getId());
                                listaUsuarios.add(usuario);
                            }
                        }
                        // Actualizar el adaptador con la nueva lista
                        adapter.setUsuarios(listaUsuarios);
                        tvMensajeVacio.setVisibility(View.GONE);
                        recyclerUsuarios.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarLoading(false);
                    Log.e(TAG, "Error al cargar usuarios", e);
                    tvMensajeVacio.setText("Error al cargar datos.");
                    tvMensajeVacio.setVisibility(View.VISIBLE);
                    recyclerUsuarios.setVisibility(View.GONE);
                });
    }

    private void mostrarLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerUsuarios.setVisibility(View.GONE);
            tvMensajeVacio.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Este método se llama gracias a la interfaz "OnUsuarioClickListener"
     * que implementamos desde nuestro UsuarioAdapter.
     */
    @Override
    public void onEditClick(Usuario usuario) {
        Toast.makeText(this, "Editando a: " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, EditarPermisosActivity.class);
        intent.putExtra("usuario_uid", usuario.getUid()); // Pasamos el UID
        startActivity(intent);
    }
}