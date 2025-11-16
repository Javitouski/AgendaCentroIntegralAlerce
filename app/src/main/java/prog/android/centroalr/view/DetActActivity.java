package prog.android.centroalr.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import prog.android.centroalr.R;

public class DetActActivity extends AppCompatActivity {

    // Menú flotante
    private View menuAcciones;
    private FloatingActionButton fabMore;
    private String estadoActual;

    // Firestore
    private FirebaseFirestore db;
    private String actividadId;
    private String actividadNombre;

    // UI detalle
    private TextView tvTitle;
    private TextView tvTipo;
    private TextView tvEstado;
    private TextView tvFecha;
    private TextView tvHora;
    private TextView tvLugar;
    private TextView tvCupo;
    private TextView tvBeneficiarios;
    private TextView tvDiasAviso;
    private TextView tvDescripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_det_act);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Extras desde la lista
        actividadId = getIntent().getStringExtra("actividadId");
        actividadNombre = getIntent().getStringExtra("event_text");

        // ----- Views del menú flotante -----
        menuAcciones = findViewById(R.id.menuAcciones);
        fabMore = findViewById(R.id.fabMore);

        // Back (flecha superior)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Toggle del menú con el FAB
        if (fabMore != null && menuAcciones != null) {
            fabMore.setOnClickListener(v -> toggleMenu());
        }

        // ---- Enlazar vistas de detalle ----
        initDetailViews(actividadNombre);

        // ---- Configurar acciones de botones del menú ----
        bindClickByIdOrText("btn_cancelar", "Cancelar", this::abrirCancelar);
        bindClickByIdOrText("btn_reagendar", "Reagendar", this::abrirReagendar);
        bindClickByIdOrText("btn_modificar", "Modificar", this::abrirModificar);

        // Extras por si tus textos usan otras palabras
        bindClickByIdOrText("btn_anular", "Anular", this::abrirCancelar);
        bindClickByIdOrText("btn_reprogramar", "Reprogramar", this::abrirReagendar);
        bindClickByIdOrText("btn_editar", "Editar", this::abrirModificar);

        // ---- Cargar datos reales SOLO si tenemos actividadId ----
        if (actividadId != null && !actividadId.trim().isEmpty()) {
            cargarActividad();
        } else {
            // Modo demo: solo se ve el título provisional si llegó por texto
            Toast.makeText(this, "No se recibió el ID de la actividad.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cada vez que vuelvas a esta pantalla, recargamos los datos desde Firestore
        if (actividadId != null && !actividadId.trim().isEmpty()) {
            cargarActividad();
        }
    }

    // ================== INICIALIZAR UI ==================

    private void initDetailViews(String provisionalTitle) {
        tvTitle         = findViewById(R.id.tv_title);
        tvTipo          = findViewById(R.id.tvTipo);
        tvEstado        = findViewById(R.id.tvEstado);
        tvFecha         = findViewById(R.id.tvFecha);
        tvHora          = findViewById(R.id.tvHora);
        tvLugar         = findViewById(R.id.tvLugar);
        tvCupo          = findViewById(R.id.tvCupo);
        tvBeneficiarios = findViewById(R.id.tvBeneficiarios);
        tvDiasAviso     = findViewById(R.id.tvDiasAviso);
        tvDescripcion   = findViewById(R.id.tvDescripcion);

        // Si vino un título desde la lista, lo mostramos mientras carga Firestore
        if (tvTitle != null && provisionalTitle != null && !provisionalTitle.trim().isEmpty()) {
            tvTitle.setText(provisionalTitle);
        }
    }

    // ================== CARGA DESDE FIRESTORE ==================

    private void cargarActividad() {
        db.collection("actividades")
                .document(actividadId)
                .get()
                .addOnSuccessListener(this::onActividadLoaded)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar la actividad.", Toast.LENGTH_SHORT).show()
                );
    }

    private void onActividadLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "La actividad ya no existe.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nombre / título
        String nombre = doc.getString("nombre");
        if (nombre != null && !nombre.isEmpty()) {
            actividadNombre = nombre;  // actualizamos al nombre real
            if (tvTitle != null) {
                tvTitle.setText(nombre);
            }
        }

        // Tipo de actividad (desde tipoActividadId)
        if (tvTipo != null) {
            DocumentReference tipoRef = doc.getDocumentReference("tipoActividadId");
            if (tipoRef != null) {
                String id = tipoRef.getId();   // p.ej. "taller"
                String label;
                switch (id) {
                    case "taller":
                        label = "Taller grupal";
                        break;
                    default:
                        label = "Tipo: " + id;
                        break;
                }
                tvTipo.setText(label);
            } else {
                tvTipo.setText("Tipo no definido");
            }
        }

        // Estado
        if (tvEstado != null) {
            String estado = doc.getString("estado");
            estadoActual = (estado != null && !estado.trim().isEmpty())
                    ? estado
                    : "activa";  // valor por defecto si no viene nada
            tvEstado.setText(estadoActual);
        }

        // 🔁 Actualizar texto del botón Cancelar / Reactivar según estado
        actualizarTextoBotonCancelar();

        // Fecha y hora
        Timestamp fechaInicio = doc.getTimestamp("fechaInicio");
        Timestamp fechaFin    = doc.getTimestamp("fechaFin");

        SimpleDateFormat dfFecha = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "CL"));
        SimpleDateFormat dfHora  = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (fechaInicio != null) {
            java.util.Date dInicio = fechaInicio.toDate();
            if (tvFecha != null) {
                tvFecha.setText(dfFecha.format(dInicio));
            }
            if (tvHora != null) {
                if (fechaFin != null) {
                    java.util.Date dFin = fechaFin.toDate();
                    tvHora.setText(dfHora.format(dInicio) + " - " + dfHora.format(dFin));
                } else {
                    tvHora.setText(dfHora.format(dInicio));
                }
            }
        } else {
            if (tvFecha != null) tvFecha.setText("Fecha no definida");
            if (tvHora != null)  tvHora.setText("");
        }

        // Lugar
        if (tvLugar != null) {
            DocumentReference lugarRef = doc.getDocumentReference("lugarId");
            String textoLugar;
            if (lugarRef == null) {
                textoLugar = "Lugar no especificado";
            } else {
                String id = lugarRef.getId();
                switch (id) {
                    case "oficina":
                        textoLugar = "Oficina principal del centro comunitario";
                        break;
                    case "salaMultiuso1":
                        textoLugar = "Sala multiuso 1";
                        break;
                    case "salaMultiuso2":
                        textoLugar = "Sala multiuso 2";
                        break;
                    default:
                        String s = id.replace("_", " ").replace("-", " ");
                        if (s.isEmpty()) {
                            textoLugar = "Lugar no especificado";
                        } else {
                            textoLugar = s.substring(0, 1).toUpperCase() + s.substring(1);
                        }
                        break;
                }
            }
            tvLugar.setText(textoLugar);
        }

        // Cupo
        if (tvCupo != null) {
            Long cupo = doc.getLong("cupo");
            tvCupo.setText("Cupo total: " + (cupo != null ? cupo : 0));
        }

        // Días de aviso previo
        if (tvDiasAviso != null) {
            Long diasAviso = doc.getLong("diasAvisoPrevio");
            tvDiasAviso.setText("Días de aviso: " + (diasAviso != null ? diasAviso : 0));
        }

        // Beneficiarios (descripcion)
        if (tvBeneficiarios != null) {
            String benef = doc.getString("beneficiariosDescripcion");
            if (benef != null && !benef.trim().isEmpty()) {
                tvBeneficiarios.setText("Beneficiarios: " + benef);
            } else {
                tvBeneficiarios.setText("Beneficiarios: no especificados");
            }
        }

        // Descripción
        if (tvDescripcion != null) {
            String desc = doc.getString("descripcion");
            tvDescripcion.setText(
                    desc != null && !desc.trim().isEmpty()
                            ? desc
                            : "Sin descripción registrada."
            );
        }
    }

    // ================== Mostrar/Ocultar menú flotante ==================

    private void toggleMenu() {
        if (menuAcciones == null || fabMore == null) return;

        if (menuAcciones.getVisibility() == View.VISIBLE) {
            // Ocultar menú
            menuAcciones.setAlpha(1f);
            menuAcciones.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> menuAcciones.setVisibility(View.GONE))
                    .start();

            fabMore.animate().rotation(0f).setDuration(150).start();
        } else {
            // Mostrar menú
            menuAcciones.setAlpha(0f);
            menuAcciones.setVisibility(View.VISIBLE);
            menuAcciones.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start();

            fabMore.animate().rotation(45f).setDuration(150).start();
        }
    }

    // ================== helpers robustos para los botones del menú ==================

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
            if (v != null) {
                v.setOnClickListener(x -> action.run());
                bound = true;
            }
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
                }
            }
        }
    }

    // 🔁 Cambiar texto del botón Cancelar / Reactivar según estado
    private void actualizarTextoBotonCancelar() {
        String texto = (estadoActual != null &&
                estadoActual.equalsIgnoreCase("cancelada"))
                ? "Reactivar actividad"
                : "Cancelar actividad";

        int id = getId("btn_cancelar");
        if (id == 0) return;

        View v = findViewById(id);
        if (v == null) return;

        if (v instanceof TextView) {
            ((TextView) v).setText(texto);
        } else if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View child = g.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setText(texto);
                    break;
                }
            }
        }
    }

    // ================== Navegación a otras pantallas ==================

    private void abrirCancelar() {
        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "No se encontró el ID de la actividad.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, CancelActActivity.class);
        i.putExtra("actividadId", actividadId);
        i.putExtra("actividadNombre", actividadNombre);
        i.putExtra("estadoActual", estadoActual != null ? estadoActual : "activa");
        startActivity(i);
    }

    private void abrirReagendar() {
        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "No se encontró el ID de la actividad.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, ReagActActivity.class);
        i.putExtra("actividadId", actividadId);
        i.putExtra("actividadNombre", actividadNombre);
        startActivity(i);
    }

    private void abrirModificar() {
        if (actividadId == null || actividadId.trim().isEmpty()) {
            Toast.makeText(this, "No se encontró el ID de la actividad.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, ModificarActActivity.class);
        i.putExtra("actividadId", actividadId);
        i.putExtra("actividadNombre", actividadNombre);
        startActivity(i);
    }
}
