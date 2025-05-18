package Models;

/**
 * Dispositivo de Entrada/Salida genérico.
 * Cada dispositivo debe especificar su forma de leer y escribir datos.
 */
public abstract class IODevice {
    protected final String id;
    protected String status;

    /**
     * Crea un dispositivo con el identificador dado.
     * @param id cadena que identifica el dispositivo.
     */
    public IODevice(String id) {
        this.id = id;
        this.status = "IDLE";
    }

    /**
     * Obtiene el identificador del dispositivo.
     * @return id único del dispositivo.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el estado actual (p.ej., "IDLE", "BUSY").
     * @return estado del dispositivo.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Lee un dato del dispositivo.
     * @return el dato leído, o -1 si no hay más datos o no aplica.
     */
    public abstract int read();

    /**
     * Escribe un dato en el dispositivo.
     * @param data valor a escribir.
     */
    public abstract void write(int data);

    @Override
    public String toString() {
        return "IODevice[id=" + id + ", status=" + status + "]";
    }
}