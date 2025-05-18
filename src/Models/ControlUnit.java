package Models;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Unidad de control que decodifica instrucciones y genera señales de control
 * basándose en un microprograma configurado.
 */
public class ControlUnit {
    // Mapa de mnemonic -> secuencia de señales de control
    private Map<String, String[]> microprogram = new HashMap<>();

    /**
     * Carga o actualiza el microprograma.
     * @param microprog Mapa que asocia cada mnemonic con su array de señales.
     */
    public void loadMicroprogram(Map<String, String[]> microprog) {
        this.microprogram = microprog;
    }

    /**
     * Decodifica la instrucción en el IR y extrae su mnemonic (primer token).
     * @param ir Registro de instrucción con la línea completa.
     * @return El mnemonic de la instrucción (p.ej. "ADD").
     * @throws IllegalStateException Si IR es nulo o está vacío.
     */
    public String decode(IR ir) {
        String instr = ir.get();
        if (instr == null || instr.isBlank()) {
            throw new IllegalStateException("IR no contiene instrucción válida.");
        }
        // El primer token es el opcode
        String[] parts = instr.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Genera y emite las señales de control correspondientes al mnemonic dado.
     * @param mnemonic El opcode decodificado.
     */
    public void generateSignals(String mnemonic) {
        String[] signals = microprogram.get(mnemonic);
        if (signals != null && signals.length > 0) {
            System.out.println("[CTRL] Generando señales para: " + mnemonic);
            Arrays.stream(signals)
                    .forEach(sig -> System.out.println("[CTRL] Señal -> " + sig));
        } else {
            System.out.println("[CTRL] No existe microprograma para: " + mnemonic);
        }
    }
}
