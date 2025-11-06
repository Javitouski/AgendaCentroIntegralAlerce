package prog.android.centroalr.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import prog.android.centroalr.R;
import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.AuthModel;

public class AgendMensActivity extends AppCompatActivity implements LogoutView {

    // Logout MVC
    private TextView btnLogout;
    private LogoutController controller;
    private AuthModel model;

    // Calendario
    private YearMonth shownMonth;
    private TextView monthTitle;
    private View prevBtn, nextBtn;
    private GridLayout grid;

    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter monthFmt =
            DateTimeFormatter.ofPattern("MMMM yyyy", esCL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agend_mens);

        // --- Logout ---
        model = new AuthModel();
        controller = new LogoutController(this, model);
        btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> controller.onLogoutClicked());

        // --- Header / flechas ---
        monthTitle = findViewById(R.id.monthYearTextView);
        prevBtn = findViewById(R.id.btn_prev_month);
        nextBtn = findViewById(R.id.btn_next_month);

        // --- ir a perfil ---
        View profile = findViewById(R.id.profile_icon);
        if (profile != null) {
            profile.setOnClickListener(v ->
                    startActivity(new Intent(AgendMensActivity.this, PerfilActivity.class)));
        }

        if (prevBtn != null) prevBtn.setOnClickListener(v -> { shownMonth = shownMonth.minusMonths(1); updateAndRender(); });
        if (nextBtn != null) nextBtn.setOnClickListener(v -> { shownMonth = shownMonth.plusMonths(1);  updateAndRender(); });

        // Doble tap en el título => volver a mes actual
        if (monthTitle != null) {
            monthTitle.setOnClickListener(new View.OnClickListener() {
                long last = 0;
                @Override public void onClick(View v) {
                    long t = System.currentTimeMillis();
                    if (t - last < 350) { shownMonth = YearMonth.now(); updateAndRender(); }
                    last = t;
                }
            });
        }

        // Tap en el texto “Agenda Mensual” => abrir semanal
        bindClickByText("Agenda Mensual", () -> {
            startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class));
        });

        // Swipe izquierda/derecha para navegar meses
        enableSwipeNavigation();

        grid = findViewById(R.id.calendar_grid);

        // Mes actual
        shownMonth = YearMonth.now();
        updateAndRender();
    }

    private void updateAndRender() {
        // Título mes en español (primera letra mayúscula)
        if (monthTitle != null) {
            String title = shownMonth.format(monthFmt);
            if (!title.isEmpty()) {
                title = title.substring(0,1).toUpperCase(esCL) + title.substring(1);
            }
            monthTitle.setText(title);
        }
        renderMonth(shownMonth);
    }

    /**
     * Renderiza el mes creando las 42 celdas desde cero.
     * No dependemos de TextViews preexistentes, así evitamos problemas de estilos/IDs.
     */
    private void renderMonth(YearMonth ym) {
        if (grid == null) return;

        // Limpiamos TODO y configuramos 6x7
        grid.removeAllViews();
        grid.setRowCount(6);
        grid.setColumnCount(7);

        // Cálculo de celdas
        final int firstDow = ym.atDay(1).getDayOfWeek().getValue(); // L=1..D=7
        final int offset   = (firstDow + 6) % 7;                    // Lunes->0
        final int daysIn   = ym.lengthOfMonth();

        final YearMonth prev = ym.minusMonths(1);
        final int prevLen    = prev.lengthOfMonth();

        final LocalDate today = LocalDate.now();

        // Tamaños/márgenes
        int cellMinHeight = dp(44);
        int cellMargin = dp(4);

        for (int i = 0; i < 42; i++) {
            int row = i / 7;
            int col = i % 7;

            // Contenedor para facilitar futuro: tags/eventos/etc.
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(row, 1f), GridLayout.spec(col, 1f)
            );
            lp.width = 0;                  // weight hace que ocupe 1/7 del ancho
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);

            // Crea el TextView del número
            TextView tv = new TextView(this);
            tv.setLayoutParams(lp);
            tv.setMinHeight(cellMinHeight);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(Typeface.DEFAULT);

            // Determina número y si pertenece al mes actual
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

            tv.setText(String.valueOf(dayNumber));
            tv.setAlpha(inCurrent ? 1f : 0.45f);

            LocalDate cellDate = (inCurrent ? ym : (i < offset ? prev : ym.plusMonths(1)))
                    .atDay(dayNumber);
            if (cellDate.equals(today)) {
                tv.setTypeface(Typeface.DEFAULT_BOLD);
            }

            // (Opcional) click del día para abrir semanal con esa fecha
            // tv.setOnClickListener(v -> {
            //     Intent it = new Intent(this, AgndSemActivity.class);
            //     it.putExtra("selected_date", cellDate.toString());
            //     startActivity(it);
            // });

            grid.addView(tv);
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
        java.util.ArrayList<View> found = new java.util.ArrayList<>();
        root.findViewsWithText(found, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : found) v.setOnClickListener(x -> action.run());
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    // ==== LogoutView ====
    @Override public void showLogoutSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override public void navigateToLogin() {
        Intent intent = new Intent(AgendMensActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
