package prog.android.centroalr.controller;

import prog.android.centroalr.model.Usuario;
import prog.android.centroalr.view.InfUsuarioView;

public class InfUsuarioController {

    private InfUsuarioView view;
    private Usuario usuarioActual;

    public InfUsuarioController(InfUsuarioView view, Usuario usuarioActual) {
        this.view = view;
        this.usuarioActual = usuarioActual;
    }

    public void cargarDatos() {
        if (usuarioActual != null) {
            view.mostrarInformacion(usuarioActual);
        } else {
            view.mostrarError("No se pudo cargar la información de sesión.");
        }
    }
}