package prog.android.centroalr.view;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CancelActActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = findLayout("activity_cancelar_act", "activity_cancel_act", "cancelar_act", "cancel_act");
        if (id != 0) {
            setContentView(id);
        } else {
            // Fallback ultra simple para no romper si el XML no se llama como esperamos
            LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this); t.setText("Cancelar actividad"); t.setTextSize(20f);
            l.addView(t); setContentView(l);
        }
    }
    private int findLayout(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "layout", getPackageName());
            if (id != 0) return id;
        }
        return 0;
    }
}
