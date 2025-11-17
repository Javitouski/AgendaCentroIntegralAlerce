package prog.android.centroalr.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActividadModel {
    private FirebaseFirestore db;

    public ActividadModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ==========================================
    // PARTE 1: LÓGICA PARA LA LISTA (ESTO FALTABA)
    // ==========================================

    public interface OnActividadesCargadasListener {
        void onSuccess(List<Actividad> lista);
        void onError(String error);
    }

    public void obtenerActividades(OnActividadesCargadasListener listener) {
        db.collection("actividades")
                .orderBy("fechaInicio")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Actividad> lista = procesarSnapshot(snapshot);
                    listener.onSuccess(lista);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    private List<Actividad> procesarSnapshot(QuerySnapshot snapshot) {
        List<Actividad> lista = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Actividad actividad = doc.toObject(Actividad.class);
            if (actividad != null) {
                actividad.setId(doc.getId());
                lista.add(actividad);
            }
        }
        return lista;
    }

    // ==========================================
    // PARTE 2: LÓGICA PARA EL DETALLE (NUEVO)
    // ==========================================

    // Interfaz para cargar UNA sola actividad
    public interface OnSingleActividadListener {
        void onSuccess(Actividad actividad);
        void onError(String error);
    }

    public void obtenerActividadPorId(String id, OnSingleActividadListener listener) {
        db.collection("actividades").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Actividad actividad = doc.toObject(Actividad.class);
                        if (actividad != null) {
                            actividad.setId(doc.getId());
                            listener.onSuccess(actividad);
                        } else {
                            listener.onError("Error al convertir datos.");
                        }
                    } else {
                        listener.onError("La actividad no existe.");
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaz para cargar textos de referencias (Lugar, Tipo, etc.)
    public interface OnTextoReferenciaListener {
        void onTexto(String texto);
    }

    public void obtenerTextoReferencia(DocumentReference ref, String nombreCampo, OnTextoReferenciaListener listener) {
        if (ref == null) {
            listener.onTexto("No asignado");
            return;
        }
        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getString(nombreCampo) != null) {
                listener.onTexto(doc.getString(nombreCampo));
            } else {
                listener.onTexto("Desconocido");
            }
        }).addOnFailureListener(e -> listener.onTexto("Error de carga"));
    }
    public interface OnOperacionListener {
        void onSuccess(String mensaje);
        void onError(String error);
    }
    public void actualizarEstadoActividad(String actividadId, String nuevoEstado, OnOperacionListener listener) {
        db.collection("actividades").document(actividadId)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Estado actualizado a: " + nuevoEstado))
                .addOnFailureListener(e -> listener.onError("Error al actualizar: " + e.getMessage()));
    }
}