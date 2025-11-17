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

public class MantenedorAdapter extends RecyclerView.Adapter<MantenedorAdapter.MantenedorViewHolder> {

    private List<SimpleMantenedorItem> itemList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(SimpleMantenedorItem item);
        void onDeleteClick(SimpleMantenedorItem item);
    }

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

    public void setItems(List<SimpleMantenedorItem> items) {
        this.itemList.clear();
        this.itemList.addAll(items);
        notifyDataSetChanged();
    }

    static class MantenedorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreItem, tvCapacidadItem; // Agregamos la referencia
        ImageButton btnEditarItem, btnBorrarItem;

        public MantenedorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreItem = itemView.findViewById(R.id.tvNombreItem);
            tvCapacidadItem = itemView.findViewById(R.id.tvCapacidadItem); // Encontrar vista
            btnEditarItem = itemView.findViewById(R.id.btnEditarItem);
            btnBorrarItem = itemView.findViewById(R.id.btnBorrarItem);
        }

        public void bind(final SimpleMantenedorItem item, final OnItemClickListener listener) {
            tvNombreItem.setText(item.getNombre());

            // Lógica de visualización de aforo
            if (item.getCapacidad() > 0) {
                tvCapacidadItem.setVisibility(View.VISIBLE);
                tvCapacidadItem.setText("Aforo máximo: " + item.getCapacidad() + " personas");
            } else {
                tvCapacidadItem.setVisibility(View.GONE);
            }

            btnEditarItem.setOnClickListener(v -> listener.onEditClick(item));
            btnBorrarItem.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }
}