package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import prog.android.centroalr.R;
import prog.android.centroalr.notificaciones.NotifScheduler;

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
    private View loadingOverlay;

    // Datos
    private Calendar nuevaFechaHora;
    private Timestamp fechaInicioActual;

    // 1. VARIABLES NECESARIAS PARA VALIDAR
    private long duracionActividadMillis = 3600000; // 1 hora por defecto
    private String currentLugarId; // Necesitamos saber el lugar para chequear conflictos

    private final SimpleDateFormat dfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reag_act);

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
        loadingOverlay = findViewById(R.id.loadingOverlay);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (actividadNombre != null && !actividadNombre.isEmpty()) {
            if (txtTitle != null) txtTitle.setText("Reagendar actividad\n" + actividadNombre);
            if (txtSelectedActivity != null) txtSelectedActivity.setText(actividadNombre);
        }

        if (btnDatePicker != null) btnDatePicker.setOnClickListener(v -> mostrarDatePicker());
        if (btnTimePicker != null) btnTimePicker.setOnClickListener(v -> mostrarTimePicker());

        // Al guardar, ahora iniciamos el flujo de validación
        if (btnCreateActivity != null) btnCreateActivity.setOnClickListener(v -> iniciarProcesoReagendamiento());

        cargarActividadActual();

        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void cargarActividadActual() {
        mostrarCarga(true);
        actividadRef.get()
                .addOnSuccessListener(this::onActividadLoaded)
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al cargar la actividad.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onActividadLoaded(DocumentSnapshot doc) {
        mostrarCarga(false);
        if (!doc.exists()) {
            Toast.makeText(this, "La actividad ya no existe.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ... (Nombre y Textos igual que antes) ...
        String nombreReal = doc.getString("nombre");
        if (nombreReal != null && !nombreReal.trim().isEmpty()) {
            actividadNombre = nombreReal;
            if (txtTitle != null) txtTitle.setText("Reagendar actividad\n" + actividadNombre);
            if (txtSelectedActivity != null) txtSelectedActivity.setText(actividadNombre);
        }

        // 2. CAPTURAR EL ID DEL LUGAR (Vital para la validación)
        DocumentReference lugarRef = doc.getDocumentReference("lugarId");
        if (lugarRef != null) {
            currentLugarId = lugarRef.getId();
        }

        fechaInicioActual = doc.getTimestamp("fechaInicio");
        Timestamp fechaFinOriginal = doc.getTimestamp("fechaFin");

        if (fechaInicioActual != null) {
            java.util.Date d = fechaInicioActual.toDate();
            if (nuevaFechaHora == null) {
                nuevaFechaHora = Calendar.getInstance();
            }
            nuevaFechaHora.setTime(d);

            if (txtDate != null) txtDate.setText(dfFecha.format(d));
            if (txtTime != null) txtTime.setText(dfHora.format(d));

            if (fechaFinOriginal != null) {
                duracionActividadMillis = fechaFinOriginal.toDate().getTime() - fechaInicioActual.toDate().getTime();
            }
        }
    }

    // ... (Date y Time Pickers igual que antes) ...
    private void mostrarDatePicker() {
        final Calendar base = (nuevaFechaHora != null) ? (Calendar) nuevaFechaHora.clone() : Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            if (nuevaFechaHora == null) nuevaFechaHora = Calendar.getInstance();
            nuevaFechaHora.set(Calendar.YEAR, y);
            nuevaFechaHora.set(Calendar.MONTH, m);
            nuevaFechaHora.set(Calendar.DAY_OF_MONTH, d);
            if (txtDate != null) txtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y));
        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker() {
        final Calendar base = (nuevaFechaHora != null) ? (Calendar) nuevaFechaHora.clone() : Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            if (nuevaFechaHora == null) nuevaFechaHora = Calendar.getInstance();
            nuevaFechaHora.set(Calendar.HOUR_OF_DAY, h);
            nuevaFechaHora.set(Calendar.MINUTE, m);
            nuevaFechaHora.set(Calendar.SECOND, 0);
            if (txtTime != null) txtTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, base.get(Calendar.HOUR_OF_DAY), base.get(Calendar.MINUTE), true).show();
    }

    // ================== NUEVA LÓGICA DE VALIDACIÓN ==================

    private void iniciarProcesoReagendamiento() {
        if (nuevaFechaHora == null && fechaInicioActual == null) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentLugarId == null) {
            Toast.makeText(this, "Error: No se identificó el lugar de la actividad.", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp nuevaFechaInicio = (nuevaFechaHora != null) ? new Timestamp(nuevaFechaHora.getTime()) : fechaInicioActual;

        if (duracionActividadMillis <= 0) duracionActividadMillis = 3600000;
        long finMillis = nuevaFechaInicio.toDate().getTime() + duracionActividadMillis;
        Timestamp nuevaFechaFin = new Timestamp(new java.util.Date(finMillis));

        String motivo = (etReason != null) ? etReason.getText().toString().trim() : "";

        mostrarCarga(true);

        // 3. LLAMAR A VALIDAR DISPONIBILIDAD
        validarDisponibilidad(currentLugarId, nuevaFechaInicio, nuevaFechaFin, () -> {
            // Si pasa la validación, guardamos
            procederAGuardar(nuevaFechaInicio, nuevaFechaFin, motivo);
        });
    }

    private interface OnDisponibilidadListener {
        void onDisponible();
    }

    private void validarDisponibilidad(String lugarId, Timestamp inicioNuevo, Timestamp finNuevo, OnDisponibilidadListener listener) {
        DocumentReference lugarRef = db.collection("lugares").document(lugarId);

        // Filtros de fecha para el día (optimización)
        Calendar cal = Calendar.getInstance();
        cal.setTime(inicioNuevo.toDate());
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        Timestamp inicioDia = new Timestamp(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
        Timestamp finDia = new Timestamp(cal.getTime());

        db.collection("actividades")
                .whereEqualTo("lugarId", lugarRef)
                .whereEqualTo("estado", "activa")
                .whereGreaterThanOrEqualTo("fechaInicio", inicioDia)
                .whereLessThanOrEqualTo("fechaInicio", finDia)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hayConflicto = false;
                    long iniN = inicioNuevo.toDate().getTime();
                    long finN = finNuevo.toDate().getTime();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // IMPORTANTE: Ignorar la actividad que estamos editando (yo mismo)
                        if (doc.getId().equals(actividadId)) continue;

                        Timestamp iniExistente = doc.getTimestamp("fechaInicio");
                        Timestamp finExistente = doc.getTimestamp("fechaFin");

                        if (iniExistente != null && finExistente != null) {
                            long iniE = iniExistente.toDate().getTime();
                            long finE = finExistente.toDate().getTime();

                            // Lógica de superposición
                            if (iniN < finE && finN > iniE) {
                                hayConflicto = true;
                                break;
                            }
                        }
                    }

                    if (hayConflicto) {
                        mostrarCarga(false);
                        Toast.makeText(ReagActActivity.this, "¡Conflicto! Ya existe otra actividad en ese horario y lugar.", Toast.LENGTH_LONG).show();
                    } else {
                        listener.onDisponible();
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al verificar disponibilidad.", Toast.LENGTH_SHORT).show();
                });
    }

    // ================== GUARDADO FINAL (Post-Validación) ==================

    private void procederAGuardar(Timestamp nuevaFechaInicio, Timestamp nuevaFechaFin, String motivo) {
        actividadRef.update(
                        "fechaInicio", nuevaFechaInicio,
                        "fechaFin", nuevaFechaFin,
                        "ultimaActualizacion", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> actualizarCita(nuevaFechaInicio, nuevaFechaFin, motivo))
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al reagendar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarCita(Timestamp nuevaFechaInicio, Timestamp nuevaFechaFin, String motivo) {
        db.collection("citas")
                .whereEqualTo("actividadId", actividadRef)
                .limit(1)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    if (qs.isEmpty()) {
                        mostrarCarga(false);
                        Toast.makeText(this, "Actividad reagendada (sin cita asociada).", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    DocumentSnapshot citaDoc = qs.getDocuments().get(0);

                    citaDoc.getReference().update(
                                    "fecha", nuevaFechaInicio,
                                    "fechaInicio", nuevaFechaInicio,
                                    "fechaFin", nuevaFechaFin,
                                    "ultimaActualizacion", FieldValue.serverTimestamp(),
                                    "motivoCambio", motivo
                            )
                            .addOnSuccessListener(aVoid -> {
                                // Notificación
                                actividadRef.get().addOnSuccessListener(snapshot -> {
                                    if (!snapshot.exists()) return;
                                    String nombre = snapshot.getString("nombre");
                                    Long diasAviso = snapshot.getLong("diasAvisoPrevio");
                                    if (diasAviso == null) diasAviso = 1L;

                                    long fechaCitaMillis = nuevaFechaInicio.toDate().getTime();
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

                                mostrarCarga(false);
                                Toast.makeText(this, "Actividad reagendada correctamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                mostrarCarga(false);
                                Toast.makeText(this, "Error al actualizar cita.", Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al buscar cita.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void mostrarCarga(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        if (btnCreateActivity != null) {
            btnCreateActivity.setEnabled(!mostrar);
        }
    }
}