package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import prog.android.centroalr.R;

public class CancelActActivity extends AppCompatActivity {

    private String actividadId;
    private String actividadNombre;
    private String estadoActual;
    private boolean esReactivar;

    private FirebaseFirestore db;

    private EditText etCancelReason;
    private TextView txtTitle;
    private TextView txtSelectedActivity;
    private View btnCancelActivity; // LinearLayout del botón

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Extras que vienen desde DetActActivity
        actividadId = getIntent().getStringExtra("actividadId");
        actividadNombre = getIntent().getStringExtra("actividadNombre");
        estadoActual = getIntent().getStringExtra("estadoActual");

        // Valor inicial (por si algo viene nulo): usamos el extra como "pista"
        esReactivar = estadoActual != null && estadoActual.equalsIgnoreCase("cancelada");

        db = FirebaseFirestore.getInstance();

        int id = findLayout("activity_cancelar_act", "activity_cancel_act", "cancelar_act", "cancel_act");
        if (id != 0) {
            setContentView(id);
            initViews();
            // 🔴 Paso clave: leer el estado REAL desde Firestore y actualizar modo/labels
            if (actividadId != null && !actividadId.trim().isEmpty()) {
                sincronizarEstadoConServidor();
            }
        } else {
            // Fallback ultra simple para no romper si el XML no se llama como esperamos
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this);
            t.setText(esReactivar ? "Reactivar actividad" : "Cancelar actividad");
            t.setTextSize(20f);
            l.addView(t);
            setContentView(l);
        }
    }

    private void initViews() {
        // Back
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        txtTitle = findViewById(R.id.txtTitle);
        txtSelectedActivity = findViewById(R.id.txtSelectedActivity);
        etCancelReason = findViewById(R.id.etCancelReason);
        btnCancelActivity = findViewById(R.id.btnCancelActivity);

        // Actividad seleccionada
        if (txtSelectedActivity != null && actividadNombre != null && !actividadNombre.isEmpty()) {
            txtSelectedActivity.setText(actividadNombre);
        }

        // Ponemos textos iniciales según esReactivar (por ahora basado en el extra)
        actualizarTituloYBoton();

        if (btnCancelActivity != null) {
            btnCancelActivity.setOnClickListener(v -> {
                if (actividadId == null || actividadId.trim().isEmpty()) {
                    Toast.makeText(this, "No se encontró el ID de la actividad.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String motivo = etCancelReason != null
                        ? etCancelReason.getText().toString().trim()
                        : "";

                // Estado nuevo según el modo actual
                String nuevoEstado = esReactivar ? "activa" : "cancelada";

                db.collection("actividades")
                        .document(actividadId)
                        // Si quieres guardar el motivo, puedes expandir aquí:
                        // .update("estado", nuevoEstado, "motivoCancelacion", motivo)
                        .update("estado", nuevoEstado)
                        .addOnSuccessListener(unused -> {
                            String msg = esReactivar
                                    ? "Actividad reactivada correctamente."
                                    : "Actividad cancelada correctamente.";
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(
                                    this,
                                    "Error al actualizar la actividad: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });
        }
    }

    /**
     * Vuelve a consultar a Firestore el estado actual de la actividad
     * y ajusta esReactivar / textos en función de ese valor.
     */
    private void sincronizarEstadoConServidor() {
        db.collection("actividades")
                .document(actividadId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String estado = doc.getString("estado");
                    estadoActual = estado;
                    esReactivar = estado != null && estado.equalsIgnoreCase("cancelada");

                    // Vuelve a dibujar título + texto del botón según el estado real
                    actualizarTituloYBoton();
                })
                .addOnFailureListener(e -> {
                    // No es crítico, solo informativo
                    Toast.makeText(this,
                            "No se pudo obtener el estado actual de la actividad.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Actualiza título y texto del botón según esReactivar.
     */
    private void actualizarTituloYBoton() {
        if (txtTitle != null) {
            String tituloBase = esReactivar ? "Reactivar actividad" : "Cancelar actividad";
            if (actividadNombre != null && !actividadNombre.isEmpty()) {
                txtTitle.setText(tituloBase + "\n" + actividadNombre);
            } else {
                txtTitle.setText(tituloBase);
            }
        }

        if (btnCancelActivity instanceof ViewGroup) {
            View child = ((ViewGroup) btnCancelActivity).getChildAt(0);
            if (child instanceof TextView) {
                ((TextView) child).setText(esReactivar ? "Reactivar actividad" : "Cancelar actividad");
            }
        }
    }

    private int findLayout(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "layout", getPackageName());
            if (id != 0) return id;
        }
        return 0;
    }
}
