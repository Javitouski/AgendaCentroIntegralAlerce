package prog.android.centroalr.view;

import prog.android.centroalr.model.Actividad;

public interface DetalleView {
    void mostrarCarga(boolean activa);
    void mostrarError(String mensaje);
    void mostrarDatosActividad(Actividad actividad);
    void mostrarNombreLugar(String nombre);
    void mostrarNombreTipo(String nombre);

    void configurarBotonEstado(boolean esCancelada);
    void navegarACancelar(String actividadId); // NUEVO
}