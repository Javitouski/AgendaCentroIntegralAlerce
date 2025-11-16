package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.R;

public class ModificarActActivity extends AppCompatActivity {

    // Firestore
    private FirebaseFirestore db;
    private String actividadId;
    private String actividadNombre;

    // UI
    private EditText etNombre;
    private EditText etDescripcion;
    private EditText etBeneficiarios;
    private EditText etCupo;
    private EditText etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo;
    private AutoCompleteTextView autoCompleteLugar;
    private Button btnFecha;
    private Button btnHora;
    private View btnGuardar; // puede ser LinearLayout o MaterialButton, según tu XML
    private View btnBack;

    // Fecha/hora seleccionadas
    private Calendar fechaSeleccionada;
    private Timestamp fechaInicioActual; // por si está en Firestore

    // IDs actuales de referencias (para tipo y lugar)
    private String currentTipoId;
    private String currentLugarId;

    // Mapeos simples entre texto visible y IDs de Firestore
    private final String[] TIPOS_LABEL = {
            "Taller grupal"
    };
    private final String[] TIPOS_ID = {
            "taller"
    };

    private final String[] LUGARES_LABEL = {
            "Oficina principal del centro comunitario",
            "Sala multiuso 1",
            "Sala multiuso 2"
    };
    private final String[] LUGARES_ID = {
            "oficina",
            "salaMultiuso1",
            "salaMultiuso2"
    };

