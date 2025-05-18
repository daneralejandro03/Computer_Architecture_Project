package Models;
import Enums.OpCode;

import java.util.List;

public class CPU {
    private ALU alu = new ALU();
    private ControlUnit controlUnit = new ControlUnit();
    private ControlWiredOrMicro controlType = new ControlWiredOrMicro();
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
    private List<String> instructionMemory; // memoria de instrucciones


    public CPU(Memory mem, AddressingMode addrMode, List<IODevice> devices, List<String> instructions) {
        this.memory = mem;
        this.addressingMode = addrMode;
        this.ioDevices = devices;
        this.instructionMemory = instructions;
    }

    /**
     * Ejecuta un ciclo de instrucción: fetch, decode y execute.
     */
    public void executeCycle() {
        try {
            // FETCH
            int currentPC = pc.get();
            mar.load(currentPC);                            // MAR <- PC
            int instructionAddress = mar.get();
            // Leer instrucción de memoria usando direccionamiento
            String rawInstruction = memoryReadInstruction(instructionAddress);
            mbr.load(rawInstruction.hashCode());           // Simulación: MBR carga un hash (puedes cambiar)
            ir.load(rawInstruction);                        // IR <- instrucción

            System.out.println("[CPU] FETCH PC=" + currentPC + " IR=\"" + rawInstruction + "\"");

            pc.increment();                                // PC++

            // DECODE
            String mnemonic = controlUnit.decode(ir);     // Decodificar opcode
            controlUnit.generateSignals(mnemonic);        // Generar señales (simulación)
            // Opcional: cambiar modo de direccionamiento basado en instrucción
            // (no implementado para simplificar)

            // EXECUTE (interpretar instrucción y operar)
            String[] parts = rawInstruction.trim().split("\\s+");
            if (parts.length == 0) {
                System.out.println("[CPU] Instrucción vacía, ciclo terminado.");
                return;
            }

            switch (mnemonic) {
                case "ADD": case "SUB": case "MUL": case "DIV":
                case "AND": case "OR": case "XOR": {
                    // Ejemplo: ADD reg1 reg2 reg3
                    // reg3 = reg1 op reg2
                    if (parts.length < 4) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida");
                        break;
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
                    // NOT reg1 reg2
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción NOT inválida");
                        break;
                    }
                    int a = registerFile.read(parts[1]);
                    int res = alu.operate(OpCode.NOT, a, 0);
                    registerFile.write(parts[2], res);
                    System.out.printf("[CPU] NOT %s(%d) -> %s(%d)%n", parts[1], a, parts[2], res);
                    break;
                }
                case "MOV_REG": {
                    // MOV_REG regSrc regDst
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción MOV_REG inválida");
                        break;
                    }
                    int val = registerFile.read(parts[1]);
                    registerFile.write(parts[2], val);
                    System.out.printf("[CPU] MOV_REG %s(%d) -> %s%n", parts[1], val, parts[2]);
                    break;
                }
                case "MOV_IMM": {
                    // MOV_IMM reg valorInmediato
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción MOV_IMM inválida");
                        break;
                    }
                    int imm = Integer.parseUnsignedInt(parts[2]);
                    registerFile.write(parts[1], imm);
                    System.out.printf("[CPU] MOV_IMM %d -> %s%n", imm, parts[1]);
                    break;
                }
                case "MOV_RAM": {
                    // MOV_RAM reg addr writeFlag(true/false)
                    if (parts.length < 4) {
                        System.err.println("[CPU] Instrucción MOV_RAM inválida");
                        break;
                    }
                    int addr = Integer.parseInt(parts[2]);
                    boolean writeFlag = Boolean.parseBoolean(parts[3]);
                    if (writeFlag) {
                        int val = registerFile.read(parts[1]);
                        bus.transfer(registerFile, parts[1], memory, addr);
                    } else {
                        bus.transfer(memory, addr, registerFile, parts[1]);
                    }
                    break;
                }
                case "JMP": {
                    // JMP addr condicion(boolean)
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción JMP inválida");
                        break;
                    }
                    boolean cond = Boolean.parseBoolean(parts[2]);
                    if (cond) {
                        int addr = Integer.parseInt(parts[1]);
                        pc.set(addr);
                        System.out.println("[CPU] JMP a " + addr);
                    }
                    break;
                }
                case "CMP": {
                    // CMP reg1 reg2
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción CMP inválida");
                        break;
                    }
                    int a = registerFile.read(parts[1]);
                    int b = registerFile.read(parts[2]);
                    int cmpResult = Integer.compare(a, b);
                    interruptManager.request(cmpResult);
                    System.out.printf("[CPU] CMP %s(%d) %s(%d), resultado=%d%n", parts[1], a, parts[2], b, cmpResult);
                    break;
                }
                case "JE": case "JNE": case "JGE": case "JL": {
                    if (parts.length < 3) {
                        System.err.println("[CPU] Instrucción " + mnemonic + " inválida");
                        break;
                    }
                    boolean cond = Boolean.parseBoolean(parts[2]);
                    if (cond && interruptManager.hasPending()) {
                        // Implementa lógica básica para JE/JNE/JGE/JL usando interrupciones
                        int cmpResult = interruptManager.queue.peek();
                        boolean jump = false;
                        switch (mnemonic) {
                            case "JE":  jump = (cmpResult == 0); break;
                            case "JNE": jump = (cmpResult != 0); break;
                            case "JGE": jump = (cmpResult >= 0); break;
                            case "JL":  jump = (cmpResult < 0); break;
                        }
                        if (jump) {
                            int addr = Integer.parseInt(parts[1]);
                            pc.set(addr);
                            System.out.printf("[CPU] %s salto a %d%n", mnemonic, addr);
                        }
                        interruptManager.queue.poll();
                    }
                    break;
                }
                case "INPUT_CHAR": {
                    // Lee un caracter desde el dispositivo de entrada 0 y lo guarda en registro ACC
                    if (ioDevices.isEmpty()) {
                        System.err.println("[CPU] No hay dispositivos de entrada disponibles");
                        break;
                    }
                    int ch = ioDevices.get(0).read();
                    registerFile.write("ACC", ch);
                    System.out.printf("[CPU] INPUT_CHAR -> ACC(%d)%n", ch);
                    break;
                }
                case "OUTPUT_CHAR": {
                    // Escribe el caracter en el dispositivo de salida 1 desde registro ACC
                    if (ioDevices.size() < 2) {
                        System.err.println("[CPU] No hay dispositivos de salida disponibles");
                        break;
                    }
                    int ch = registerFile.read("ACC");
                    ioDevices.get(1).write(ch);
                    System.out.printf("[CPU] OUTPUT_CHAR desde ACC(%d)%n", ch);
                    break;
                }
                default:
                    System.err.println("[CPU] Instrucción no soportada: " + mnemonic);
                    break;
            }

            // Procesar interrupciones pendientes
            interruptManager.process();

        } catch (Exception e) {
            System.err.println("[CPU] Error en ciclo de ejecución: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simula la lectura de la instrucción desde la memoria en la dirección dada.
     * En tu caso, la memoria debería almacenar las instrucciones como Strings.
     */
    private String memoryReadInstruction(int addr) {
        if (instructionMemory == null) {
            System.err.println("[CPU] Memoria de instrucciones no inicializada.");
            return null;
        }
        if (addr < 0 || addr >= instructionMemory.size()) {
            System.err.println("[CPU] Dirección de instrucción fuera de rango: " + addr);
            return null;
        }
        return instructionMemory.get(addr);
    }

    public Memory getMemory() {
        return this.memory;
    }

}
