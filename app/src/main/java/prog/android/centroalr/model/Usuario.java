package prog.android.centroalr.model;

import java.util.HashMap;
import java.util.Map;

public class Usuario {

    private String uid;
    private String email;
    private String nombre;
    private String rol; // "admin" o "usuario"

    // El "mapa" de permisos.
    private Map<String, Boolean> permisos;

    // Constructor vacío para Firestore
    public Usuario() { }

    // Constructor para un usuario nuevo
    public Usuario(String uid, String email, String nombre, String rol) {
        this.uid = uid;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;

        // Un usuario nuevo NUNCA tiene permisos por defecto
        this.permisos = new HashMap<>();
        if (rol.equals("usuario")) {
            permisos.put("PUEDE_CREAR_ACTIVIDAD", false);
            permisos.put("PUEDE_MODIFICAR_ACTIVIDAD", false);
            permisos.put("PUEDE_CANCELAR_ACTIVIDAD", false);
            permisos.put("PUEDE_REAGENDAR_ACTIVIDAD", false);
            permisos.put("PUEDE_ADJUNTAR_ARCHIVOS", false);
            permisos.put("PUEDE_GESTIONAR_MANTENEDORES", false);
        }
        // Si el rol es "admin", no importa el mapa, siempre tiene acceso.
    }

    // --- Getters y Setters ---
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Map<String, Boolean> getPermisos() { return permisos; }
    public void setPermisos(Map<String, Boolean> permisos) { this.permisos = permisos; }

    // --- Helper de permisos (¡Muy útil!) ---
    public boolean tienePermiso(String permiso) {
        // Si es Admin, siempre tiene permiso
        if (this.rol.equals("admin")) {
            return true;
        }
        // Si es usuario, revisa el mapa
        if (this.permisos == null || !this.permisos.containsKey(permiso)) {
            return false;
        }
        return this.permisos.get(permiso);
    }
}