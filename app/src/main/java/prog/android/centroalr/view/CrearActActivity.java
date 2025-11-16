package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;                    // <-- IMPORTANTE
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import prog.android.centroalr.R;

public class CrearActActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etDescripcion;
    private EditText etBeneficiarios;
    private EditText etCupo;
    private EditText etDiasAviso;
    private AutoCompleteTextView autoCompleteTipo;
    private AutoCompleteTextView autoCompleteLugar;
    private TextInputLayout dropdownTipo;
    private TextInputLayout inputLugar;
    private Button btnFecha;
    private Button btnHora;
    private Button btnCrear;

    private FirebaseFirestore db;

    // Fecha y hora seleccionadas
    private Calendar fechaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_act);

        db = FirebaseFirestore.getInstance();

        // ==== BACK BUTTON ====
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // volver a la pantalla anterior
        }
        // =====================

        // Referencias a vistas
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

        // Valores por defecto simples para tipo y lugar
        autoCompleteTipo.setText("Taller", false);
        autoCompleteLugar.setText("Oficina principal del centro comunitario", false);

        // Listeners de fecha y hora
        btnFecha.setOnClickListener(v -> mostrarDatePicker());
        btnHora.setOnClickListener(v -> mostrarTimePicker());

        btnCrear.setOnClickListener(v -> validarYGuardar());
    }
    private void mostrarDatePicker() {
        final Calendar hoy = Calendar.getInstance();

        int year = hoy.get(Calendar.YEAR);
        int month = hoy.get(Calendar.MONTH);
        int day = hoy.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    if (fechaSeleccionada == null) {
                        fechaSeleccionada = Calendar.getInstance();
                    }
                    fechaSeleccionada.set(Calendar.YEAR, y);
                    fechaSeleccionada.set(Calendar.MONTH, m);
                    fechaSeleccionada.set(Calendar.DAY_OF_MONTH, d);

                    String texto = d + "/" + (m + 1) + "/" + y;
                    btnFecha.setText(texto);
                },
                year, month, day
        );

        dialog.show();
    }

    private void mostrarTimePicker() {
        final Calendar ahora = Calendar.getInstance();

        int hour = ahora.get(Calendar.HOUR_OF_DAY);
        int minute = ahora.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    if (fechaSeleccionada == null) {
                        fechaSeleccionada = Calendar.getInstance();
                    }
                    fechaSeleccionada.set(Calendar.HOUR_OF_DAY, h);
                    fechaSeleccionada.set(Calendar.MINUTE, m);
                    fechaSeleccionada.set(Calendar.SECOND, 0);

                    String texto = String.format("%02d:%02d", h, m);
                    btnHora.setText(texto);
                },
                hour, minute, true
        );

        dialog.show();
    }

    private void validarYGuardar() {
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);
        String beneficiarios = texto(etBeneficiarios);
        String cupoStr = texto(etCupo);
        String diasAvisoStr = texto(etDiasAviso);

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingresa un nombre");
            etNombre.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            etDescripcion.setError("Ingresa una descripción");
            etDescripcion.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cupoStr)) {
            etCupo.setError("Ingresa el cupo máximo");
            etCupo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(diasAvisoStr)) {
            etDiasAviso.setError("Ingresa los días de aviso");
            etDiasAviso.requestFocus();
            return;
        }

        if (fechaSeleccionada == null) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        long cupo;
        long diasAviso;
        try {
            cupo = Long.parseLong(cupoStr);
            diasAviso = Long.parseLong(diasAvisoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cupo y días de aviso deben ser números", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCrear.setEnabled(false);

        // Fecha y hora final para guardar
        Timestamp fechaHora = new Timestamp(fechaSeleccionada.getTime());

        // Por ahora tratamos la actividad como puntual:
        // fechaFin = misma fecha/hora que inicio
        Timestamp fechaFin = fechaHora;

        // Referencias fijas según los documentos que creaste en Firestore
        DocumentReference tipoRef = db.collection("tiposActividades").document("taller");
        DocumentReference lugarRef = db.collection("lugares").document("oficina");
        DocumentReference proyectoRef = db.collection("proyecto").document("proyecto001");
        DocumentReference socioRef = db.collection("socioComunitario").document("socioComunitario001");
        DocumentReference oferenteRef = db.collection("oferentes").document("oferente001");
        DocumentReference usuarioRef = db.collection("usuarios").document("user001");

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("descripcion", descripcion);
        actividad.put("beneficiariosDescripcion", beneficiarios);
        actividad.put("cupo", cupo);
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("estado", "activa");
        actividad.put("fechaCreacion", FieldValue.serverTimestamp());
        actividad.put("fechaInicio", fechaHora);
        actividad.put("fechaFin", fechaFin);        // <<< NUEVO
        actividad.put("periodicidad", "puntual");
        actividad.put("tieneArchivos", false);

        actividad.put("tipoActividadId", tipoRef);
        actividad.put("lugarId", lugarRef);
        actividad.put("proyectoId", proyectoRef);
        actividad.put("socioComunitarioId", socioRef);
        actividad.put("creadaPorUsuarioId", usuarioRef);

        // IMPORTANTE: nombre consistente con lo que lee ListaActividadesActivity
        actividad.put("oferenteId", oferenteRef);   // antes: "oferenteIds"

        db.collection("actividades")
                .add(actividad)
                .addOnSuccessListener(docRef -> {
                    // Una vez creada la actividad, creamos una cita inicial para el mismo usuario
                    crearCitaInicial(docRef, usuarioRef, lugarRef, fechaHora);
                })
                .addOnFailureListener(e -> {
                    btnCrear.setEnabled(true);
                    Toast.makeText(this, "Error al guardar actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void crearCitaInicial(DocumentReference actividadRef,
                                  DocumentReference usuarioRef,
                                  DocumentReference lugarRef,
                                  Timestamp fechaHora) {

        Map<String, Object> cita = new HashMap<>();
        cita.put("actividadId", actividadRef);
        cita.put("usuarioId", usuarioRef);
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
                    Toast.makeText(this, "Actividad y cita creadas con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Actividad creada, pero la cita falló: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private String texto(EditText editText) {
        if (editText == null) return "";
        return editText.getText().toString().trim();
    }
}
