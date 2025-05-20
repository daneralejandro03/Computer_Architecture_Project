package View;

import Controller.SimulatorController;
import Models.RegisterFile; // Para el tipo de dato
import Models.Memory;     // Para el tipo de dato

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map; // Para los registros

public class SimulatorView extends JFrame {
    private SimulatorController controller;

    // Componentes de la GUI
    private JButton stepButton;
    private JButton runButton;
    private JButton loadButton;
    private JButton resetButton;

    private JLabel pcLabel;
    private JTextField pcValueField;
    private JLabel irLabel;
    private JTextField irValueField;
    // ... más campos para ACC, otros registros, flags ...

    private JTextArea memoryArea;
    private JTextArea outputArea;
    private JTextField inputField; // Para la entrada del usuario

    public SimulatorView() {
        setTitle("Simulador CPU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        initComponents();
        setVisible(true); // Mover al final después de que el controlador esté listo
    }

    private void initComponents() {
        // Panel de Control (Botones)
        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Cargar Programa");
        stepButton = new JButton("Ejecutar Ciclo (Step)");
        runButton = new JButton("Ejecutar Completo (Run)");
        resetButton = new JButton("Reset");

        controlPanel.add(loadButton);
        controlPanel.add(stepButton);
        controlPanel.add(runButton);
        controlPanel.add(resetButton);
        add(controlPanel, BorderLayout.NORTH);

        // Panel de Estado de la CPU (Registros, PC, IR)
        JPanel statusPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // 0 filas = tantas como sea necesario
        pcLabel = new JLabel("PC:");
        pcValueField = new JTextField(10);
        pcValueField.setEditable(false);
        irLabel = new JLabel("IR:");
        irValueField = new JTextField(20);
        irValueField.setEditable(false);

        statusPanel.add(pcLabel);
        statusPanel.add(pcValueField);
        statusPanel.add(irLabel);
        statusPanel.add(irValueField);
        // ... añadir más JLabels y JTextFields para ACC, MBR, MAR, Registros Generales, Flags ...

        // Ejemplo para registros generales (simplificado, necesitarías un panel más dinámico)
        // JTextArea registersArea = new JTextArea(10, 20);
        // registersArea.setEditable(false);
        // statusPanel.add(new JLabel("Registros:"));
        // statusPanel.add(new JScrollPane(registersArea));


        // Panel Principal (Memoria, Salida, Entrada)
        JPanel mainDisplayPanel = new JPanel(new GridLayout(1, 2));

        // Área de Memoria
        memoryArea = new JTextArea(20, 30);
        memoryArea.setEditable(false);
        mainDisplayPanel.add(new JScrollPane(memoryArea));
        memoryArea.setBorder(BorderFactory.createTitledBorder("Memoria"));


        // Panel de I/O
        JPanel ioPanel = new JPanel(new BorderLayout());
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("Salida del Programa"));
        ioPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        inputField = new JTextField(); // Podrías tener un botón para enviar la entrada
        inputField.setBorder(BorderFactory.createTitledBorder("Entrada para INPUT_CHAR"));
        ioPanel.add(inputField, BorderLayout.SOUTH);
        mainDisplayPanel.add(ioPanel);


        // Ensamblar paneles
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, statusPanel, mainDisplayPanel);
        centerSplit.setResizeWeight(0.3); // Darle menos espacio inicial al panel de estado
        add(centerSplit, BorderLayout.CENTER);


        // Action Listeners (se configuran cuando el controlador está listo)
    }

    public void setController(SimulatorController controller) {
        this.controller = controller;
        // Ahora que tenemos el controlador, configuramos los listeners
        stepButton.addActionListener(e -> controller.stepExecution());
        runButton.addActionListener(e -> controller.runFullExecution());
        resetButton.addActionListener(e -> controller.resetSimulation());
        loadButton.addActionListener(e -> {
            // Lógica para seleccionar archivo de programa
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/Files");
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String programPath = fileChooser.getSelectedFile().getAbsolutePath();
                // Aquí necesitarías leer el archivo y luego llamar a controller.loadProgram(...)
                // Esta parte de leer el archivo puede estar en el controlador o aquí.
                System.out.println("Cargar programa desde: " + programPath + " (lógica de carga pendiente)");
                // List<String> instructions = loadInstructionsFromFile(programPath);
                // controller.loadProgram(instructions, ""); // Pasa datos de entrada si es necesario
            }
        });
    }

    // Métodos para actualizar la GUI llamados por el Controlador
    public void refreshDisplay(int pc, String ir, RegisterFile registers, Memory memory, boolean halted,
                               boolean zeroFlag, boolean carryFlag, boolean signFlag, boolean overflowFlag) {
        pcValueField.setText(String.valueOf(pc));
        irValueField.setText(ir != null ? ir : "N/A");
        // Aquí actualizarías todos los demás campos: ACC, otros registros, flags...
        // Ejemplo para mostrar registros (necesitarías un componente adecuado):
        // StringBuilder regText = new StringBuilder();
        // if (registers != null) {
        //     for (String regName : registers.getRegisterNames()) {
        //         regText.append(regName).append(": ").append(registers.read(regName)).append("\n");
        //     }
        // }
        // registersArea.setText(regText.toString());

        // Ejemplo para mostrar memoria (necesitarías un componente adecuado o lógica para formatear):
        StringBuilder memText = new StringBuilder();
        if (memory != null) {
            for (int i = 0; i < 32; i++) { // Mostrar las primeras 32 posiciones
                memText.append(String.format("[%04d]: %d\n", i, memory.read(i)));
            }
        }
        memoryArea.setText(memText.toString());


        stepButton.setEnabled(!halted); // Deshabilitar si la CPU está detenida
        runButton.setEnabled(!halted);
    }


    public void appendOutput(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength()); // Auto-scroll
    }

    public String promptForInput(String promptMessage) {
        // Para una entrada simple, podrías usar JOptionPane
        // return JOptionPane.showInputDialog(this, promptMessage, "Entrada Requerida", JOptionPane.QUESTION_MESSAGE);
        // O, si quieres usar el inputField:
        JOptionPane.showMessageDialog(this, promptMessage + "\nPor favor, escribe en el campo 'Entrada para INPUT_CHAR' y presiona Enter (o un botón 'Enviar Entrada').");
        // Necesitarías un mecanismo para que el controlador espere esta entrada.
        // Esto es más complejo y puede requerir pausar la ejecución de la CPU.
        // Una solución simple es que el inputField tenga un ActionListener que llame a un método en el controller.
        // Por ahora, devolvemos el contenido del campo de texto, pero la sincronización es el desafío.
        String input = inputField.getText();
        inputField.setText(""); // Limpiar después de leer
        return input; // Esto es síncrono, la CPU se bloquearía. Para evitarlo, se necesita un enfoque más asíncrono.
    }


    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    // Este método es un ejemplo, necesitarías implementarlo o ponerlo en el controlador
    // private List<String> loadInstructionsFromFile(String filePath) { ... }
}