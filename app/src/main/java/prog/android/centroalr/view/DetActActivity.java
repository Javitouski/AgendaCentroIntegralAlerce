package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.R;

public class DetActActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_det_act);

        // Muestra si te pasaron el texto del evento desde la agenda
        TextView tv = findTextViewByAnyId("event_title", "tv_title", "tv_evento", "tv_event");
        String event = getIntent().getStringExtra("event_text");
        if (tv != null && event != null) tv.setText(event);

        // Enlazamos botones por ID si existen; si no, fallback por texto visible
        bindClickByIdOrText("btn_cancelar", "Cancelar", () ->
                startActivity(new Intent(this, CancelActActivity.class)));

        bindClickByIdOrText("btn_reagendar", "Reagendar", () ->
                startActivity(new Intent(this, ReagActActivity.class)));

        bindClickByIdOrText("btn_modificar", "Modificar", () ->
                startActivity(new Intent(this, ModificarActActivity.class)));

        // Extras por si tus textos usan otras palabras
        bindClickByIdOrText("btn_anular", "Anular", () ->
                startActivity(new Intent(this, CancelActActivity.class)));
        bindClickByIdOrText("btn_reprogramar", "Reprogramar", () ->
                startActivity(new Intent(this, ReagActActivity.class)));
        bindClickByIdOrText("btn_editar", "Editar", () ->
                startActivity(new Intent(this, ModificarActActivity.class)));
    }

    // --------- helpers robustos (no rompen si no existen los IDs) ---------

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

    /** Busca Button/TextView cuyo texto CONTENGA la frase (case-insensitive) */
    private void bindByTextContains(String piece, Runnable action) {
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

            if (v instanceof Button || v instanceof TextView) {
                CharSequence cs = (v instanceof Button)
                        ? ((Button) v).getText()
                        : ((TextView) v).getText();
                if (cs != null && cs.toString().toLowerCase().contains(piece.toLowerCase())) {
                    v.setOnClickListener(x -> action.run());
                    // No hacemos return para permitir múltiples matches si existen
                }
            }
        }
    }
}
