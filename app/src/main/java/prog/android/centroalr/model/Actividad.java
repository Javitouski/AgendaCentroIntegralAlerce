package prog.android.centroalr.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Actividad {

    private String id;
    private String nombre;
    private String descripcion;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private int cupo;
    private String estado;

    // Nuevos campos alineados con Firestore
    private String beneficiariosDescripcion;
    private int diasAvisoPrevio;
    private Timestamp fechaCreacion;
    private String periodicidad;
    private boolean tieneArchivos;

    private DocumentReference lugarId;
    private DocumentReference tipoActividadId;
    private DocumentReference proyectoId;
    private DocumentReference socioComunitarioId;
    private DocumentReference oferenteId;
    private DocumentReference creadaPorUsuarioId;

    // Constructor vacío requerido por Firestore
    public Actividad() {}

    public Actividad(String id,
                     String nombre,
                     String descripcion,
                     Timestamp fechaInicio,
                     Timestamp fechaFin,
                     int cupo,
                     String estado,
                     String beneficiariosDescripcion,
                     int diasAvisoPrevio,
                     Timestamp fechaCreacion,
                     String periodicidad,
                     boolean tieneArchivos,
                     DocumentReference lugarId,
                     DocumentReference tipoActividadId,
                     DocumentReference proyectoId,
                     DocumentReference socioComunitarioId,
                     DocumentReference oferenteId,
                     DocumentReference creadaPorUsuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cupo = cupo;
        this.estado = estado;
        this.beneficiariosDescripcion = beneficiariosDescripcion;
        this.diasAvisoPrevio = diasAvisoPrevio;
        this.fechaCreacion = fechaCreacion;
        this.periodicidad = periodicidad;
        this.tieneArchivos = tieneArchivos;
        this.lugarId = lugarId;
        this.tipoActividadId = tipoActividadId;
        this.proyectoId = proyectoId;
        this.socioComunitarioId = socioComunitarioId;
        this.oferenteId = oferenteId;
        this.creadaPorUsuarioId = creadaPorUsuarioId;
    }

    // Getters y setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Timestamp fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Timestamp getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Timestamp fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getBeneficiariosDescripcion() {
        return beneficiariosDescripcion;
    }

    public void setBeneficiariosDescripcion(String beneficiariosDescripcion) {
        this.beneficiariosDescripcion = beneficiariosDescripcion;
    }

    public int getDiasAvisoPrevio() {
        return diasAvisoPrevio;
    }

    public void setDiasAvisoPrevio(int diasAvisoPrevio) {
        this.diasAvisoPrevio = diasAvisoPrevio;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public boolean isTieneArchivos() {
        return tieneArchivos;
    }

    public void setTieneArchivos(boolean tieneArchivos) {
        this.tieneArchivos = tieneArchivos;
    }

    public DocumentReference getLugarId() {
        return lugarId;
    }

    public void setLugarId(DocumentReference lugarId) {
        this.lugarId = lugarId;
    }

    public DocumentReference getTipoActividadId() {
        return tipoActividadId;
    }

    public void setTipoActividadId(DocumentReference tipoActividadId) {
        this.tipoActividadId = tipoActividadId;
    }

    public DocumentReference getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(DocumentReference proyectoId) {
        this.proyectoId = proyectoId;
    }

    public DocumentReference getSocioComunitarioId() {
        return socioComunitarioId;
    }

    public void setSocioComunitarioId(DocumentReference socioComunitarioId) {
        this.socioComunitarioId = socioComunitarioId;
    }

    public DocumentReference getOferenteId() {
        return oferenteId;
    }

    public void setOferenteId(DocumentReference oferenteId) {
        this.oferenteId = oferenteId;
    }

    public DocumentReference getCreadaPorUsuarioId() {
        return creadaPorUsuarioId;
    }

    public void setCreadaPorUsuarioId(DocumentReference creadaPorUsuarioId) {
        this.creadaPorUsuarioId = creadaPorUsuarioId;
    }
}
