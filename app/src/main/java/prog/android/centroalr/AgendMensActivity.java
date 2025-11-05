package prog.android.centroalr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import android.view.View; // <-- necesario para findViewsWithText y flags

import prog.android.centroalr.controller.LogoutController;
import prog.android.centroalr.model.AuthModel;
import prog.android.centroalr.view.LogoutView;

// 1. Implementa la interfaz de la Vista
public class AgendMensActivity extends AppCompatActivity implements LogoutView {

    // Vistas
    private TextView btnLogout;

    // 2. Referencia al Controlador y Modelo
    private LogoutController controller;
    private AuthModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agend_mens);

        // 3. Inicializar MVC
        model = new AuthModel();
        controller = new LogoutController(this, model);

        // UI refs
        btnLogout = findViewById(R.id.btn_logout);

        // 4. Delegar evento al Controlador
        btnLogout.setOnClickListener(v -> controller.onLogoutClicked());

        // 5. Navegación solicitada (no toca layouts)
        wireUi();
    }

    // --- Navegación añadida ---

    /** Conecta icono de perfil, salto a Semanal y etiquetas de eventos -> Detalle */
    private void wireUi() {
        // 1) Icono de perfil (ID dinámico) -> PerfilActivity
        int profId = getResources().getIdentifier("profile_icon", "id", getPackageName());
        if (profId != 0) {
            View profile = findViewById(profId);
            if (profile != null) {
                profile.setOnClickListener(v ->
                        startActivity(new Intent(AgendMensActivity.this, PerfilActivity.class))
                );
            }
        }

        // 2) Texto "Agenda Mensual" -> AgndSemActivity (fallback por texto visible)
        bindByText("Agenda Mensual", () ->
                startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class)));


        bindByText("Calendario Mensual", () ->
        startActivity(new Intent(AgendMensActivity.this, AgndSemActivity.class)));

        // 3) Etiquetas tipo R/1-13:30, M/1-14:30... -> DetActActivity (regex por texto)
        attachEventClickers();
    }

    /** Busca vistas cuyo texto visible sea exactamente `text` y les aplica `action`. */
    private void bindByText(String text, Runnable action) {
        final View root = findViewById(android.R.id.content);
        if (root == null) return;
        java.util.ArrayList<View> out = new java.util.ArrayList<>();
        root.findViewsWithText(out, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : out) v.setOnClickListener(x -> action.run());
    }

    /** Hace clickeables todos los TextView que parezcan etiquetas de evento (R/1-13:30, etc.). */
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

            if (v instanceof android.widget.TextView) {
                CharSequence cs = ((android.widget.TextView) v).getText();
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

    // --- Implementación de los métodos de LogoutView ---

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
}
