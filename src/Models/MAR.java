package Models;

public class MAR {
    private int address;

    public void load(int addr) {
        this.address = addr;
    }

    public int get() {
        return address;
    }
}