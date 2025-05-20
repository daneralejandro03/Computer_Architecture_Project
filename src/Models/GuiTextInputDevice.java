package Models;

import Controller.SimulatorController; // Make sure this import is correct

public class GuiTextInputDevice extends IODevice {
    private SimulatorController controller;
    private String buffer = "";
    private int bufferPos = 0;
    private boolean waitingForInput = false;

    public GuiTextInputDevice(String id, SimulatorController controller) {
        super(id);
        this.controller = controller;
        this.status = "IDLE";
    }

    @Override
    public int read() {
        if (controller == null) {
            System.err.println("GuiTextInputDevice: Controller no configurado.");
            this.status = "ERROR";
            return -1;
        }

        // Si el buffer está vacío o se ha consumido, pedir nueva entrada
        if (bufferPos >= buffer.length()) {
            this.status = "WAITING";
            waitingForInput = true; // Señal para el controlador/vista
            // En una implementación más avanzada, la CPU pausaría aquí.
            // Por ahora, el JOptionPane en promptForInput es bloqueante.
            String inputLine = controller.requestInputFromView("Ingrese entrada para " + getId() + ":");
            waitingForInput = false;

            if (inputLine == null) { // Usuario canceló o cerró el diálogo
                this.status = "EOF"; // O un error, dependiendo de cómo quieras manejarlo
                buffer = "";
                bufferPos = 0;
                return -1;
            }
            buffer = inputLine + "\n"; // Añadir newline para simular entrada de línea
            bufferPos = 0;
            this.status = "BUSY"; // Procesando nueva entrada
        }

        if (bufferPos < buffer.length()) {
            this.status = "BUSY";
            return buffer.charAt(bufferPos++);
        } else {
            // Esto no debería ocurrir si la lógica anterior es correcta
            this.status = "EOF"; // O IDLE si se espera más entrada después
            return -1;
        }
    }

    public boolean isWaitingForInput() {
        return waitingForInput;
    }

    @Override
    public void write(int data) {
        throw new UnsupportedOperationException("GuiTextInputDevice no soporta escritura.");
    }
}

