import Models.*;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));

        // Ruta del archivo con las instrucciones
        String instructionFile = "Files/program.txt";  // Pon aquí el path real

        // Leer instrucciones desde archivo
        List<String> instructionMemory = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(instructionFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    instructionMemory.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo de instrucciones: " + e.getMessage());
            return;
        }

        // Inicializar memoria de datos con tamaño (ej: 2048 posiciones)
        Memory dataMemory = new Memory(2048);

        // Inicializar dispositivos I/O: un TextInputDevice y un TextOutputDevice
        IODevice inputDevice = null;
        IODevice outputDevice = null;
        try {
            inputDevice = new TextInputDevice("input.txt", "Files/input.txt");
            outputDevice = new TextOutputDevice("output.txt", "OutputDevice");
        } catch (IOException e) {
            System.err.println("Error inicializando dispositivos I/O: " + e.getMessage());
            return;
        }
        List<IODevice> ioDevices = Arrays.asList(inputDevice, outputDevice);

        // Modo de direccionamiento directo
        AddressingMode directMode = (addr, cpu) -> cpu.getMemory().read(addr);

        // Crear CPU con memoria de datos, direccionamiento y dispositivos I/O
        CPU cpu = new CPU(dataMemory, directMode, ioDevices, instructionMemory);

        // Ejecutar ciclos de CPU, por ejemplo 120
        System.out.println("Inicio de simulación CPU:");
        for (int i = 0; i < 120; i++) {
            System.out.printf("Ciclo %d\n", i+1);
            cpu.executeCycle();
        }
        System.out.println("Fin de simulación CPU.");
    }
}
