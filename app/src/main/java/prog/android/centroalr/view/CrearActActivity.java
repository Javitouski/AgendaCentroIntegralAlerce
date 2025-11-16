package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // PASO 3: Importar
import android.widget.RadioButton; // PASO 3: Importar
import android.widget.RadioGroup; // PASO 3: Importar
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch; // PASO 3: Importar

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit; // PASO 3: Importar

import prog.android.centroalr.R;

public class CrearActActivity extends AppCompatActivity {

    // Vistas existentes
    private EditText etNombre, etDescripcion, etBeneficiarios, etCupo, etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo, autoCompleteLugar;
    private TextInputLayout dropdownTipo, inputLugar;
    private Button btnFecha, btnHora, btnCrear;

    // --- PASO 3: Nuevas vistas para Periodicidad ---
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual, rbPeriodica;
    private LinearLayout grupoFechaFin;
    private Button btnFechaFin;
    // --- Fin PASO 3 ---

    private FirebaseFirestore db;

    // --- PASO 3: Calendarios separados ---
    private Calendar fechaInicioSeleccionada;
    private Calendar fechaFinSeleccionada;
    // --- Fin PASO 3 ---

    // Variables para Dropdowns (Paso 2)
    private List<String> tiposNombres = new ArrayList<>();
    private List<String> tiposIds = new ArrayList<>();
    private String tipoSeleccionadoId;

