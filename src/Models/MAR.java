package Models;

/**
 * Memory Address Register (MAR) que almacena la dirección de acceso a memoria.
 */
public class MAR {
    private int address;

    /**
     * Carga una dirección en el MAR.
     * @param addr dirección a almacenar.
     */
    public void load(int addr) {
        this.address = addr;
    }

    /**
     * Obtiene la dirección almacenada en el MAR.
     * @return dirección actual.
     */
    public int get() {
        return address;
    }

    /**
     * Reinicia la dirección a cero.
     */
    public void reset() {
        this.address = 0;
    }

    @Override
    public String toString() {
        return "MAR[address=" + address + "]";
    }
}
