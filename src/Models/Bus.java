package Models;

import Models.RegisterFile;
import Models.Memory;
import Models.MBR;
import Models.MAR;

/**
 * Bus del sistema que rutea datos entre componentes de la CPU.
 */
public class Bus {
    /**
     * Transfiere datos de un registro a otro dentro del RegisterFile.
     * @param rf       Banco de registros.
     * @param srcReg   Nombre del registro origen.
     * @param dstReg   Nombre del registro destino.
     */
    public void transfer(RegisterFile rf, String srcReg, String dstReg) {
        int data = rf.read(srcReg);
        // CORRECCIÓN: Añadimos una descripción para la operación de escritura.
        rf.write(dstReg, data, String.format("(Copia desde %s)", srcReg));
        System.out.printf("[BUS] Registro %s -> Registro %s : %d%n", srcReg, dstReg, data);
    }

    /**
     * Transfiere datos de un registro a una dirección de memoria.
     * @param rf     Banco de registros.
     * @param srcReg Nombre del registro origen.
     * @param mem    Memoria principal.
     * @param addr   Dirección de memoria destino.
     */
    public void transfer(RegisterFile rf, String srcReg, Memory mem, int addr) {
        int data = rf.read(srcReg);
        mem.write(addr, data);
        System.out.printf("[BUS] Registro %s -> Memoria[%d] : %d%n", srcReg, addr, data);
    }

    /**
     * Transfiere datos de memoria a un registro.
     * @param mem     Memoria principal.
     * @param addr    Dirección de memoria origen.
     * @param rf      Banco de registros.
     * @param dstReg  Nombre del registro destino.
     */
    public void transfer(Memory mem, int addr, RegisterFile rf, String dstReg) {
        int data = mem.read(addr);
        // CORRECCIÓN: Añadimos una descripción para la operación de escritura.
        rf.write(dstReg, data, String.format("(Cargado desde Mem[0x%04X])", addr));
        System.out.printf("[BUS] Memoria[%d] -> Registro %s : %d%n", addr, dstReg, data);
    }

    /**
     * Carga un dato en el MBR (Memory Buffer Register).
     * @param data  Dato a cargar.
     * @param mbr   Registro de buffer de memoria.
     */
    public void transferToMBR(int data, MBR mbr) {
        mbr.load(data);
        System.out.printf("[BUS] Dato %d -> MBR%n", data);
    }

    /**
     * Carga una dirección en el MAR (Memory Address Register).
     * @param addr  Dirección a cargar.
     * @param mar   Registro de dirección de memoria.
     */
    public void transferToMAR(int addr, MAR mar) {
        mar.load(addr);
        System.out.printf("[BUS] Dirección %d -> MAR%n", addr);
    }
}