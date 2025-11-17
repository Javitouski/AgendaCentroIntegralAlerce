package prog.android.centroalr.view;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import prog.android.centroalr.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<DocumentSnapshot> lista;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificationAdapter(List<DocumentSnapshot> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DocumentSnapshot doc = lista.get(position);

        String titulo = doc.getString("titulo");
        String mensaje = doc.getString("mensaje");
        Long timestamp = doc.getLong("timestamp");
        boolean leido = Boolean.TRUE.equals(doc.getBoolean("leido"));

        h.txtTitulo.setText(titulo);
        h.txtMensaje.setText(mensaje);

        if (timestamp != null) {
            h.txtFecha.setText(df.format(timestamp));
        }

        // Si no está leído → texto en negrita
        h.txtTitulo.setTypeface(null, leido ? Typeface.NORMAL : Typeface.BOLD);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitulo, txtMensaje, txtFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitulo = itemView.findViewById(R.id.notif_title);
            txtMensaje = itemView.findViewById(R.id.notif_message);
            txtFecha = itemView.findViewById(R.id.notif_date);
        }
    }
}
