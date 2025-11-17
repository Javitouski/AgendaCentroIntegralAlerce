package prog.android.centroalr.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import prog.android.centroalr.R;
import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.AuthModel;

public class AgendMensActivity extends AppCompatActivity implements LogoutView {

    // Logout MVC
    private TextView btnLogout;
    private LogoutController controller;
    private AuthModel model;

    // Calendario
    private YearMonth shownMonth;
    private LocalDate selectedDate; // día actualmente seleccionado

    private TextView monthTitle;
    private View prevBtn, nextBtn;
    private GridLayout grid;
    private LinearLayout llEventosMensuales;

    // Firebase
    private FirebaseFirestore db;
    private final Map<LocalDate, List<Actividad>> eventosMes = new HashMap<>();
    private final ZoneId zone = ZoneId.systemDefault();

    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter monthFmt =
            DateTimeFormatter.ofPattern("MMMM yyyy", esCL);
    private final DateTimeFormatter headerDayFmt =
            DateTimeFormatter.ofPattern("d/M/yyyy", esCL);

    private final java.text.SimpleDateFormat fechaListaFormat =
            new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agend_mens);

        // --- Logout ---
        model = new AuthModel();
        controller = new LogoutController(this, model);
        btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> controller.onLogoutClicked());

        // --- Firebase ---
        db = FirebaseFirestore.getInstance();

        // --- Header / flechas / grid / eventos ---
        monthTitle = findViewById(R.id.monthYearTextView);
        prevBtn = findViewById(R.id.btn_prev_month);
        nextBtn = findViewById(R.id.btn_next_month);
        grid = findViewById(R.id.calendar_grid);
        llEventosMensuales = findViewById(R.id.llEventosMensuales);

        // --- ir a perfil ---
        View profile = findViewById(R.id.profile_icon);
        if (profile != null) {
            profile.setOnClickListener(v ->
                    startActivity(new Intent(AgendMensActivity.this, PerfilActivity.class)));
        }

        // --- ir a notificaciones ---
        View btnNotifications = findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(AgendMensActivity.this, NotificationsActivity.class)));
        }

        // Navegación con flechas
        if (prevBtn != null) {
            prevBtn.setOnClickListener(v -> {
                shownMonth = shownMonth.minusMonths(1);
                updateAndRender();
            });
        }
        if (nextBtn != null) {
            nextBtn.setOnClickListener(v -> {
                shownMonth = shownMonth.plusMonths(1);
                updateAndRender();
            });
        }

        // Doble tap en el título => volver a mes actual
        if (monthTitle != null) {
            monthTitle.setOnClickListener(new View.OnClickListener() {
                long last = 0;
                @Override public void onClick(View v) {
                    long t = System.currentTimeMillis();
                    if (t - last < 350) {
                        selectedDate = LocalDate.now();
                        shownMonth = YearMonth.from(selectedDate);
                        updateAndRender();
                    }
                    last = t;
                }
            });
        }

        // Tap en “Agenda Mensual” => abrir semanal (sin fecha específica)
        bindClickByText("Agenda Mensual", () -> {
            startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class));
        });

        // Swipe izquierda/derecha para navegar meses
        enableSwipeNavigation();

        // Inicial: hoy
        selectedDate = LocalDate.now();
        shownMonth = YearMonth.from(selectedDate);
        updateAndRender();
    }

    /** Actualiza el título y dispara la carga de actividades del mes. */
    private void updateAndRender() {
        // Título mes en español (primera letra mayúscula)
        if (monthTitle != null) {
            String title = shownMonth.format(monthFmt);
            if (!title.isEmpty()) {
                title = title.substring(0,1).toUpperCase(esCL) + title.substring(1);
            }
            monthTitle.setText(title);
        }
        loadMonthEvents(shownMonth);
    }

    /** Carga las actividades del mes mostrado usando fechaInicio en Firestore. */
    private void loadMonthEvents(YearMonth ym) {
        if (db == null) {
            if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
                selectedDate = ym.atDay(1);
            }
            renderMonth(ym);
            renderDayEvents(selectedDate);
            return;
        }

        eventosMes.clear();

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1); // exclusivo

        Timestamp startTs = new Timestamp(java.util.Date.from(start.atStartOfDay(zone).toInstant()));
        Timestamp endTs   = new Timestamp(java.util.Date.from(end.atStartOfDay(zone).toInstant()));

        db.collection("actividades")
                .whereGreaterThanOrEqualTo("fechaInicio", startTs)
                .whereLessThan("fechaInicio", endTs)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Actividad act = doc.toObject(Actividad.class);
                        if (act == null || act.getFechaInicio() == null) continue;

                        // Guardar id del documento
                        act.setId(doc.getId());

                        LocalDate diaActividad = act.getFechaInicio().toDate()
                                .toInstant()
                                .atZone(zone)
                                .toLocalDate();

                        List<Actividad> list = eventosMes.get(diaActividad);
                        if (list == null) {
                            list = new ArrayList<>();
                            eventosMes.put(diaActividad, list);
                        }
                        list.add(act);
                    }

                    if (!ym.equals(shownMonth)) return; // usuario cambió de mes rápido

                    if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
                        selectedDate = ym.atDay(1);
                    }
                    renderMonth(shownMonth);
                    renderDayEvents(selectedDate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al cargar actividades del mes",
                            Toast.LENGTH_SHORT).show();
                    eventosMes.clear();

                    if (!ym.equals(shownMonth)) return;

                    if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
                        selectedDate = ym.atDay(1);
                    }
                    renderMonth(shownMonth);
                    renderDayEvents(selectedDate);
                });
    }

    /**
     * Renderiza el mes creando las 42 celdas desde cero.
     * Marca con un punto verde los días con actividades y permite seleccionar un día.
     */
    private void renderMonth(YearMonth ym) {
        if (grid == null) return;

        grid.removeAllViews();
        grid.setRowCount(6);
        grid.setColumnCount(7);

        final int firstDow = ym.atDay(1).getDayOfWeek().getValue(); // L=1..D=7
        final int offset   = (firstDow + 6) % 7;                    // Lunes->0
        final int daysIn   = ym.lengthOfMonth();

        final YearMonth prev = ym.minusMonths(1);
        final int prevLen    = prev.lengthOfMonth();

        final LocalDate today = LocalDate.now();

        int cellMinHeight = dp(44);
        int cellMargin = dp(4);

        // Aseguramos que la fecha seleccionada esté en el mes mostrado
        if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
            selectedDate = ym.atDay(1);
        }

        for (int i = 0; i < 42; i++) {
            int row = i / 7;
            int col = i % 7;

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(row, 1f), GridLayout.spec(col, 1f)
            );
            lp.width = 0;
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);

            TextView tv = new TextView(this);
            tv.setLayoutParams(lp);
            tv.setMinHeight(cellMinHeight);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(Typeface.DEFAULT);

            int dayNumber;
            boolean inCurrent;
            if (i < offset) {
                dayNumber = prevLen - (offset - 1 - i); // arrastre anterior
                inCurrent = false;
            } else if (i < offset + daysIn) {
                dayNumber = (i - offset) + 1;           // mes actual
                inCurrent = true;
            } else {
                dayNumber = (i - (offset + daysIn)) + 1; // arrastre siguiente
                inCurrent = false;
            }

            LocalDate cellDate = (inCurrent ? ym : (i < offset ? prev : ym.plusMonths(1)))
                    .atDay(dayNumber);

            List<Actividad> actividadesDelDia = eventosMes.get(cellDate);
            boolean hasEvents = (actividadesDelDia != null && !actividadesDelDia.isEmpty());

            String baseText = String.valueOf(dayNumber);
            if (hasEvents) {
                String text = baseText + " •";
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
                tv.setText(baseText);
            }

            tv.setAlpha(inCurrent ? 1f : 0.45f);

            if (cellDate.equals(today)) {
                tv.setTypeface(Typeface.DEFAULT_BOLD);
            }

            // Resaltar día seleccionado
            if (cellDate.equals(selectedDate)) {
                tv.setBackgroundResource(R.drawable.bg_dia_selected);
                tv.setTextColor(Color.parseColor("#066D0A"));
            }

            // Click del día => cambiar seleccionado y mostrar actividades abajo
            tv.setOnClickListener(v -> {
                selectedDate = cellDate;
                renderMonth(shownMonth);
                renderDayEvents(selectedDate);
            });

            grid.addView(tv);
        }
    }

    /** Muestra en la parte inferior las actividades del día seleccionado. */
    private void renderDayEvents(LocalDate date) {
        if (llEventosMensuales == null || date == null) return;

        llEventosMensuales.removeAllViews();

        List<Actividad> lista = eventosMes.get(date);
        boolean hayActividades = (lista != null && !lista.isEmpty());

        // Cabecera
        TextView header = new TextView(this);
        String titulo = (hayActividades ? "Actividades para " : "No hay actividades para ")
                + date.format(headerDayFmt);
        header.setText(titulo);
        header.setTextColor(Color.parseColor("#066D0A"));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        header.setPadding(dp(12), dp(8), dp(12), dp(16));
        llEventosMensuales.addView(header);

        if (!hayActividades) return;

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Actividad act : lista) {
            View card = inflater.inflate(R.layout.item_actividad, llEventosMensuales, false);

            TextView tvNombre = card.findViewById(R.id.tvNombreActividad);
            TextView tvFechas = card.findViewById(R.id.tvFechas);
            TextView tvLugar = card.findViewById(R.id.tvLugar);
            TextView tvEstado = card.findViewById(R.id.tvEstado);
            TextView tvInicial = card.findViewById(R.id.tvInicialActividad);

            String nombre = act.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                tvNombre.setText(nombre);
                tvInicial.setText(nombre.trim().substring(0, 1).toUpperCase());
            } else {
                tvNombre.setText("Sin nombre");
                tvInicial.setText("?");
            }

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

            tvLugar.setText(nombreLugarLegible(act));

            String estado = (act.getEstado() != null && !act.getEstado().isEmpty())
                    ? act.getEstado()
                    : "SIN ESTADO";
            tvEstado.setText(estado);

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

            llEventosMensuales.addView(card);
        }
    }

    private void enableSwipeNavigation() {
        final GestureDetector gd = new GestureDetector(
                this,
                new GestureDetector.SimpleOnGestureListener() {
                    private static final int T = 80, V = 80;
                    @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        if (e1 == null || e2 == null) return false;
                        float dx = e2.getX() - e1.getX();
                        if (Math.abs(dx) > T && Math.abs(vx) > V) {
                            shownMonth = (dx < 0) ? shownMonth.plusMonths(1) : shownMonth.minusMonths(1);
                            updateAndRender();
                            return true;
                        }
                        return false;
                    }
                });

        View root = findViewById(android.R.id.content);
        if (root != null) {
            root.setOnTouchListener((v, ev) -> gd.onTouchEvent(ev));
        }
    }

    /** Busca una vista por texto visible y le asigna una acción. */
    private void bindClickByText(String text, Runnable action) {
        View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayList<View> found = new ArrayList<>();
        root.findViewsWithText(found, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : found) v.setOnClickListener(x -> action.run());
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    /** Igual que en la semanal: hace el lugar más legible. */
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

    // ==== LogoutView ====
    @Override
    public void showLogoutSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(AgendMensActivity.this, LogInActivity.class);
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
