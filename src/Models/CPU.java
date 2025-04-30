package Models;

public class CPU {
    private ControlUnit controlUnit;
    private ALU alu;
    private Memory memory;

    public CPU(ControlUnit controlUnit, ALU alu, Memory memory) {
        this.controlUnit = controlUnit;
        this.alu = alu;
        this.memory = memory;
    }

    public ControlUnit getControlUnit() {
        return controlUnit;
    }

    public ALU getAlu() {
        return alu;
    }

    public Memory getMemory() {
        return memory;
    }

    /**
     * Método para ejecutar un programa: carga las instrucciones en memoria y
     * luego inicia el ciclo de fetch, decode, execute.
     */
    public void execute(Program program) {
        // Cargar el programa en la memoria
        memory.setInstructions(program.getInstructions());

        System.out.println("=== Iniciando ejecución del programa ===");
        // Recorremos las instrucciones
        for (int i = 0; i < program.getInstructions().size(); i++) {
            Instruction instr = controlUnit.fetch(i);
            if (instr != null) {
                controlUnit.decode(instr);
                controlUnit.execute(instr);
            }
        }
        System.out.println("=== Fin de la ejecución del programa ===");
    }
}
