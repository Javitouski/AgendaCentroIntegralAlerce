package prog.android.centroalr.model;

/**
 * Clase de modelo simple para usar en los adaptadores de mantenedores.
 * Solo guarda el ID del documento de Firestore y el nombre a mostrar.
 */
public class SimpleMantenedorItem {
    private String id;
    private String nombre;

    // Constructor vacío necesario para algunas conversiones
    public SimpleMantenedorItem() {}

    public SimpleMantenedorItem(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}