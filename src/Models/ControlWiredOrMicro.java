package Models;

import java.util.Map;
import java.util.HashMap;

/**
 * Control por cableado o microprogramado: permite cargar un microprograma
 * y alternar entre modos WIRED y MICRO.
 */
public class ControlWiredOrMicro {
    private String mode = "WIRED";
    private Map<String, String[]> microprogram = new HashMap<>();

    /**
     * Carga un microprograma (mnemonic -> array de señales) y activa el modo MICRO.
     * @param microprog Mapa de mnemonic a señales.
     */
    public void loadMicroprogram(Map<String, String[]> microprog) {
        this.microprogram = microprog;
        this.mode = "MICRO";
    }

    /**
     * Cambia el modo de control.
     * @param m Debe ser "WIRED" o "MICRO".
     * @throws IllegalArgumentException si el modo no es válido.
     */
    public void switchMode(String m) {
        if (!"WIRED".equals(m) && !"MICRO".equals(m)) {
            throw new IllegalArgumentException("Modo inválido: " + m);
        }
        this.mode = m;
    }

    /**
     * Devuelve el modo actual (WIRED o MICRO).
     */
    public String getMode() {
        return mode;
    }

    /**
     * Obtiene el microprograma cargado.
     */
    public Map<String, String[]> getMicroprogram() {
        return microprogram;
    }
}
