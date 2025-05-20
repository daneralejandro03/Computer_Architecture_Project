package Controller;

import Models.CPU;
import Models.RegisterFile; // For type hint in refreshDisplay if needed
import Models.Memory;     // For type hint in refreshDisplay if needed
import View.SimulatorView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// No se necesita Map aquí a menos que el controlador mismo manipule mapas de microprogramas.

public class SimulatorController {
    private CPU cpuModel;
    private SimulatorView mainView;
    private List<String> lastLoadedProgram = new ArrayList<>(); // Para la función de reset

    public SimulatorController(CPU model, SimulatorView view) {
        this.cpuModel = model;
        this.mainView = view;
        // Actualiza la vista con el estado inicial del modelo (CPU vacía o con programa por defecto)
        updateView();
    }

    /**
     * Carga instrucciones desde un archivo en la CPU y la resetea.
     * @param filePath Ruta del archivo de programa.
     */
    public void loadProgramFromFile(String filePath) {
        List<String> instructions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("//")) {
                    instructions.add(line.trim());
                }
            }
        } catch (IOException e) {
            mainView.showMessage("Error al leer el archivo de programa: " + e.getMessage());
            return;
        }

        if (instructions.isEmpty()) {
            mainView.showMessage("El archivo de programa '" + filePath + "' está vacío o no contiene instrucciones válidas.");
            return;
        }

        this.lastLoadedProgram = new ArrayList<>(instructions); // Guardar para el reset
        cpuModel.loadNewProgram(instructions); // Usa el método en CPU que resetea y carga
        mainView.showMessage("Programa cargado desde: " + filePath + " (" + instructions.size() + " instrucciones)");
        updateView(); // Actualizar la vista para reflejar el estado reseteado y listo
    }

    /**
     * Ejecuta un solo ciclo de la CPU y actualiza la vista.
     */
    public void stepExecution() {
        if (!cpuModel.isHalted()) {
            cpuModel.executeCycle();
            updateView(); // Actualizar la vista después de cada ciclo
        } else {
            mainView.showMessage("CPU está detenida. No se pueden ejecutar más ciclos.\nCargue un programa o resetee.");
        }
    }

    /**
     * Ejecuta la simulación completa en un hilo separado para no bloquear la GUI.
     */
    public void runFullExecution() {
        if (cpuModel.isHalted()) {
            mainView.showMessage("CPU ya está detenida. Cargue un programa o resetee para ejecutar.");
            return;
        }

        // Deshabilitar botones mientras corre para evitar múltiples ejecuciones
        mainView.setExecutionControlsEnabled(false);

        new Thread(() -> {
            while (!cpuModel.isHalted()) {
                cpuModel.executeCycle();
                // Para actualizar la GUI desde otro hilo, usa SwingUtilities.invokeLater
                javax.swing.SwingUtilities.invokeLater(this::updateView);
                try {
                    Thread.sleep(100); // Pequeña pausa para visualización (ajustable)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaurar estado de interrupción
                    javax.swing.SwingUtilities.invokeLater(() -> mainView.showMessage("Ejecución interrumpida."));
                    break; // Salir del bucle si el hilo es interrumpido
                }
            }
            // Cuando termina el bucle (halted o interrumpido)
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (cpuModel.isHalted()) {
                    mainView.showMessage("CPU completó la ejecución o se detuvo.");
                }
                updateView(); // Una última actualización para el estado final
                mainView.setExecutionControlsEnabled(true); // Rehabilitar botones
            });
        }).start();
    }

    /**
     * Resetea la simulación, recargando el último programa cargado (o un estado vacío).
     */
    public void resetSimulation() {
        // Recargar el último programa conocido o un estado vacío si no hay ninguno.
        // Si lastLoadedProgram está vacío, loadNewProgram lo manejará creando una lista vacía.
        cpuModel.loadNewProgram(new ArrayList<>(lastLoadedProgram));
        mainView.showMessage("Simulación reseteada. PC en 0. Programa recargado (si aplica).");
        updateView();
    }

    /**
     * Pide entrada al usuario a través de la vista.
     * Esta es una implementación simple y bloqueante.
     * @param prompt Mensaje para el usuario.
     * @return La entrada del usuario o null si se cancela.
     */
    public String requestInputFromView(String prompt) {
        // Esta función se llamaría desde un GuiInputDialog que a su vez es llamado por la CPU
        // cuando encuentra una instrucción INPUT_CHAR.
        return mainView.promptForInput(prompt);
    }

    /**
     * Envía un carácter para ser mostrado en el área de salida de la vista.
     * @param character El carácter a mostrar.
     */
    public void displayOutputInView(char character) {
        // Esta función se llamaría desde un GuiOutputDialog que a su vez es llamado por la CPU
        // cuando encuentra una instrucción OUTPUT_CHAR.
        mainView.appendOutput(String.valueOf(character));
    }

    /**
     * Obtiene el estado actual de la CPU y le dice a la vista que se actualice.
     */
    private void updateView() {
        if (cpuModel == null || mainView == null) return; // Salvaguarda

        // Usar los getters que añadimos a CPU.java
        mainView.refreshDisplay(
                cpuModel.getPCValue(),            // int
                cpuModel.getIRValue(),            // String
                cpuModel.getRegisterFile(),       // RegisterFile (la vista puede usar sus métodos)
                cpuModel.getMemory(),             // Memory (la vista puede usar sus métodos)
                cpuModel.isHalted(),              // boolean
                cpuModel.getAlu().isZero(),       // boolean
                cpuModel.getAlu().isCarry(),      // boolean
                cpuModel.getAlu().isSign(),       // boolean
                cpuModel.getAlu().isOverflow(),   // boolean
                // Podrías añadir MBR y MAR si los quieres mostrar:
                cpuModel.getMBR().get(),          // int
                cpuModel.getMAR().get()           // int
        );
    }
}