package prog.android.centroalr.controller;

import java.util.List;
import prog.android.centroalr.model.Actividad;
import prog.android.centroalr.model.ActividadModel;
import prog.android.centroalr.view.ActividadesView;

public class ActividadesController {

    private ActividadesView view;
    private ActividadModel model;

    public ActividadesController(ActividadesView view) {
        this.view = view;
        this.model = new ActividadModel();
    }

    public void cargarLista() {
        // Ahora coincide con la interfaz (envía boolean)
        view.mostrarCarga(true);

        model.obtenerActividades(new ActividadModel.OnActividadesCargadasListener() {
            @Override
            public void onSuccess(List<Actividad> lista) {
                view.mostrarCarga(false);
                if (lista.isEmpty()) {
                    view.mostrarMensajeVacio();
                } else {
                    view.mostrarListaActividades(lista);
                }
            }

            @Override
            public void onError(String error) {
                view.mostrarCarga(false);
                view.mostrarError("Error al cargar: " + error);
            }
        });
    }
}