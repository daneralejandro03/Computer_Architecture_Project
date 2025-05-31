package Models;

import Enums.OpCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CPU {
    // Componentes de la CPU
    private ALU alu = new ALU();
    private ControlWiredOrMicro controlType = new ControlWiredOrMicro();
    private ControlUnit controlUnit = new ControlUnit(controlType);
    private ProgramCounter pc = new ProgramCounter();
    private MAR mar = new MAR();
    private IR ir = new IR();
    private MBR mbr = new MBR();
    private RegisterFile registerFile = new RegisterFile();
    private Memory memory;
    private Bus bus = new Bus();
    private InterruptManager interruptManager = new InterruptManager();
    private AddressingMode addressingMode;
    private List<IODevice> ioDevices;
    private List<String> instructionMemory = new ArrayList<>();
    private boolean halted = false;
    private String haltReason = "Ejecución no ha finalizado."; // Mensaje por defecto

    // --- NUEVAS VARIABLES DE ESTADO PARA CONTROL MICROPROGRAMADO ---
    private int microPC = 0; // Contador de programa para el microcódigo
    private String[] currentMicroprogram = null; // Almacena las señales de la instrucción actual
    private String operand1 = null; // Para el primer operando decodificado (registro o valor)
    private String operand2 = null; // Para el segundo operando decodificado
    private String destination = null; // Para el registro destino

    public CPU(Memory mem, AddressingMode addrMode, List<IODevice> devices, List<String> initialInstructions) {
        this.memory = mem;
        this.addressingMode = addrMode;
        this.ioDevices = devices;
        if (initialInstructions != null) {
            this.instructionMemory = new ArrayList<>(initialInstructions);
        }

        // --- NUEVO MICROPROGRAMA DETALLADO ---
        Map<String, String[]> microprogramData = new HashMap<>();

        // El ciclo de FETCH es el punto de partida para cada instrucción.
        microprogramData.put("FETCH", new String[]{
            "PC_out,MAR_in",
            "Mem_read,PC_inc",
            "MBR_out,IR_in",
            "Decode"
        });

        // Microprogramas para cada instrucción soportada.
        microprogramData.put("ADD", new String[]{"Decode_Operands_R_R_R", "Reg1_to_ALU_A,Reg2_to_ALU_B", "ALU_ADD,MBR_in", "MBR_out,Reg_Dest_in"});
        microprogramData.put("SUB", new String[]{"Decode_Operands_R_R_R", "Reg1_to_ALU_A,Reg2_to_ALU_B", "ALU_SUB,MBR_in", "MBR_out,Reg_Dest_in"});
        microprogramData.put("MOV_IMM", new String[]{"Decode_Operands_R_IMM", "Immediate_to_MBR", "MBR_out,Reg_Dest_in"});
        microprogramData.put("MOV_REG", new String[]{"Decode_Operands_R_R", "Reg1_to_MBR", "MBR_out,Reg_Dest_in"});
        microprogramData.put("OUTPUT_CHAR", new String[]{"ACC_to_Bus,Write_to_Device_1"});
        microprogramData.put("INPUT_CHAR", new String[]{"Read_from_Device_0,Bus_to_ACC"});
        microprogramData.put("JMP", new String[]{"Decode_Operand_Addr", "Address_to_PC"});
        microprogramData.put("CMP", new String[]{"Decode_Operands_R_R", "Reg1_to_ALU_A,Reg2_to_ALU_B", "ALU_CMP,Interrupt_Request"});
        microprogramData.put("JE", new String[]{"Cond_Jump_if_Zero"});
        microprogramData.put("JNE", new String[]{"Cond_Jump_if_Not_Zero"});
        microprogramData.put("HLT", new String[]{"HALT_CPU"});
        microprogramData.put("MUL", new String[]{"Decode_Operands_R_R_R","Reg1_to_ALU_A,Reg2_to_ALU_B","ALU_MUL,MBR_in","MBR_out,Reg_Dest_in"});
        // Añade aquí los microprogramas para las demás instrucciones (MUL, DIV, JGE, etc.)

        this.controlType.loadMicroprogram(microprogramData);
        this.halted = false;
    }
    public String getHaltReason() {
        return this.haltReason;
    }
    /**
     * Carga un nuevo programa y resetea completamente el estado de la CPU,
     * incluyendo el estado de la unidad de control microprogramada.
     */
    public void loadNewProgram(List<String> newInstructions) {
        this.instructionMemory = (newInstructions != null) ? new ArrayList<>(newInstructions) : new ArrayList<>();
        this.pc.reset();
        this.ir.clear();
        this.registerFile.clear();
        this.interruptManager.queue.clear();
        this.mbr.clear();
        this.mar.reset();

        // Reseteo del estado de la unidad de control
        this.halted = false;
        this.currentMicroprogram = null;
        this.microPC = 0;
        this.operand1 = null;
        this.operand2 = null;
        this.destination = null;

        System.out.println("[CPU] Nuevo programa cargado. CPU reseteada y lista.");
    }

    /**
     * Motor de la CPU. Ejecuta UNA micro-instrucción (señal) por ciclo.
     * El ciclo completo de una instrucción de lenguaje ensamblador tomará múltiples llamadas a executeCycle.
     */
    public void executeCycle() {
        if (halted) {
            return;
        }

        // Si no hay un microprograma en ejecución, comenzamos con el ciclo de FETCH.
        if (currentMicroprogram == null) {
            currentMicroprogram = controlType.getMicroprogram().get("FETCH");
            microPC = 0;
        }

        // Obtener la micro-señal actual a ejecutar.
        String signal = currentMicroprogram[microPC];

        // Imprimir estado para depuración.
        System.out.printf("[MicroCTRL] PC: %d | IR: %s | Signal: %s%n", pc.get(), ir.get(), signal);

        // Interpretar y ejecutar la señal.
        executeSignal(signal);

        // Avanzar al siguiente paso del microprograma.
        microPC++;

        // Si hemos terminado todas las señales del microprograma actual, lo reseteamos.
        // El siguiente ciclo de la CPU comenzará automáticamente con un nuevo FETCH.
        if (microPC >= currentMicroprogram.length) {
            currentMicroprogram = null;
            microPC = 0;
        }
    }

    /**
     * El corazón de la Unidad de Control. Interpreta cada señal y realiza la operación de hardware correspondiente.
     * @param signal La señal de control a ejecutar.
     */
    private void executeSignal(String signal) {
        String[] parts = (ir.get() != null && !ir.get().isBlank()) ? ir.get().trim().split("\\s+") : new String[0];

        switch(signal) {
            // --- SEÑALES DE FETCH ---
            case "PC_out,MAR_in":
                mar.load(pc.get());
                break;
            case "Mem_read,PC_inc":
                String instruction = memoryReadInstruction(mar.get());
                if (instruction == null || instruction.trim().isEmpty()) {
                    this.halted = true;
                    currentMicroprogram = new String[]{"HALT_CPU"};
                    microPC = -1; // Para que el siguiente ciclo ejecute HALT_CPU
                    return;
                }
                mbr.load(instruction.hashCode()); // Simulado. MBR contendría la instrucción binaria.
                ir.load(instruction);
                pc.increment();
                break;
            case "MBR_out,IR_in":
                // Acción conceptual. En nuestra simulación, MBR->IR ya ocurrió.
                break;
            case "Decode":
                String mnemonic = controlUnit.decode(ir);
                currentMicroprogram = controlType.getMicroprogram().get(mnemonic);
                if (currentMicroprogram == null) {
                    this.haltReason = "Error: La instrucción '" + mnemonic + "' no es reconocida."; // <-- MENSAJE ESPECÍFICO
                    System.err.println("[CPU] " + this.haltReason);

                    this.halted = true;
                    // LA SOLUCIÓN: Asignamos un microprograma vacío para evitar el NullPointerException.
                    // La CPU se detendrá de forma segura en el siguiente ciclo gracias a la bandera 'halted'.
                    currentMicroprogram = new String[0];
                }
                microPC = -1; // Se incrementará a 0 al final del ciclo.
                break;

            // --- SEÑALES DE DECODIFICACIÓN DE OPERANDOS ---
            case "Decode_Operands_R_R_R": // ADD R1 R2 R3
                if (parts.length > 3) { this.operand1 = parts[1]; this.operand2 = parts[2]; this.destination = parts[3]; }
                break;
            case "Decode_Operands_R_R": // CMP R1 R2
                if (parts.length > 2) { this.operand1 = parts[1]; this.operand2 = parts[2]; }
                break;
            case "Decode_Operands_R_IMM": // MOV_IMM R1 123
                if (parts.length > 2) { this.destination = parts[1]; this.operand1 = parts[2]; }
                break;
            case "Decode_Operand_Addr": // JMP 10
                if (parts.length > 1) { this.operand1 = parts[1]; }
                break;

            // --- SEÑALES DE EJECUCIÓN ---
            case "Reg1_to_ALU_A,Reg2_to_ALU_B":
                // Acción conceptual. Los operandos se leen directamente en la señal de operación.
                break;
            case "ALU_ADD,MBR_in":
                mbr.load(alu.operate(OpCode.ADD, registerFile.read(operand1), registerFile.read(operand2)));
                break;
            case "ALU_SUB,MBR_in":
                 mbr.load(alu.operate(OpCode.SUB, registerFile.read(operand1), registerFile.read(operand2)));
                break;
            case "ALU_CMP,Interrupt_Request":
                int cmpResult = Integer.compare(registerFile.read(operand1), registerFile.read(operand2));
                interruptManager.request(cmpResult);
                break;
            case "ALU_MUL,MBR_in":
                mbr.load(alu.operate(OpCode.MUL, registerFile.read(operand1), registerFile.read(operand2)));
                break;
            case "MBR_out,Reg_Dest_in": { // Usado por ADD, SUB, MUL, MOV_IMM, etc.
                String mnemonics = controlUnit.decode(ir);

                String description = ""; // Descripción por defecto

                // Creamos una descripción basada en la instrucción
                if ("ADD".equals(mnemonics) || "SUB".equals(mnemonics) || "MUL".equals(mnemonics) || "DIV".equals(mnemonics)) {
                    description = String.format("(%s %s, %s)", mnemonics, this.operand1, this.operand2);
                } else if ("MOV_IMM".equals(mnemonics)) {
                    description = String.format("(Carga Inmediata: %s)", this.operand1);
                } else if ("MOV_REG".equals(mnemonics)) {
                     description = String.format("(Copia de %s)", this.operand1);
                }

                registerFile.write(destination, mbr.get(), description);
                break;
            }
            case "Immediate_to_MBR":
                mbr.load(Integer.parseInt(operand1));
                break;
            case "Reg1_to_MBR":
                mbr.load(registerFile.read(parts[1]));
                break;
            case "ACC_to_Bus,Write_to_Device_1":
                ioDevices.get(1).write(registerFile.read("ACC"));
                break;
            case "Read_from_Device_0,Bus_to_ACC":{
                int value = ioDevices.get(0).read();
                registerFile.write("ACC", value, "(Entrada de Dispositivo)");
                break;
            }
            case "Address_to_PC":
                pc.set(Integer.parseInt(operand1));
                // Al modificar el PC, debemos anular el microprograma actual para forzar un nuevo FETCH desde la nueva dirección
                currentMicroprogram = new String[]{}; // Termina el microprograma actual
                break;
            case "Cond_Jump_if_Zero":
                if (interruptManager.hasPending() && interruptManager.queue.poll() == 0) {
                    executeSignal("Decode_Operand_Addr"); // Reutiliza la señal para obtener la dirección
                    executeSignal("Address_to_PC"); // Reutiliza la señal para saltar
                }
                break;
             case "Cond_Jump_if_Not_Zero":
                if (interruptManager.hasPending() && interruptManager.queue.poll() != 0) {
                    executeSignal("Decode_Operand_Addr");
                    executeSignal("Address_to_PC");
                }
                break;

            // --- SEÑAL DE HALT ---
            case "HALT_CPU":
                this.haltReason = "Ejecución finalizada correctamente por instrucción HLT."; // <-- MENSAJE DE ÉXITO
                this.halted = true;
                break;

            default:
                System.err.println("[MicroCTRL] Error: Señal de control desconocida: " + signal);
                this.halted = true;
                break;
        }
    }

    private String memoryReadInstruction(int addr) {
        if (instructionMemory == null || addr < 0 || addr >= instructionMemory.size()) {
            return null;
        }
        return instructionMemory.get(addr);
    }

    // --- GETTERS Y SETTERS (Sin cambios) ---
    public boolean isHalted() { return halted; }
    public ProgramCounter getPC() { return this.pc; }
    public IR getIR() { return this.ir; }
    public RegisterFile getRegisterFile() { return this.registerFile; }
    public Memory getMemory() { return this.memory; }
    public ALU getAlu() { return this.alu; }
    public MBR getMBR() { return this.mbr; }
    public MAR getMAR() { return this.mar; }
    public ControlWiredOrMicro getControlType() { return this.controlType; }
    public int getPCValue() { return this.pc.get(); }
    public String getIRValue() { return this.ir != null ? this.ir.get() : null; }
    public int getACCValue() { return this.registerFile.read("ACC"); }
    public void setIoDevicesList(List<IODevice> devices) { this.ioDevices = new ArrayList<>(devices); }
}