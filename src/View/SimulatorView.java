package View;

import Controller.SimulatorController;
import Models.RegisterFile;
import Models.Memory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
// Eliminado ActionEvent ya que usamos lambdas y no se necesita explícitamente
// Eliminado Map ya que RegisterFile.getRegisterNames() y read() son suficientes para el JTextArea
import javax.swing.filechooser.FileNameExtensionFilter; // Para filtrar archivos

public class SimulatorView extends JFrame {
    private SimulatorController controller;

    // Componentes de la GUI - Panel de Control
    private JButton stepButton;
    private JButton runButton;
    private JButton loadButton;
    private JButton resetButton;

    // Componentes de la GUI - Panel de Estado de CPU
    private JTextField pcValueField;
    private JTextField irValueField;
    private JTextField accValueField; // Acumulador
    private JTextField marValueField; // Memory Address Register
    private JTextField mbrValueField; // Memory Buffer Register
    private JTextArea registersArea;  // Para registros generales
    private JPanel flagsPanel;        // Panel para las banderas
    private JCheckBox zeroFlagCheckBox;
    private JCheckBox carryFlagCheckBox;
    private JCheckBox signFlagCheckBox;
    private JCheckBox overflowFlagCheckBox;


    // Componentes de la GUI - Panel Principal
    private JTextArea memoryArea;
    private JTextArea outputArea;
    private JTextField inputField; // Para la entrada del usuario (podría ser mejorado)
    private JButton submitInputButton; // Botón para enviar la entrada del inputField

    public SimulatorView() {
        setTitle("Simulador CPU Gráfico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // Un poco más grande para acomodar más info
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10)); // Añadir espacio entre componentes
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen general

