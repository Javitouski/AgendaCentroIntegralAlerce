package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton; // Importar
import android.widget.LinearLayout; // Importar
import android.widget.Toast; // Importar
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import prog.android.centroalr.R;

public class MantenedoresHubActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout btnLugares, btnTipos, btnProyectos, btnSocios, btnOferentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenedores_hub);

        // Encontrar vistas
        btnBack = findViewById(R.id.btnBack);
        btnLugares = findViewById(R.id.btnMantenedorLugares);
        btnTipos = findViewById(R.id.btnMantenedorTipos);
        btnProyectos = findViewById(R.id.btnMantenedorProyectos);
        btnSocios = findViewById(R.id.btnMantenedorSocios);
        btnOferentes = findViewById(R.id.btnMantenedorOferentes);

        // Configurar listeners
        btnBack.setOnClickListener(v -> finish());

        // --- Conectar a los mantenedores CRUD (la mayoría aún no existen) ---

        // TODO: Crear MantenedorLugaresActivity.class
        btnLugares.setOnClickListener(v -> {
            startActivity(new Intent(this, MantenedorLugaresActivity.class));
        });

// TODO: Crear MantenedorTiposActivity.class
        btnTipos.setOnClickListener(v -> {
            // Toast.makeText(this, "Mantenedor de Tipos (próximamente)", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MantenedorTiposActivity.class)); // <-- LÍNEA NUEVA
        });

        btnProyectos.setOnClickListener(v -> {
            // Toast.makeText(this, "Mantenedor de Proyectos (próximamente)", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MantenedorProyectosActivity.class)); // <-- LÍNEA NUEVA
        });

        // TODO: Crear MantenedorSociosActivity.class
        btnSocios.setOnClickListener(v->{
            startActivity(new Intent(this, MantenedorSociosActivity.class));
        });

        // TODO: Crear MantenedorOferentesActivity.class
        btnOferentes.setOnClickListener(v->{
            startActivity(new Intent(this, MantenedorOferentesActivity.class));
        });
        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                // Obtenemos el tamaño exacto de las barras del sistema (arriba y abajo)
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Aplicamos ese tamaño como "relleno" (padding) al contenedor principal
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                return insets;
            });
        }
    }
}