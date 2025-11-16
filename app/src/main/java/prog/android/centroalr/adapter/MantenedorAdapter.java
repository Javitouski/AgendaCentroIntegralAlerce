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
import prog.android.centroalr.model.SimpleMantenedorItem;

/**
 * Adaptador genérico para las pantallas de Mantenedor (Lugares, Tipos, etc.)
 * Utiliza el layout item_mantenedor_simple.xml
 */
public class MantenedorAdapter extends RecyclerView.Adapter<MantenedorAdapter.MantenedorViewHolder> {

    private List<SimpleMantenedorItem> itemList = new ArrayList<>();
    private final OnItemClickListener listener;

    /**
     * Interfaz para comunicar clics de Editar y Borrar a la Activity.
     */
    public interface OnItemClickListener {
        void onEditClick(SimpleMantenedorItem item);
        void onDeleteClick(SimpleMantenedorItem item);
    }

    /**
     * Constructor que recibe el listener.
     */
    public MantenedorAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MantenedorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mantenedor_simple, parent, false);
        return new MantenedorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MantenedorViewHolder holder, int position) {
        SimpleMantenedorItem item = itemList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Método para actualizar la lista de ítems desde la Activity.
     */
    public void setItems(List<SimpleMantenedorItem> items) {
        this.itemList.clear();
        this.itemList.addAll(items);
        notifyDataSetChanged(); // Refresca la lista
    }

    // ====================================================================
    //  VIEWHOLDER (El controlador de cada fila)
    // ====================================================================
    static class MantenedorViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombreItem;
        ImageButton btnEditarItem, btnBorrarItem;

        public MantenedorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreItem = itemView.findViewById(R.id.tvNombreItem);
            btnEditarItem = itemView.findViewById(R.id.btnEditarItem);
            btnBorrarItem = itemView.findViewById(R.id.btnBorrarItem);
        }

        /**
         * Pinta los datos y asigna los listeners de clic.
         */
        public void bind(final SimpleMantenedorItem item, final OnItemClickListener listener) {
            tvNombreItem.setText(item.getNombre());

            btnEditarItem.setOnClickListener(v -> listener.onEditClick(item));
            btnBorrarItem.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }
}