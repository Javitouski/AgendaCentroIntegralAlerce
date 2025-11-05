package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import prog.android.centroalr.R;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Intento por ID conocido, fallback por texto visible
        bindClickByIdOrText("revw1kwa5pgf", "Información personal",
                () -> startActivity(new Intent(this, InfUsuarioActivity.class)));

        bindClickByIdOrText("rv6ictxqv14", "Cambiar contraseña",
                () -> startActivity(new Intent(PerfilActivity.this, ChangePasswordActivity.class)));

        bindClickByIdOrText("rjidxs2v4tj8", "Tus actividades",
                () -> startActivity(new Intent(this, AgndSemActivity.class)));
    }

    // ----- helpers -----
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
}