package Models;

public class IR {
    private String instruction;

    public void load(String instr) {
        this.instruction = instr;
    }

    public String get() {
        return instruction;
    }
}
