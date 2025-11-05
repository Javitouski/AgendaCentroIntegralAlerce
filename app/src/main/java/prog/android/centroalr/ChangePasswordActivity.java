package prog.android.centroalr;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrent;
    private EditText etNew;
    private Button btnSubmit;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 👇 Usa tu layout tal cual. Si se llama activity_rest_contra, cambia aquí.
        setContentView(R.layout.activity_rest_contra);

        wireViewsSafely();
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> onChangePassword());
        }
    }

    // ----------------- UI wiring robusto (IDs conocidos + fallback por hint/texto) -----------------

    private void wireViewsSafely() {
        // 1) Intento por IDs típicos (ajusta/añade si conoces los tuyos)
        etCurrent = findEditTextByAnyId("et_current", "current_password", "password_actual", "et_password_actual", "input_current");
        etNew     = findEditTextByAnyId("et_new", "new_password", "password_nueva", "et_password_nueva", "input_new");
        btnSubmit = findButtonByAnyId  ("btn_change", "btn_update", "btn_cambiar", "btn_actualizar", "button_change");
        progress  = findProgressByAnyId("progress", "progressBar", "pb", "loading");

        // 2) Fallback si no hay IDs: por hint/texto en español
        if (etCurrent == null || etNew == null) {
            fallbackScanFieldsByHint();
        }
        if (btnSubmit == null) {
            btnSubmit = findButtonByTextContains("Cambiar", "Actualizar", "Guardar");
        }

        // 3) Garantiza tipo password si el layout no lo puso
        if (etCurrent != null && (etCurrent.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == 0) {
            etCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (etNew != null && (etNew.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == 0) {
            etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private EditText findEditTextByAnyId(String... names) {
        for (String n : names) {
            int id = getId(n);
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof EditText) return (EditText) v;
            }
        }
        return null;
    }

    private Button findButtonByAnyId(String... names) {
        for (String n : names) {
            int id = getId(n);
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof Button) return (Button) v;
            }
        }
        return null;
    }

    private ProgressBar findProgressByAnyId(String... names) {
        for (String n : names) {
            int id = getId(n);
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof ProgressBar) return (ProgressBar) v;
            }
        }
        return null;
    }

    private Button findButtonByTextContains(String... pieces) {
        View root = findViewById(android.R.id.content);
        if (root == null) return null;
        java.util.ArrayDeque<View> st = new java.util.ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            View v = st.pop();
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) st.push(g.getChildAt(i));
            }
            if (v instanceof Button) {
                CharSequence cs = ((Button) v).getText();
                if (cs != null) {
                    String s = cs.toString().toLowerCase();
                    for (String p : pieces) {
                        if (s.contains(p.toLowerCase())) return (Button) v;
                    }
                }
            }
        }
        return null;
    }

    private void fallbackScanFieldsByHint() {
        View root = findViewById(android.R.id.content);
        if (root == null) return;
        java.util.ArrayDeque<View> st = new java.util.ArrayDeque<>();
        st.push(root);

        EditText guessCurrent = null, guessNew = null;

        while (!st.isEmpty()) {
            View v = st.pop();
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup g = (android.view.ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) st.push(g.getChildAt(i));
            }
            if (v instanceof EditText) {
                EditText e = (EditText) v;
                CharSequence hint = e.getHint();
                String h = hint != null ? hint.toString().toLowerCase() : "";
                if (h.contains("actual") || h.contains("old") || h.contains("anterior")) {
                    guessCurrent = e;
                } else if (h.contains("nueva") || h.contains("new")) {
                    guessNew = e;
                }
            }
        }
        if (etCurrent == null) etCurrent = guessCurrent;
        if (etNew == null) etNew = guessNew;
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    // ----------------- Lógica de cambio en sesión -----------------

    private void onChangePassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            toast("Debes iniciar sesión para cambiar la contraseña.");
            return;
        }
        String current = etCurrent != null ? etCurrent.getText().toString().trim() : "";
        String newer   = etNew     != null ? etNew.getText().toString().trim()     : "";

        if (current.isEmpty() || newer.isEmpty()) {
            toast("Completa ambos campos.");
            return;
        }
        if (newer.length() < 6) {
            toast("La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        setLoading(true);

        if (user.getEmail() == null) {
            setLoading(false);
            toast("La cuenta no tiene email. Usa 'Olvidé mi contraseña'.");
            return;
        }

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), current))
                .addOnSuccessListener(unused ->
                        user.updatePassword(newer)
                                .addOnSuccessListener(u -> {
                                    setLoading(false);
                                    toast("Contraseña actualizada.");
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    toast("Error al actualizar: " + e.getMessage());
                                })
                )
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Reautenticación fallida: " + e.getMessage());
                });
    }

    private void setLoading(boolean on) {
        if (progress != null) progress.setVisibility(on ? View.VISIBLE : View.GONE);
        if (btnSubmit != null) btnSubmit.setEnabled(!on);
        if (etCurrent != null) etCurrent.setEnabled(!on);
        if (etNew != null) etNew.setEnabled(!on);
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}
