package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import prog.android.centroalr.notificaciones.NotifScheduler; // 🔔
import prog.android.centroalr.notificaciones.NotifHelper;    // 🔔

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import prog.android.centroalr.R;

public class ReagActActivity extends AppCompatActivity {

    private String actividadId;
    private String actividadNombre;

    // Firestore
    private FirebaseFirestore db;
    private DocumentReference actividadRef;

    // UI
    private TextView txtTitle;
    private TextView txtSelectedActivity;
    private TextView txtDate;
    private TextView txtTime;
    private View btnDatePicker;
    private View btnTimePicker;
    private View btnCreateActivity;
    private View btnBack;
    private EditText etReason;

    // Fecha/hora nueva (o actual si no se cambia)
    private Calendar nuevaFechaHora;
    private Timestamp fechaInicioActual;   // por si no se cambia nada

    private final SimpleDateFormat dfFecha =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfHora =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reag_act);

        // Extras
        actividadId = getIntent().getStringExtra("actividadId");
        actividadNombre = getIntent().getStringExtra("actividadNombre");

        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "No se recibió el ID de la actividad.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        actividadRef = db.collection("actividades").document(actividadId);

        // Referencias UI
        txtTitle = findViewById(R.id.txtTitle);
        txtSelectedActivity = findViewById(R.id.txtSelectedActivity);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnTimePicker = findViewById(R.id.btnTimePicker);
        btnCreateActivity = findViewById(R.id.btnCreateActivity);
        btnBack = findViewById(R.id.btnBack);
        etReason = findViewById(R.id.etReason);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Mostrar nombre
        if (actividadNombre != null && !actividadNombre.isEmpty()) {
            if (txtTitle != null) {
                txtTitle.setText("Reagendar actividad\n" + actividadNombre);
            }
            if (txtSelectedActivity != null) {
                txtSelectedActivity.setText(actividadNombre);
            }
        }

        // Pickers
        if (btnDatePicker != null) {
            btnDatePicker.setOnClickListener(v -> mostrarDatePicker());
        }
        if (btnTimePicker != null) {
            btnTimePicker.setOnClickListener(v -> mostrarTimePicker());
        }
        if (btnCreateActivity != null) {
            btnCreateActivity.setOnClickListener(v -> guardarReagendamiento());
        }

        cargarActividadActual();
        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                // Obtenemos el tamaño exacto de las barras del sistema (arriba y abajo)
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Aplicamos ese tamaño como "relleno" (padding) al contenedor principal
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                return insets;
            });
        }
    }

    // ================== CARGA DE ACTIVIDAD ==================

    private void cargarActividadActual() {
        actividadRef.get()
                .addOnSuccessListener(this::onActividadLoaded)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar la actividad.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onActividadLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "La actividad ya no existe.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String nombreReal = doc.getString("nombre");
        if (nombreReal != null && !nombreReal.trim().isEmpty()) {
            actividadNombre = nombreReal;
            if (txtTitle != null) {
                txtTitle.setText("Reagendar actividad\n" + actividadNombre);
            }
            if (txtSelectedActivity != null) {
                txtSelectedActivity.setText(actividadNombre);
            }
        }

        fechaInicioActual = doc.getTimestamp("fechaInicio");
        if (fechaInicioActual != null) {
            java.util.Date d = fechaInicioActual.toDate();
            if (nuevaFechaHora == null) {
                nuevaFechaHora = Calendar.getInstance();
            }
            nuevaFechaHora.setTime(d);

            if (txtDate != null) txtDate.setText(dfFecha.format(d));
            if (txtTime != null) txtTime.setText(dfHora.format(d));
        }
    }

    // ================== DATE / TIME PICKERS ==================

    private void mostrarDatePicker() {
        final Calendar base = (nuevaFechaHora != null)
                ? (Calendar) nuevaFechaHora.clone()
                : Calendar.getInstance();

        int year = base.get(Calendar.YEAR);
        int month = base.get(Calendar.MONTH);
        int day = base.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    if (nuevaFechaHora == null) {
                        nuevaFechaHora = Calendar.getInstance();
                    }
                    nuevaFechaHora.set(Calendar.YEAR, y);
                    nuevaFechaHora.set(Calendar.MONTH, m);
                    nuevaFechaHora.set(Calendar.DAY_OF_MONTH, d);

                    if (txtDate != null) {
                        txtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                d, m + 1, y));
                    }
                },
                year, month, day
        );
        dialog.show();
    }

    private void mostrarTimePicker() {
        final Calendar base = (nuevaFechaHora != null)
                ? (Calendar) nuevaFechaHora.clone()
                : Calendar.getInstance();

        int hour = base.get(Calendar.HOUR_OF_DAY);
        int minute = base.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    if (nuevaFechaHora == null) {
                        nuevaFechaHora = Calendar.getInstance();
                    }
                    nuevaFechaHora.set(Calendar.HOUR_OF_DAY, h);
                    nuevaFechaHora.set(Calendar.MINUTE, m);
                    nuevaFechaHora.set(Calendar.SECOND, 0);

                    if (txtTime != null) {
                        txtTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    }
                },
                hour, minute, true
        );
        dialog.show();
    }

    // ================== GUARDAR REAGENDAMIENTO ==================

    private void guardarReagendamiento() {
        if (nuevaFechaHora == null && fechaInicioActual == null) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp nuevaFecha = (nuevaFechaHora != null)
                ? new Timestamp(nuevaFechaHora.getTime())
                : fechaInicioActual;

        String motivo = (etReason != null)
                ? etReason.getText().toString().trim()
                : "";

        if (btnCreateActivity != null) btnCreateActivity.setEnabled(false);

        actividadRef.update(
                        "fechaInicio", nuevaFecha,
                        "ultimaActualizacion", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> actualizarCita(nuevaFecha, motivo))
                .addOnFailureListener(e -> {
                    if (btnCreateActivity != null) btnCreateActivity.setEnabled(true);
                    Toast.makeText(this, "Error al reagendar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Actualiza la primera cita asociada y programa notificación.
     */
    private void actualizarCita(Timestamp nuevaFecha, String motivo) {

        db.collection("citas")
                .whereEqualTo("actividadId", actividadRef)
                .limit(1)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {

                    if (qs.isEmpty()) {
                        Toast.makeText(this, "Actividad reagendada (sin cita asociada).", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    DocumentSnapshot citaDoc = qs.getDocuments().get(0);

                    citaDoc.getReference().update(
                                    "fecha", nuevaFecha,
                                    "ultimaActualizacion", FieldValue.serverTimestamp(),
                                    "motivoCambio", motivo
                            )
                            .addOnSuccessListener(aVoid -> {

                                // ===========================
                                // 🔔 PROGRAMAR NOTIFICACIÓN
                                // ===========================
                                actividadRef.get().addOnSuccessListener(snapshot -> {

                                    if (!snapshot.exists()) return;

                                    // Leer nombre y días de aviso desde Firestore
                                    String nombre = snapshot.getString("nombre");
                                    Long diasAviso = snapshot.getLong("diasAvisoPrevio");
                                    if (diasAviso == null) diasAviso = 1L;

                                    long fechaCitaMillis = nuevaFecha.toDate().getTime();
                                    long tiempoAviso = fechaCitaMillis - TimeUnit.DAYS.toMillis(diasAviso);

                                    if (tiempoAviso > System.currentTimeMillis()) {
                                        NotifScheduler.programar(
                                                this,
                                                tiempoAviso,
                                                "Actividad reagendada: " + nombre,
                                                "Tu actividad fue reagendada. Nueva fecha próxima."
                                        );
                                    }

                                });
                                // ===========================

                                Toast.makeText(this, "Actividad reagendada correctamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Actividad reagendada, pero la cita no se pudo actualizar.", Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Actividad reagendada, pero no se pudo revisar la cita.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
