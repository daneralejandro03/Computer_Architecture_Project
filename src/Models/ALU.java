package Models;

public class ALU {
    public int operate(String op, int a, int b) {
        switch (op) {
            case "ADD": return a + b;
            case "SUB": return a - b;
            case "MUL": return a * b;
            case "DIV": return a / b;
            case "AND": return a & b;
            case "OR":  return a | b;
            case "XOR": return a ^ b;
            default: throw new IllegalArgumentException("Operación no soportada: " + op);
        }
    }
}
