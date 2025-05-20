package Models;

import Enums.OpCode; // Asegúrate que este enum existe y está en la ruta correcta (ej. src/Enums/OpCode.java)
import java.util.ArrayList; // Para el método loadNewProgram
import java.util.List;
import java.util.Map;    // Necesario para el microprograma
import java.util.HashMap; // Necesario para el microprograma

public class CPU {
    private ALU alu = new ALU();
    private ControlWiredOrMicro controlType = new ControlWiredOrMicro(); // Se inicializa aquí
    private ControlUnit controlUnit = new ControlUnit(controlType);      // Usa la instancia controlType
    private ProgramCounter pc = new ProgramCounter();
    private MAR mar = new MAR();
    private IR ir = new IR();
    private MBR mbr = new MBR();
    private RegisterFile registerFile = new RegisterFile();
    private Memory memory; // Se asigna en el constructor
    private Bus bus = new Bus();
    private InterruptManager interruptManager = new InterruptManager();
    private AddressingMode addressingMode; // Se asigna en el constructor
    private List<IODevice> ioDevices;       // Se asigna en el constructor
    private List<String> instructionMemory = new ArrayList<>(); // Inicializar para evitar null si se llama a loadNewProgram antes
    private boolean halted = false;


    public CPU(Memory mem, AddressingMode addrMode, List<IODevice> devices, List<String> initialInstructions) {
        this.memory = mem;
        this.addressingMode = addrMode;
        this.ioDevices = devices;
        // this.instructionMemory se cargará con loadNewProgram o se puede inicializar aquí
        if (initialInstructions != null) {
            this.instructionMemory = new ArrayList<>(initialInstructions);
        } else {
            this.instructionMemory = new ArrayList<>();
        }

        // Cargar el microprograma de ejemplo en controlType
        Map<String, String[]> microprogramData = new HashMap<>();
        microprogramData.put("MOV_IMM", new String[]{
                "PC_out, MAR_in, Read_Instruction_Memory", "Memory_data_to_MBR (simulated)",
                "MBR_to_IR", "Decode_Instruction_in_IR", "Extract_Immediate_Value_from_IR",
                "Immediate_Value_to_Register_via_Bus"});
        microprogramData.put("OUTPUT_CHAR", new String[]{
                "Register_ACC_to_Bus", "Bus_to_Output_Device_Data_Register",
                "Start_Output_Device_Operation", "Check_Output_Device_Status_Loop"});
        microprogramData.put("INPUT_CHAR", new String[]{
                "Start_Input_Device_Operation", "Check_Input_Device_Status_Loop",
                "Input_Device_Data_Register_to_Bus", "Bus_to_Register_ACC"});
        microprogramData.put("HLT", new String[]{
                "Signal_Set_CPU_Halted_Flag", "End_Cycle_Processing"});
        microprogramData.put("ADD", new String[]{
                "Decode_Registers", "Reg1_to_ALU_A", "Reg2_to_ALU_B",
                "ALU_Perform_ADD", "ALU_Result_to_Reg3"});
        // Añade más mnemónicos y sus señales aquí...

        this.controlType.loadMicroprogram(microprogramData);
        this.halted = false; // Asegurar que halted esté en false al inicio.
    }

    /**
     * Carga un nuevo programa en la memoria de instrucciones y resetea el estado de la CPU.
     * @param newInstructions La lista de nuevas instrucciones a cargar.
     */
    public void loadNewProgram(List<String> newInstructions) {
        if (newInstructions == null) {
            this.instructionMemory = new ArrayList<>();
        } else {
            this.instructionMemory = new ArrayList<>(newInstructions); // Usar una nueva copia defensiva
        }
        this.pc.reset();         // Reiniciar el contador de programa a 0
        this.halted = false;       // Asegurar que la CPU no esté detenida
        if (this.ir != null) this.ir.clear(); // Limpiar el registro de instrucción
        if (this.registerFile != null) this.registerFile.clear(); // Opcional: Limpiar registros generales
        if (this.interruptManager != null && this.interruptManager.queue != null) {
            this.interruptManager.queue.clear(); // Limpiar interrupciones pendientes
        }
        // Considera resetear MBR, MAR, y ALU flags si es necesario para un estado limpio.
        // if (this.mbr != null) this.mbr.clear();
        // if (this.mar != null) this.mar.reset();
        // if (this.alu != null) { /* reset ALU flags si tiene un método para ello */ }

        System.out.println("[CPU] Nuevo programa cargado (" + this.instructionMemory.size() + " instrucciones). PC reseteado. CPU lista.");
    }


