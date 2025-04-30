package Models;

import java.util.ArrayList;
import java.util.List;

public class Program {

    private List<Instruction> instructions;

    public Program() {
        this.instructions = new ArrayList<>();
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }
}
