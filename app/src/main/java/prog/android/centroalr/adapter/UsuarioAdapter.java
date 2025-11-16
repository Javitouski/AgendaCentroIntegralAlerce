package prog.android.centroalr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarioList = new ArrayList<>();
    private final OnUsuarioClickListener listener;

    /**
     * Interfaz para comunicar clics desde el adaptador a la Activity.
     */
    public interface OnUsuarioClickListener {
        void onEditClick(Usuario usuario);
        // Podríamos añadir onInfoClick, onDeleteClick, etc. aquí en el futuro.
    }

    /**
     * Constructor que recibe el listener.
     */
    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    /**
     * Infla el layout (item_usuario.xml) por cada fila.
     */
    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    /**
     * "Pinta" los datos (nombre, email, rol) del usuario en la fila.
     */
    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    /**
     * Método para actualizar la lista de usuarios desde la Activity.
     */
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarioList.clear();
        this.usuarioList.addAll(usuarios);
        notifyDataSetChanged(); // Refresca la lista en la UI
    }

    // ====================================================================
    //  VIEWHOLDER (El controlador de cada fila individual)
    // ====================================================================
    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        // Vistas del layout item_usuario.xml
        TextView tvNombreUsuario, tvEmailUsuario, tvRolUsuario;
        ImageButton btnEditarPermisos;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encontrar las vistas por su ID
            tvNombreUsuario = itemView.findViewById(R.id.tvNombreUsuario);
            tvEmailUsuario = itemView.findViewById(R.id.tvEmailUsuario);
            tvRolUsuario = itemView.findViewById(R.id.tvRolUsuario);
            btnEditarPermisos = itemView.findViewById(R.id.btnEditarPermisos);
        }

        /**
         * "Pinta" los datos del objeto Usuario en las vistas de esta fila.
         */
        public void bind(final Usuario usuario, final OnUsuarioClickListener listener) {
            tvNombreUsuario.setText(usuario.getNombre());
            tvEmailUsuario.setText(usuario.getEmail());
            tvRolUsuario.setText(usuario.getRol());

            // Asignar el listener al botón de editar
            btnEditarPermisos.setOnClickListener(v -> listener.onEditClick(usuario));
        }
    }
}