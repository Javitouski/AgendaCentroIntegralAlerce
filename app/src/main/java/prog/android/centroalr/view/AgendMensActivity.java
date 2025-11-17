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
import com.google.firebase.firestore.DocumentReference;
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
    private final DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", esCL);
    private final DateTimeFormatter headerDayFmt = DateTimeFormatter.ofPattern("d/M/yyyy", esCL);

    // Formato de hora
    private final java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

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

        // --- UI ---
        monthTitle = findViewById(R.id.monthYearTextView);
        prevBtn = findViewById(R.id.btn_prev_month);
        nextBtn = findViewById(R.id.btn_next_month);
        grid = findViewById(R.id.calendar_grid);
        llEventosMensuales = findViewById(R.id.llEventosMensuales);

        View profile = findViewById(R.id.profile_icon);
        if (profile != null) {
            profile.setOnClickListener(v ->
                    startActivity(new Intent(AgendMensActivity.this, PerfilActivity.class)));
        }

        View btnNotifications = findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(AgendMensActivity.this, NotificationsActivity.class)));
        }

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

        bindClickByText("Agenda Mensual", () -> {
            startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class));
        });

        enableSwipeNavigation();

        // Inicial
        selectedDate = LocalDate.now();
        shownMonth = YearMonth.from(selectedDate);
        updateAndRender();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAndRender();
    }

    private void updateAndRender() {
        if (monthTitle != null) {
            String title = shownMonth.format(monthFmt);
            if (!title.isEmpty()) {
                title = title.substring(0,1).toUpperCase(esCL) + title.substring(1);
            }
            monthTitle.setText(title);
        }
        loadMonthEvents(shownMonth);
    }

    // === AQUÍ ESTÁ LA CORRECCIÓN ===
    private void loadMonthEvents(YearMonth ym) {
        if (db == null) {
            if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
                selectedDate = ym.atDay(1);
            }
            renderMonth(ym);
            renderDayEvents(selectedDate);
            return;
        }

        // NO LIMPIAR AQUÍ (eventosMes.clear()) -> ESTO CAUSA DUPLICADOS AL CARGAR RÁPIDO

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        Timestamp startTs = new Timestamp(java.util.Date.from(start.atStartOfDay(zone).toInstant()));
        Timestamp endTs   = new Timestamp(java.util.Date.from(end.atStartOfDay(zone).toInstant()));

        // Leemos 'citas'
        db.collection("citas")
                .whereGreaterThanOrEqualTo("fechaInicio", startTs)
                .whereLessThan("fechaInicio", endTs)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // LIMPIAR AQUÍ: Solo cuando tenemos los datos nuevos en la mano
                    eventosMes.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Actividad act = doc.toObject(Actividad.class);
                        if (act == null || act.getFechaInicio() == null) continue;

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

                    if (!ym.equals(shownMonth)) return;

                    if (selectedDate == null || !YearMonth.from(selectedDate).equals(ym)) {
                        selectedDate = ym.atDay(1);
                    }
                    renderMonth(shownMonth);
                    renderDayEvents(selectedDate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar agenda", Toast.LENGTH_SHORT).show();
                    eventosMes.clear(); // En error limpiamos para no mostrar datos viejos
                    renderMonth(shownMonth);
                    renderDayEvents(selectedDate);
                });
    }

    private void renderMonth(YearMonth ym) {
        if (grid == null) return;

        grid.removeAllViews();
        grid.setRowCount(6);
        grid.setColumnCount(7);

        final int firstDow = ym.atDay(1).getDayOfWeek().getValue();
        final int offset   = (firstDow + 6) % 7;
        final int daysIn   = ym.lengthOfMonth();

        final YearMonth prev = ym.minusMonths(1);
        final int prevLen    = prev.lengthOfMonth();

        final LocalDate today = LocalDate.now();

        int cellMinHeight = dp(44);
        int cellMargin = dp(4);

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
                dayNumber = prevLen - (offset - 1 - i);
                inCurrent = false;
            } else if (i < offset + daysIn) {
                dayNumber = (i - offset) + 1;
                inCurrent = true;
            } else {
                dayNumber = (i - (offset + daysIn)) + 1;
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

            if (cellDate.equals(selectedDate)) {
                tv.setBackgroundResource(R.drawable.bg_dia_selected);
                tv.setTextColor(Color.parseColor("#066D0A"));
            }

            tv.setOnClickListener(v -> {
                selectedDate = cellDate;
                renderMonth(shownMonth);
                renderDayEvents(selectedDate);
            });

            grid.addView(tv);
        }
    }

    private void renderDayEvents(LocalDate date) {
        if (llEventosMensuales == null || date == null) return;

        llEventosMensuales.removeAllViews();

        List<Actividad> lista = eventosMes.get(date);
        boolean hayActividades = (lista != null && !lista.isEmpty());

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

            // HORA
            String textoFechas;
            if (act.getFechaInicio() != null) {
                String inicio = timeFormat.format(act.getFechaInicio().toDate());
                String fin = (act.getFechaFin() != null) ? timeFormat.format(act.getFechaFin().toDate()) : "??";
                textoFechas = inicio + " - " + fin + " hrs";
            } else {
                textoFechas = "--:--";
            }
            tvFechas.setText(textoFechas);

            // LUGAR DINÁMICO
            cargarNombreLugar(act, tvLugar);

            String estado = (act.getEstado() != null && !act.getEstado().isEmpty())
                    ? act.getEstado()
                    : "SIN ESTADO";
            tvEstado.setText(estado);

            card.setOnClickListener(v -> {
                if (act.getId() != null) {
                    Intent i = new Intent(this, DetActActivity.class);
                    i.putExtra("actividadId", act.getId());
                    startActivity(i);
                }
            });

            llEventosMensuales.addView(card);
        }
    }

    // Helper para cargar nombre lugar
    private void cargarNombreLugar(Actividad act, TextView tvTarget) {
        DocumentReference ref = act.getLugarId();
        if (ref == null) {
            tvTarget.setText("Lugar no especificado");
            return;
        }
        tvTarget.setText("Cargando...");
        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String nombre = doc.getString("descripcion");
                if (nombre == null) nombre = doc.getString("nombre");
                tvTarget.setText(nombre != null ? nombre : "Sin nombre");
            } else {
                tvTarget.setText("Lugar desconocido");
            }
        }).addOnFailureListener(e -> tvTarget.setText("Error"));
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