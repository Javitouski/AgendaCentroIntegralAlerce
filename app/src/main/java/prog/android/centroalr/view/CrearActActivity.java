package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent; // Importar
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

import com.google.android.material.textfield.TextInputLayout;
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
import java.util.Locale; // Importar
import java.util.Map;
import java.util.concurrent.TimeUnit;

import prog.android.centroalr.MyApplication; // IMPORTAR
import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario; // IMPORTAR

public class CrearActActivity extends AppCompatActivity {

    // Vistas
    private EditText etNombre, etDescripcion, etBeneficiarios, etCupo, etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo, autoCompleteLugar;
    private Button btnFecha, btnHora, btnCrear;
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual, rbPeriodica;
    private LinearLayout grupoFechaFin;
    private Button btnFechaFin;

    // Vistas NUEVAS
    private AutoCompleteTextView autoCompleteProyecto, autoCompleteSocio, autoCompleteOferente;

    // Firebase y Usuario
    private FirebaseFirestore db;
    private Usuario usuarioActual; // <-- NUEVO: Para el UID

    // Calendarios
    private Calendar fechaInicioSeleccionada;
    private Calendar fechaFinSeleccionada;

    // --- Listas y IDs para TODOS los Dropdowns ---
    private List<String> tiposNombres = new ArrayList<>();
    private List<String> tiposIds = new ArrayList<>();
    private String tipoSeleccionadoId;

    private List<String> lugaresNombres = new ArrayList<>();
    private List<String> lugaresIds = new ArrayList<>();
    private String lugarSeleccionadoId;

    // NUEVO
    private List<String> proyectosNombres = new ArrayList<>();
    private List<String> proyectosIds = new ArrayList<>();
    private String proyectoSeleccionadoId;

    // NUEVO
    private List<String> sociosNombres = new ArrayList<>();
    private List<String> sociosIds = new ArrayList<>();
    private String socioSeleccionadoId;

