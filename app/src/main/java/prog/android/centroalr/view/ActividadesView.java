package prog.android.centroalr.view;

import java.util.List;
import prog.android.centroalr.model.Actividad;

public interface ActividadesView {
    // Cambiamos para que acepte true/false
    void mostrarCarga(boolean mostrar);

    void mostrarListaActividades(List<Actividad> actividades);

    // Agregamos este que faltaba
    void mostrarMensajeVacio();

    void mostrarError(String mensaje);
}