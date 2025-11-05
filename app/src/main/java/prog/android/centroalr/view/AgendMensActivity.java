package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import prog.android.centroalr.R; // <-- IMPORTANTE: R del paquete raíz
import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.AuthModel;

public class AgendMensActivity extends AppCompatActivity implements LogoutView {

    // --- Logout (lo que ya tenías) ---
    private TextView btnLogout;
    private LogoutController controller;
    private AuthModel model;

    // --- Calendario (nuevo) ---
    private YearMonth shownMonth;
    private TextView monthTitle; // se resuelve por id o por contenido
    private final Locale esCL = new Locale("es", "CL");
    private final DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", esCL);
    private final Pattern monthYearRegex = Pattern.compile(
            "(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+\\d{4}",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agend_mens);

        // ---- MVC logout (tal cual) ----
        model = new AuthModel();
        controller = new LogoutController(this, model);
        btnLogout = findViewById(getId("btn_logout"));
        if (btnLogout != null) btnLogout.setOnClickListener(v -> controller.onLogoutClicked());

        // ---- Navegación y calendario ----
        wireUi();                 // perfil, semanal, detalle por etiquetas
        setupMonthHeader();       // título dinámico + hoy
        bindPrevNextButtons();    // botones prev/next si existen
        enableSwipeNavigation();  // gestos izquierda/derecha
    }

    // =============================================================================================
    // ==========  CALENDARIO MENSUAL: TÍTULO DINÁMICO + NAVEGACIÓN DE MESES  ======================
    // =============================================================================================

    /** Resuelve el TextView del título y lo setea al mes actual. */
    private void setupMonthHeader() {
        // 1) Intento por IDs comunes
        monthTitle = findTextViewByAnyId("tv_month", "month_title", "title_month", "txt_month", "titulo_mes");

        // 2) Si no hay ID, busco cualquiera cuyo texto parezca "octubre 2025", etc.
        if (monthTitle == null) monthTitle = findFirstMonthYearTextView();

        // 3) Seteo mes actual
        shownMonth = YearMonth.now();
        updateMonthTitle();

        // 4) Doble tap en el título => volver a "hoy"
        if (monthTitle != null) {
            monthTitle.setOnClickListener(new View.OnClickListener() {
                private long lastTap = 0;
                @Override public void onClick(View v) {
                    long t = System.currentTimeMillis();
                    if (t - lastTap < 350) { // double tap
                        shownMonth = YearMonth.now();
                        updateMonthTitle();
                        Toast.makeText(AgendMensActivity.this, "Volviendo a este mes", Toast.LENGTH_SHORT).show();
                    }
                    lastTap = t;
                }
            });
        }

        // 5) Si hay texto "Hoy" en la pantalla, lo engancho también
        bindByTextContains("Hoy", () -> {
            shownMonth = YearMonth.now();
            updateMonthTitle();
        });
    }

    /** Botones prev/next si existen (IDs comunes + fallback por texto). */
    private void bindPrevNextButtons() {
        // Prev
        bindClickByIdOrText("btn_prev_month", "Anterior", this::goPrevMonth);
        bindClickByIdOrText("prev_month", "«", this::goPrevMonth);
        bindClickByIdOrText("arrow_back", "<", this::goPrevMonth);
        bindClickByIdOrText("ic_arrow_back", "<", this::goPrevMonth);

        // Next
        bindClickByIdOrText("btn_next_month", "Siguiente", this::goNextMonth);
        bindClickByIdOrText("next_month", "»", this::goNextMonth);
        bindClickByIdOrText("arrow_forward", ">", this::goNextMonth);
        bindClickByIdOrText("ic_arrow_forward", ">", this::goNextMonth);
    }

    /** Gestos izquierda/derecha para cambiar de mes sin depender de XML. */
    private void enableSwipeNavigation() {
        final GestureDetector gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int THRESHOLD = 80;
            private static final int VELOCITY = 80;
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > THRESHOLD && Math.abs(vx) > VELOCITY) {
                    if (dx < 0) goNextMonth(); else goPrevMonth();
                    return true;
                }
                return false;
            }
        });
        View root = findViewById(android.R.id.content);
        if (root != null) root.setOnTouchListener((v, ev) -> gd.onTouchEvent(ev));
    }

    private void goPrevMonth() { shownMonth = shownMonth.minusMonths(1); updateMonthTitle(); }
    private void goNextMonth() { shownMonth = shownMonth.plusMonths(1); updateMonthTitle(); }

    /** Actualiza el texto del título. Si no encontré título, intento reemplazar el que exista. */
    private void updateMonthTitle() {
        String title = capitalizeFirst(shownMonth.format(monthFmt));
        if (monthTitle != null) {
            monthTitle.setText(title);
            return;
        }
        // Fallback: busca cualquier TextView con "mes yyyy" y lo reemplaza
        View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayDeque<View> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            View v = st.pop();
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) st.push(g.getChildAt(i));
            }
            if (v instanceof TextView) {
                CharSequence cs = ((TextView) v).getText();
                if (cs != null && monthYearRegex.matcher(cs.toString()).matches()) {
                    ((TextView) v).setText(title);
                    break;
                }
            }
        }
    }

    private TextView findFirstMonthYearTextView() {
        View root = findViewById(android.R.id.content);
        if (root == null) return null;
        ArrayDeque<View> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            View v = st.pop();
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) st.push(g.getChildAt(i));
            }
            if (v instanceof TextView) {
                CharSequence cs = ((TextView) v).getText();
                if (cs != null && monthYearRegex.matcher(cs.toString()).matches()) {
                    return (TextView) v;
                }
            }
        }
        return null;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(esCL) + s.substring(1);
    }

    // =============================================================================================
    // ==================  NAVEGACIÓN PREVIA (perfil / semanal / detalle)  =========================
    // =============================================================================================

    private void wireUi() {
        // Icono de perfil (id = profile_icon) -> PerfilActivity (mismo paquete .view)
        int profId = getId("profile_icon");
        if (profId != 0) {
            View profile = findViewById(profId);
            if (profile != null) {
                profile.setOnClickListener(v ->
                        startActivity(new Intent(AgendMensActivity.this, PerfilActivity.class)));
            }
        }

        // Texto "Agenda Mensual" -> Semanal (fallback por texto visible)
        bindByText("Agenda Mensual",
                () -> startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class)));
        // Alternativa por si en tu layout dice "Calendario Mensual"
        bindByText("Calendario Mensual",
                () -> startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class)));

        // Etiquetas de evento tipo R/1-13:30 -> Detalle
        attachEventClickers();
    }

    /** Hace clickeables los TextView que parezcan etiquetas de evento (R/1-13:30, etc.). */
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
                    // Coincide con "R/1-13:30", "M/1-14:30", etc. (no depende de IDs)
                    if (s.matches(".*\\w+/\\d+-\\d{1,2}:\\d{2}.*")) {
                        v.setOnClickListener(click -> {
                            Intent i = new Intent(AgendMensActivity.this, DetActActivity.class);
                            i.putExtra("event_text", s);
                            startActivity(i);
                        });
                    }
                }
            }
        }
    }

    // =============================================================================================
    // ====================================  HELPERS  ==============================================
    // =============================================================================================

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private TextView findTextViewByAnyId(String... names) {
        for (String n : names) {
            int id = getId(n);
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof TextView) return (TextView) v;
            }
        }
        return null;
    }

    private void bindClickByIdOrText(String idName, String fallbackText, Runnable action) {
        boolean bound = false;
        int id = getId(idName);
        if (id != 0) {
            View v = findViewById(id);
            if (v != null) { v.setOnClickListener(x -> action.run()); bound = true; }
        }
        if (!bound) bindByTextContains(fallbackText, action);
    }

    private void bindByText(String text, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        ArrayList<View> out = new ArrayList<>();
        root.findViewsWithText(out, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : out) v.setOnClickListener(x -> action.run());
    }

    private void bindByTextContains(String piece, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;

        ArrayDeque<View> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            View v = stack.pop();

            if (v instanceof TextView) {
                CharSequence cs = ((TextView) v).getText();
                if (cs != null && cs.toString().toLowerCase(esCL).contains(piece.toLowerCase(esCL))) {
                    v.setOnClickListener(x -> action.run());
                }
            }

            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) stack.push(g.getChildAt(i));
            }
        }
    }

    // ---- LogoutView ----
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
