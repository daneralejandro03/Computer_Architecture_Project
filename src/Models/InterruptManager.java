package Models;

import java.util.ArrayList;
import java.util.List;

public class InterruptManager {
    private List<Integer> queue = new ArrayList<>();

    public void request(int n) {
        queue.add(n);
    }

    public void process() {
        // Procesar interrupciones
    }
}