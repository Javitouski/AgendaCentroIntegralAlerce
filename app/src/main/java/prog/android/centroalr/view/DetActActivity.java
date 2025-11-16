package prog.android.centroalr.view;

import android.content.Intent; // ¡CORREGIDO! Sin 's'
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout; // Importar LinearLayout
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton; // Importar FAB
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

// Importar clases de Permisos
import prog.android.centroalr.MyApplication;
import prog.android.centroalr.R;
import prog.android.centroalr.model.Usuario;

public class DetActActivity extends AppCompatActivity {

    private ImageView btnBack;
    // Referencias a los TextViews del XML nuevo
    private TextView tvTitle, tvTipo, tvEstado, tvFecha, tvHora, tvLugar, tvCupo, tvBeneficiarios, tvDiasAviso, tvDescripcion;

    // Referencias al nuevo Menú Flotante
    private FloatingActionButton fabMore;
    private LinearLayout menuAcciones;
    private View btnModificar, btnReagendar, btnCancelar; // Son los LinearLayouts clickeables

    private FirebaseFirestore db;
    private String actividadId;

    // Variable para el usuario
    private Usuario usuarioActual;

    private static final String TAG = "DetActActivity";
    private final Locale esCL = new Locale("es", "CL");
    // Formatos separados para Fecha y Hora (como en tu nuevo XML)
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE d 'de' MMMM", esCL);
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", esCL);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_det_act);

        // 1. Cargar el perfil de usuario
        MyApplication myApp = (MyApplication) getApplicationContext();
        usuarioActual = myApp.getUsuarioActual();

        // 2. CHEQUEO DE SEGURIDAD
        if (usuarioActual == null) {
            Toast.makeText(this, "Error: Sesión no encontrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Detenemos la ejecución
        }

        db = FirebaseFirestore.getInstance();

        // Obtener el ID de la actividad
        if (getIntent() != null && getIntent().hasExtra("actividadId")) {
            actividadId = getIntent().getStringExtra("actividadId");
        } else {
            Toast.makeText(this, "Error: No se pudo cargar la actividad.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "actividadId es nulo, no se puede cargar la actividad.");
            finish();
            return;
        }

        initViews(); // 3. Inicializar Vistas con IDs correctos
        initListeners(); // 4. Inicializar Listeners (CON LÓGICA DE PERMISOS)

        cargarDetallesActividad(); // 5. Cargar datos
    }

    private void initViews() {
        // IDs del nuevo XML
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tv_title);
        tvTipo = findViewById(R.id.tvTipo);
        tvEstado = findViewById(R.id.tvEstado);
        tvFecha = findViewById(R.id.tvFecha);
        tvHora = findViewById(R.id.tvHora);
        tvLugar = findViewById(R.id.tvLugar);
        tvCupo = findViewById(R.id.tvCupo);
        tvBeneficiarios = findViewById(R.id.tvBeneficiarios);
        tvDiasAviso = findViewById(R.id.tvDiasAviso);
        tvDescripcion = findViewById(R.id.tvDescripcion);

        // Menú Flotante
        fabMore = findViewById(R.id.fabMore);
        menuAcciones = findViewById(R.id.menuAcciones);
        btnModificar = findViewById(R.id.btn_modificar);
        btnReagendar = findViewById(R.id.btn_reagendar);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }


    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        // --- LÓGICA DE PERMISOS PARA EL MENÚ FLOTANTE ---

        // 1. Revisar todos los permisos relevantes
        boolean canModify = usuarioActual.tienePermiso("PUEDE_MODIFICAR_ACTIVIDAD");
        boolean canReagendar = usuarioActual.tienePermiso("PUEDE_REAGENDAR_ACTIVIDAD");
        boolean canCancel = usuarioActual.tienePermiso("PUEDE_CANCELAR_ACTIVIDAD");

        // 2. ¿Tiene permiso para *alguna* acción?
        boolean hasAnyActionPermission = canModify || canReagendar || canCancel;

