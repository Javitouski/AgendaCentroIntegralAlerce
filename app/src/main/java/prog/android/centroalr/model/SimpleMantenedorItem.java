package prog.android.centroalr.model;

public class SimpleMantenedorItem {
    private String id;
    private String nombre;
    private int capacidad; // Nuevo campo

    // Constructor simple (para Tipos, Proyectos, etc.)
    public SimpleMantenedorItem(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = 0;
    }

    // Constructor completo (para Lugares)
    public SimpleMantenedorItem(String id, String nombre, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
}