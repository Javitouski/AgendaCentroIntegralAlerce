package prog.android.centroalr.view;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import prog.android.centroalr.R;

public class InfUsuarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inf_usuario);

        // Botón atrás del layout (ImageView con id @+id/btnBack)
        View back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }

        // Si en el futuro agregas una Toolbar y activas el "Up", esto la soporta:
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
