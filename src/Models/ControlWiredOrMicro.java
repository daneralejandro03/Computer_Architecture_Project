package Models;

import java.util.Map;

public class ControlWiredOrMicro {
    private String mode;

    public void loadMicroprogram(Map<String, String> code) {
        // Carga de microprograma
    }

    public void switchMode(String m) {
        this.mode = m;
    }
}
