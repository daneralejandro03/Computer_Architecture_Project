package Controller;

import Models.CPU;
import Models.Memory; // etc.
import View.SimulatorView; // Suponiendo que la vista está en View.SimulatorView
import java.util.List;
import java.util.Map;

public class SimulatorController {
    private CPU cpuModel;
    private SimulatorView mainView;

    public SimulatorController(CPU model, SimulatorView view) {
        this.cpuModel = model;
        this.mainView = view;
        // Inicialmente, actualiza la vista con el estado inicial del modelo
        updateView();
    }

    public void loadProgram(List<String> instructions, String initialInputData) {
        // Aquí necesitarías un método en CPU para cargar un nuevo programa
        // y reiniciar el estado.
        // cpuModel.reset();
        // cpuModel.loadInstructions(instructions);
        // cpuModel.setInputDataForDevice(initialInputData); // Mecanismo para la entrada
        System.out.println("Programa cargado (simulado). PC reseteado.");
        // pcModel.reset(); // Asumiendo que CPU tiene un método reset que resetea PC etc.
        // this.cpuModel = new CPU(new Memory(2048), directMode, ioDevices, instructions); // O recrear CPU
        updateView();
    }

    public void stepExecution() {
        if (!cpuModel.isHalted()) {
            cpuModel.executeCycle();
            updateView(); // Actualizar la vista después de cada ciclo
        } else {
            mainView.showMessage("CPU está detenida. No se pueden ejecutar más ciclos.");
        }
    }

    public void runFullExecution() {
        // Implementar un bucle que llame a stepExecution() hasta que cpu.isHalted()
        // ¡Cuidado! Esto podría congelar la GUI si se hace en el mismo hilo.
        // Para una ejecución continua, se usa un SwingWorker o un Timer.
        // Por ahora, nos enfocaremos en el "step".
        new Thread(() -> { // Ejecutar en un hilo separado para no congelar la GUI
            while (!cpuModel.isHalted()) {
                cpuModel.executeCycle();
                // Para actualizar la GUI desde otro hilo, usa SwingUtilities.invokeLater
                javax.swing.SwingUtilities.invokeLater(this::updateView);
                try {
                    Thread.sleep(100); // Pequeña pausa para ver la ejecución
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (cpuModel.isHalted()) {
                    mainView.showMessage("CPU completó la ejecución o se detuvo.");
                }
            });
        }).start();
    }


    public void resetSimulation() {
        // Aquí necesitarías un método en CPU para resetear todo a un estado inicial
        // cpuModel.reset(); // Esto implicaría resetear PC, registros, flags, etc.
        // y posiblemente recargar el programa original o limpiar la memoria.
        System.out.println("Simulación reseteada (lógica de reset en CPU pendiente).");
        updateView();
    }

    // Método para que la CPU (a través de un IODevice de GUI) solicite entrada
    public String requestInput(String prompt) {
        return mainView.promptForInput(prompt);
    }

    // Método para que la CPU (a través de un IODevice de GUI) envíe salida
    public void displayOutput(char character) {
        mainView.appendOutput(String.valueOf(character));
    }

    private void updateView() {
        // Obtener todos los datos necesarios de la CPU y sus componentes
        // Esto requiere que CPU, RegisterFile, Memory, etc., tengan getters.
        // mainView.setPC(cpuModel.getPCValue());
        // mainView.setIR(cpuModel.getIRContents());
        // mainView.setACC(cpuModel.getACCValue());
        // mainView.setRegisters(cpuModel.getRegisterFileState());
        // mainView.setMemoryDump(cpuModel.getMemoryContents(0, 64)); // Mostrar primeros 64 bytes
        // mainView.updateFlags(cpuModel.getALUFlags());
        // mainView.setHaltedStatus(cpuModel.isHalted());
        // ... y así sucesivamente para todos los elementos de la GUI
        System.out.println("Vista actualizada (simulado) - PC: " + cpuModel.getPC().get() + " Halted: " + cpuModel.isHalted());
        // En una implementación real, llamarías a los métodos de mainView para actualizar los componentes Swing.
        mainView.refreshDisplay(
                cpuModel.getPC().get(),
                cpuModel.getIR().get(), // Suponiendo que IR tiene un get() que devuelve el string
                cpuModel.getRegisterFile(), // La vista necesitará iterar o obtener valores específicos
                cpuModel.getMemory(), // La vista podría mostrar un segmento
                cpuModel.isHalted(),
                // Pasa más estados aquí...
                cpuModel.getAlu().isZero(), // Ejemplo para flags
                cpuModel.getAlu().isCarry(),
                cpuModel.getAlu().isSign(),
                cpuModel.getAlu().isOverflow()
        );

    }
}