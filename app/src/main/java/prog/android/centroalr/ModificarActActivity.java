package prog.android.centroalr;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ModificarActActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = findLayout("activity_modificar_act", "modificar_act", "activity_editar_act", "editar_act");
        if (id != 0) {
            setContentView(id);
        } else {
            LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this); t.setText("Modificar actividad"); t.setTextSize(20f);
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
