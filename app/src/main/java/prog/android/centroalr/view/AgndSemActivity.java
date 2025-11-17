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

public class AgndSemActivity extends AppCompatActivity implements LogoutView {

    // --- Logout MVC ---
    private TextView btnCerrarSesion;
    private LogoutController logoutController;
    private AuthModel authModel;

    // Usuario actual
    private Usuario usuarioActual;

    // --- Semana / día seleccionado ---
    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter titleFmt =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", esCL);
    private final DateTimeFormatter shortFmt =
            DateTimeFormatter.ofPattern("d/M/yyyy", esCL);

    private LocalDate weekStart;     // Lunes de la semana actual
    private LocalDate selectedDate;  // Día actualmente seleccionado
    private int selectedIndex = 0;   // 0..6

    // UI
    private TextView tvMes;
    private TextView[] dayBtns = new TextView[7];
    private LinearLayout llEventos;
    private ImageButton btnPrevWeek;
    private ImageButton btnNextWeek;

    // --- Firebase / datos ---
    private FirebaseFirestore db;
    // Mapa: día -> lista de actividades de ese día
    private final Map<LocalDate, List<Actividad>> eventosSemana = new HashMap<>();
    private final SimpleDateFormat fechaListaFormat =
            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agnd_sem);

        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);

        // ====== Logout wiring ======
        authModel = new AuthModel();
        logoutController = new LogoutController(this, authModel);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> logoutController.onLogoutClicked());
        }

        // ====== Usuario actual desde MyApplication ======
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // ====== Firebase ======
        db = FirebaseFirestore.getInstance();

        // ====== Icono usuario -> Perfil ======
        ImageView ivUser = findViewById(R.id.ivUserAvatar);
        if (ivUser != null) {
            ivUser.setOnClickListener(v ->
                    startActivity(new Intent(AgndSemActivity.this, PerfilActivity.class))
            );
        }

        // ====== Bottom nav ======
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

        // ====== Botón Crear Actividad según permisos ======
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

        // ====== Referencias vista semanal ======
        tvMes = findViewById(R.id.tvMes);
        llEventos = findViewById(R.id.llEventos);

        dayBtns[0] = findViewById(R.id.btnLunes);
        dayBtns[1] = findViewById(R.id.btnMartes);
        dayBtns[2] = findViewById(R.id.btnMiercoles);
        dayBtns[3] = findViewById(R.id.btnJueves);
        dayBtns[4] = findViewById(R.id.btnViernes);
        dayBtns[5] = findViewById(R.id.btnSabado);
        dayBtns[6] = findViewById(R.id.btnDomingo);

        // ====== Semana base (desde Intent o hoy) ======
        LocalDate base = LocalDate.now();
        if (getIntent() != null && getIntent().hasExtra("selected_date")) {
            try {
                base = LocalDate.parse(getIntent().getStringExtra("selected_date"));
            } catch (Exception ignore) {}
        }

        weekStart = startOfWeek(base);  // lunes
        selectedDate = base;
        selectedIndex = selectedDate.getDayOfWeek().getValue() - 1; // L=1..D=7 -> 0..6

        // Clickers de los 7 días
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            if (dayBtns[i] != null) {
                dayBtns[i].setOnClickListener(v -> selectDay(idx));
            }
        }

        // Navegación de semanas
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

        // Render inicial (mientras no llegan datos)
        applySelectionUi();
        loadDayEvents(selectedDate);

        // ====== Fallbacks de texto/ID del diseño original ======
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

        // Cargar las actividades reales de la semana
        loadWeekEvents();
    }

    // ================== LÓGICA DE SEMANA ==================

    private LocalDate startOfWeek(LocalDate date) {
        int dow = date.getDayOfWeek().getValue(); // L=1..D=7
        return date.minusDays(dow - 1L);          // lunes
    }

    private void selectDay(int idx) {
        selectedIndex = idx;
        selectedDate = weekStart.plusDays(idx);
        applySelectionUi();
        loadDayEvents(selectedDate);
    }

    // ================== CARGA DESDE FIRESTORE ==================

    /**
     * Carga todas las actividades cuya fechaInicio cae dentro de la semana
     * [weekStart, weekStart + 7) y las guarda en eventosSemana.
     */
    private void loadWeekEvents() {
        if (db == null) return;

        eventosSemana.clear();

        LocalDate monday = weekStart;
        LocalDate nextMonday = weekStart.plusDays(7);

        ZoneId zone = ZoneId.systemDefault();
        Date mondayDate = Date.from(monday.atStartOfDay(zone).toInstant());
        Date nextMondayDate = Date.from(nextMonday.atStartOfDay(zone).toInstant());

        Timestamp mondayTs = new Timestamp(mondayDate);
        Timestamp nextMondayTs = new Timestamp(nextMondayDate);

        db.collection("actividades")                                // colección
                .whereGreaterThanOrEqualTo("fechaInicio", mondayTs) // campo de fecha
                .whereLessThan("fechaInicio", nextMondayTs)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Actividad act = doc.toObject(Actividad.class);
                        if (act == null || act.getFechaInicio() == null) continue;

                        // Guardamos el ID del documento de Firestore en la Actividad
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

                    // Refrescamos UI con los datos reales
                    applySelectionUi();          // puntos verdes
                    loadDayEvents(selectedDate); // actividades del día seleccionado
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al cargar actividades de la semana",
                            Toast.LENGTH_SHORT).show();
                    eventosSemana.clear();
                    applySelectionUi();
                    loadDayEvents(selectedDate);
                });
    }

    // ================== UI DE DÍAS Y EVENTOS ==================

    /** Actualiza título, estilo de los días y marca con un punto verde los días con actividades. */
    private void applySelectionUi() {
        // Título con fecha seleccionada
        if (tvMes != null && selectedDate != null) {
            String t = selectedDate.format(titleFmt);
            tvMes.setText(capitalizeFirst(t));
        }

        for (int i = 0; i < 7; i++) {
            TextView tv = dayBtns[i];
            if (tv == null) continue;

            // Texto base del día según strings.xml
            String baseLabel;
            switch (i) {
                case 0: baseLabel = getString(R.string.lunes); break;
                case 1: baseLabel = getString(R.string.martes); break;
                case 2: baseLabel = getString(R.string.miercoles); break;
                case 3: baseLabel = getString(R.string.jueves); break;
                case 4: baseLabel = getString(R.string.viernes); break;
                case 5: baseLabel = getString(R.string.sabado); break;
                case 6:
                default: baseLabel = getString(R.string.domingo); break;
            }

            // ¿Hay actividades ese día?
            LocalDate dia = weekStart.plusDays(i);
            List<Actividad> list = eventosSemana.get(dia);
            boolean hasEvents = (list != null && !list.isEmpty());

            if (hasEvents) {
                // Añadimos un "•" verde al final del texto
                String text = baseLabel + " •";
                SpannableString span = new SpannableString(text);
                int dotIndex = text.length() - 1;
                span.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#18990D")),
                        dotIndex,
                        dotIndex + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                tv.setText(span);
            } else {
                tv.setText(baseLabel);
            }

            // Resaltar día seleccionado
            if (i == selectedIndex) {
                tv.setBackgroundResource(R.drawable.bg_dia_selected);
                tv.setTextColor(Color.parseColor("#066D0A"));
            } else {
                tv.setBackground(null);
                tv.setTextColor(Color.parseColor("#066D0A"));
            }
        }
    }

    /**
     * Muestra en llEventos el listado de actividades del día indicado.
     * - Si no hay: "No hay actividades para X".
     * - Si hay: "Actividades para X" + tarjetas usando item_actividad.
     */
    private void loadDayEvents(LocalDate date) {
        if (llEventos == null || date == null) return;

        llEventos.removeAllViews();

        List<Actividad> lista = eventosSemana.get(date);
        boolean hayActividades = (lista != null && !lista.isEmpty());

        // Cabecera de texto
        TextView header = new TextView(this);
        String titulo = (hayActividades ? "Actividades para " : "No hay actividades para ")
                + date.format(shortFmt);
        header.setText(titulo);
        header.setTextColor(Color.parseColor("#066D0A"));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        header.setPadding(dp(12), dp(8), dp(12), dp(16));
        llEventos.addView(header);

        if (!hayActividades) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Actividad act : lista) {
            View card = inflater.inflate(R.layout.item_actividad, llEventos, false);

            TextView tvNombre = card.findViewById(R.id.tvNombreActividad);
            TextView tvFechas = card.findViewById(R.id.tvFechas);
            TextView tvLugar = card.findViewById(R.id.tvLugar);
            TextView tvEstado = card.findViewById(R.id.tvEstado);
            TextView tvInicial = card.findViewById(R.id.tvInicialActividad);

            // Nombre + inicial
            String nombre = act.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                tvNombre.setText(nombre);
                tvInicial.setText(nombre.trim().substring(0, 1).toUpperCase());
            } else {
                tvNombre.setText("Sin nombre");
                tvInicial.setText("?");
            }

            // Fechas (misma lógica que en ActividadesAdapter)
            String textoFechas;
            if (act.getFechaInicio() != null && act.getFechaFin() != null) {
                String inicio = fechaListaFormat.format(act.getFechaInicio().toDate());
                String fin = fechaListaFormat.format(act.getFechaFin().toDate());
                textoFechas = inicio + " - " + fin;
            } else if (act.getFechaInicio() != null) {
                textoFechas = fechaListaFormat.format(act.getFechaInicio().toDate());
            } else {
                textoFechas = "Fecha no definida";
            }
            tvFechas.setText(textoFechas);

            // Lugar legible
            tvLugar.setText(nombreLugarLegible(act));

            // Estado
            String estado = (act.getEstado() != null && !act.getEstado().isEmpty())
                    ? act.getEstado()
                    : "SIN ESTADO";
            tvEstado.setText(estado);

            // Click a detalle: aquí mandamos actividadId a DetActActivity
            card.setOnClickListener(v -> {
                if (act.getId() != null) {
                    Intent i = new Intent(this, DetActActivity.class);
                    i.putExtra("actividadId", act.getId());
                    startActivity(i);
                } else {
                    Toast.makeText(this,
                            "No se pudo obtener el ID de la actividad.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            llEventos.addView(card);
        }
    }

    // ================== HELPERS ==================

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(esCL) + s.substring(1);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    /** Mismo método que en ActividadesAdapter para mostrar el lugar legible. */
    private String nombreLugarLegible(Actividad actividad) {
        if (actividad.getLugarId() == null) {
            return "Lugar no especificado";
        }

        String id = actividad.getLugarId().getId(); // p.ej. "oficina", "salaMultiuso1", etc.

        switch (id) {
            case "oficina":
                return "Oficina principal del centro comunitario";
            case "salaMultiuso1":
                return "Sala multiuso 1";
            case "salaMultiuso2":
                return "Sala multiuso 2";
            default:
                String s = id.replace("_", " ").replace("-", " ");
                if (s.isEmpty()) return "Lugar no especificado";
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private void bindClickByIdOrText(String idName, String fallbackText, Runnable action) {
        boolean bound = false;
        int id = getId(idName);
        if (id != 0) {
            View v = findViewById(id);
            if (v != null) {
                v.setOnClickListener(x -> action.run());
                bound = true;
            }
        }
        if (!bound) bindByText(fallbackText, action);
    }

    private void bindByText(String text, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayList<View> out = new ArrayList<>();
        root.findViewsWithText(out, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : out) v.setOnClickListener(x -> action.run());
    }

    private void bindEventById(String idName) {
        int id = getId(idName);
        if (id == 0) return;
        View v = findViewById(id);
        if (v instanceof TextView) {
            v.setOnClickListener(click -> {
                String s = ((TextView) v).getText().toString();
                Intent i = new Intent(this, DetActActivity.class);
                i.putExtra("event_text", s);
                startActivity(i);
            });
        }
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
                        v.setOnClickListener(click -> {
                            Intent i = new Intent(this, DetActActivity.class);
                            i.putExtra("event_text", s);
                            startActivity(i);
                        });
                    }
                }
            }
        }
    }

    // ====== LogoutView ======

    @Override
    public void showLogoutSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(AgndSemActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onLogoutSuccess() {
        showLogoutSuccessMessage("Sesión cerrada exitosamente.");
        navigateToLogin();
    }

    @Override
    public void onLogoutFailure(String message) {
        showLogoutSuccessMessage("Error al cerrar sesión: " + message);
    }
}
