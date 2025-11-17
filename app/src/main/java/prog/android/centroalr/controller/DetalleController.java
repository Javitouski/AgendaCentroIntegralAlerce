package prog.android.centroalr.controller;

import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.ActividadModel;
import prog.android.centroalr.view.DetalleView;

public class DetalleController {
    private DetalleView view;
    private ActividadModel model;
    private Actividad actividadActual; // Guardamos la actividad cargada

    public DetalleController(DetalleView view) {
        this.view = view;
        this.model = new ActividadModel();
    }

    public void cargarDetalle(String actividadId) {
        view.mostrarCarga(true);
        model.obtenerActividadPorId(actividadId, new ActividadModel.OnSingleActividadListener() {
            @Override
            public void onSuccess(Actividad actividad) {
                actividadActual = actividad; // Guardamos referencia local
                view.mostrarCarga(false);
                view.mostrarDatosActividad(actividad);

                resolverReferencias(actividad);

                // Verificamos si está cancelada para configurar el botón
                boolean esCancelada = "cancelada".equalsIgnoreCase(actividad.getEstado());
                view.configurarBotonEstado(esCancelada);
            }

            @Override
            public void onError(String error) {
                view.mostrarCarga(false);
                view.mostrarError(error);
            }
        });
    }

    private void resolverReferencias(Actividad actividad) {
        model.obtenerTextoReferencia(actividad.getLugarId(), "descripcion", view::mostrarNombreLugar);
        model.obtenerTextoReferencia(actividad.getTipoActividadId(), "nombre", view::mostrarNombreTipo);
    }

    // NUEVO: Lógica para el botón Cancelar/Reactivar
    public void gestionarBotonEstado() {
        if (actividadActual == null) return;

        String estadoActual = actividadActual.getEstado();

        if ("cancelada".equalsIgnoreCase(estadoActual)) {
            // LÓGICA DE REACTIVAR: Llamamos al modelo directamente
            view.mostrarCarga(true);
            model.actualizarEstadoActividad(actividadActual.getId(), "activa", new ActividadModel.OnOperacionListener() {
                @Override
                public void onSuccess(String mensaje) {
                    view.mostrarCarga(false);
                    view.mostrarError("Actividad reactivada exitosamente");
                    cargarDetalle(actividadActual.getId()); // Recargar para ver cambios
                }

                @Override
                public void onError(String error) {
                    view.mostrarCarga(false);
                    view.mostrarError(error);
                }
            });
        } else {
            // LÓGICA DE CANCELAR: Mandamos a la otra pantalla
            view.navegarACancelar(actividadActual.getId());
        }
    }
}