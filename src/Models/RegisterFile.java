package Models;
import java.util.HashMap;
import java.util.Map;

public class RegisterFile {
    private Map<String, Integer> regs = new HashMap<>();

    public int read(String name) {
        return regs.getOrDefault(name, 0);
    }

    public void write(String name, int val) {
        regs.put(name, val);
    }
}