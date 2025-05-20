package Models;

import java.util.Arrays;
// Ya no necesita importar Map ni HashMap directamente si el microprograma
// y su gestión se delegan a ControlWiredOrMicro.

/**
 * Unidad de control que decodifica instrucciones y genera señales de control
 * basándose en una instancia de ControlWiredOrMicro.
 */
public class ControlUnit {
    private ControlWiredOrMicro controlLogic; // Referencia a la lógica de control (cableada/micro)

    /**
     * Crea una ControlUnit.
     *
     * @param controlLogic La instancia de ControlWiredOrMicro que define el comportamiento
     * y contiene el microprograma.
     */
    public ControlUnit(ControlWiredOrMicro controlLogic) {
        if (controlLogic == null) {
            throw new IllegalArgumentException("ControlWiredOrMicro instance cannot be null.");
        }
        this.controlLogic = controlLogic;
    }

    // El método loadMicroprogram(Map<String, String[]> microprog) ya no es necesario aquí,
    // ya que la carga y gestión del microprograma se realiza en la clase ControlWiredOrMicro.

    /**
     * Decodifica la instrucción en el IR y extrae su mnemonic (primer token).
     *
     * @param ir Registro de instrucción con la línea completa.
     * @return El mnemonic de la instrucción (p.ej. "ADD").
     * @throws IllegalStateException Si IR es nulo, o su contenido es nulo o está vacío.
     */
    public String decode(IR ir) {
        if (ir == null) {
            throw new IllegalStateException("IR instance cannot be null.");
        }
        String instr = ir.get();
        if (instr == null || instr.isBlank()) {
            throw new IllegalStateException("IR no contiene instrucción válida o está vacío.");
        }
        // El primer token es el opcode
        String[] parts = instr.trim().split("\\s+");
        return parts[0]; // parts[0] siempre existirá si instr.isBlank() es falso después de trim().
    }

    /**
     * Genera y emite las señales de control correspondientes al mnemonic dado,
     * usando el microprograma y el modo obtenidos de la instancia ControlWiredOrMicro.
     *
     * @param mnemonic El opcode decodificado.
     */
    public void generateSignals(String mnemonic) {
        String[] signals = null;
        String currentMode = controlLogic.getMode(); // Obtener el modo actual (WIRED o MICRO)

        if ("MICRO".equals(currentMode)) {
            // Solo intentar obtener señales del microprograma si estamos en modo MICRO
            if (controlLogic.getMicroprogram() != null) {
                signals = controlLogic.getMicroprogram().get(mnemonic);
            }

            if (signals != null && signals.length > 0) {
                System.out.println("[CTRL] (" + currentMode + ") Generando señales para: " + mnemonic);
                for (String sig : signals) { // Bucle for-each es más simple aquí
                    System.out.println("[CTRL] Señal -> " + sig);
                }
            } else {
                System.out.println("[CTRL] (" + currentMode + ") No existe microprograma para: " + mnemonic + " o el microprograma no está cargado.");
            }
        } else if ("WIRED".equals(currentMode)) {
            // Lógica para control cableado (actualmente solo un mensaje)
            System.out.println("[CTRL] (" + currentMode + ") Control cableado activado para: " + mnemonic + ". (Simulación de señales específicas para WIRED no implementada).");
            // Aquí podrías tener una lógica diferente para generar/simular señales cableadas
            // si decides implementar esa funcionalidad en detalle.
        } else {
            System.err.println("[CTRL] Modo de control desconocido: " + currentMode);
        }
    }
}