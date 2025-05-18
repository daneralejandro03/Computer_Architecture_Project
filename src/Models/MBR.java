
package Models;

/**
 * Memory Buffer Register (MBR) que almacena temporalmente datos leídos o por escribir.
 */
public class MBR {
    private int data;

    /**
     * Carga un dato en el MBR.
     * @param data valor a almacenar.
     */
    public void load(int data) {
        this.data = data;
    }

    /**
     * Obtiene el dato almacenado en el MBR.
     * @return valor cargado.
     */
    public int get() {
        return data;
    }

    /**
     * Limpia el contenido del MBR (lo pone en cero).
     */
    public void clear() {
        this.data = 0;
    }

    @Override
    public String toString() {
        return "MBR[data=" + data + "]";
    }
}
