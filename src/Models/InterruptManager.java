package Models;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Administrador de interrupciones: cola FIFO de códigos de interrupción.
 */
public class InterruptManager {
    final Queue<Integer> queue = new ArrayDeque<>();

    /**
     * Solicita una interrupción identificada por su código.
     * @param code código de interrupción.
     */
    public void request(int code) {
        queue.add(code);
    }

    /**
     * Procesa todas las interrupciones en cola.
     * Por defecto, imprime cada código y limpia la cola.
     */
    public void process() {
        while (!queue.isEmpty()) {
            int code = queue.poll();
            // Aquí podría llamarse a una rutina de servicio de interrupción.
            System.out.println("[INT] Procesando interrupción code=" + code);
        }
    }

    /**
     * Comprueba si hay interrupciones pendientes.
     * @return true si la cola no está vacía.
     */
    public boolean hasPending() {
        return !queue.isEmpty();
    }

    @Override
    public String toString() {
        return "InterruptManager" + queue.toString();
    }
}
