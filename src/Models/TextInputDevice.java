package Models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Dispositivo de entrada que lee un archivo de texto carácter a carácter.
 * Cada llamada a read() devuelve el siguiente carácter (código ASCII) o -1 al final.
 */
public class TextInputDevice extends IODevice {
    private final BufferedReader reader;

    /**
     * Crea un TextInputDevice con el identificador dado y ruta al archivo.
     * @param id identificador del dispositivo.
     * @param filePath ruta del archivo de instrucciones.
     * @throws IOException si no se puede abrir el archivo.
     */
    public TextInputDevice(String id, String filePath) throws IOException {
        super(id);
        this.reader = new BufferedReader(new FileReader(filePath));
        this.status = "OPEN";
    }

    /**
     * Lee el siguiente carácter del archivo.
     * @return el código ASCII del carácter, o -1 si fin de archivo.
     */
    @Override
    public int read() {
        try {
            int c = reader.read();
            if (c == -1) {
                status = "EOF";
                reader.close();
            } else {
                status = "BUSY";
            }
            return c;
        } catch (IOException e) {
            status = "ERROR";
            throw new RuntimeException("Error leyendo archivo: " + e.getMessage(), e);
        }
    }

    /**
     * TextInputDevice no soporta escritura.
     * @param data dato a escribir (no utilizado).
     * @throws UnsupportedOperationException siempre.
     */
    @Override
    public void write(int data) {
        throw new UnsupportedOperationException("TextInputDevice no soporta write().");
    }
}