        initComponents();
        // setVisible(true); // Se hará visible desde Main después de setController
    }

    private void initComponents() {
        // --- Panel de Control (Botones) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        loadButton = new JButton("Cargar Programa");
        stepButton = new JButton("Ejecutar Ciclo (Step)");
        runButton = new JButton("Ejecutar Completo (Run)");
        resetButton = new JButton("Reset");

        controlPanel.add(loadButton);
        controlPanel.add(stepButton);
        controlPanel.add(runButton);
        controlPanel.add(resetButton);
        add(controlPanel, BorderLayout.NORTH);

        // --- Panel de Estado de la CPU (Registros, PC, IR, etc.) ---
        JPanel statusPanelContainer = new JPanel(new BorderLayout(5,5));
        statusPanelContainer.setBorder(BorderFactory.createTitledBorder("Estado de la CPU"));

        JPanel cpuRegistersPanel = new JPanel();
        // Usar GridBagLayout para más control sobre la alineación
        cpuRegistersPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Espaciado
        gbc.anchor = GridBagConstraints.WEST;

        // PC
        gbc.gridx = 0; gbc.gridy = 0;
        cpuRegistersPanel.add(new JLabel("PC:"), gbc);
        pcValueField = new JTextField(8);
        pcValueField.setEditable(false);
        pcValueField.setFont(new Font("Monospaced", Font.BOLD, 12));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cpuRegistersPanel.add(pcValueField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        // IR
        gbc.gridx = 0; gbc.gridy = 1;
        cpuRegistersPanel.add(new JLabel("IR:"), gbc);
        irValueField = new JTextField(20);
        irValueField.setEditable(false);
        irValueField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cpuRegistersPanel.add(irValueField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // ACC
        gbc.gridx = 0; gbc.gridy = 2;
        cpuRegistersPanel.add(new JLabel("ACC:"), gbc);
        accValueField = new JTextField(8);
        accValueField.setEditable(false);
        accValueField.setFont(new Font("Monospaced", Font.BOLD, 12));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cpuRegistersPanel.add(accValueField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // MAR
        gbc.gridx = 0; gbc.gridy = 3;
        cpuRegistersPanel.add(new JLabel("MAR:"), gbc);
        marValueField = new JTextField(8);
        marValueField.setEditable(false);
        marValueField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cpuRegistersPanel.add(marValueField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // MBR
        gbc.gridx = 0; gbc.gridy = 4;
        cpuRegistersPanel.add(new JLabel("MBR:"), gbc);
        mbrValueField = new JTextField(8);
        mbrValueField.setEditable(false);
        mbrValueField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cpuRegistersPanel.add(mbrValueField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        statusPanelContainer.add(cpuRegistersPanel, BorderLayout.NORTH);

        // Registros Generales
        registersArea = new JTextArea(8, 25);
        registersArea.setEditable(false);
        registersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane registersScrollPane = new JScrollPane(registersArea);
        registersScrollPane.setBorder(BorderFactory.createTitledBorder("Registros Generales"));
        statusPanelContainer.add(registersScrollPane, BorderLayout.CENTER);

        // Banderas de la ALU
        flagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flagsPanel.setBorder(BorderFactory.createTitledBorder("Banderas ALU"));
        zeroFlagCheckBox = new JCheckBox("Zero (Z)");
        zeroFlagCheckBox.setEnabled(false); // Solo visualización
        carryFlagCheckBox = new JCheckBox("Carry (C)");
        carryFlagCheckBox.setEnabled(false);
        signFlagCheckBox = new JCheckBox("Sign (S/N)");
        signFlagCheckBox.setEnabled(false);
        overflowFlagCheckBox = new JCheckBox("Overflow (V/O)");
        overflowFlagCheckBox.setEnabled(false);
        flagsPanel.add(zeroFlagCheckBox);
        flagsPanel.add(carryFlagCheckBox);
        flagsPanel.add(signFlagCheckBox);
        flagsPanel.add(overflowFlagCheckBox);
        statusPanelContainer.add(flagsPanel, BorderLayout.SOUTH);


        // --- Panel Principal (Memoria, Salida, Entrada) ---
        JPanel mainDisplayPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 fila, 2 columnas, con espaciado

        // Área de Memoria
        memoryArea = new JTextArea(20, 35);
        memoryArea.setEditable(false);
        memoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane memoryScrollPane = new JScrollPane(memoryArea);
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder("Memoria de Datos"));
        mainDisplayPanel.add(memoryScrollPane);

        // Panel de I/O (Salida y Entrada)
        JPanel ioPanel = new JPanel(new BorderLayout(5,5));
        ioPanel.setBorder(BorderFactory.createTitledBorder("Entrada/Salida del Programa"));

        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setLineWrap(true); // Para que el texto se ajuste
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        // outputScrollPane.setBorder(BorderFactory.createTitledBorder("Salida del Programa")); // El título ya está en ioPanel
        ioPanel.add(outputScrollPane, BorderLayout.CENTER);

        JPanel inputAreaPanel = new JPanel(new BorderLayout(5,0));
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // inputField.setBorder(BorderFactory.createTitledBorder("Entrada para INPUT_CHAR")); // Título puede ser redundante
        submitInputButton = new JButton("Enviar Entrada");
        inputAreaPanel.add(new JLabel("Entrada:"), BorderLayout.WEST);
        inputAreaPanel.add(inputField, BorderLayout.CENTER);
        inputAreaPanel.add(submitInputButton, BorderLayout.EAST);
        ioPanel.add(inputAreaPanel, BorderLayout.SOUTH);

        mainDisplayPanel.add(ioPanel);


        // --- Ensamblar paneles principales ---
        // Usar JSplitPane para permitir al usuario redimensionar las áreas
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, statusPanelContainer, mainDisplayPanel);
        centerSplit.setResizeWeight(0.35); // Darle un poco más de espacio al panel de estado
        add(centerSplit, BorderLayout.CENTER);

        // Action Listeners se configuran en setController
    }

    public void setController(SimulatorController controller) {
        this.controller = controller;
        // Ahora que tenemos el controlador, configuramos los listeners
        stepButton.addActionListener(e -> controller.stepExecution());
        runButton.addActionListener(e -> controller.runFullExecution());
        resetButton.addActionListener(e -> controller.resetSimulation());

        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/Files");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de Programa (.txt, .asm)", "txt", "asm");
            fileChooser.setFileFilter(filter);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String programPath = fileChooser.getSelectedFile().getAbsolutePath();
                if (this.controller != null) {
                    this.controller.loadProgramFromFile(programPath);
                }
            }
        });

        // Listener para el botón de enviar entrada (ejemplo básico)
        // La lógica real de cómo esta entrada se consume por la CPU es más compleja
        // y depende de cómo implementes tu IODevice de GUI.
        submitInputButton.addActionListener(e -> {
            String inputText = inputField.getText();
            if (this.controller != null && !inputText.isEmpty()) {
                // Esto es conceptual. El controlador necesitaría un método para manejar esta entrada
                // y de alguna manera pasarla al IODevice de la CPU cuando esté esperando.
                // controller.provideInputToCPU(inputText);
                System.out.println("Entrada enviada (simulado): " + inputText);
                // outputArea.append("Entrada manual: " + inputText + "\n"); // Para feedback visual
                // inputField.setText(""); // Limpiar campo
            }
        });
    }

    /**
     * Actualiza todos los componentes de la GUI con el estado actual del simulador.
     * Se añaden MBR y MAR a la firma.
     */
    public void refreshDisplay(int pc, String ir, RegisterFile registers, Memory memory, boolean halted,
                               boolean zeroFlag, boolean carryFlag, boolean signFlag, boolean overflowFlag,
                               int mbrValue, int marValue) { // Añadidos MBR y MAR
        pcValueField.setText(String.valueOf(pc));
        irValueField.setText(ir != null ? ir : "---");

        if (registers != null) {
            accValueField.setText(String.valueOf(registers.read("ACC")));
            // Mostrar otros registros generales
            StringBuilder regText = new StringBuilder();
            // Ordenar los nombres de los registros para una visualización consistente
            java.util.List<String> sortedRegNames = new java.util.ArrayList<>(registers.getRegisterNames());
            java.util.Collections.sort(sortedRegNames);

            for (String regName : sortedRegNames) {
                if (!regName.equals("ACC")) { // Ya mostramos ACC por separado
                    regText.append(String.format("%-4s: %d\n", regName, registers.read(regName)));
                }
            }
            registersArea.setText(regText.toString());
        } else {
            accValueField.setText("N/A");
            registersArea.setText("Registros no disponibles.");
        }

        marValueField.setText(String.valueOf(marValue));
        mbrValueField.setText(String.valueOf(mbrValue));

        // Actualizar Banderas
        zeroFlagCheckBox.setSelected(zeroFlag);
        carryFlagCheckBox.setSelected(carryFlag);
        signFlagCheckBox.setSelected(signFlag);
        overflowFlagCheckBox.setSelected(overflowFlag);

        // Actualizar Memoria (mostrar un rango, por ejemplo, las primeras 64 posiciones)
        StringBuilder memText = new StringBuilder();
        if (memory != null) {
            int displaySize = Math.min(64, memory.getSize()); // Mostrar hasta 64 o el tamaño real
            for (int i = 0; i < displaySize; i++) {
                // Formato: [dirección_hex]: valor_decimal (valor_hex)
                memText.append(String.format("[%04X]: %-5d (%04X)\n", i, memory.read(i), memory.read(i) & 0xFFFF));
                if ((i + 1) % 4 == 0 && i < displaySize -1) { // Añadir una línea extra cada 4 para agrupar
                    // memText.append("\n"); // Opcional para más espaciado visual
                }
            }
        } else {
            memoryArea.setText("Memoria no disponible.");
        }
        memoryArea.setText(memText.toString());
        if (memoryArea.getDocument().getLength() > 0) { // Evitar error si está vacío
            memoryArea.setCaretPosition(0); // Scroll al inicio
        }


        // Habilitar/deshabilitar botones de control
        setExecutionControlsEnabled(!halted);
    }


    public void appendOutput(String text) {
        // Asegurar que se actualiza en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength()); // Auto-scroll
        });
    }

    /**
     * Muestra un diálogo para solicitar entrada al usuario.
     * Esta es una implementación bloqueante.
     * @param promptMessage El mensaje a mostrar al usuario.
     * @return La cadena ingresada por el usuario, o null si cancela.
     */
    public String promptForInput(String promptMessage) {
        // Esta es una forma simple y bloqueante.
        // Para una mejor experiencia de usuario, se necesitaría un manejo más asíncrono.
        return JOptionPane.showInputDialog(this, promptMessage, "Entrada Requerida", JOptionPane.QUESTION_MESSAGE);
    }


    public void showMessage(String message) {
        // Asegurar que se actualiza en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    /**
     * Habilita o deshabilita los controles de ejecución (Step, Run).
     * El botón de Load y Reset suelen permanecer habilitados.
     * @param enabled true para habilitar, false para deshabilitar.
     */
    public void setExecutionControlsEnabled(boolean enabled) {
        stepButton.setEnabled(enabled);
        runButton.setEnabled(enabled);
        // loadButton.setEnabled(enabled); // Generalmente se deja habilitado para cargar otro programa
        // resetButton.setEnabled(true); // Reset siempre debería estar disponible
    }
}