package prog.android.centroalr.view;

import prog.android.centroalr.model.Usuario;

public interface InfUsuarioView {
    void mostrarInformacion(Usuario usuario);
    void mostrarError(String mensaje);
}