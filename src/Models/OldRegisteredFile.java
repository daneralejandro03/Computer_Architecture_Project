package Models;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Set;

/**
 * Banco de registros de propósito general.
 * Permite lectura/escritura por nombre, borrado y consulta de registros.
 */
public class OldRegisteredFile {
    private final Map<String, Integer> regs = new HashMap<>();

    /**
     * Lee el valor de un registro; devuelve 0 si no existe.
     *
     * @param name Nombre del registro (sensible a mayúsculas).
     * @return Valor almacenado o 0.
     */
    public int read(String name) {
        return regs.getOrDefault(name, 0);
    }

    /**
     * Escribe un valor en el registro; crea el registro si no existía.
     *
     * @param name Nombre del registro.
     * @param val  Valor a escribir.
     */
    public void write(String name, int val) {
        regs.put(name, val);
    }

    /**
     * Elimina todos los registros, reseteando el banco.
     */
    public void clear() {
        regs.clear();
    }

    /**
     * Comprueba si el registro existe en el banco.
     *
     * @param name Nombre del registro.
     * @return true si el registro fue escrito antes.
     */
    public boolean hasRegister(String name) {
        return regs.containsKey(name);
    }

    /**
     * Obtiene un conjunto inmodificable con todos los nombres de registros existentes.
     *
     * @return Set de nombres de registros.
     */
    public Set<String> getRegisterNames() {
        return Collections.unmodifiableSet(regs.keySet());
    }

    @Override
    public String toString() {
        return "RegisterFile" + regs.toString();
    }
}
