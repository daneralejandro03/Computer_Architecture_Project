package Models;

/**
 * Instruction Register (IR) que almacena la instrucción a ejecutar.
 */
public class IR {
    private String instruction;

    /**
     * Carga la instrucción en el IR.
     * @param instr instrucción a almacenar.
     */
    public void load(String instr) {
        this.instruction = instr;
    }

    /**
     * Obtiene la instrucción almacenada.
     * @return instrucción actual o null si está vacío.
     */
    public String get() {
        return instruction;
    }

    /**
     * Limpia el contenido del registro de instrucción.
     */
    public void clear() {
        this.instruction = null;
    }

    @Override
    public String toString() {
        return "IR[instruction=" + instruction + "]";
    }
}
