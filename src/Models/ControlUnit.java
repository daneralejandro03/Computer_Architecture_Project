package Models;
import java.util.List;

public class ControlUnit {
    private CPU cpu; // Referencia a la CPU, si se necesita coordinar.

    public ControlUnit(CPU cpu) {
        this.cpu = cpu;
    }

    /**
     * En un simulador más completo, este método obtendría la siguiente instrucción
     * desde la memoria usando un contador de programa. Aquí lo simplificamos.
     */
    public Instruction fetch(int index) {
        List<Instruction> instructions = cpu.getMemory().getInstructions();
        if (index < instructions.size()) {
            return instructions.get(index);
        }
        return null; // No hay más instrucciones
    }

    /**
     * Decodifica la instrucción (en la práctica, interpretaría opcode, etc.).
     */
    public void decode(Instruction instr) {
        // Aquí podrías preparar lo necesario antes de ejecutar,
        // por ejemplo, identificar si hay que acceder a memoria, etc.
        // Por ahora, solo mostramos un mensaje.
        System.out.println("Decodificando instrucción: " + instr.getOpcode());
    }

    /**
     * Ejecuta la instrucción haciendo uso de la ALU o de la memoria, según sea necesario.
     */
    public void execute(Instruction instr) {
        // Para este ejemplo, asumimos que las instrucciones aritméticas se resuelven en la ALU.
        int result = cpu.getAlu().validateOpcode(instr.getOpcode(), instr.getOperand1(), instr.getOperand2());
        System.out.println("Resultado de " + instr.getOpcode() + " = " + result);
        // En un sistema más complejo, podrías almacenar el resultado en memoria o en registros.
    }
}
