package prog.android.centroalr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // Inicial del nombre en el badge
        String nombre = actividad.getNombre();
        if (nombre != null && !nombre.trim().isEmpty()) {
            holder.tvInicial.setText(nombre.trim().substring(0, 1).toUpperCase());
        } else {
            holder.tvInicial.setText("?");
        }

        // Nombre visible
        holder.tvNombre.setText(nombre != null ? nombre : "Sin nombre");

        // Fechas
        String textoFechas;
        if (actividad.getFechaInicio() != null && actividad.getFechaFin() != null) {
            String inicio = dateFormat.format(actividad.getFechaInicio().toDate());
            String fin = dateFormat.format(actividad.getFechaFin().toDate());
            textoFechas = inicio + " - " + fin;
        } else if (actividad.getFechaInicio() != null) {
            textoFechas = dateFormat.format(actividad.getFechaInicio().toDate());
        } else {
            textoFechas = "Fecha no definida";
        }
        holder.tvFechas.setText(textoFechas);

        // Lugar legible
        String lugarTexto = nombreLugarLegible(actividad);
        holder.tvLugar.setText(lugarTexto);

        // Estado
        String estado = actividad.getEstado() != null && !actividad.getEstado().isEmpty()
                ? actividad.getEstado()
                : "SIN ESTADO";
        holder.tvEstado.setText(estado);

        // Click
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
    private String nombreLugarLegible(Actividad actividad) {
        if (actividad.getLugarId() == null) {
            return "Lugar no especificado";
        }

        String id = actividad.getLugarId().getId(); // p.ej. "oficina", "salaMultiuso1", etc.

        // Ajusta estos casos a los IDs reales de tu colección "lugares"
        switch (id) {
            case "oficina":
                return "Oficina principal del centro comunitario";
            case "salaMultiuso1":
                return "Sala multiuso 1";
            case "salaMultiuso2":
                return "Sala multiuso 2";
            default:
                // Fallback: al menos capitalizar un poco
                String s = id.replace("_", " ").replace("-", " ");
                if (s.isEmpty()) return "Lugar no especificado";
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }


}
