package prog.android.centroalr;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReagActActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = findLayout("activity_reag_act", "activity_reagendar_act", "activity_reprogramar_act",
                "reag_act", "reagendar_act", "reprogramar_act");
        if (id != 0) {
            setContentView(id);
        } else {
            LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this); t.setText("Reagendar actividad"); t.setTextSize(20f);
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
