package Models;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Set;

public class RegisterFile {

    // --- Clase interna para almacenar el valor y su procedencia ---
    public static class RegisterInfo {
        public final int value;
        public final String description;

        public RegisterInfo(int value, String description) {
            this.value = value;
            this.description = description;
        }
    }

    private final Map<String, RegisterInfo> regs = new HashMap<>();

    /**
     * Lee solo el valor numérico de un registro.
     */
    public int read(String name) {
        RegisterInfo info = regs.get(name);
        return (info != null) ? info.value : 0;
    }

    /**
     * Obtiene el objeto completo con valor y descripción.
     */
    public RegisterInfo getRegisterInfo(String name) {
        return regs.get(name);
    }

    /**
     * Escribe un valor y su descripción en un registro.
     */
    public void write(String name, int val, String description) {
        regs.put(name, new RegisterInfo(val, description));
    }

    public void clear() {
        regs.clear();
    }

    public Set<String> getRegisterNames() {
        return Collections.unmodifiableSet(regs.keySet());
    }

    @Override
    public String toString() {
        return "RegisterFile" + regs.toString();
    }
}