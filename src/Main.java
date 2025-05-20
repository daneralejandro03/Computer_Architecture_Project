// Modify your Main.java (the one that sets up MVC) like this:
// package <your_main_package_if_any>; // Asegúrate que el package sea correcto si tienes uno

import Models.*;
import Controller.SimulatorController;
import View.SimulatorView;

import java.io.BufferedReader; // No es necesario aquí si la carga es por GUI
import java.io.FileReader;   // No es necesario aquí
import java.io.IOException;  // No es necesario aquí
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// Map y HashMap no son necesarios en Main si el microprograma se carga en CPU

public class Main {
    public static void main(String[] args) {
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));

        // --- 1. Crear el Modelo (CPU y sus componentes) ---
        // La memoria de datos y el modo de direccionamiento se pueden crear aquí.
        Memory dataMemory = new Memory(2048);
        AddressingMode directMode = (addr, cpuInstance) -> {
            // Asegurarse que cpuInstance y su memoria no son null
            if (cpuInstance != null && cpuInstance.getMemory() != null) {
                return cpuInstance.getMemory().read(addr);
            }
            // Manejar el caso donde no se puede leer, quizás lanzar una excepción o devolver un valor por defecto
            System.err.println("Error en AddressingMode: cpuInstance o su memoria es null.");
            return 0; // O lanzar una excepción apropiada
        };

        // Las instrucciones iniciales estarán vacías; se cargarán a través de la GUI.
        List<String> initialInstructions = new ArrayList<>();

        // Los IODevices se crearán después del controlador, ya que lo necesitan.
        // Por ahora, pasamos una lista vacía o con placeholders si CPU lo requiere.
        // Mejor aún, la CPU podría tener un método para setear los IODevices después.
        // Por simplicidad, los crearemos después y los pasaremos al constructor de CPU,
        // pero el controlador se pasará a los IODevices *después* de que el controlador se cree.
        // Esto es un poco circular. Una mejor manera es que CPU tenga un método setIODevices.

        // Alternativa: Crear CPU sin IODevices y luego setearlos.
        // CPU cpuModel = new CPU(dataMemory, directMode, new ArrayList<>(), initialInstructions);

        // --- 2. Crear la Vista ---
        // Es importante crear la vista en el Event Dispatch Thread (EDT) de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            SimulatorView mainView = new SimulatorView();

            // --- 3. Crear el Controlador ---
            // La CPU se crea aquí y se le pasan los IODevices vacíos por ahora.
            // Los IODevices reales se crearán después y se asignarán al controlador.
            // Esto aún no es ideal. Vamos a crear los IODevices y pasarlos a la CPU,
            // y el controlador se pasará a los IODevices.

            // Los IODevices necesitan el controlador, y la CPU necesita los IODevices.
            // El controlador necesita la CPU y la Vista.
            // Orden: Vista -> CPU (sin IODevices GUI aún) -> Controlador -> IODevices GUI (con controlador) -> Setear IODevices en CPU

            CPU cpuModel = new CPU(dataMemory, directMode, new ArrayList<>(), initialInstructions); // CPU inicial sin IODevices GUI

            SimulatorController controller = new SimulatorController(cpuModel, mainView);
            mainView.setController(controller); // La vista necesita una referencia al controlador

            // --- 4. Crear IODevices GUI y configurarlos en la CPU ---
            GuiTextInputDevice guiInput = new GuiTextInputDevice("GUI_Input_0", controller);
            GuiTextOutputDevice guiOutput = new GuiTextOutputDevice("GUI_Output_1", controller);

            List<IODevice> guiIODevices = Arrays.asList(guiInput, guiOutput);

            // Necesitas un método en CPU para setear los IODevices después de la construcción
            // o modificar el constructor de CPU para que acepte una lista que se pueda llenar después.
            // Por ahora, asumiremos que CPU.java tiene un método: cpuModel.setIoDevicesList(guiIODevices);
            // Si no, la forma más simple es modificar el constructor de CPU para que tome los IODevices
            // y que los IODevices tomen el controlador.

            // *** SOLUCIÓN MÁS DIRECTA PARA EL ERROR ACTUAL ***
            // Crear los IODevices y pasarlos directamente al constructor de CPU.
            // Esto requiere que el controlador ya exista si los IODevices lo necesitan en su constructor.
            // Lo cual es el caso. Entonces:
            // 1. Crear Vista
            // 2. Crear CPU (aún sin IODevices que necesiten el controlador)
            // 3. Crear Controlador (con Vista y CPU)
            // 4. Crear IODevices GUI (pasándoles el Controlador)
            // 5. Setear los IODevices en la CPU (necesita un método setter en CPU)

            // Vamos a añadir un método setIoDevices a CPU.java
            // (Ver la modificación sugerida para CPU.java más abajo)
            cpuModel.setIoDevicesList(guiIODevices);


            mainView.setVisible(true); // Ahora que todo está conectado, hacer visible la GUI
            controller.loadProgramFromFile("Files/program.txt"); // Cargar programa inicial automáticamente
        });
    }
}

// --- Sugerencia de método para añadir a Models/CPU.java ---
// public void setIoDevicesList(List<IODevice> devices) {
//     if (devices == null) {
//         this.ioDevices = new ArrayList<>();
//     } else {
//         this.ioDevices = devices;
//     }
//     System.out.println("[CPU] I/O Devices actualizados. Número de dispositivos: " + this.ioDevices.size());
// }
// --- Fin de sugerencia para CPU.java ---

