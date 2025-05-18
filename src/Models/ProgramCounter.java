package Models;

/**
 * Contador de programa que mantiene la dirección de la próxima instrucción a ejecutar.
 */
public class ProgramCounter {
    private int value = 0;

    /**
     * Incrementa el contador en 1.
     */
    public void increment() {
        value++;
    }

    /**
     * Establece el valor del contador.
     *
     * @param val Nuevo valor del contador.
     */
    public void set(int val) {
        this.value = val;
    }

    /**
     * Obtiene el valor actual del contador.
     *
     * @return Valor del contador.
     */
    public int get() {
        return value;
    }

    /**
     * Reinicia el contador a cero.
     */
    public void reset() {
        value = 0;
    }
}
