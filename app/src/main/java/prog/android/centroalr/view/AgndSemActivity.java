package prog.android.centroalr.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.model.Usuario;

// IMPORTANTE: Si no te compila, asegúrate de importar DocumentReference aunque no lo uses directamente aquí
import com.google.firebase.firestore.DocumentReference;

public class AgndSemActivity extends AppCompatActivity implements LogoutView {

    // ... (Variables y onCreate igual que antes) ...
    // SOLO COPIA EL MÉTODO loadWeekEvents CORREGIDO DE ABAJO, O TODO EL ARCHIVO SI PREFIERES

    // --- Logout MVC ---
    private TextView btnCerrarSesion;
    private LogoutController logoutController;
    private AuthModel authModel;
    private Usuario usuarioActual;

    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter titleFmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", esCL);
    private final DateTimeFormatter shortFmt = DateTimeFormatter.ofPattern("d/M/yyyy", esCL);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private LocalDate weekStart;
    private LocalDate selectedDate;
    private int selectedIndex = 0;

    private TextView tvMes;
    private TextView[] dayBtns = new TextView[7];
    private LinearLayout llEventos;
    private ImageButton btnPrevWeek;
    private ImageButton btnNextWeek;

    private FirebaseFirestore db;
    private final Map<LocalDate, List<Actividad>> eventosSemana = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agnd_sem);

        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);

        authModel = new AuthModel();
        logoutController = new LogoutController(this, authModel);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> logoutController.onLogoutClicked());
        }

        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        db = FirebaseFirestore.getInstance();

        ImageView ivUser = findViewById(R.id.ivUserAvatar);
        if (ivUser != null) {
            ivUser.setOnClickListener(v ->
                    startActivity(new Intent(AgndSemActivity.this, PerfilActivity.class))
            );
        }

        View btnListaActividades = findViewById(R.id.btnListaActividades);
        if (btnListaActividades != null) {
            btnListaActividades.setOnClickListener(v ->
                    startActivity(new Intent(this, ListaActividadesActivity.class)));
        }

        View fabInicio = findViewById(R.id.fabInicio);
        if (fabInicio != null) {
            fabInicio.setOnClickListener(v ->
                    startActivity(new Intent(this, AgendMensActivity.class)));
        }

        View btnCrearActividad = findViewById(R.id.btnCrearActividad);
        if (btnCrearActividad != null) {
            if (usuarioActual.tienePermiso("PUEDE_CREAR_ACTIVIDAD")) {
                btnCrearActividad.setVisibility(View.VISIBLE);
                btnCrearActividad.setOnClickListener(v ->
                        startActivity(new Intent(this, CrearActActivity.class)));
            } else {
                btnCrearActividad.setVisibility(View.GONE);
            }
        }

        tvMes = findViewById(R.id.tvMes);
        llEventos = findViewById(R.id.llEventos);

        dayBtns[0] = findViewById(R.id.btnLunes);
        dayBtns[1] = findViewById(R.id.btnMartes);
        dayBtns[2] = findViewById(R.id.btnMiercoles);
        dayBtns[3] = findViewById(R.id.btnJueves);
        dayBtns[4] = findViewById(R.id.btnViernes);
        dayBtns[5] = findViewById(R.id.btnSabado);
        dayBtns[6] = findViewById(R.id.btnDomingo);

        LocalDate base = LocalDate.now();
        if (getIntent() != null && getIntent().hasExtra("selected_date")) {
            try {
                base = LocalDate.parse(getIntent().getStringExtra("selected_date"));
            } catch (Exception ignore) {}
        }

        weekStart = startOfWeek(base);
        selectedDate = base;
        selectedIndex = selectedDate.getDayOfWeek().getValue() - 1;

        for (int i = 0; i < 7; i++) {
            final int idx = i;
            if (dayBtns[i] != null) {
                dayBtns[i].setOnClickListener(v -> selectDay(idx));
            }
        }

        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                weekStart = weekStart.minusWeeks(1);
                selectedDate = weekStart;
                selectedIndex = 0;
                applySelectionUi();
                loadWeekEvents();
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                weekStart = weekStart.plusWeeks(1);
                selectedDate = weekStart;
                selectedIndex = 0;
                applySelectionUi();
                loadWeekEvents();
            });
        }

        applySelectionUi();
        // loadWeekEvents() se llamará en onResume, así que no hace falta aquí
        loadDayEvents(selectedDate);

        // ... Listeners auxiliares ...
        bindClickByIdOrText("rn6aagqs6pq8", "Lista de Actividades",
                () -> startActivity(new Intent(this, ListaActividadesActivity.class)));
        bindClickByIdOrText("r3pjyjmbkmo9", "Inicio",
                () -> startActivity(new Intent(this, AgendMensActivity.class)));
        bindClickByIdOrText("rqwq9k1hp05", "Crear Actividad",
                () -> {
                    if (usuarioActual.tienePermiso("PUEDE_CREAR_ACTIVIDAD")) {
                        startActivity(new Intent(this, CrearActActivity.class));
                    }
                });

        bindEventById("rtumfhvig6h");
        bindEventById("roa0repy8oae");
        attachEventClickers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeekEvents();
    }

    private LocalDate startOfWeek(LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return date.minusDays(dow - 1L);
    }

    private void selectDay(int idx) {
        selectedIndex = idx;
        selectedDate = weekStart.plusDays(idx);
        applySelectionUi();
        loadDayEvents(selectedDate);
    }

    // === AQUÍ ESTÁ LA CORRECCIÓN DE DUPLICADOS ===
    private void loadWeekEvents() {
        if (db == null) return;

        // NO LIMPIAMOS AQUÍ para evitar parpadeos o condiciones de carrera
        // eventosSemana.clear(); <-- ESTO SE VA

        LocalDate monday = weekStart;
        LocalDate nextMonday = weekStart.plusDays(7);

        ZoneId zone = ZoneId.systemDefault();
        Date mondayDate = Date.from(monday.atStartOfDay(zone).toInstant());
        Date nextMondayDate = Date.from(nextMonday.atStartOfDay(zone).toInstant());

        Timestamp mondayTs = new Timestamp(mondayDate);
        Timestamp nextMondayTs = new Timestamp(nextMondayDate);

        db.collection("citas")
                .whereGreaterThanOrEqualTo("fechaInicio", mondayTs)
                .whereLessThan("fechaInicio", nextMondayTs)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // LIMPIAMOS AQUÍ, justo antes de llenar con datos nuevos
                    eventosSemana.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Actividad act = doc.toObject(Actividad.class);
                        if (act == null || act.getFechaInicio() == null) continue;

                        act.setId(doc.getId());

                        LocalDate diaActividad = act.getFechaInicio().toDate()
                                .toInstant()
                                .atZone(zone)
                                .toLocalDate();

                        List<Actividad> list = eventosSemana.get(diaActividad);
                        if (list == null) {
                            list = new ArrayList<>();
                            eventosSemana.put(diaActividad, list);
                        }
                        list.add(act);
                    }
                    applySelectionUi();
                    loadDayEvents(selectedDate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar semana", Toast.LENGTH_SHORT).show();
                    // En error sí limpiamos por si acaso
                    eventosSemana.clear();
                    applySelectionUi();
                    loadDayEvents(selectedDate);
                });
    }

    // ... (Resto de métodos applySelectionUi, loadDayEvents, cargarNombreLugar igual que antes) ...

    private void applySelectionUi() {
        if (tvMes != null && selectedDate != null) {
            String t = selectedDate.format(titleFmt);
            tvMes.setText(capitalizeFirst(t));
        }
        for (int i = 0; i < 7; i++) {
            TextView tv = dayBtns[i];
            if (tv == null) continue;
            String baseLabel;
            switch (i) {
                case 0: baseLabel = getString(R.string.lunes); break;
                case 1: baseLabel = getString(R.string.martes); break;
                case 2: baseLabel = getString(R.string.miercoles); break;
                case 3: baseLabel = getString(R.string.jueves); break;
                case 4: baseLabel = getString(R.string.viernes); break;
                case 5: baseLabel = getString(R.string.sabado); break;
                case 6: default: baseLabel = getString(R.string.domingo); break;
            }
            LocalDate dia = weekStart.plusDays(i);
            List<Actividad> list = eventosSemana.get(dia);
            boolean hasEvents = (list != null && !list.isEmpty());
            if (hasEvents) {
                String text = baseLabel + " •";
                SpannableString span = new SpannableString(text);
                int dotIndex = text.length() - 1;
                span.setSpan(new ForegroundColorSpan(Color.parseColor("#18990D")), dotIndex, dotIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(span);
            } else {
                tv.setText(baseLabel);
            }
            if (i == selectedIndex) {
                tv.setBackgroundResource(R.drawable.bg_dia_selected);
                tv.setTextColor(Color.parseColor("#066D0A"));
            } else {
                tv.setBackground(null);
                tv.setTextColor(Color.parseColor("#066D0A"));
            }
        }
    }

    private void loadDayEvents(LocalDate date) {
        if (llEventos == null || date == null) return;
        llEventos.removeAllViews();
        List<Actividad> lista = eventosSemana.get(date);
        boolean hayActividades = (lista != null && !lista.isEmpty());
        TextView header = new TextView(this);
        String titulo = (hayActividades ? "Actividades para " : "No hay actividades para ") + date.format(shortFmt);
        header.setText(titulo);
        header.setTextColor(Color.parseColor("#066D0A"));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        header.setPadding(dp(12), dp(8), dp(12), dp(16));
        llEventos.addView(header);
        if (!hayActividades) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Actividad act : lista) {
            View card = inflater.inflate(R.layout.item_actividad, llEventos, false);
            TextView tvNombre = card.findViewById(R.id.tvNombreActividad);
            TextView tvFechas = card.findViewById(R.id.tvFechas);
            TextView tvLugar = card.findViewById(R.id.tvLugar);
            TextView tvEstado = card.findViewById(R.id.tvEstado);
            TextView tvInicial = card.findViewById(R.id.tvInicialActividad);
            String nombre = act.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                tvNombre.setText(nombre);
                tvInicial.setText(nombre.substring(0, 1).toUpperCase());
            } else {
                tvNombre.setText("Sin nombre");
                tvInicial.setText("?");
            }
            String textoFechas;
            if (act.getFechaInicio() != null) {
                String inicio = timeFormat.format(act.getFechaInicio().toDate());
                String fin = (act.getFechaFin() != null) ? timeFormat.format(act.getFechaFin().toDate()) : "??";
                textoFechas = inicio + " - " + fin + " hrs";
            } else {
                textoFechas = "--:--";
            }
            tvFechas.setText(textoFechas);
            cargarNombreLugar(act, tvLugar);
            String estado = (act.getEstado() != null) ? act.getEstado() : "";
            tvEstado.setText(estado);
            card.setOnClickListener(v -> {
                if (act.getId() != null) {
                    Intent i = new Intent(this, DetActActivity.class);
                    i.putExtra("actividadId", act.getId());
                    startActivity(i);
                }
            });
            llEventos.addView(card);
        }
    }

    private void cargarNombreLugar(Actividad act, TextView tvTarget) {
        DocumentReference ref = act.getLugarId();
        if (ref == null) { tvTarget.setText("Lugar no especificado"); return; }
        tvTarget.setText("Cargando...");
        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String nombre = doc.getString("descripcion");
                if (nombre == null) nombre = doc.getString("nombre");
                tvTarget.setText(nombre != null ? nombre : "Sin nombre");
            } else { tvTarget.setText("Lugar desconocido"); }
        }).addOnFailureListener(e -> tvTarget.setText("Error"));
    }

    private String capitalizeFirst(String s) { return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase(esCL) + s.substring(1); }
    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }
    private int getId(String name) { return getResources().getIdentifier(name, "id", getPackageName()); }
    private void bindClickByIdOrText(String idName, String fallbackText, Runnable action) {
        boolean bound = false;
        int id = getId(idName);
        if (id != 0) { View v = findViewById(id); if (v != null) { v.setOnClickListener(x -> action.run()); bound = true; } }
        if (!bound) bindByText(fallbackText, action);
    }
    private void bindByText(String text, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayList<View> found = new ArrayList<>();
        root.findViewsWithText(found, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : found) v.setOnClickListener(x -> action.run());
    }
    private void bindEventById(String idName) {
        int id = getId(idName);
        if (id == 0) return;
        View v = findViewById(id);
        if (v instanceof TextView) { v.setOnClickListener(c -> { String s = ((TextView) v).getText().toString(); Intent i = new Intent(this, DetActActivity.class); i.putExtra("event_text", s); startActivity(i); }); }
    }
    private void attachEventClickers() {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayDeque<View> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            View v = stack.pop();
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) stack.push(g.getChildAt(i));
            }
            if (v instanceof TextView) {
                CharSequence cs = ((TextView) v).getText();
                if (cs != null) {
                    String s = cs.toString();
                    if (s.matches("\\d{1,2}/\\d{1,2}/\\d{4} - .+ - \\d{1,2}:\\d{2}")) {
                        v.setOnClickListener(c -> { Intent i = new Intent(this, DetActActivity.class); i.putExtra("event_text", s); startActivity(i); });
                    }
                }
            }
        }
    }

    // Logout
    @Override public void showLogoutSuccessMessage(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    @Override public void navigateToLogin() { Intent i = new Intent(AgndSemActivity.this, LogInActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); finish(); }
    @Override public Context getContext() { return getApplicationContext(); }
    @Override public void onLogoutSuccess() { showLogoutSuccessMessage("Sesión cerrada exitosamente."); navigateToLogin(); }
    @Override public void onLogoutFailure(String message) { showLogoutSuccessMessage("Error al cerrar sesión: " + message); }
}