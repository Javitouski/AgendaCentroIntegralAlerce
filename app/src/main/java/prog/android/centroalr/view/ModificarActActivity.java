package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.R;

public class ModificarActActivity extends AppCompatActivity {

    // Firestore
    private FirebaseFirestore db;
    private String actividadId;

    // UI
    private EditText etNombre, etDescripcion, etBeneficiarios, etCupo, etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo, autoCompleteLugar;
    private Button btnFecha, btnHora;
    private View btnGuardar, btnBack;
    private View loadingOverlay;

    // Datos
    private Calendar fechaInicioSeleccionada;
    private long duracionActividadMillis = 0;

    // Listas para Dropdowns
    private List<String> tiposNombres = new ArrayList<>();
    private List<String> tiposIds = new ArrayList<>();
    private String tipoSeleccionadoId;

    private List<String> lugaresNombres = new ArrayList<>();
    private List<String> lugaresIds = new ArrayList<>();
    private String lugarSeleccionadoId;

    private final SimpleDateFormat dfFechaBtn = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfHoraBtn = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_act);

        db = FirebaseFirestore.getInstance();
        actividadId = getIntent().getStringExtra("actividadId");

        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initListeners();

        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mostrarCarga(true);
        cargarTipos(() -> cargarLugares(() -> cargarActividad()));
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etBeneficiarios = findViewById(R.id.etBeneficiarios);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        autoCompleteTipo = findViewById(R.id.autoCompleteTipo);
        autoCompleteLugar = findViewById(R.id.autoCompleteLugar);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void initListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnFecha != null) btnFecha.setOnClickListener(v -> mostrarDatePicker());
        if (btnHora != null) btnHora.setOnClickListener(v -> mostrarTimePicker());
        if (btnGuardar != null) btnGuardar.setOnClickListener(v -> validarYGuardar());

        autoCompleteTipo.setOnItemClickListener((parent, view, position, id) ->
                tipoSeleccionadoId = tiposIds.get(position));

        autoCompleteLugar.setOnItemClickListener((parent, view, position, id) ->
                lugarSeleccionadoId = lugaresIds.get(position));
    }

    private void cargarTipos(Runnable onComplete) {
        db.collection("tiposActividades").get().addOnSuccessListener(qs -> {
            tiposNombres.clear(); tiposIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("nombre");
                if (n != null) { tiposNombres.add(n); tiposIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposNombres);
            autoCompleteTipo.setAdapter(adapter);
            onComplete.run();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar tipos", Toast.LENGTH_SHORT).show();
            onComplete.run();
        });
    }

    private void cargarLugares(Runnable onComplete) {
        db.collection("lugares").get().addOnSuccessListener(qs -> {
            lugaresNombres.clear(); lugaresIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("descripcion");
                if (n == null) n = doc.getString("nombre");
                if (n != null) { lugaresNombres.add(n); lugaresIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lugaresNombres);
            autoCompleteLugar.setAdapter(adapter);
            onComplete.run();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
            onComplete.run();
        });
    }

    private void cargarActividad() {
        db.collection("actividades").document(actividadId).get()
                .addOnSuccessListener(doc -> {
                    mostrarCarga(false);
                    if (!doc.exists()) {
                        Toast.makeText(this, "La actividad no existe", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    llenarCampos(doc);
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al cargar actividad", Toast.LENGTH_SHORT).show();
                });
    }

    private void llenarCampos(DocumentSnapshot doc) {
        etNombre.setText(doc.getString("nombre"));
        etDescripcion.setText(doc.getString("descripcion"));
        etBeneficiarios.setText(doc.getString("beneficiariosDescripcion"));

        Long cupo = doc.getLong("cupo");
        if (cupo != null) etCupo.setText(String.valueOf(cupo));

        Long dias = doc.getLong("diasAvisoPrevio");
        if (dias != null) etDiasAviso.setText(String.valueOf(dias));

        Timestamp inicio = doc.getTimestamp("fechaInicio");
        Timestamp fin = doc.getTimestamp("fechaFin");

        if (inicio != null) {
            fechaInicioSeleccionada = Calendar.getInstance();
            fechaInicioSeleccionada.setTime(inicio.toDate());
            btnFecha.setText(dfFechaBtn.format(inicio.toDate()));
            btnHora.setText(dfHoraBtn.format(inicio.toDate()));

            if (fin != null) {
                duracionActividadMillis = fin.toDate().getTime() - inicio.toDate().getTime();
            }
        }

        DocumentReference tipoRef = doc.getDocumentReference("tipoActividadId");
        if (tipoRef != null) {
            tipoSeleccionadoId = tipoRef.getId();
            int index = tiposIds.indexOf(tipoSeleccionadoId);
            if (index >= 0) autoCompleteTipo.setText(tiposNombres.get(index), false);
        }

        DocumentReference lugarRef = doc.getDocumentReference("lugarId");
        if (lugarRef != null) {
            lugarSeleccionadoId = lugarRef.getId();
            int index = lugaresIds.indexOf(lugarSeleccionadoId);
            if (index >= 0) {
                autoCompleteLugar.setText(lugaresNombres.get(index), false);
            } else {
                autoCompleteLugar.setText("Lugar no encontrado", false);
            }
        }
    }

    private void mostrarDatePicker() {
        if (fechaInicioSeleccionada == null) fechaInicioSeleccionada = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            fechaInicioSeleccionada.set(Calendar.YEAR, y);
            fechaInicioSeleccionada.set(Calendar.MONTH, m);
            fechaInicioSeleccionada.set(Calendar.DAY_OF_MONTH, d);
            btnFecha.setText(dfFechaBtn.format(fechaInicioSeleccionada.getTime()));
        }, fechaInicioSeleccionada.get(Calendar.YEAR), fechaInicioSeleccionada.get(Calendar.MONTH),
                fechaInicioSeleccionada.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker() {
        if (fechaInicioSeleccionada == null) fechaInicioSeleccionada = Calendar.getInstance();
        new TimePickerDialog(this, (v, h, m) -> {
            fechaInicioSeleccionada.set(Calendar.HOUR_OF_DAY, h);
            fechaInicioSeleccionada.set(Calendar.MINUTE, m);
            btnHora.setText(dfHoraBtn.format(fechaInicioSeleccionada.getTime()));
        }, fechaInicioSeleccionada.get(Calendar.HOUR_OF_DAY),
                fechaInicioSeleccionada.get(Calendar.MINUTE), true).show();
    }

    // ================== VALIDACIÓN Y GUARDADO ==================

    private void validarYGuardar() {
        if (fechaInicioSeleccionada == null) {
            Toast.makeText(this, "Fecha requerida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lugarSeleccionadoId == null) {
            Toast.makeText(this, "Lugar requerido", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCarga(true);

        // 1. Calcular fechas
        if (duracionActividadMillis <= 0) duracionActividadMillis = 3600000;
        Timestamp tsInicio = new Timestamp(fechaInicioSeleccionada.getTime());
        Timestamp tsFin = new Timestamp(new java.util.Date(fechaInicioSeleccionada.getTimeInMillis() + duracionActividadMillis));

        // 2. Validar Disponibilidad ANTES de guardar
        validarDisponibilidad(lugarSeleccionadoId, tsInicio, tsFin, () -> {
            // Si no hay conflicto, procedemos a actualizar
            procederAActualizar(tsInicio, tsFin);
        });
    }

    // --- VALIDACIÓN ---
    private interface OnDisponibilidadListener {
        void onDisponible();
    }

    private void validarDisponibilidad(String lugarId, Timestamp inicioNuevo, Timestamp finNuevo, OnDisponibilidadListener listener) {
        DocumentReference lugarRef = db.collection("lugares").document(lugarId);

        // Filtro por día para optimizar
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
                        // ¡IMPORTANTE! Ignorar la misma actividad que estamos editando
                        if (doc.getId().equals(actividadId)) continue;

                        Timestamp iniExistente = doc.getTimestamp("fechaInicio");
                        Timestamp finExistente = doc.getTimestamp("fechaFin");

                        if (iniExistente != null && finExistente != null) {
                            long iniE = iniExistente.toDate().getTime();
                            long finE = finExistente.toDate().getTime();

                            if (iniN < finE && finN > iniE) {
                                hayConflicto = true;
                                break;
                            }
                        }
                    }

                    if (hayConflicto) {
                        mostrarCarga(false);
                        Toast.makeText(ModificarActActivity.this, "¡Conflicto! Ya existe otra actividad en ese lugar y horario.", Toast.LENGTH_LONG).show();
                    } else {
                        listener.onDisponible();
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al verificar disponibilidad", Toast.LENGTH_SHORT).show();
                });
    }

    // --- ACTUALIZAR ---
    private void procederAActualizar(Timestamp tsInicio, Timestamp tsFin) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", etNombre.getText().toString().trim());
        updates.put("descripcion", etDescripcion.getText().toString().trim());
        updates.put("beneficiariosDescripcion", etBeneficiarios.getText().toString().trim());

        String cupo = etCupo.getText().toString().trim();
        updates.put("cupo", cupo.isEmpty() ? 0 : Long.parseLong(cupo));

        String dias = etDiasAviso.getText().toString().trim();
        updates.put("diasAvisoPrevio", dias.isEmpty() ? 0 : Long.parseLong(dias));

        updates.put("fechaInicio", tsInicio);
        updates.put("fechaFin", tsFin);

        if (tipoSeleccionadoId != null)
            updates.put("tipoActividadId", db.collection("tiposActividades").document(tipoSeleccionadoId));
        if (lugarSeleccionadoId != null)
            updates.put("lugarId", db.collection("lugares").document(lugarSeleccionadoId));

        updates.put("ultimaActualizacion", FieldValue.serverTimestamp());

        db.collection("actividades").document(actividadId).update(updates)
                .addOnSuccessListener(v -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
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
        if (btnGuardar != null) btnGuardar.setEnabled(!mostrar);
    }
}