package Models;

public class ALU {

    /**
     * Método que "valida" o ejecuta la operación según el opcode.
     * Devuelve el resultado de la operación.
     */
    public int validateOpcode(String opcode, int operand1, int operand2) {
        int result = 0;

        switch (opcode.toUpperCase()) {
            case "ADD":
                result = operand1 + operand2;
                break;
            case "SUB":
                result = operand1 - operand2;
                break;
            case "MUL":
                result = operand1 * operand2;
                break;
            case "DIV":
                if (operand2 != 0) {
                    result = operand1 / operand2;
                } else {
                    System.out.println("Error: División por cero.");
                }
                break;
            default:
                System.out.println("Opcode no reconocido: " + opcode);
                break;
        }

        return result;
    }
}
