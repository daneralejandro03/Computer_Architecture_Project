
package Models;

import Enums.OpCode;

/**
 * Unidad Aritmético-Lógica (ALU) que ejecuta operaciones básicas
 * y mantiene banderas de estado.
 */
public final class ALU {
    // Flags de estado
    private boolean zeroFlag;
    private boolean carryFlag;
    private boolean signFlag;
    private boolean overflowFlag;

    /**
     * Ejecuta la operación indicada.
     *
     * @param op  El código de operación (enum OpCode).
     * @param a   Operando izquierdo.
     * @param b   Operando derecho (ignorarse para NOT).
     * @return    Resultado de la operación.
     * @throws ArithmeticException Si se divide por cero.
     */
    public int operate(OpCode op, int a, int b) {
        int result;
        carryFlag = false;
        overflowFlag = false;

        switch (op) {
            case ADD: {
                long sum = (long) a + b;
                result = (int) sum;
                overflowFlag = (sum != result);
                carryFlag = (sum >>> 32) != 0;
                break;
            }
            case SUB: {
                long diff = (long) a - b;
                result = (int) diff;
                overflowFlag = (diff != result);
                carryFlag = (diff >>> 32) != 0;
                break;
            }
            case MUL: {
                long prod = (long) a * b;
                result = (int) prod;
                overflowFlag = (prod != result);
                // carryFlag no se usa normalmente en multiplicación
                break;
            }
            case DIV: {
                if (b == 0) {
                    throw new ArithmeticException("División por cero en ALU: " + a + " / " + b);
                }
                result = a / b;
                break;
            }
            case AND:
                result = a & b;
                break;
            case OR:
                result = a | b;
                break;
            case XOR:
                result = a ^ b;
                break;
            case NOT:
                result = ~a;
                break;
            default:
                throw new IllegalArgumentException("Operación no soportada: " + op);
        }

        zeroFlag = (result == 0);
        signFlag = (result < 0);
        return result;
    }

    /** @return true si el último resultado fue cero. */
    public boolean isZero()    { return zeroFlag; }
    /** @return true si hubo acarreo en la última operación. */
    public boolean isCarry()   { return carryFlag; }
    /** @return true si el resultado fue negativo. */
    public boolean isSign()    { return signFlag; }
    /** @return true si hubo overflow en la última operación. */
    public boolean isOverflow(){ return overflowFlag; }
}
