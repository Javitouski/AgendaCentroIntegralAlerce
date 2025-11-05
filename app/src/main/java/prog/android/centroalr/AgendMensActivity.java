package prog.android.centroalr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

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
        btnLogout.setOnClickListener(v -> {
            controller.onLogoutClicked();
        });
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