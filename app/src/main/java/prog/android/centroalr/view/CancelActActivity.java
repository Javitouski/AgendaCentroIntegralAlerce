package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import prog.android.centroalr.R;

public class CancelActActivity extends AppCompatActivity {

    private String actividadId;
    private FirebaseFirestore db;

    private EditText etCancelReason;
    private TextView txtTitle, txtSelectedActivity;
    private View btnCancelActivity, loadingOverlay, btnBack;

    private boolean esReactivar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelar_act);

        actividadId = getIntent().getStringExtra("actividadId");
        db = FirebaseFirestore.getInstance();

        initViews();

        // Configuración de bordes (Insets) - MANTENIDO DE TU CÓDIGO
        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (actividadId != null) {
            cargarEstadoActividad();
        } else {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtSelectedActivity = findViewById(R.id.txtSelectedActivity);
        etCancelReason = findViewById(R.id.etCancelReason);
        btnCancelActivity = findViewById(R.id.btnCancelActivity);
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancelActivity != null) btnCancelActivity.setOnClickListener(v -> ejecutarAccion());
    }

    private void cargarEstadoActividad() {
        mostrarCarga(true);
        db.collection("actividades").document(actividadId).get()
                .addOnSuccessListener(doc -> {
                    mostrarCarga(false);
                    if (doc.exists()) {
                        String nombre = doc.getString("nombre");
                        String estado = doc.getString("estado");

                        // Mostrar nombre real
                        if (txtSelectedActivity != null) txtSelectedActivity.setText(nombre);

                        // Determinar si es cancelar o reactivar
                        esReactivar = "cancelada".equalsIgnoreCase(estado);
                        actualizarUI();
                    } else {
                        Toast.makeText(this, "La actividad no existe", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarUI() {
        if (txtTitle != null) {
            txtTitle.setText(esReactivar ? "Reactivar actividad" : "Cancelar actividad");
        }

        // Actualizar texto del botón (manejo seguro)
        if (btnCancelActivity instanceof ViewGroup) {
            // Si es un layout complejo (como parece ser en tu XML)
            ViewGroup vg = (ViewGroup) btnCancelActivity;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setText(esReactivar ? "Reactivar actividad" : "Cancelar actividad");
                    break;
                }
            }
        } else if (btnCancelActivity instanceof TextView) {
            // Si es un botón simple
            ((TextView) btnCancelActivity).setText(esReactivar ? "Reactivar actividad" : "Cancelar actividad");
        }
    }

    private void ejecutarAccion() {
        String nuevoEstado = esReactivar ? "activa" : "cancelada";
        // Opcional: Guardar el motivo si lo deseas
        // String motivo = etCancelReason.getText().toString();

        mostrarCarga(true);
        db.collection("actividades").document(actividadId)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(v -> {
                    mostrarCarga(false);
                    String accion = esReactivar ? "reactivada" : "cancelada";
                    Toast.makeText(this, "Actividad " + accion + " correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarCarga(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        if (btnCancelActivity != null) btnCancelActivity.setEnabled(!mostrar);
    }

}