    private final SimpleDateFormat dfFechaBtn =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfHoraBtn =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_act);

        db = FirebaseFirestore.getInstance();

        // Extras que vienen desde DetActActivity
        actividadId = getIntent().getStringExtra("actividadId");
        actividadNombre = getIntent().getStringExtra("actividadNombre");

        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "No se recibió el ID de la actividad.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initDropdowns();
        initListeners();

        cargarActividad(); // trae la info actual desde Firestore
    }

    // ================== INICIALIZAR VISTAS ==================

    private void initViews() {
        etNombre        = findEditText(R.id.etNombre, R.id.etName);
        etDescripcion   = findEditText(R.id.etDescripcion, 0);
        etBeneficiarios = findEditText(R.id.etBeneficiarios, 0);
        etCupo          = findEditText(R.id.etCupo, 0);
        etDiasAviso     = findEditText(R.id.etDiasAviso, 0);

        autoCompleteTipo  = findViewByIdSafe(AutoCompleteTextView.class, R.id.autoCompleteTipo);
        autoCompleteLugar = findViewByIdSafe(AutoCompleteTextView.class, R.id.autoCompleteLugar);

        btnFecha   = findViewByIdSafe(Button.class, R.id.btnFecha);
        btnHora    = findViewByIdSafe(Button.class, R.id.btnHora);

        // Botón inferior "Editar / Guardar cambios"
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        if (btnGuardar == null) {

            btnGuardar = findViewById(R.id.btnGuardarCambios);
        }

        // Flecha back
        btnBack = findViewById(R.id.btnBack);
    }

    // Helpers para evitar NullPointer si usas otros ids
    private EditText findEditText(int... ids) {
        for (int id : ids) {
            if (id == 0) continue;
            View v = findViewById(id);
            if (v instanceof EditText) return (EditText) v;
        }
        return null;
    }

    private <T> T findViewByIdSafe(Class<T> clazz, int id) {
        if (id == 0) return null;
        View v = findViewById(id);
        if (clazz.isInstance(v)) {
            return clazz.cast(v);
        }
        return null;
    }

    // ================== DROPDOWNS (tipo / lugar) ==================

    private void initDropdowns() {
        // Si quieres que realmente sean dropdown, aquí irían ArrayAdapter
        // Por ahora dejamos solo el texto manual, puedes mejorar esto luego.
        if (autoCompleteTipo != null && autoCompleteTipo.getText().toString().isEmpty()) {
            autoCompleteTipo.setText("Taller grupal", false);
        }
        if (autoCompleteLugar != null && autoCompleteLugar.getText().toString().isEmpty()) {
            autoCompleteLugar.setText("Oficina principal del centro comunitario", false);
        }
    }

    // ================== LISTENERS (fecha, hora, guardar, back) ==================

    private void initListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnFecha != null) {
            btnFecha.setOnClickListener(v -> mostrarDatePicker());
        }

        if (btnHora != null) {
            btnHora.setOnClickListener(v -> mostrarTimePicker());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> validarYGuardar());
        }
    }

    private void mostrarDatePicker() {
        final Calendar base = (fechaSeleccionada != null)
                ? (Calendar) fechaSeleccionada.clone()
                : Calendar.getInstance();

        int year  = base.get(Calendar.YEAR);
        int month = base.get(Calendar.MONTH);
        int day   = base.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    if (fechaSeleccionada == null) {
                        fechaSeleccionada = Calendar.getInstance();
                    }
                    fechaSeleccionada.set(Calendar.YEAR, y);
                    fechaSeleccionada.set(Calendar.MONTH, m);
                    fechaSeleccionada.set(Calendar.DAY_OF_MONTH, d);

                    if (btnFecha != null) {
                        btnFecha.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                d, m + 1, y));
                    }
                },
                year, month, day
        );
        dialog.show();
    }

    private void mostrarTimePicker() {
        final Calendar base = (fechaSeleccionada != null)
                ? (Calendar) fechaSeleccionada.clone()
                : Calendar.getInstance();

        int hour   = base.get(Calendar.HOUR_OF_DAY);
        int minute = base.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    if (fechaSeleccionada == null) {
                        fechaSeleccionada = Calendar.getInstance();
                    }
                    fechaSeleccionada.set(Calendar.HOUR_OF_DAY, h);
                    fechaSeleccionada.set(Calendar.MINUTE, m);
                    fechaSeleccionada.set(Calendar.SECOND, 0);

                    if (btnHora != null) {
                        btnHora.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    }
                },
                hour, minute, true
        );
        dialog.show();
    }

    // ================== CARGAR ACTIVIDAD DESDE FIRESTORE ==================

    private void cargarActividad() {
        db.collection("actividades")
                .document(actividadId)
                .get()
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

        // Nombre
        String nombre = doc.getString("nombre");
        if (etNombre != null && nombre != null) {
            etNombre.setText(nombre);
        }
        actividadNombre = nombre; // actualizamos

        // Descripción
        String descripcion = doc.getString("descripcion");
        if (etDescripcion != null && descripcion != null) {
            etDescripcion.setText(descripcion);
        }

        // Beneficiarios
        String benef = doc.getString("beneficiariosDescripcion");
        if (etBeneficiarios != null && benef != null) {
            etBeneficiarios.setText(benef);
        }

        // Cupo
        Long cupo = doc.getLong("cupo");
        if (etCupo != null && cupo != null) {
            etCupo.setText(String.valueOf(cupo));
        }

        // Días de aviso previo
        Long diasAviso = doc.getLong("diasAvisoPrevio");
        if (etDiasAviso != null && diasAviso != null) {
            etDiasAviso.setText(String.valueOf(diasAviso));
        }

        // Fecha/hora
        fechaInicioActual = doc.getTimestamp("fechaInicio");
        if (fechaInicioActual != null) {
            java.util.Date d = fechaInicioActual.toDate();
            if (fechaSeleccionada == null) {
                fechaSeleccionada = Calendar.getInstance();
            }
            fechaSeleccionada.setTime(d);

            if (btnFecha != null) {
                btnFecha.setText(dfFechaBtn.format(d));
            }
            if (btnHora != null) {
                btnHora.setText(dfHoraBtn.format(d));
            }
        }

        // Tipo
        DocumentReference tipoRef = doc.getDocumentReference("tipoActividadId");
        currentTipoId = (tipoRef != null) ? tipoRef.getId() : null;
        if (autoCompleteTipo != null && currentTipoId != null) {
            autoCompleteTipo.setText(labelDesdeTipoId(currentTipoId), false);
        }

        // Lugar
        DocumentReference lugarRef = doc.getDocumentReference("lugarId");
        currentLugarId = (lugarRef != null) ? lugarRef.getId() : null;
        if (autoCompleteLugar != null && currentLugarId != null) {
            autoCompleteLugar.setText(labelDesdeLugarId(currentLugarId), false);
        }
    }

    // ================== GUARDAR CAMBIOS ==================

    private void validarYGuardar() {
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);
        String beneficiarios = texto(etBeneficiarios);
        String cupoStr = texto(etCupo);
        String diasAvisoStr = texto(etDiasAviso);

        if (nombre.isEmpty()) {
            if (etNombre != null) {
                etNombre.setError("Ingresa un nombre");
                etNombre.requestFocus();
            }
            return;
        }

        if (descripcion.isEmpty()) {
            if (etDescripcion != null) {
                etDescripcion.setError("Ingresa una descripción");
                etDescripcion.requestFocus();
            }
            return;
        }

        long cupo;
        long diasAviso;
        try {
            cupo = Long.parseLong(cupoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cupo debe ser un número", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            diasAviso = Long.parseLong(diasAvisoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Días de aviso debe ser un número", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fechaSeleccionada == null && fechaInicioActual == null) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fecha/hora final a guardar
        Timestamp nuevaFechaInicio;
        if (fechaSeleccionada != null) {
            nuevaFechaInicio = new Timestamp(fechaSeleccionada.getTime());
        } else {
            // Si el usuario no tocó la fecha/hora, mantenemos la original
            nuevaFechaInicio = fechaInicioActual;
        }

        // Tipo y lugar: si no se reconoce el texto, se usa el ID actual
        String tipoTexto = (autoCompleteTipo != null)
                ? autoCompleteTipo.getText().toString()
                : null;
        String lugarTexto = (autoCompleteLugar != null)
                ? autoCompleteLugar.getText().toString()
                : null;

        String nuevoTipoId = tipoIdDesdeLabel(tipoTexto);
        if (nuevoTipoId == null) nuevoTipoId = currentTipoId;

        String nuevoLugarId = lugarIdDesdeLabel(lugarTexto);
        if (nuevoLugarId == null) nuevoLugarId = currentLugarId;

        DocumentReference tipoRef = (nuevoTipoId != null)
                ? db.collection("tiposActividades").document(nuevoTipoId)
                : null;

        DocumentReference lugarRef = (nuevoLugarId != null)
                ? db.collection("lugares").document(nuevoLugarId)
                : null;

        // Construimos el mapa de cambios
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("descripcion", descripcion);
        updates.put("beneficiariosDescripcion", beneficiarios);
        updates.put("cupo", cupo);
        updates.put("diasAvisoPrevio", diasAviso);
        updates.put("fechaInicio", nuevaFechaInicio);
        updates.put("fechaFin", nuevaFechaInicio);
        if (tipoRef != null)  updates.put("tipoActividadId", tipoRef);
        if (lugarRef != null) updates.put("lugarId", lugarRef);
        updates.put("ultimaActualizacion", FieldValue.serverTimestamp());

        if (btnGuardar != null) btnGuardar.setEnabled(false);

        db.collection("actividades")
                .document(actividadId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (btnGuardar != null) btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ================== HELPERS DE TEXTO Y MAPEOS ==================

    private String texto(EditText et) {
        if (et == null) return "";
        return et.getText().toString().trim();
    }

    private String labelDesdeTipoId(String id) {
        if (id == null) return "";
        for (int i = 0; i < TIPOS_ID.length; i++) {
            if (id.equals(TIPOS_ID[i])) return TIPOS_LABEL[i];
        }
        return "Tipo: " + id;
    }

    private String labelDesdeLugarId(String id) {
        if (id == null) return "";
        for (int i = 0; i < LUGARES_ID.length; i++) {
            if (id.equals(LUGARES_ID[i])) return LUGARES_LABEL[i];
        }
        // fallback
        String s = id.replace("_", " ").replace("-", " ");
        if (s.isEmpty()) return "Lugar no especificado";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String tipoIdDesdeLabel(String label) {
        if (label == null) return null;
        for (int i = 0; i < TIPOS_LABEL.length; i++) {
            if (label.equalsIgnoreCase(TIPOS_LABEL[i])) {
                return TIPOS_ID[i];
            }
        }
        return null; // fuerza uso de currentTipoId
    }

    private String lugarIdDesdeLabel(String label) {
        if (label == null) return null;
        for (int i = 0; i < LUGARES_LABEL.length; i++) {
            if (label.equalsIgnoreCase(LUGARES_LABEL[i])) {
                return LUGARES_ID[i];
            }
        }
        return null; // fuerza uso de currentLugarId
    }
}
