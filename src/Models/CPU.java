package Models;

import Enums.OpCode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CPU {
    // ... (tus campos existentes: alu, controlType, controlUnit, pc, mar, ir, etc.) ...
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
    private List<String> instructionMemory;
    private boolean halted = false;

    public CPU(Memory mem, AddressingMode addrMode, List<IODevice> devices, List<String> instructions) {
        this.memory = mem;
        this.addressingMode = addrMode;
        this.ioDevices = devices;
        this.instructionMemory = instructions;

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
        this.controlType.loadMicroprogram(microprogramData);
        this.halted = false;
    }

    public boolean isHalted() {
        return halted;
    }

    public void executeCycle() {
        // ... (tu lógica de executeCycle existente y robusta) ...
        if (halted) {
            System.out.println("[CPU] Halted. No more cycles will be executed.");
            return;
        }
        try {
            // FETCH
            int currentPC = pc.get();
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
            ir.load(rawInstruction);

            System.out.println("[CPU] FETCH PC=" + currentPC + " IR=\"" + rawInstruction + "\"");

            pc.increment();

            // DECODE
            String mnemonic = controlUnit.decode(ir);
            controlUnit.generateSignals(mnemonic);

            // EXECUTE
            String[] parts = rawInstruction.trim().split("\\s+");

            switch (mnemonic) {
                case "ADD": case "SUB": case "MUL": case "DIV":
                case "AND": case "OR": case "XOR": {
                    if (parts.length < 4) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int a = registerFile.read(parts[1]);
                    int b = registerFile.read(parts[2]);
                    OpCode op = OpCode.valueOf(mnemonic);
                    int res = alu.operate(op, a, b);
                    registerFile.write(parts[3], res);
                    System.out.printf("[CPU] %s %s(%d) %s(%d) -> %s(%d)%n",
                            mnemonic, parts[1], a, parts[2], b, parts[3], res);
                    break;
                }
                case "NOT": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción NOT inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int a = registerFile.read(parts[1]);
                    int res = alu.operate(OpCode.NOT, a, 0);
                    registerFile.write(parts[2], res);
                    System.out.printf("[CPU] NOT %s(%d) -> %s(%d)%n", parts[1], a, parts[2], res);
                    break;
                }
                case "MOV_REG": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción MOV_REG inválida: " + rawInstruction);
                        this.halted = true; break;
                    }
                    int val = registerFile.read(parts[1]);
                    registerFile.write(parts[2], val);
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
                        registerFile.write(parts[1], imm);
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
                        int effectiveAddr = this.addressingMode.resolve(baseAddr, this);
                        boolean writeFlag = Boolean.parseBoolean(parts[3]);
                        if (writeFlag) {
                            bus.transfer(registerFile, parts[1], memory, effectiveAddr);
                        } else {
                            bus.transfer(memory, effectiveAddr, registerFile, parts[1]);
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
                                pc.set(addr);
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
                    int a = registerFile.read(parts[1]);
                    int b = registerFile.read(parts[2]);
                    int cmpResult = Integer.compare(a, b);
                    interruptManager.request(cmpResult);
                    System.out.printf("[CPU] CMP %s(%d) %s(%d), resultado=%d%n", parts[1], a, parts[2], b, cmpResult);
                    break;
                }
                case "JE": case "JNE": case "JGE": case "JL": {
                    if (parts.length < 2) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida (falta dirección): " + rawInstruction);
                        this.halted = true; break;
                    }
                    if (interruptManager.hasPending()) {
                        int cmpResult = interruptManager.queue.peek();
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
                                    pc.set(addr);
                                    System.out.printf("[CPU] %s salto a %d (basado en CMP previo resultado=%d)%n", mnemonic, addr, cmpResult);
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("[CPU] Dirección inválida para " + mnemonic + ": " + parts[1] + " en \"" + rawInstruction + "\"");
                                this.halted = true;
                            }
                        }
                        interruptManager.queue.poll();
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
                    int ch = ioDevices.get(0).read();
                    if (ch == -1 && "EOF".equals(ioDevices.get(0).getStatus())) {
                        System.out.println("[CPU] INPUT_CHAR: EOF alcanzado. ACC se establece a -1.");
                        registerFile.write("ACC", -1);
                    } else if ("ERROR".equals(ioDevices.get(0).getStatus())) {
                        System.err.println("[CPU] INPUT_CHAR: Error en dispositivo de entrada.");
                        this.halted = true;
                    }
                    else {
                        registerFile.write("ACC", ch);
                        System.out.printf("[CPU] INPUT_CHAR -> ACC(%d) '%c'%n", ch, (char) ch);
                    }
                    break;
                }
                case "OUTPUT_CHAR": {
                    if (ioDevices.size() < 2 || ioDevices.get(1) == null) {
                        System.err.println("[CPU] No hay dispositivo de salida (1) disponible o configurado.");
                        this.halted = true; break;
                    }
                    int ch = registerFile.read("ACC");
                    if (ch == -1) {
                        System.out.println("[CPU] OUTPUT_CHAR: ACC es -1 (probablemente EOF), no se envía salida.");
                    } else {
                        ioDevices.get(1).write(ch);
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

            if (halted && !mnemonic.equals("HLT")) {
                System.out.println("[CPU] CPU detenida debido a un error en la instrucción: " + rawInstruction);
            }

            interruptManager.process();

        } catch (IllegalArgumentException e) {
            System.err.println("[CPU] Error crítico en ciclo de ejecución: Mnemónico desconocido para OpCode - " + e.getMessage() + " en \"" + ir.get() + "\"");
            e.printStackTrace();
            this.halted = true;
        } catch (Exception e) {
            System.err.println("[CPU] Error crítico en ciclo de ejecución: " + e.getMessage() + " (Instrucción: \"" + (ir != null ? ir.get() : "desconocida") + "\")");
            e.printStackTrace();
            this.halted = true;
        }
    }

    private String memoryReadInstruction(int addr) {
        if (instructionMemory == null) {
            return null;
        }
        if (addr < 0 || addr >= instructionMemory.size()) {
            return null;
        }
        return instructionMemory.get(addr);
    }

    // --- INICIO: GETTERS PARA EL CONTROLADOR Y LA VISTA ---
    public ProgramCounter getPC() { // Devuelve el objeto PC
        return this.pc;
    }

    public IR getIR() { // Devuelve el objeto IR
        return this.ir;
    }

    public RegisterFile getRegisterFile() { // Devuelve el objeto RegisterFile
        return this.registerFile;
    }

    public Memory getMemory() { // Devuelve el objeto Memory (este ya lo tenías)
        return this.memory;
    }

    public ALU getAlu() { // Devuelve el objeto ALU
        return this.alu;
    }

    public MBR getMBR() { // Getter para MBR
        return this.mbr;
    }

    public MAR getMAR() { // Getter para MAR
        return this.mar;
    }

    public ControlWiredOrMicro getControlType() { // Getter para ControlType (ya lo tenías)
        return this.controlType;
    }

    // Getters para valores específicos (alternativa o complemento a devolver objetos)
    public int getPCValue() {
        return this.pc.get();
    }

    public String getIRValue() {
        return this.ir.get();
    }

    public int getACCValue() { // Ejemplo para un registro específico como ACC
        return this.registerFile.read("ACC");
    }
    // --- FIN: GETTERS ---
}