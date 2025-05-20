import Models.*;
import Controller.SimulatorController;
import View.SimulatorView; // Asegúrate que los paquetes sean correctos

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Es mejor cargar el programa inicial a través de la GUI o pasarlo al controlador
        // Aquí, solo inicializamos los componentes básicos.

        // --- 1. Crear el Modelo (CPU y sus componentes) ---
        Memory dataMemory = new Memory(2048); // Tamaño de memoria de datos
        List<IODevice> ioDevices = new ArrayList<>(); // Se configurarán con dispositivos GUI o adaptadores

        // Si quieres seguir usando tus IODevices de archivo para la salida/entrada inicial
        // o para logging, puedes hacerlo, pero la GUI necesitará su propia forma de interactuar.
        // Considera crear IODevices específicos para la GUI.
        // Por ahora, dejaremos ioDevices vacío o con versiones adaptadas.

        // Ejemplo de cómo podrías adaptar un TextOutputDevice para que escriba en la GUI
        // TextOutputDevice guiAdaptedOutput = new TextOutputDevice("gui_out", "dummy_path_not_used.txt") {
        //     @Override
        //     public void write(int data) {
        //         // Aquí llamarías al controlador para que actualice la JTextArea de la GUI
        //         // if (controller != null) controller.displayOutput((char)data);
        //         // System.out.print((char)data); // Log a consola si quieres
        //     }
        // };
        // TextInputDevice guiAdaptedInput = new TextInputDevice("gui_in", "dummy_path_not_used.txt") {
        //      private String buffer = "";
        //      private int bufferPos = 0;
        //      public void setInputBuffer(String input) { this.buffer = input + "\n"; this.bufferPos = 0; }
        //
        //      @Override
        //      public int read() {
        //          // if (controller != null && (buffer == null || bufferPos >= buffer.length())) {
        //          //    setInputBuffer(controller.requestInput("Ingrese un caracter:"));
        //          // }
        //          // if (buffer != null && bufferPos < buffer.length()) return buffer.charAt(bufferPos++);
        //          return -1; // EOF
        //      }
        // };
        // ioDevices.add(guiAdaptedInput);
        // ioDevices.add(guiAdaptedOutput);


        AddressingMode directMode = (addr, cpuInstance) -> cpuInstance.getMemory().read(addr);
        List<String> initialInstructions = new ArrayList<>(); // Cargar desde archivo vía GUI después

        CPU cpuModel = new CPU(dataMemory, directMode, ioDevices, initialInstructions);

        // --- 2. Crear la Vista ---
        // Es importante crear la vista en el Event Dispatch Thread (EDT) de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            SimulatorView mainView = new SimulatorView();

            // --- 3. Crear el Controlador y enlazarlo ---
            SimulatorController controller = new SimulatorController(cpuModel, mainView);
            mainView.setController(controller); // La vista necesita una referencia al controlador

            // Pasar las instancias adaptadas de IODevice al controlador para que las use con la GUI
            // if(guiAdaptedOutput instanceof YourGuiAdaptedOutputClassName) {
            //     controller.setGuiOutput((YourGuiAdaptedOutputClassName) guiAdaptedOutput);
            // }
            // if(guiAdaptedInput instanceof YourGuiAdaptedInputClassName) {
            //      controller.setGuiInput((YourGuiAdaptedInputClassName) guiAdaptedInput);
            // }


            mainView.setVisible(true); // Ahora que todo está conectado, hacer visible la GUI
        });
    }
}