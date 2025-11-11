package prog.android.centroalr.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import prog.android.centroalr.R;
import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.AuthModel;

public class AgndSemActivity extends AppCompatActivity implements LogoutView {

    // --- Logout MVC ---
    private TextView btnCerrarSesion;
    private LogoutController logoutController;
    private AuthModel authModel;

    // --- Semana / día seleccionado ---
    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter titleFmt =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", esCL);
    private final DateTimeFormatter shortFmt =
            DateTimeFormatter.ofPattern("d/M/yyyy", esCL);

    private LocalDate weekStart;     // Lunes de la semana actual
    private LocalDate selectedDate;  // Día actualmente seleccionado (dentro de esa semana)

    // UI
    private TextView tvMes;          // Título superior (mes/fecha)
    private TextView[] dayBtns = new TextView[7]; // LUN..DOM en el header
    private LinearLayout llEventos;  // Contenedor de eventos del día
    private int selectedIndex = 0;   // 0..6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agnd_sem);

        // ====== Logout wiring ======
        authModel = new AuthModel();
        logoutController = new LogoutController(this, authModel);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> logoutController.onLogoutClicked());
        }

        // ====== Icono usuario -> Perfil ======
        ImageView ivUser = findViewById(R.id.ivUserAvatar);
        if (ivUser != null) {
            ivUser.setOnClickListener(v ->
                    startActivity(new Intent(AgndSemActivity.this, PerfilActivity.class))
            );
        }

        // ====== Bottom nav por IDs ======
        View btnPerfil = findViewById(R.id.btnPerfil);
        if (btnPerfil != null) btnPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        View fabInicio = findViewById(R.id.fabInicio);
        if (fabInicio != null) fabInicio.setOnClickListener(v ->
                startActivity(new Intent(this, AgendMensActivity.class)));

        View btnCrearActividad = findViewById(R.id.btnCrearActividad);
        if (btnCrearActividad != null) btnCrearActividad.setOnClickListener(v ->
                startActivity(new Intent(this, CrearActActivity.class)));

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

        // Cálculo de semana base (desde extra o hoy)
        LocalDate base = LocalDate.now();
        if (getIntent() != null && getIntent().hasExtra("selected_date")) {
            try {
                base = LocalDate.parse(getIntent().getStringExtra("selected_date"));
            } catch (Exception ignore) {}
        }
        weekStart = startOfWeek(base);    // lunes
        selectedDate = base;
        selectedIndex = selectedDate.getDayOfWeek().getValue() - 1; // L=1..D=7 -> 0..6

        // Clickers de días
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            if (dayBtns[i] != null) {
                dayBtns[i].setOnClickListener(v -> selectDay(idx));
            }
        }

        // Render inicial
        applySelectionUi();
        loadDayEvents(selectedDate);

        // ====== (Opcional) Fallbacks a texto/ID "dinámico" existentes ======
        bindClickByIdOrText("rn6aagqs6pq8", "Perfil",
                () -> startActivity(new Intent(this, PerfilActivity.class)));
        bindClickByIdOrText("r3pjyjmbkmo9", "Inicio",
                () -> startActivity(new Intent(this, AgendMensActivity.class)));
        bindClickByIdOrText("rqwq9k1hp05", "Crear Actividad",
                () -> startActivity(new Intent(this, CrearActActivity.class)));

        bindEventById("rtumfhvig6h");
        bindEventById("roa0repy8oae");
        attachEventClickers();
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

    /** Actualiza estilos de botones de días y el título con la fecha seleccionada. */
    private void applySelectionUi() {
        // Título con fecha (capitalizando primera letra)
        if (tvMes != null) {
            String t = selectedDate.format(titleFmt);
            tvMes.setText(capitalizeFirst(t));
        }

        // Resaltar día seleccionado
        for (int i = 0; i < 7; i++) {
            TextView tv = dayBtns[i];
            if (tv == null) continue;

            if (i == selectedIndex) {
                // Seleccionado: mismo fondo que en tu XML (bg_dia_selected)
                tv.setBackgroundResource(R.drawable.bg_dia_selected);
                tv.setTextColor(Color.parseColor("#066D0A"));
            } else {
                // No seleccionado: transparente
                tv.setBackground(null);
                tv.setTextColor(Color.parseColor("#066D0A"));
            }
        }
    }

    /** Carga (o maqueta) los eventos del día en llEventos. */
    private void loadDayEvents(LocalDate date) {
        if (llEventos == null) return;
        llEventos.removeAllViews();

        // TODO Integra tu fuente real (BD/Firestore/REST). Dejo placeholder "sin actividades".
        TextView empty = new TextView(this);
        empty.setText("No hay actividades para " + date.format(shortFmt));
        empty.setTextColor(Color.parseColor("#066D0A"));
        empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        empty.setPadding(dp(12), dp(8), dp(12), dp(16));
        llEventos.addView(empty);
    }

    // ================== HELPERS ==================

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(esCL) + s.substring(1);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private void bindClickByIdOrText(String idName, String fallbackText, Runnable action) {
        boolean bound = false;
        int id = getId(idName);
        if (id != 0) {
            View v = findViewById(id);
            if (v != null) { v.setOnClickListener(x -> action.run()); bound = true; }
        }
        if (!bound) bindByText(fallbackText, action);
    }

    private void bindByText(String text, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        java.util.ArrayList<View> out = new java.util.ArrayList<>();
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

        java.util.ArrayDeque<View> stack = new java.util.ArrayDeque<>();
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
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(AgndSemActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
