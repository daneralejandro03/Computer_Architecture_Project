package Models;

import Controller.SimulatorController; // Make sure this import is correct

public class GuiTextOutputDevice extends IODevice {
    private SimulatorController controller;

    public GuiTextOutputDevice(String id, SimulatorController controller) {
        super(id);
        this.controller = controller;
        this.status = "IDLE";
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("GuiTextOutputDevice no soporta lectura.");
    }

    @Override
    public void write(int data) {
        if (controller == null) {
            System.err.println("GuiTextOutputDevice: Controller no configurado.");
            this.status = "ERROR";
            return;
        }
        this.status = "BUSY";
        controller.displayOutputInView((char) data);
        this.status = "IDLE"; // Vuelve a IDLE después de escribir
    }
}