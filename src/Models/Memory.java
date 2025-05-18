package Models;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Memoria principal con particiones lógicas y validación de direcciones.
 * Implementa AddressingMode para resolver direcciones (directo por defecto).
 */
public class Memory implements AddressingMode {
    private final int size;
    private final List<Integer> partitions;

    /**
     * Crea una memoria de tamaño dado, inicializando todas las posiciones a 0.
     * @param size tamaño (número de celdas) de la memoria.
     */
    public Memory(int size) {
        this.size = size;
        this.partitions = new ArrayList<>(Collections.nCopies(size, 0));
    }

    /**
     * Lee un valor de memoria en la dirección indicada.
     * @param addr dirección a leer.
     * @return valor almacenado.
     * @throws IndexOutOfBoundsException si la dirección no es válida.
     */
    public int read(int addr) {
        validateAddress(addr);
        return partitions.get(addr);
    }

    /**
     * Escribe un valor en la dirección de memoria indicada.
     * @param addr dirección destino.
     * @param val valor a escribir.
     * @throws IndexOutOfBoundsException si la dirección no es válida.
     */
    public void write(int addr, int val) {
        validateAddress(addr);
        partitions.set(addr, val);
    }

    private void validateAddress(int addr) {
        if (addr < 0 || addr >= size) {
            throw new IndexOutOfBoundsException("Dirección fuera de rango: " + addr);
        }
    }

    /**
     * Devuelve el tamaño de la memoria.
     * @return tamaño en celdas.
     */
    public int getSize() {
        return size;
    }

    /**
     * Modo de direccionamiento directo: devuelve el contenido de la dirección.
     * @param addr dirección a resolver.
     * @param cpu referencia a la CPU (no usada en modo directo).
     * @return valor leído.
     */
    @Override
    public int resolve(int addr, CPU cpu) {
        return read(addr);
    }
}