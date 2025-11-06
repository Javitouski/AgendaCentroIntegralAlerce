package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import prog.android.centroalr.R;
import prog.android.centroalr.view.NotificationsAdapter;
import prog.android.centroalr.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private View emptyState;
    private List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setupViews();
        loadNotifications();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.notifications_recycler);
        emptyState = findViewById(R.id.empty_state);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_mark_all_read).setOnClickListener(v -> markAllAsRead());

        adapter = new NotificationsAdapter(notifications, this::onNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        // Aquí cargas tus notificaciones desde la BD o API
        notifications.clear();
        notifications.addAll(getDummyNotifications());
        updateUI();
    }

    private void updateUI() {
        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private void onNotificationClick(Notification notification) {
        notification.setRead(true);
        adapter.notifyDataSetChanged();
        // Aquí navegas a la pantalla correspondiente
    }

    private void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        adapter.notifyDataSetChanged();
    }

    private List<Notification> getDummyNotifications() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification(1, "Nueva cita agendada",
                "Tienes una nueva cita para el 15 de Octubre", "Hace 2 horas", false));
        list.add(new Notification(2, "Recordatorio",
                "Tu cita es mañana a las 10:00 AM", "Hace 5 horas", false));
        list.add(new Notification(3, "Cita cancelada",
                "La cita del 12 de Octubre fue cancelada", "Hace 1 día", true));
        return list;
    }
}