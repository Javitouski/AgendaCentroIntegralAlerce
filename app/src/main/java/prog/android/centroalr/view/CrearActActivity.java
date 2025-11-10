package prog.android.centroalr.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.R;

public class CrearActActivity extends AppCompatActivity {

    private EditText etNombre;
    private AutoCompleteTextView autoCompleteTipo, autoCompleteLugar;
    private Button btnFecha, btnHora, btnCrear;
    private FirebaseFirestore db;

    private Calendar calendario = Calendar.getInstance();
    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_act);

        // Inicialización
        db = FirebaseFirestore.getInstance();

        etNombre = findViewById(R.id.etNombre);
        autoCompleteTipo = findViewById(R.id.autoCompleteTipo);
        autoCompleteLugar = findViewById(R.id.autoCompleteLugar);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnCrear = findViewById(R.id.btnCrearActividad);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Cargar datos desde Firestore
        cargarTiposActividades();
        cargarLugares();

        // Eventos de fecha y hora
        btnFecha.setOnClickListener(v -> mostrarSelectorFecha());
        btnHora.setOnClickListener(v -> mostrarSelectorHora());

        // Botón para crear
        btnCrear.setOnClickListener(v -> guardarActividad());
    }

    private void cargarTiposActividades() {
        ArrayList<String> listaTipos = new ArrayList<>();
        db.collection("tiposActividades")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null) listaTipos.add(nombre);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaTipos);
                    autoCompleteTipo.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error cargando tipos", Toast.LENGTH_SHORT).show());
    }

    private void cargarLugares() {
        ArrayList<String> listaLugares = new ArrayList<>();
        db.collection("lugares")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null) listaLugares.add(nombre);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaLugares);
                    autoCompleteLugar.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error cargando lugares", Toast.LENGTH_SHORT).show());
    }

    private void mostrarSelectorFecha() {
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendario.set(year, month, dayOfMonth);
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            fechaSeleccionada = formato.format(calendario.getTime());
            btnFecha.setText(fechaSeleccionada);
        }, año, mes, dia);
        datePicker.show();
    }

    private void mostrarSelectorHora() {
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendario.set(Calendar.MINUTE, minute);
            SimpleDateFormat formato = new SimpleDateFormat("HH:mm", Locale.getDefault());
            horaSeleccionada = formato.format(calendario.getTime());
            btnHora.setText(horaSeleccionada);
        }, hora, minuto, true);
        timePicker.show();
    }

    private void guardarActividad() {
        String nombre = etNombre.getText().toString().trim();
        String tipo = autoCompleteTipo.getText().toString().trim();
        String lugar = autoCompleteLugar.getText().toString().trim();

        if (nombre.isEmpty() || tipo.isEmpty() || lugar.isEmpty() || fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaHoraCompleta = fechaSeleccionada + " " + horaSeleccionada;

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("Nombre", nombre);
        actividad.put("tipo", tipo);
        actividad.put("lugar", lugar);
        actividad.put("fechaInicio", fechaHoraCompleta);

        db.collection("actividades")
                .add(actividad)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Actividad creada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