    // NUEVO
    private List<String> oferentesNombres = new ArrayList<>();
    private List<String> oferentesIds = new ArrayList<>();
    private String oferenteSeleccionadoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_act);

        // --- Cargar Perfil de Usuario (Seguridad) ---
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        // --- Fin Carga de Perfil ---

        db = FirebaseFirestore.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Referencias a vistas
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etBeneficiarios = findViewById(R.id.etBeneficiarios);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnCrear = findViewById(R.id.btnCrearActividad);

        // Dropdowns (Paso 2)
        autoCompleteTipo = findViewById(R.id.autoCompleteTipo);
        autoCompleteLugar = findViewById(R.id.autoCompleteLugar);

        // Periodicidad (Paso 3)
        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);
        grupoFechaFin = findViewById(R.id.grupoFechaFin);
        btnFechaFin = findViewById(R.id.btnFechaFin);

        // --- NUEVAS Vistas (Paso 4) ---
        autoCompleteProyecto = findViewById(R.id.autoCompleteProyecto);
        autoCompleteSocio = findViewById(R.id.autoCompleteSocio);
        autoCompleteOferente = findViewById(R.id.autoCompleteOferente);
        // --- Fin NUEVAS Vistas ---

        // Carga de TODOS los Dropdowns
        cargarTiposActividad();
        cargarLugares();
        cargarProyectos(); // <-- NUEVO
        cargarSocios(); // <-- NUEVO
        cargarOferentes(); // <-- NUEVO

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

    // ... (mostrarDatePickerInicio, mostrarTimePickerInicio, mostrarDatePickerFin - SIN CAMBIOS) ...
    private void mostrarDatePickerInicio() {
        final Calendar hoy = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    if (fechaInicioSeleccionada == null) {
                        fechaInicioSeleccionada = Calendar.getInstance();
                    }
                    fechaInicioSeleccionada.set(Calendar.YEAR, y);
                    fechaInicioSeleccionada.set(Calendar.MONTH, m);
                    fechaInicioSeleccionada.set(Calendar.DAY_OF_MONTH, d);
                    String texto = d + "/" + (m + 1) + "/" + y;
                    btnFecha.setText(texto);
                },
                hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void mostrarTimePickerInicio() {
        final Calendar ahora = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    if (fechaInicioSeleccionada == null) {
                        fechaInicioSeleccionada = Calendar.getInstance();
                    }
                    fechaInicioSeleccionada.set(Calendar.HOUR_OF_DAY, h);
                    fechaInicioSeleccionada.set(Calendar.MINUTE, m);
                    fechaInicioSeleccionada.set(Calendar.SECOND, 0);
                    String texto = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                    btnHora.setText(texto);
                },
                ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true
        );
        dialog.show();
    }

    private void mostrarDatePickerFin() {
        final Calendar hoy = (fechaInicioSeleccionada != null) ? fechaInicioSeleccionada : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    fechaFinSeleccionada = Calendar.getInstance();
                    fechaFinSeleccionada.set(y, m, d, 23, 59, 59);
                    String texto = d + "/" + (m + 1) + "/" + y;
                    btnFechaFin.setText(texto);
                },
                hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)
        );
        if (fechaInicioSeleccionada != null) {
            dialog.getDatePicker().setMinDate(fechaInicioSeleccionada.getTimeInMillis());
        }
        dialog.show();
    }


    private void validarYGuardar() {
        // ... (Validaciones de Nombre, Descripcion, Cupo, DiasAviso, FechaInicio) ...
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);

        // --- VALIDACIONES DE TODOS LOS DROPDOWNS ---
        if (tipoSeleccionadoId == null) {
            Toast.makeText(this, "Selecciona un tipo", Toast.LENGTH_SHORT).show(); return;
        }
        if (lugarSeleccionadoId == null) {
            Toast.makeText(this, "Selecciona un lugar", Toast.LENGTH_SHORT).show(); return;
        }
        // NUEVO
        if (proyectoSeleccionadoId == null) {
            Toast.makeText(this, "Selecciona un proyecto", Toast.LENGTH_SHORT).show(); return;
        }
        // NUEVO
        if (socioSeleccionadoId == null) {
            Toast.makeText(this, "Selecciona un socio", Toast.LENGTH_SHORT).show(); return;
        }
        // NUEVO
        if (oferenteSeleccionadoId == null) {
            Toast.makeText(this, "Selecciona un oferente", Toast.LENGTH_SHORT).show(); return;
        }
        // --- FIN VALIDACIONES DROPDOWNS ---


        // ... (Validación de Periodicidad y Fechas - SIN CAMBIOS) ...
        String periodicidadStr;
        Timestamp fechaInicioTS = new Timestamp(fechaInicioSeleccionada.getTime());
        Timestamp fechaFinTS;
        if (rbPeriodica.isChecked()) {
            periodicidadStr = "periodica";
            if (fechaFinSeleccionada == null) { /* ... validación fecha fin ... */
                Toast.makeText(this, "Selecciona una fecha de fin", Toast.LENGTH_SHORT).show(); return;
            }
            fechaFinTS = new Timestamp(fechaFinSeleccionada.getTime());
        } else {
            periodicidadStr = "puntual";
            fechaFinTS = fechaInicioTS;
        }

        btnCrear.setEnabled(false);
        // (Aquí iría la lógica de Cupo, DiasAviso, etc. que ya tenías)
        long cupo = Long.parseLong(texto(etCupo));
        long diasAviso = Long.parseLong(texto(etDiasAviso));
        String beneficiarios = texto(etBeneficiarios);

        // --- TODAS LAS REFERENCIAS SON AHORA DINÁMICAS ---
        DocumentReference tipoRef = db.collection("tiposActividades").document(tipoSeleccionadoId);
        DocumentReference lugarRef = db.collection("lugares").document(lugarSeleccionadoId);
        DocumentReference proyectoRef = db.collection("proyecto").document(proyectoSeleccionadoId); // <-- CORREGIDO
        DocumentReference socioRef = db.collection("socioComunitario").document(socioSeleccionadoId); // <-- CORREGIDO
        DocumentReference oferenteRef = db.collection("oferentes").document(oferenteSeleccionadoId); // <-- CORREGIDO

        // --- USUARIO CREADOR ES AHORA DINÁMICO ---
        DocumentReference usuarioRef = db.collection("usuarios").document(usuarioActual.getUid()); // <-- CORREGIDO

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
        actividad.put("tieneArchivos", false);
        actividad.put("tipoActividadId", tipoRef);
        actividad.put("lugarId", lugarRef);
        actividad.put("proyectoId", proyectoRef);
        actividad.put("socioComunitarioId", socioRef);
        actividad.put("creadaPorUsuarioId", usuarioRef);
        actividad.put("oferenteId", oferenteRef);

        db.collection("actividades")
                .add(actividad)
                .addOnSuccessListener(docRef -> {
                    if (periodicidadStr.equals("puntual")) {
                        crearCitaUnica(docRef, usuarioRef, lugarRef, fechaInicioTS);
                    } else {
                        crearCitasPeriodicas(docRef, usuarioRef, lugarRef, fechaInicioSeleccionada, fechaFinSeleccionada);
                    }
                })
                .addOnFailureListener(e -> {
                    btnCrear.setEnabled(true);
                    Toast.makeText(this, "Error al guardar actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ... (crearCitaUnica y crearCitasPeriodicas - SIN CAMBIOS) ...
    private void crearCitaUnica(DocumentReference actividadRef, DocumentReference usuarioRef, DocumentReference lugarRef, Timestamp fechaHora) {
        Map<String, Object> cita = new HashMap<>();
        // ... (contenido de la cita)
        cita.put("actividadId", actividadRef);
        cita.put("usuarioId", usuarioRef);
        cita.put("lugarId", lugarRef);
        cita.put("estado", "Confirmado");
        cita.put("fecha", fechaHora);
        cita.put("fechaCreacion", FieldValue.serverTimestamp());
        cita.put("ultimaActualizacion", FieldValue.serverTimestamp());
        cita.put("creadaPorUsuarioId", usuarioRef);
        // ...
        db.collection("citas")
                .add(cita)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Actividad (puntual) y cita creadas con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Actividad creada, pero la cita falló: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void crearCitasPeriodicas(DocumentReference actividadRef, DocumentReference usuarioRef, DocumentReference lugarRef, Calendar fechaInicio, Calendar fechaFin) {
        WriteBatch batch = db.batch();
        Calendar fechaIteracion = (Calendar) fechaInicio.clone();
        int citasCreadas = 0;
        while (!fechaIteracion.after(fechaFin)) {
            Map<String, Object> cita = new HashMap<>();
            // ... (contenido de la cita)
            cita.put("actividadId", actividadRef);
            cita.put("usuarioId", usuarioRef);
            cita.put("lugarId", lugarRef);
            cita.put("estado", "Confirmado");
            cita.put("fecha", new Timestamp(fechaIteracion.getTime()));
            cita.put("fechaCreacion", FieldValue.serverTimestamp());
            cita.put("ultimaActualizacion", FieldValue.serverTimestamp());
            cita.put("creadaPorUsuarioId", usuarioRef);
            // ...
            DocumentReference citaRef = db.collection("citas").document();
            batch.set(citaRef, cita);
            citasCreadas++;
            fechaIteracion.add(Calendar.DAY_OF_YEAR, 7);
        }

        final int totalCitasCreadas = citasCreadas; // Corrección para el listener
        if (citasCreadas > 0) {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Actividad periódica y " + totalCitasCreadas + " citas creadas con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Actividad creada, pero falló la creación de citas periódicas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Actividad periódica creada (sin citas en el rango)", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ... (cargarTiposActividad y cargarLugares - SIN CAMBIOS) ...
    private void cargarTiposActividad() {
        db.collection("tiposActividades").get().addOnSuccessListener(querySnapshot -> {
            tiposNombres.clear(); tiposIds.clear(); tipoSeleccionadoId = null;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombre = doc.getString("nombre");
                if (nombre != null) { tiposNombres.add(nombre); tiposIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposNombres);
            autoCompleteTipo.setAdapter(adapter);
            autoCompleteTipo.setOnItemClickListener((parent, view, position, id) -> tipoSeleccionadoId = tiposIds.get(position));
        }).addOnFailureListener(e -> Log.e("CrearAct", "Error al cargar tipos", e));
    }

    private void cargarLugares() {
        db.collection("lugares").get().addOnSuccessListener(querySnapshot -> {
            lugaresNombres.clear(); lugaresIds.clear(); lugarSeleccionadoId = null;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombre = doc.getString("descripcion");
                if (nombre == null) { nombre = doc.getString("nombre"); }
                if (nombre != null) { lugaresNombres.add(nombre); lugaresIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lugaresNombres);
            autoCompleteLugar.setAdapter(adapter);
            autoCompleteLugar.setOnItemClickListener((parent, view, position, id) -> lugarSeleccionadoId = lugaresIds.get(position));
        }).addOnFailureListener(e -> Log.e("CrearAct", "Error al cargar lugares", e));
    }


    // --- NUEVOS MÉTODOS DE CARGA (Paso 4) ---
    // (Son copias de cargarLugares/cargarTipos, ajustadas)

    private void cargarProyectos() {
        // Usamos "nombre" según la colección "proyecto"
        db.collection("proyecto").get().addOnSuccessListener(querySnapshot -> {
            proyectosNombres.clear(); proyectosIds.clear(); proyectoSeleccionadoId = null;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombre = doc.getString("nombre");
                if (nombre != null) { proyectosNombres.add(nombre); proyectosIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proyectosNombres);
            autoCompleteProyecto.setAdapter(adapter);
            autoCompleteProyecto.setOnItemClickListener((parent, view, position, id) -> proyectoSeleccionadoId = proyectosIds.get(position));
        }).addOnFailureListener(e -> Log.e("CrearAct", "Error al cargar proyectos", e));
    }

    private void cargarSocios() {
        // Usamos "nombre" según la colección "socioComunitario"
        db.collection("socioComunitario").get().addOnSuccessListener(querySnapshot -> {
            sociosNombres.clear(); sociosIds.clear(); socioSeleccionadoId = null;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombre = doc.getString("nombre");
                if (nombre != null) { sociosNombres.add(nombre); sociosIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sociosNombres);
            autoCompleteSocio.setAdapter(adapter);
            autoCompleteSocio.setOnItemClickListener((parent, view, position, id) -> socioSeleccionadoId = sociosIds.get(position));
        }).addOnFailureListener(e -> Log.e("CrearAct", "Error al cargar socios", e));
    }

    private void cargarOferentes() {
        // Usamos "nombre" según la colección "oferentes"
        db.collection("oferentes").get().addOnSuccessListener(querySnapshot -> {
            oferentesNombres.clear(); oferentesIds.clear(); oferenteSeleccionadoId = null;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombre = doc.getString("nombre");
                if (nombre != null) { oferentesNombres.add(nombre); oferentesIds.add(doc.getId()); }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, oferentesNombres);
            autoCompleteOferente.setAdapter(adapter);
            autoCompleteOferente.setOnItemClickListener((parent, view, position, id) -> oferenteSeleccionadoId = oferentesIds.get(position));
        }).addOnFailureListener(e -> Log.e("CrearAct", "Error al cargar oferentes", e));
    }

    // --- Fin NUEVOS MÉTODOS ---


    private String texto(EditText editText) {
        if (editText == null) return "";
        return editText.getText().toString().trim();
    }
}