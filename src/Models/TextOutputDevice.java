package Models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Dispositivo de salida que escribe caracteres en un archivo de texto.
 */
public class TextOutputDevice extends IODevice {
    private BufferedWriter writer;

    /**
     * Crea un TextOutputDevice con un identificador y archivo destino.
     *
     * @param filePath Ruta del archivo de salida.
     * @param id       Identificador del dispositivo.
     * @throws IOException Si no se puede abrir el archivo.
     */
    public TextOutputDevice(String filePath, String id) throws IOException {
        super(id);
        this.writer = new BufferedWriter(new FileWriter(filePath));
        this.status = "OPEN";
    }

    /**
     * Escribe un carácter en el archivo.
     *
     * @param data Código ASCII del carácter a escribir.
     */
    @Override
    public void write(int data) {
        try {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            this.status = "ERROR";
            throw new RuntimeException("Error escribiendo en archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Dispositivo de salida no soporta lectura.
     *
     * @return Siempre -1.
     */
    @Override
    public int read() {
        throw new UnsupportedOperationException("TextOutputDevice no soporta read().");
    }

    /**
     * Cierra el archivo de salida.
     */
    public void close() {
        try {
            writer.close();
            this.status = "CLOSED";
        } catch (IOException e) {
            this.status = "ERROR";
            throw new RuntimeException("Error cerrando archivo: " + e.getMessage(), e);
        }
    }
}
