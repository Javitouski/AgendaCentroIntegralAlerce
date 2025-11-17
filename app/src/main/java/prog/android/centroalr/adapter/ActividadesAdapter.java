package prog.android.centroalr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import prog.android.centroalr.R;
import prog.android.centroalr.model.Actividad;

public class ActividadesAdapter extends RecyclerView.Adapter<ActividadesAdapter.ActividadViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Actividad actividad);
    }

    private List<Actividad> actividades = new ArrayList<>();
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ActividadesAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Actividad> nuevaLista) {
        actividades.clear();
        actividades.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad actividad = actividades.get(position);

        // 1. Inicial del nombre en el badge
        String nombre = actividad.getNombre();
        if (nombre != null && !nombre.trim().isEmpty()) {
            holder.tvInicial.setText(nombre.trim().substring(0, 1).toUpperCase());
        } else {
            holder.tvInicial.setText("?");
        }

        // 2. Nombre visible
        holder.tvNombre.setText(nombre != null ? nombre : "Sin nombre");

        // 3. Fechas y Horas (Mejorado para mostrar hora si es el mismo día)
        String textoFechas;
        if (actividad.getFechaInicio() != null) {
            String fecha = dateFormat.format(actividad.getFechaInicio().toDate());
            String horaIni = timeFormat.format(actividad.getFechaInicio().toDate());
            String horaFin = (actividad.getFechaFin() != null) ? timeFormat.format(actividad.getFechaFin().toDate()) : "??";

            // Ejemplo: "17-11-2025 (14:00 - 15:00)"
            textoFechas = fecha + " (" + horaIni + " - " + horaFin + ")";
        } else {
            textoFechas = "Fecha no definida";
        }
        holder.tvFechas.setText(textoFechas);

        // 4. LUGAR (Solución del problema de las letras)
        // Usamos un tag para evitar problemas al hacer scroll rápido (recycling)
        String tagId = actividad.getId();
        holder.tvLugar.setTag(tagId);

        if (actividad.getLugarId() != null) {
            // Ponemos un texto temporal
            holder.tvLugar.setText("Cargando lugar...");

            // Consultamos el nombre real
            actividad.getLugarId().get().addOnSuccessListener(documentSnapshot -> {
                // Verificamos que esta vista siga correspondiendo a la misma actividad
                if (holder.tvLugar.getTag() != null && holder.tvLugar.getTag().equals(tagId)) {
                    if (documentSnapshot.exists()) {
                        // Buscamos 'descripcion' primero, luego 'nombre'
                        String lugarReal = documentSnapshot.getString("descripcion");
                        if (lugarReal == null) lugarReal = documentSnapshot.getString("nombre");

                        holder.tvLugar.setText(lugarReal != null ? lugarReal : "Sin nombre");
                    } else {
                        holder.tvLugar.setText("Lugar no encontrado");
                    }
                }
            }).addOnFailureListener(e -> {
                if (holder.tvLugar.getTag().equals(tagId)) {
                    holder.tvLugar.setText("Error al cargar");
                }
            });
        } else {
            holder.tvLugar.setText("Lugar no especificado");
        }

        // 5. Estado
        String estado = actividad.getEstado() != null && !actividad.getEstado().isEmpty()
                ? actividad.getEstado()
                : "SIN ESTADO";
        holder.tvEstado.setText(estado);

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(actividad);
            }
        });
    }


    @Override
    public int getItemCount() {
        return actividades.size();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvFechas, tvLugar, tvEstado, tvInicial;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre   = itemView.findViewById(R.id.tvNombreActividad);
            tvFechas   = itemView.findViewById(R.id.tvFechas);
            tvLugar    = itemView.findViewById(R.id.tvLugar);
            tvEstado   = itemView.findViewById(R.id.tvEstado);
            tvInicial  = itemView.findViewById(R.id.tvInicialActividad);
        }
    }
}