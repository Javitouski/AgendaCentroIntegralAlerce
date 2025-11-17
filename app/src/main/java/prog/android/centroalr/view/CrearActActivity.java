package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class CrearActActivity extends AppCompatActivity {

    // Vistas
    private EditText etNombre, etDescripcion, etBeneficiarios, etCupo, etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo, autoCompleteLugar;

    // Selector de Frecuencia (Asegúrate de tenerlo en el XML)
    private AutoCompleteTextView autoCompleteFrecuencia;

    private Button btnFecha, btnHora, btnCrear;
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual, rbPeriodica;
    private LinearLayout grupoFechaFin;
    private Button btnFechaFin;

    // Vistas Opcionales
    private AutoCompleteTextView autoCompleteProyecto, autoCompleteSocio, autoCompleteOferente;

    // Firebase y Usuario
    private FirebaseFirestore db;
    private Usuario usuarioActual;

    // Calendarios
    private Calendar fechaInicioSeleccionada;
    private Calendar fechaFinSeleccionada;

    // Listas para Dropdowns
    private List<String> tiposNombres = new ArrayList<>();
    private List<String> tiposIds = new ArrayList<>();
    private String tipoSeleccionadoId;

    private List<String> lugaresNombres = new ArrayList<>();
    private List<String> lugaresIds = new ArrayList<>();
    private String lugarSeleccionadoId;
    private Map<String, Integer> capacidadPorLugar = new HashMap<>();

    private List<String> proyectosNombres = new ArrayList<>();
    private List<String> proyectosIds = new ArrayList<>();
    private String proyectoSeleccionadoId;

    private List<String> sociosNombres = new ArrayList<>();
    private List<String> sociosIds = new ArrayList<>();
    private String socioSeleccionadoId;

    private List<String> oferentesNombres = new ArrayList<>();
    private List<String> oferentesIds = new ArrayList<>();
    private String oferenteSeleccionadoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_act);

        // Seguridad
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();
        if (usuarioActual == null) {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Referencias UI
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etBeneficiarios = findViewById(R.id.etBeneficiarios);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);

        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnCrear = findViewById(R.id.btnCrearActividad);

        autoCompleteTipo = findViewById(R.id.autoCompleteTipo);
        autoCompleteLugar = findViewById(R.id.autoCompleteLugar);

        // Periodicidad
        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);
        grupoFechaFin = findViewById(R.id.grupoFechaFin);
        btnFechaFin = findViewById(R.id.btnFechaFin);

        // Referencia al dropdown de frecuencia
        autoCompleteFrecuencia = findViewById(R.id.autoCompleteFrecuencia);

        autoCompleteProyecto = findViewById(R.id.autoCompleteProyecto);
        autoCompleteSocio = findViewById(R.id.autoCompleteSocio);
        autoCompleteOferente = findViewById(R.id.autoCompleteOferente);

        // Cargar Dropdowns
        cargarTiposActividad();
        cargarLugares();
        cargarProyectos();
        cargarSocios();
        cargarOferentes();

        // Configurar opciones de frecuencia
        setupFrecuenciaDropdown();

        // Listeners
        btnFecha.setOnClickListener(v -> mostrarDatePickerInicio());
        btnHora.setOnClickListener(v -> mostrarTimePickerInicio());
        btnCrear.setOnClickListener(v -> validarYGuardar());
        btnFechaFin.setOnClickListener(v -> mostrarDatePickerFin());

        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPeriodica) {
                grupoFechaFin.setVisibility(View.VISIBLE);
            } else {
                grupoFechaFin.setVisibility(View.GONE);
                fechaFinSeleccionada = null;
                btnFechaFin.setText("Seleccionar fecha de fin");
            }
        });
    }

    private void setupFrecuenciaDropdown() {
        if (autoCompleteFrecuencia != null) {
            String[] frecuencias = new String[]{"Diaria", "Semanal", "Mensual"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frecuencias);
            autoCompleteFrecuencia.setAdapter(adapter);
            autoCompleteFrecuencia.setText("Semanal", false);
        }
    }

    private void mostrarDatePickerInicio() {
        final Calendar hoy = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            if (fechaInicioSeleccionada == null) fechaInicioSeleccionada = Calendar.getInstance();
            fechaInicioSeleccionada.set(Calendar.YEAR, y);
            fechaInicioSeleccionada.set(Calendar.MONTH, m);
            fechaInicioSeleccionada.set(Calendar.DAY_OF_MONTH, d);
            btnFecha.setText(d + "/" + (m + 1) + "/" + y);
        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void mostrarTimePickerInicio() {
        final Calendar ahora = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            if (fechaInicioSeleccionada == null) fechaInicioSeleccionada = Calendar.getInstance();
            fechaInicioSeleccionada.set(Calendar.HOUR_OF_DAY, h);
            fechaInicioSeleccionada.set(Calendar.MINUTE, m);
            fechaInicioSeleccionada.set(Calendar.SECOND, 0);
            btnHora.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void mostrarDatePickerFin() {
        final Calendar hoy = (fechaInicioSeleccionada != null) ? fechaInicioSeleccionada : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            fechaFinSeleccionada = Calendar.getInstance();
            fechaFinSeleccionada.set(y, m, d, 23, 59, 59);
            btnFechaFin.setText(d + "/" + (m + 1) + "/" + y);
        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));
        if (fechaInicioSeleccionada != null) dialog.getDatePicker().setMinDate(fechaInicioSeleccionada.getTimeInMillis());
        dialog.show();
    }

    private void validarYGuardar() {
        String nombre = texto(etNombre);
        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Requerido"); return; }

        String cupoStr = texto(etCupo);
        if (TextUtils.isEmpty(cupoStr)) { etCupo.setError("Requerido"); return; }
        long cupo = Long.parseLong(cupoStr);

        String diasStr = texto(etDiasAviso);
        if (TextUtils.isEmpty(diasStr)) { etDiasAviso.setError("Requerido"); return; }
        long diasAviso = Long.parseLong(diasStr);

        if (fechaInicioSeleccionada == null) {
            Toast.makeText(this, "Selecciona fecha de inicio", Toast.LENGTH_SHORT).show(); return;
        }
        if (tipoSeleccionadoId == null || lugarSeleccionadoId == null) {
            Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show(); return;
        }

        // Validación Aforo
        int capacidadMaxima = 0;
        if (capacidadPorLugar.containsKey(lugarSeleccionadoId)) capacidadMaxima = capacidadPorLugar.get(lugarSeleccionadoId);
        if (capacidadMaxima > 0 && cupo > capacidadMaxima) {
            etCupo.setError("Excede capacidad (" + capacidadMaxima + ")"); return;
        }

        // Lógica Periodicidad
        String periodicidadStr;
        String frecuenciaSeleccionada = "NINGUNA";
        Timestamp fechaInicioTS = new Timestamp(fechaInicioSeleccionada.getTime());
        Timestamp fechaFinTS;

        if (rbPeriodica.isChecked()) {
            periodicidadStr = "periodica";

            if (autoCompleteFrecuencia != null) {
                frecuenciaSeleccionada = autoCompleteFrecuencia.getText().toString().toUpperCase();
            }
            if (frecuenciaSeleccionada.isEmpty()) frecuenciaSeleccionada = "SEMANAL";

            if (fechaFinSeleccionada == null) {
                Toast.makeText(this, "Selecciona fecha de fin", Toast.LENGTH_SHORT).show(); return;
            }
            if (fechaFinSeleccionada.before(fechaInicioSeleccionada)) {
                Toast.makeText(this, "Fecha fin inválida", Toast.LENGTH_SHORT).show(); return;
            }
            fechaFinTS = new Timestamp(fechaFinSeleccionada.getTime());
        } else {
            periodicidadStr = "puntual";
            fechaFinTS = fechaInicioTS;
        }

        // --- PREPARAR DATOS ---
        btnCrear.setEnabled(false);
        String descripcion = texto(etDescripcion);
        String beneficiarios = texto(etBeneficiarios);

        DocumentReference tipoRef = db.collection("tiposActividades").document(tipoSeleccionadoId);
        DocumentReference lugarRef = db.collection("lugares").document(lugarSeleccionadoId);
        DocumentReference usuarioRef = db.collection("usuarios").document(usuarioActual.getUid());
        DocumentReference proyectoRef = (proyectoSeleccionadoId != null) ? db.collection("proyecto").document(proyectoSeleccionadoId) : null;
        DocumentReference socioRef = (socioSeleccionadoId != null) ? db.collection("socioComunitario").document(socioSeleccionadoId) : null;
        DocumentReference oferenteRef = (oferenteSeleccionadoId != null) ? db.collection("oferentes").document(oferenteSeleccionadoId) : null;

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("descripcion", descripcion);
        actividad.put("beneficiariosDescripcion", beneficiarios);
        actividad.put("cupo", cupo);
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("estado", "activa");
        actividad.put("fechaCreacion", FieldValue.serverTimestamp());
        actividad.put("fechaInicio", fechaInicioTS);
        actividad.put("fechaFin", fechaFinTS);
        actividad.put("periodicidad", periodicidadStr);
        actividad.put("frecuencia", frecuenciaSeleccionada);
        actividad.put("tieneArchivos", false);
        actividad.put("tipoActividadId", tipoRef);
        actividad.put("lugarId", lugarRef);
        actividad.put("creadaPorUsuarioId", usuarioRef);
        actividad.put("proyectoId", proyectoRef);
        actividad.put("socioComunitarioId", socioRef);
        actividad.put("oferenteId", oferenteRef);

        String finalFrecuencia = frecuenciaSeleccionada;

        db.collection("actividades").add(actividad)
                .addOnSuccessListener(docRef -> {
                    if (periodicidadStr.equals("puntual")) {
                        crearCitaUnica(docRef, usuarioRef, lugarRef, fechaInicioTS);
                    } else {
                        crearCitasPeriodicas(docRef, usuarioRef, lugarRef, fechaInicioSeleccionada, fechaFinSeleccionada, finalFrecuencia);
                    }
                })
                .addOnFailureListener(e -> {
                    btnCrear.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // === CORRECCIÓN CLAVE: GUARDAR DATOS COMPLETOS EN LA CITA ===

    private void crearCitaUnica(DocumentReference actRef, DocumentReference userRef, DocumentReference lugarRef, Timestamp fecha) {
        Map<String, Object> cita = new HashMap<>();
        cita.put("actividadId", actRef);
        cita.put("usuarioId", userRef);
        cita.put("creadaPorUsuarioId", userRef);

        // COPIAMOS LOS DATOS PARA QUE LA AGENDA LOS VEA
        cita.put("nombre", texto(etNombre));
        cita.put("descripcion", texto(etDescripcion));
        cita.put("lugarId", lugarRef);

        // USAMOS 'fechaInicio' QUE ES LO QUE BUSCA LA AGENDA
        cita.put("fechaInicio", fecha);
        cita.put("fechaFin", fecha);

        cita.put("estado", "Confirmado");
        cita.put("fechaCreacion", FieldValue.serverTimestamp());
        cita.put("ultimaActualizacion", FieldValue.serverTimestamp());

        db.collection("citas").add(cita).addOnSuccessListener(r -> {
            Toast.makeText(this, "Actividad creada", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnCrear.setEnabled(true);
            Toast.makeText(this, "Error creando cita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void crearCitasPeriodicas(DocumentReference actRef, DocumentReference userRef, DocumentReference lugarRef, Calendar inicio, Calendar fin, String frecuencia) {
        WriteBatch batch = db.batch();
        Calendar iteracion = (Calendar) inicio.clone();
        int count = 0;

        int fieldToAdd = Calendar.DAY_OF_YEAR;
        int amountToAdd = 7;

        if (frecuencia.equals("DIARIA")) { amountToAdd = 1; }
        else if (frecuencia.equals("MENSUAL")) { fieldToAdd = Calendar.MONTH; amountToAdd = 1; }

        while (!iteracion.after(fin)) {
            Map<String, Object> cita = new HashMap<>();
            cita.put("actividadId", actRef);
            cita.put("usuarioId", userRef);
            cita.put("creadaPorUsuarioId", userRef);
            cita.put("nombre", texto(etNombre));
            cita.put("descripcion", texto(etDescripcion));
            cita.put("lugarId", lugarRef);

            Timestamp tsFecha = new Timestamp(iteracion.getTime());
            cita.put("fechaInicio", tsFecha);
            cita.put("fechaFin", tsFecha);
            cita.put("estado", "Confirmado");
            cita.put("fechaCreacion", FieldValue.serverTimestamp());
            cita.put("ultimaActualizacion", FieldValue.serverTimestamp());

            DocumentReference newRef = db.collection("citas").document();
            batch.set(newRef, cita);
            count++;

            iteracion.add(fieldToAdd, amountToAdd);
        }

        if (count > 0) {
            // AQUÍ ESTABA EL ERROR: Usamos una variable final para el mensaje
            final int finalCount = count;
            batch.commit().addOnSuccessListener(v -> {
                Toast.makeText(this, "Creadas " + finalCount + " citas periódicas", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                btnCrear.setEnabled(true);
                Toast.makeText(this, "Error guardando: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            btnCrear.setEnabled(true);
            Toast.makeText(this, "No se crearon citas (Revisa rango de fechas)", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Cargas ---
    private void cargarLugares() {
        db.collection("lugares").get().addOnSuccessListener(qs -> {
            lugaresNombres.clear(); lugaresIds.clear(); capacidadPorLugar.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("descripcion");
                if (n == null) n = doc.getString("nombre");
                Long c = doc.getLong("capacidad");
                int cap = (c != null) ? c.intValue() : 0;
                if (n != null) { lugaresNombres.add(n); lugaresIds.add(doc.getId()); capacidadPorLugar.put(doc.getId(), cap); }
            }
            autoCompleteLugar.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lugaresNombres));
            autoCompleteLugar.setOnItemClickListener((p, v, pos, id) -> lugarSeleccionadoId = lugaresIds.get(pos));
        });
    }

    private void cargarTiposActividad() {
        db.collection("tiposActividades").get().addOnSuccessListener(qs -> {
            tiposNombres.clear(); tiposIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("nombre");
                if (n != null) { tiposNombres.add(n); tiposIds.add(doc.getId()); }
            }
            autoCompleteTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposNombres));
            autoCompleteTipo.setOnItemClickListener((p, v, pos, id) -> tipoSeleccionadoId = tiposIds.get(pos));
        });
    }
    private void cargarProyectos() {
        db.collection("proyecto").get().addOnSuccessListener(qs -> {
            proyectosNombres.clear(); proyectosIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("nombre");
                if (n != null) { proyectosNombres.add(n); proyectosIds.add(doc.getId()); }
            }
            autoCompleteProyecto.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proyectosNombres));
            autoCompleteProyecto.setOnItemClickListener((p, v, pos, id) -> proyectoSeleccionadoId = proyectosIds.get(pos));
        });
    }
    private void cargarSocios() {
        db.collection("socioComunitario").get().addOnSuccessListener(qs -> {
            sociosNombres.clear(); sociosIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("nombre");
                if (n != null) { sociosNombres.add(n); sociosIds.add(doc.getId()); }
            }
            autoCompleteSocio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sociosNombres));
            autoCompleteSocio.setOnItemClickListener((p, v, pos, id) -> socioSeleccionadoId = sociosIds.get(pos));
        });
    }
    private void cargarOferentes() {
        db.collection("oferentes").get().addOnSuccessListener(qs -> {
            oferentesNombres.clear(); oferentesIds.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                String n = doc.getString("nombre");
                if (n != null) { oferentesNombres.add(n); oferentesIds.add(doc.getId()); }
            }
            autoCompleteOferente.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, oferentesNombres));
            autoCompleteOferente.setOnItemClickListener((p, v, pos, id) -> oferenteSeleccionadoId = oferentesIds.get(pos));
        });
    }

    private String texto(EditText et) { return et != null ? et.getText().toString().trim() : ""; }
}