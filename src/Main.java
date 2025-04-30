import Models.CPU;
import Models.ControlUnit;
import Models.ALU;
import Models.Memory;
import Models.Program;
import Models.Instruction;
import Models.InputDevice;
import Models.OutputDevice;

public class Main {
    public static void main(String[] args) {
// 1. Crear componentes principales
        Memory memory = new Memory();
        ALU alu = new ALU();
        CPU cpu = new CPU(null, alu, memory);  // Inicialmente sin ControlUnit

        // 2. Crear la unidad de control y asignarla a la CPU
        ControlUnit controlUnit = new ControlUnit(cpu);
        // Ajustar la referencia en la CPU (si no se pasó en el constructor)
        // O bien pasar la referencia desde el principio.
        // Aquí lo hacemos manualmente:
        cpu = new CPU(controlUnit, alu, memory);

        // 3. Crear un programa con algunas instrucciones
        Program program = new Program();
        program.addInstruction(new Instruction("ADD", 5, 3));
        program.addInstruction(new Instruction("SUB", 10, 4));
        program.addInstruction(new Instruction("MUL", 2, 6));
        program.addInstruction(new Instruction("DIV", 20, 5));
        program.addInstruction(new Instruction("DIV", 20, 0)); // Caso de error de división

        // 4. Ejecutar el programa en la CPU
        cpu.execute(program);

        // 5. Ejemplo de uso de dispositivos de entrada/salida
        InputDevice inputDevice = new InputDevice();
        OutputDevice outputDevice = new OutputDevice();

        int valorLeido = inputDevice.read();
        System.out.println("Valor leído de InputDevice: " + valorLeido);
        outputDevice.write(valorLeido);
    }
}