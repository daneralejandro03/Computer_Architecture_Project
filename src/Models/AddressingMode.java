package Models;

public interface AddressingMode {
    int resolve(int addr, CPU cpu);
}