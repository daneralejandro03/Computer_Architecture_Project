package Models;

public class Instruction {

    private String opcode;
    private int operand1;
    private int operand2;

    public Instruction(String opcode, int operand1, int operand2) {
        this.opcode = opcode;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public String getOpcode() {
        return opcode;
    }

    public int getOperand1() {
        return operand1;
    }

    public int getOperand2() {
        return operand2;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public void setOperand1(int operand1) {
        this.operand1 = operand1;
    }

    public void setOperand2(int operand2) {
        this.operand2 = operand2;
    }
}
