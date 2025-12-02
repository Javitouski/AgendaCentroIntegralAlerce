package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;   // ⬅️ AGREGADO
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import prog.android.centroalr.R;
import prog.android.centroalr.notificaciones.NotifHelper;   // ⬅️ AGREGADO
import prog.android.centroalr.view.NotificationAdapter;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private LinearLayout emptyState;
    private ImageView btnBack, btnMarkAll;
    private FirebaseFirestore db;

    private NotificationAdapter adapter;
    private List<DocumentSnapshot> listaNotificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();

        NotifHelper.enviarNotificacion(
                this,
                "PRUEBA DEFINITIVA",
                "Si no ves esta notificación, algo del sistema está bloqueando."
        );

        recycler = findViewById(R.id.notifications_recycler);
        emptyState = findViewById(R.id.empty_state);
        btnBack = findViewById(R.id.btn_back);
        btnMarkAll = findViewById(R.id.btn_mark_all_read);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(listaNotificaciones);
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnMarkAll.setOnClickListener(v -> marcarTodasLeidas());

        cargarNotificaciones();

        // -----------------------------------------
        // 🔔 BOTÓN DE PRUEBA DE NOTIFICACIÓN
        // -----------------------------------------
        Button btnTest = findViewById(R.id.btnTestNotif);
        if (btnTest != null) {
            btnTest.setOnClickListener(v -> {
                NotifHelper.enviarNotificacion(
                        NotificationsActivity.this,
                        "Notificación de prueba",
                        "El sistema de notificaciones funciona correctamente 😊"
                );
            });
        }
        // -----------------------------------------

        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void cargarNotificaciones() {
        db.collection("notificaciones")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    listaNotificaciones.clear();
                    listaNotificaciones.addAll(value.getDocuments());
                    adapter.notifyDataSetChanged();

                    if (listaNotificaciones.isEmpty()) {
                        recycler.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                });
    }

    private void marcarTodasLeidas() {
        for (DocumentSnapshot doc : listaNotificaciones) {
            doc.getReference().update("leido", true);
        }
    }
}
