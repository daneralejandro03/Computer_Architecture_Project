package Models;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private List<Instruction> instructions;
    private List<Integer> data;

    public Memory() {
        this.instructions = new ArrayList<>();
        this.data = new ArrayList<>();
    }

    /**
     * Opcional: cargar el programa en la memoria (instrucciones).
     */
    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * Acceso a datos: se podría simular lectura y escritura
     * en posiciones de memoria. Aquí, para simplicidad, usamos una lista.
     */
    public void setData(List<Integer> data) {
        this.data = data;
    }

    public List<Integer> getData() {
        return data;
    }
}