        if (hasAnyActionPermission) {
            // 3. Si tiene permisos, mostrar el FAB principal
            fabMore.setVisibility(View.VISIBLE);
            fabMore.setOnClickListener(v -> {
                // Alternar la visibilidad del menú
                if (menuAcciones.getVisibility() == View.VISIBLE) {
                    menuAcciones.setVisibility(View.GONE);
                } else {
                    menuAcciones.setVisibility(View.VISIBLE);
                }
            });

            // 4. Configurar visibilidad y clicks de CADA botón DENTRO del menú

            // Botón Modificar
            if (canModify) {
                btnModificar.setVisibility(View.VISIBLE);
                btnModificar.setOnClickListener(v -> {
                    Intent intent = new Intent(DetActActivity.this, ModificarActActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                });
            } else {
                btnModificar.setVisibility(View.GONE);
            }

            // Botón Reagendar
            if (canReagendar) {
                btnReagendar.setVisibility(View.VISIBLE);
                btnReagendar.setOnClickListener(v -> {
                    Intent intent = new Intent(DetActActivity.this, ReagActActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                });
            } else {
                btnReagendar.setVisibility(View.GONE);
            }

            // Botón Cancelar
            if (canCancel) {
                btnCancelar.setVisibility(View.VISIBLE);
                btnCancelar.setOnClickListener(v -> {
                    Intent intent = new Intent(DetActActivity.this, CancelActActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                });
            } else {
                btnCancelar.setVisibility(View.GONE);
            }

        } else {
            // 5. Si no tiene NINGÚN permiso, ocultar el FAB principal
            fabMore.setVisibility(View.GONE);
        }
    }

    private void cargarDetallesActividad() {
        // mostrarLoading(true); // loadingOverlay fue removido
        DocumentReference docRef = db.collection("actividades").document(actividadId);

        docRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // --- Mapeo Básico (usando nuevos IDs) ---
                tvTitle.setText(doc.getString("nombre"));
                tvEstado.setText(capitalizeFirst(doc.getString("estado")));
                tvDescripcion.setText(doc.getString("descripcion"));

                // Mapeo de Cupo (los otros campos no están en la BD de actividad)
                Long cupo = doc.getLong("cupo");
                tvCupo.setText(String.format(Locale.ROOT, "Cupo total: %d", cupo != null ? cupo.intValue() : 0));
                tvBeneficiarios.setText("Beneficiarios inscritos: N/A"); // Dato no disponible
                tvDiasAviso.setText("Días de aviso: N/A"); // Dato no disponible

                // Mapeo de Fecha y Hora (separados)
                Timestamp tsInicio = doc.getTimestamp("fechaInicio");
                Timestamp tsFin = doc.getTimestamp("fechaFin");
                if (tsInicio != null) {
                    tvFecha.setText(capitalizeFirst(dateFmt.format(tsInicio.toDate())));
                    String horaInicio = timeFmt.format(tsInicio.toDate());
                    String horaFin = (tsFin != null) ? timeFmt.format(tsFin.toDate()) : "--:--";
                    tvHora.setText(String.format("%s - %s", horaInicio, horaFin));
                }

                // --- Mapeo de Referencias ---
                cargarReferencia(doc.getDocumentReference("lugarId"), "descripcion", tvLugar, "Sin lugar");
                cargarReferencia(doc.getDocumentReference("tipoActividadId"), "nombre", tvTipo, "Sin tipo");

                // mostrarLoading(false);
            } else {
                Log.e(TAG, "No se encontró el documento: " + actividadId);
                Toast.makeText(this, "Error: Actividad no encontrada.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al cargar actividad: " + actividadId, e);
            Toast.makeText(this, "Error de red al cargar.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Helper para cargar un documento de referencia y poner un campo de texto en un TextView.
     */
    private void cargarReferencia(DocumentReference ref, String campo, TextView textView, String fallback) {
        if (ref != null) {
            ref.get().addOnSuccessListener(doc -> {
                if (doc.exists() && doc.contains(campo)) {
                    textView.setText(doc.getString(campo));
                } else {
                    textView.setText(fallback);
                }
            }).addOnFailureListener(e -> {
                textView.setText(fallback);
                Log.w(TAG, "No se pudo cargar la referencia: " + ref.getPath(), e);
            });
        } else {
            textView.setText(fallback);
        }
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(esCL) + s.substring(1);
    }
}