    public boolean isHalted() {
        return halted;
    }

    public void executeCycle() {
        if (halted) {
            // Ya no se imprime aquí si el bucle principal en Main lo maneja.
            // Pero si se llama directamente, es una buena guarda.
            // System.out.println("[CPU] Halted. No more cycles will be executed.");
            return;
        }
        try {
            // FETCH
            int currentPC = pc.get(); //
            mar.load(currentPC);
            int instructionAddress = mar.get();
            String rawInstruction = memoryReadInstruction(instructionAddress);

            if (rawInstruction == null) {
                System.out.println("[CPU] FETCH PC=" + currentPC + ": No instruction found (end of program). Halting CPU.");
                this.halted = true;
                return;
            }
            if (rawInstruction.trim().isEmpty()){
                System.out.println("[CPU] FETCH PC=" + currentPC + ": Instruction is empty. Halting CPU.");
                this.halted = true;
                return;
            }

            mbr.load(rawInstruction.hashCode());
            ir.load(rawInstruction); //

            System.out.println("[CPU] FETCH PC=" + currentPC + " IR=\"" + ir.get() + "\""); //

            pc.increment(); //

            // DECODE
            String mnemonic = controlUnit.decode(ir); //
            controlUnit.generateSignals(mnemonic); //

            // EXECUTE
            String[] parts = rawInstruction.trim().split("\\s+");

            switch (mnemonic) {
                case "ADD": case "SUB": case "MUL": case "DIV":
                case "AND": case "OR": case "XOR": {
                    if (parts.length < 4) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int a = registerFile.read(parts[1]); //
                    int b = registerFile.read(parts[2]); //
                    OpCode op = OpCode.valueOf(mnemonic); //
                    int res = alu.operate(op, a, b); //
                    registerFile.write(parts[3], res); //
                    System.out.printf("[CPU] %s %s(%d) %s(%d) -> %s(%d)%n",
                            mnemonic, parts[1], a, parts[2], b, parts[3], res);
                    break;
                }
                case "NOT": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción NOT inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int a = registerFile.read(parts[1]); //
                    int res = alu.operate(OpCode.NOT, a, 0); //
                    registerFile.write(parts[2], res); //
                    System.out.printf("[CPU] NOT %s(%d) -> %s(%d)%n", parts[1], a, parts[2], res);
                    break;
                }
                case "MOV_REG": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción MOV_REG inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int val = registerFile.read(parts[1]); //
                    registerFile.write(parts[2], val); //
                    System.out.printf("[CPU] MOV_REG %s(%d) -> %s%n", parts[1], val, parts[2]);
                    break;
                }
                case "MOV_IMM": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción MOV_IMM inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    try {
                        int imm = Integer.parseUnsignedInt(parts[2]);
                        registerFile.write(parts[1], imm); //
                        System.out.printf("[CPU] MOV_IMM %d -> %s%n", imm, parts[1]);
                    } catch (NumberFormatException e) {
                        System.err.println("[CPU] Valor inmediato inválido para MOV_IMM: " + parts[2] + " en \"" + rawInstruction + "\"");
                        this.halted = true;
                    }
                    break;
                }
                case "MOV_RAM": {
                    if (parts.length < 4) {
                        System.err.println("[CPU] Instrucción MOV_RAM inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    try {
                        int baseAddr = Integer.parseInt(parts[2]);
                        int effectiveAddr = this.addressingMode.resolve(baseAddr, this); //
                        boolean writeFlag = Boolean.parseBoolean(parts[3]);
                        if (writeFlag) {
                            bus.transfer(registerFile, parts[1], memory, effectiveAddr); //
                        } else {
                            bus.transfer(memory, effectiveAddr, registerFile, parts[1]); //
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("[CPU] Dirección base inválida para MOV_RAM: " + parts[2] + " en \"" + rawInstruction + "\"");
                        this.halted = true;
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("[CPU] Error de acceso a memoria en MOV_RAM (" + parts[2] + "): " + e.getMessage() + " en \"" + rawInstruction + "\"");
                        this.halted = true;
                    }
                    break;
                }
                case "JMP": {
                    if (parts.length < 2) {
                        System.err.println("[CPU] Instrucción JMP inválida (falta dirección): " + rawInstruction);
                        this.halted = true; break;
                    }
                    boolean cond = true;
                    if (parts.length >= 3) {
                        cond = Boolean.parseBoolean(parts[2]);
                    }

                    if (cond) {
                        try {
                            int addr = Integer.parseInt(parts[1]);
                            if (addr < 0 || addr >= instructionMemory.size()) {
                                System.err.println("[CPU] Dirección de JMP inválida (fuera de rango): " + addr + " en \"" + rawInstruction + "\"");
                                this.halted = true;
                            } else {
                                pc.set(addr); //
                                System.out.println("[CPU] JMP a " + addr);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("[CPU] Dirección inválida para JMP: " + parts[1] + " en \"" + rawInstruction + "\"");
                            this.halted = true;
                        }
                    }
                    break;
                }
                case "CMP": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción CMP inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int a = registerFile.read(parts[1]); //
                    int b = registerFile.read(parts[2]); //
                    int cmpResult = Integer.compare(a, b);
                    interruptManager.request(cmpResult); //
                    System.out.printf("[CPU] CMP %s(%d) %s(%d), resultado=%d%n", parts[1], a, parts[2], b, cmpResult);
                    break;
                }
                case "JE": case "JNE": case "JGE": case "JL": {
                    if (parts.length < 2) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida (falta dirección): " + rawInstruction);
                        this.halted = true; break;
                    }
                    if (interruptManager.hasPending()) { //
                        int cmpResult = interruptManager.queue.peek(); //
                        boolean jump = false;
                        switch (mnemonic) {
                            case "JE":  jump = (cmpResult == 0); break;
                            case "JNE": jump = (cmpResult != 0); break;
                            case "JGE": jump = (cmpResult >= 0); break;
                            case "JL":  jump = (cmpResult < 0); break;
                        }
                        if (jump) {
                            try {
                                int addr = Integer.parseInt(parts[1]);
                                if (addr < 0 || addr >= instructionMemory.size()) {
                                    System.err.println("[CPU] Dirección de " + mnemonic + " inválida (fuera de rango): " + addr + " en \"" + rawInstruction + "\"");
                                    this.halted = true;
                                } else {
                                    pc.set(addr); //
                                    System.out.printf("[CPU] %s salto a %d (basado en CMP previo resultado=%d)%n", mnemonic, addr, cmpResult);
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("[CPU] Dirección inválida para " + mnemonic + ": " + parts[1] + " en \"" + rawInstruction + "\"");
                                this.halted = true;
                            }
                        }
                        interruptManager.queue.poll(); //
                    } else {
                        System.err.println("[CPU] " + mnemonic + " ejecutado sin un CMP previo pendiente. (" + rawInstruction + ")");
                    }
                    break;
                }
                case "INPUT_CHAR": {
                    if (ioDevices.isEmpty() || ioDevices.get(0) == null) {
                        System.err.println("[CPU] No hay dispositivo de entrada (0) disponible o configurado.");
                        this.halted = true; break;
                    }
                    IODevice currentInputDevice = ioDevices.get(0);
                    int ch = currentInputDevice.read(); //
                    if (ch == -1 && "EOF".equals(currentInputDevice.getStatus())) { //
                        System.out.println("[CPU] INPUT_CHAR: EOF alcanzado. ACC se establece a -1.");
                        registerFile.write("ACC", -1); //
                    } else if ("ERROR".equals(currentInputDevice.getStatus())) { //
                        System.err.println("[CPU] INPUT_CHAR: Error en dispositivo de entrada.");
                        this.halted = true;
                    }
                    else {
                        registerFile.write("ACC", ch); //
                        System.out.printf("[CPU] INPUT_CHAR -> ACC(%d) '%c'%n", ch, (char) ch);
                    }
                    break;
                }
                case "OUTPUT_CHAR": {
                    if (ioDevices.size() < 2 || ioDevices.get(1) == null) {
                        System.err.println("[CPU] No hay dispositivo de salida (1) disponible o configurado.");
                        this.halted = true; break;
                    }
                    int ch = registerFile.read("ACC"); //
                    if (ch == -1) {
                        System.out.println("[CPU] OUTPUT_CHAR: ACC es -1 (probablemente EOF), no se envía salida.");
                    } else {
                        ioDevices.get(1).write(ch); //
                        System.out.printf("[CPU] OUTPUT_CHAR desde ACC(%d) '%c'%n", ch, (char) ch);
                    }
                    break;
                }
                case "HLT": {
                    System.out.println("[CPU] HLT instruction received. Halting CPU.");
                    this.halted = true;
                    break;
                }
                default:
                    System.err.println("[CPU] Instrucción no soportada: \"" + mnemonic + "\" en línea \"" + rawInstruction + "\"");
                    this.halted = true;
                    break;
            }

            if (halted && !mnemonic.equals("HLT") && !rawInstruction.trim().isEmpty()) {
                System.out.println("[CPU] CPU detenida debido a un error o condición en la instrucción: " + rawInstruction);
            }

            interruptManager.process(); //

        } catch (IllegalArgumentException e) {
            System.err.println("[CPU] Error crítico en ciclo de ejecución: Mnemónico desconocido para OpCode - " + e.getMessage() + " en \"" + (ir != null ? ir.get() : "desconocida") + "\""); //
            e.printStackTrace();
            this.halted = true;
        } catch (Exception e) {
            System.err.println("[CPU] Error crítico en ciclo de ejecución: " + e.getMessage() + " (Instrucción: \"" + (ir != null ? ir.get() : "desconocida") + "\")"); //
            e.printStackTrace();
            this.halted = true;
        }
    }

    private String memoryReadInstruction(int addr) {
        if (instructionMemory == null || instructionMemory.isEmpty()) { // Añadida comprobación de isEmpty
            // No imprimir error aquí, executeCycle lo manejará.
            return null;
        }
        if (addr < 0 || addr >= instructionMemory.size()) {
            // No imprimir error aquí, executeCycle lo manejará.
            return null;
        }
        return instructionMemory.get(addr);
    }

    // --- INICIO: GETTERS PARA EL CONTROLADOR Y LA VISTA ---
    public ProgramCounter getPC() {
        return this.pc;
    }

    public IR getIR() {
        return this.ir;
    }

    public RegisterFile getRegisterFile() {
        return this.registerFile;
    }

    public Memory getMemory() { // Este ya existía
        return this.memory;
    }

    public ALU getAlu() {
        return this.alu;
    }

    public MBR getMBR() {
        return this.mbr;
    }

    public MAR getMAR() {
        return this.mar;
    }

    public ControlWiredOrMicro getControlType() { // Este ya existía
        return this.controlType;
    }

    // Getters para valores específicos que pueden ser útiles directamente
    public int getPCValue() {
        return this.pc.get(); //
    }

    public String getIRValue() {
        if (this.ir == null) return null; // salvaguarda
        return this.ir.get(); //
    }

    public int getACCValue() {
        return this.registerFile.read("ACC"); //
    }
    // --- FIN: GETTERS ---

    public void setIoDevicesList(List<IODevice> devices) {
        if (devices == null) {
            this.ioDevices = new ArrayList<>(); // Evitar NullPointerException si se pasa null
        } else {
            // Es una buena práctica crear una nueva lista para evitar que modificaciones
            // externas a la lista 'devices' afecten inesperadamente a la CPU.
            this.ioDevices = new ArrayList<>(devices);
        }

        // Log para confirmar que los dispositivos se han establecido
        if (this.ioDevices != null) {
            System.out.println("[CPU] I/O Devices actualizados. Número de dispositivos: " + this.ioDevices.size());
            // Opcional: imprimir IDs para verificar
            if (this.ioDevices.size() > 0 && this.ioDevices.get(0) != null) {
                System.out.println("[CPU] Input device (0) ID: " + this.ioDevices.get(0).getId());
            }
            if (this.ioDevices.size() > 1 && this.ioDevices.get(1) != null) {
                System.out.println("[CPU] Output device (1) ID: " + this.ioDevices.get(1).getId());
            }
        } else {
            System.out.println("[CPU] Advertencia: La lista de I/O Devices es null después de intentar establecerla.");
        }
    }
}