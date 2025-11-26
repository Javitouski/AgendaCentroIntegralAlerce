package prog.android.centroalr.controller;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.view.ActividadesView;

public class ActividadesController {

    private ActividadesView view;
    private FirebaseFirestore db;

    public interface OnUsuariosCargadosListener {
        void onUsuariosCargados(List<String> nombres, List<String> ids);
    }

    public ActividadesController(ActividadesView view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
    }

    public void cargarUsuariosParaFiltro(OnUsuariosCargadosListener listener) {
        // ... (Este método déjalo igual que antes) ...
        db.collection("usuarios").orderBy("nombre").get()
                .addOnSuccessListener(qs -> {
                    List<String> names = new ArrayList<>();
                    List<String> ids = new ArrayList<>();
                    names.add("Todos los usuarios");
                    ids.add(null);
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        if (doc.getString("nombre") != null) {
                            names.add(doc.getString("nombre"));
                            ids.add(doc.getId());
                        }
                    }
                    listener.onUsuariosCargados(names, ids);
                })
                .addOnFailureListener(e -> {
                    List<String> n = new ArrayList<>(); n.add("Todos");
                    List<String> i = new ArrayList<>(); i.add(null);
                    listener.onUsuariosCargados(n, i);
                });
    }

    // === AQUÍ ESTÁ LA MODIFICACIÓN DEL MENSAJE ===
    public void cargarLista(String filtroUsuarioId) {
        view.mostrarCarga(true);

        Query query = db.collection("actividades")
                .orderBy("fechaInicio", Query.Direction.ASCENDING);

        if (filtroUsuarioId != null && !filtroUsuarioId.isEmpty()) {
            query = query.whereEqualTo("creadaPorUsuarioId", db.collection("usuarios").document(filtroUsuarioId));
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Actividad> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Actividad act = doc.toObject(Actividad.class);
                        if (act != null) {
                            act.setId(doc.getId());
                            lista.add(act);
                        }
                    }
                    view.mostrarCarga(false);

                    if (lista.isEmpty()) {
                        view.mostrarMensajeVacio(); // Esto mostrará el texto definido en la Activity
                    } else {
                        view.mostrarListaActividades(lista);
                    }
                })
                .addOnFailureListener(e -> {
                    view.mostrarCarga(false);

                    // TRADUCCIÓN DE ERRORES
                    String mensajeAmigable;
                    String errorReal = e.getMessage();

                    if (errorReal != null && errorReal.contains("FAILED_PRECONDITION")) {
                        mensajeAmigable = "Error de configuración: Falta crear el índice en Firebase. Revisa el Logcat.";
                    } else if (errorReal != null && errorReal.contains("PERMISSION_DENIED")) {
                        mensajeAmigable = "No tienes permisos para ver esta información.";
                    } else if (errorReal != null && errorReal.contains("UNAVAILABLE")) {
                        mensajeAmigable = "Sin conexión a internet o servidor no disponible.";
                    } else {
                        mensajeAmigable = "Ocurrió un error al cargar datos.";
                    }

                    view.mostrarError(mensajeAmigable);
                });
    }
}