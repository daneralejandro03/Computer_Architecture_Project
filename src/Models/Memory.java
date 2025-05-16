package Models;
import java.util.ArrayList;
import java.util.List;

public class Memory implements AddressingMode {
    private int size;
    private List<Integer> partitions = new ArrayList<>();

    public Memory(int size) {
        this.size = size;
        // Inicializar particiones si es necesario
    }

    public int read(int addr) {
        // Lectura de memoria
        return partitions.get(addr);
    }

    public void write(int addr, int val) {
        // Escritura en memoria
        partitions.set(addr, val);
    }

    @Override
    public int resolve(int addr, CPU cpu) {
        return read(addr);
    }
}