    private List<String> lugaresNombres = new ArrayList<>();
    private List<String> lugaresIds = new ArrayList<>();
    private String lugarSeleccionadoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_act);

        db = FirebaseFirestore.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Referencias a vistas (existentes)
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etBeneficiarios = findViewById(R.id.etBeneficiarios);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        dropdownTipo = findViewById(R.id.dropdownTipo);
        autoCompleteTipo = findViewById(R.id.autoCompleteTipo);
        inputLugar = findViewById(R.id.inputLugar);
        autoCompleteLugar = findViewById(R.id.autoCompleteLugar);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnCrear = findViewById(R.id.btnCrearActividad);

        // --- PASO 3: Referencias a vistas nuevas ---
        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);
        grupoFechaFin = findViewById(R.id.grupoFechaFin);
        btnFechaFin = findViewById(R.id.btnFechaFin);
        // --- Fin PASO 3 ---

        // Carga de Dropdowns (Paso 2)
        cargarTiposActividad();
        cargarLugares();

        // Listeners
        btnFecha.setOnClickListener(v -> mostrarDatePickerInicio());
        btnHora.setOnClickListener(v -> mostrarTimePickerInicio());
        btnCrear.setOnClickListener(v -> validarYGuardar());

        // --- PASO 3: Listeners para Periodicidad ---
        btnFechaFin.setOnClickListener(v -> mostrarDatePickerFin());
        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPeriodica) {
                // Si es periódica, mostrar el grupo "Fecha Fin"
                grupoFechaFin.setVisibility(View.VISIBLE);
            } else {
                // Si es puntual, ocultarlo y limpiar la fecha fin
                grupoFechaFin.setVisibility(View.GONE);
                fechaFinSeleccionada = null;
                btnFechaFin.setText("Seleccionar fecha de fin");
            }
        });
        // --- Fin PASO 3 ---
    }

    // --- PASO 3: Métodos Date/Time Picker actualizados ---
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
                    // Importante: setear la hora al final del día (23:59:59)
                    fechaFinSeleccionada.set(y, m, d, 23, 59, 59);
                    String texto = d + "/" + (m + 1) + "/" + y;
                    btnFechaFin.setText(texto);
                },
                hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)
        );
        // La fecha de fin no puede ser anterior a la de inicio
        if (fechaInicioSeleccionada != null) {
            dialog.getDatePicker().setMinDate(fechaInicioSeleccionada.getTimeInMillis());
        }
        dialog.show();
    }
    // --- Fin PASO 3 ---

    private void validarYGuardar() {
        // ... (Validaciones existentes de Nombre, Descripcion, Cupo, DiasAviso) ...
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);
        String beneficiarios = texto(etBeneficiarios);
        String cupoStr = texto(etCupo);
        String diasAvisoStr = texto(etDiasAviso);
        // ... (Aquí irían tus validaciones de TextUtils.isEmpty) ...

        long cupo;
        long diasAviso;
        try {
            cupo = Long.parseLong(cupoStr);
            diasAviso = Long.parseLong(diasAvisoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cupo y días de aviso deben ser números", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de Fecha/Hora Inicio
        if (fechaInicioSeleccionada == null) {
            Toast.makeText(this, "Selecciona fecha y hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de Dropdowns (Paso 2)
        if (tipoSeleccionadoId == null) { /* ... validación de tipo ... */
            Toast.makeText(this, "Selecciona un tipo", Toast.LENGTH_SHORT).show(); return;
        }
        if (lugarSeleccionadoId == null) { /* ... validación de lugar ... */
            Toast.makeText(this, "Selecciona un lugar", Toast.LENGTH_SHORT).show(); return;
        }

        // --- PASO 3: Validación de Periodicidad ---
        String periodicidadStr;
        Timestamp fechaInicioTS = new Timestamp(fechaInicioSeleccionada.getTime());
        Timestamp fechaFinTS;

        if (rbPeriodica.isChecked()) {
            periodicidadStr = "periodica";
            if (fechaFinSeleccionada == null) {
                Toast.makeText(this, "Selecciona una fecha de fin para la actividad periódica", Toast.LENGTH_SHORT).show();
                btnFechaFin.requestFocus();
                return;
            }
            if (fechaFinSeleccionada.before(fechaInicioSeleccionada)) {
                Toast.makeText(this, "La fecha de fin no puede ser anterior a la de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
            fechaFinTS = new Timestamp(fechaFinSeleccionada.getTime());
        } else {
            periodicidadStr = "puntual";
            fechaFinTS = fechaInicioTS; // En puntual, fin es igual a inicio
        }
        // --- Fin PASO 3 ---

        btnCrear.setEnabled(false);

        // Referencias dinámicas (Paso 2)
        DocumentReference tipoRef = db.collection("tiposActividades").document(tipoSeleccionadoId);
        DocumentReference lugarRef = db.collection("lugares").document(lugarSeleccionadoId);

        // Referencias que aún son fijas (TODO: Paso 4 - Mantenedores)
        DocumentReference proyectoRef = db.collection("proyecto").document("proyecto001");
        DocumentReference socioRef = db.collection("socioComunitario").document("socioComunitario001");
        DocumentReference oferenteRef = db.collection("oferentes").document("oferente001");
        DocumentReference usuarioRef = db.collection("usuarios").document("user001"); // TODO: Debería ser el usuario logueado

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("descripcion", descripcion);
        actividad.put("beneficiariosDescripcion", beneficiarios);
        actividad.put("cupo", cupo);
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("estado", "activa");
        actividad.put("fechaCreacion", FieldValue.serverTimestamp());
        actividad.put("fechaInicio", fechaInicioTS);
        actividad.put("fechaFin", fechaFinTS); // <-- PASO 3: Fecha fin dinámica
        actividad.put("periodicidad", periodicidadStr); // <-- PASO 3: Periodicidad dinámica
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
                    // --- PASO 3: Decidir qué tipo de citas crear ---
                    if (periodicidadStr.equals("puntual")) {
                        crearCitaUnica(docRef, usuarioRef, lugarRef, fechaInicioTS);
                    } else {
                        // Asumimos periodicidad SEMANAL por ahora
                        crearCitasPeriodicas(docRef, usuarioRef, lugarRef, fechaInicioSeleccionada, fechaFinSeleccionada);
                    }
                    // --- Fin PASO 3 ---
                })
                .addOnFailureListener(e -> {
                    btnCrear.setEnabled(true);
                    Toast.makeText(this, "Error al guardar actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- PASO 3: Método renombrado ---
    private void crearCitaUnica(DocumentReference actividadRef,
                                DocumentReference usuarioRef,
                                DocumentReference lugarRef,
                                Timestamp fechaHora) {

        Map<String, Object> cita = new HashMap<>();
        cita.put("actividadId", actividadRef);
        cita.put("usuarioId", usuarioRef); // Esto parece ser el 'oferente' de la cita
        cita.put("lugarId", lugarRef);
        cita.put("estado", "Confirmado");
        cita.put("fecha", fechaHora);
        cita.put("fechaCreacion", FieldValue.serverTimestamp());
        cita.put("ultimaActualizacion", FieldValue.serverTimestamp());
        cita.put("creadaPorUsuarioId", usuarioRef);
        cita.put("motivoCambio", "");
        cita.put("observaciones", "");

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

    // --- PASO 3: Nuevo método para citas periódicas ---
    private void crearCitasPeriodicas(DocumentReference actividadRef,
                                      DocumentReference usuarioRef,
                                      DocumentReference lugarRef,
                                      Calendar fechaInicio,
                                      Calendar fechaFin) {

        // Usamos un WriteBatch para crear todas las citas en una sola operación
        WriteBatch batch = db.batch();

        // Clonamos la fecha de inicio para no modificar la original
        Calendar fechaIteracion = (Calendar) fechaInicio.clone();

        int citasCreadas = 0;

        // Bucle: mientras la fecha de iteración sea ANTES o IGUAL que la fecha de fin
        // Asumimos periodicidad SEMANAL (sumamos 7 días)
        while (!fechaIteracion.after(fechaFin)) {

            // 1. Crear el objeto Cita
            Map<String, Object> cita = new HashMap<>();
            cita.put("actividadId", actividadRef);
            cita.put("usuarioId", usuarioRef);
            cita.put("lugarId", lugarRef);
            cita.put("estado", "Confirmado");
            cita.put("fecha", new Timestamp(fechaIteracion.getTime())); // Fecha de esta iteración
            cita.put("fechaCreacion", FieldValue.serverTimestamp());
            cita.put("ultimaActualizacion", FieldValue.serverTimestamp());
            cita.put("creadaPorUsuarioId", usuarioRef);
            cita.put("motivoCambio", "");
            cita.put("observaciones", "");

            // 2. Añadir la cita al batch (lote)
            // Creamos una nueva referencia de documento vacía para la cita
            DocumentReference citaRef = db.collection("citas").document();
            batch.set(citaRef, cita);

            citasCreadas++;

            // 3. Avanzar a la siguiente semana
            fechaIteracion.add(Calendar.DAY_OF_YEAR, 7);
        }

        // 4. Ejecutar el batch
        if (citasCreadas > 0) {

            // ¡CORRECCIÓN! Creamos una copia 'final' de la variable
            final int totalCitasCreadas = citasCreadas;

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Y usamos la copia 'final' aquí
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
    // --- Fin PASO 3 ---


    // --- Métodos de Carga (Paso 2) ---

    private void cargarTiposActividad() {
        db.collection("tiposActividades")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    tiposNombres.clear();
                    tiposIds.clear();
                    tipoSeleccionadoId = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null) {
                            tiposNombres.add(nombre);
                            tiposIds.add(doc.getId());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposNombres);
                    autoCompleteTipo.setAdapter(adapter);
                    autoCompleteTipo.setOnItemClickListener((parent, view, position, id) -> {
                        tipoSeleccionadoId = tiposIds.get(position);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("CrearAct", "Error al cargar tipos de actividad", e);
                    Toast.makeText(this, "Error al cargar tipos de actividad", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarLugares() {
        db.collection("lugares")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lugaresNombres.clear();
                    lugaresIds.clear();
                    lugarSeleccionadoId = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String nombre = doc.getString("descripcion");
                        if (nombre == null) { nombre = doc.getString("nombre"); }
                        if (nombre != null) {
                            lugaresNombres.add(nombre);
                            lugaresIds.add(doc.getId());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lugaresNombres);
                    autoCompleteLugar.setAdapter(adapter);
                    autoCompleteLugar.setOnItemClickListener((parent, view, position, id) -> {
                        lugarSeleccionadoId = lugaresIds.get(position);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("CrearAct", "Error al cargar lugares", e);
                    Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
                });
    }

    private String texto(EditText editText) {
        if (editText == null) return "";
        return editText.getText().toString().trim();
    }
}