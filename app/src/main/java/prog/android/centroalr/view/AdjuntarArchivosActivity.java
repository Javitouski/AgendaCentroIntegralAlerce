package prog.android.centroalr.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import prog.android.centroalr.R;

public class AdjuntarArchivosActivity extends AppCompatActivity {

    // Vistas del layout
    private ImageView btnBack;
    private LinearLayout fileAttachmentArea;
    private LinearLayout btnAddFiles;
    private TextView txtUploadHint;
    private TextView txtSelectedActivity;
    private LinearLayout spinnerActivityContainer;

    private Uri archivoUri = null;
    private String idUsuario;

    // Launcher para seleccionar archivo
    private final ActivityResultLauncher<String> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            archivoUri = uri;
                            String nombre = uri.getLastPathSegment();
                            if (nombre == null) nombre = "Archivo seleccionado";
                            txtUploadHint.setText(nombre);
                        } else {
                            Toast.makeText(this, "No se seleccionó archivo", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjuntar_archivos);

        // ID del usuario (puede ser null si no lo mandas)
        Intent intent = getIntent();
        idUsuario = intent.getStringExtra("idUsuario");

        initViews();
        setupListeners();
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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        fileAttachmentArea = findViewById(R.id.fileAttachmentArea);
        btnAddFiles = findViewById(R.id.btnAddFiles);
        txtUploadHint = findViewById(R.id.txtUploadHint);
        txtSelectedActivity = findViewById(R.id.txtSelectedActivity);
        spinnerActivityContainer = findViewById(R.id.spinnerActivityContainer);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (fileAttachmentArea != null) {
            fileAttachmentArea.setOnClickListener(v -> seleccionarArchivo());
        }

        if (btnAddFiles != null) {
            btnAddFiles.setOnClickListener(v -> {
                if (archivoUri == null) {
                    Toast.makeText(this, "Primero selecciona un archivo", Toast.LENGTH_SHORT).show();
                    return;
                }
                subirArchivoAFirebase(archivoUri);
            });
        }

        if (spinnerActivityContainer != null) {
            spinnerActivityContainer.setOnClickListener(v ->
                    Toast.makeText(this, "Luego aquí puedes elegir la actividad", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void seleccionarArchivo() {
        // */* = cualquier tipo de archivo
        pickFileLauncher.launch("*/*");
    }

    private void subirArchivoAFirebase(Uri uri) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Subiendo archivo...");
        dialog.setCancelable(false);
        dialog.show();

        String carpeta = (idUsuario != null && !idUsuario.isEmpty())
                ? idUsuario
                : "sin_id";

        String nombreArchivo = "archivo_" + System.currentTimeMillis();

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("usuarios/" + carpeta + "/" + nombreArchivo);

        ref.putFile(uri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    dialog.dismiss();
                    guardarInfoEnFirestore(carpeta, nombreArchivo, downloadUri.toString());
                }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void guardarInfoEnFirestore(String idUsuario, String nombre, String url) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(idUsuario)
                .collection("archivos")
                .add(new ArchivoInfo(nombre, url))
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar info", Toast.LENGTH_LONG).show()
                );
    }

    // Clase simple para Firestore
    public static class ArchivoInfo {
        public String nombre;
        public String url;
        public Object fechaSubida;

        public ArchivoInfo() {
        }

        public ArchivoInfo(String nombre, String url) {
            this.nombre = nombre;
            this.url = url;
            this.fechaSubida = FieldValue.serverTimestamp();
        }
    }
}
