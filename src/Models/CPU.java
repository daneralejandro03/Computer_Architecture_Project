package Models;
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

    public CPU(Memory mem, AddressingMode addrMode, List<IODevice> devices) {
        this.memory = mem;
        this.addressingMode = addrMode;
        this.ioDevices = devices;
    }

    public void executeCycle() {
        // Implementar ciclo de ejecución de la CPU
    }
}