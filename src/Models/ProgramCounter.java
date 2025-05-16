package Models;

public class ProgramCounter {
    private int value;

    public void increment() {
        value++;
    }

    public void set(int val) {
        this.value = val;
    }

    public int get() {
        return value;
    }
}