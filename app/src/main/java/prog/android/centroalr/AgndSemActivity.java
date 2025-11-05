package prog.android.centroalr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AgndSemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agnd_sem);

        // Botonera inferior: intento por ID conocido y fallback por texto
        bindClickByIdOrText("rn6aagqs6pq8", "Perfil",
                () -> startActivity(new Intent(this, PerfilActivity.class)));

        bindClickByIdOrText("r3pjyjmbkmo9", "Inicio",
                () -> startActivity(new Intent(this, AgendMensActivity.class)));

        bindClickByIdOrText("rqwq9k1hp05", "Crear Actividad",
                () -> startActivity(new Intent(this, CrearActActivity.class)));

        // Eventos con ID explícito (si existen)
        bindEventById("rtumfhvig6h");
        bindEventById("roa0repy8oae");

        // Cualquier TextView con "dd/MM/yyyy - ... - HH:mm"
        attachEventClickers();
    }

    // ----- helpers seguros -----
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
}
