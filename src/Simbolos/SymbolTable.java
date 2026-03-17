/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Tabla de símbolos utilizada para almacenar información semántica
sobre las variables declaradas en el programa.

Cada entrada almacena:
 - nombre de la variable
 - tipo declarado (Integer, String, Boolean, Byte)
 - valor asignado (si aplica)

Esta estructura es fundamental para validaciones semánticas como:
 - Verificar si una variable existe
 - Consultar su tipo
 - Validar asignaciones
 - Validar condiciones en While, For e If
*/

package Simbolos;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // Mapa que almacena nombre → símbolo
    private final Map<String, Simbolo> tablaSimbolos;

    public SymbolTable() {
        this.tablaSimbolos = new HashMap<>();
    }

    // ============================================================
    // MÉTODOS PRINCIPALES
    // ============================================================

    /**
     * Agrega una variable a la tabla de símbolos.
     * Si ya existe, no la sobrescribe.
     */
    public void agregar(String nombre, String tipo) {
        if (!tablaSimbolos.containsKey(nombre)) {
            tablaSimbolos.put(nombre, new Simbolo(nombre, tipo));
        }
    }

    /**
     * Verifica si una variable existe en la tabla.
     */
    public boolean existe(String nombre) {
        return tablaSimbolos.containsKey(nombre);
    }

    /**
     * Devuelve el tipo de una variable.
     * Si no existe, devuelve null.
     */
    public String getTipo(String nombre) {
        if (tablaSimbolos.containsKey(nombre)) {
            return tablaSimbolos.get(nombre).getTipo();
        }
        return null;
    }

    /**
     * Asigna un valor a una variable existente.
     */
    public void setValor(String nombre, Object valor) {
        if (tablaSimbolos.containsKey(nombre)) {
            tablaSimbolos.get(nombre).setValor(valor);
        }
    }

    /**
     * Devuelve el valor actual de una variable.
     * Si no existe, devuelve null.
     */
    public Object getValor(String nombre) {
        if (tablaSimbolos.containsKey(nombre)) {
            return tablaSimbolos.get(nombre).getValor();
        }
        return null;
    }

    /**
     * Limpia toda la tabla de símbolos.
     */
    public void limpiar() {
        tablaSimbolos.clear();
    }

    // ============================================================
    // REPRESENTACIÓN PARA ARCHIVO simbolos.txt
    // ============================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TABLA DE SÍMBOLOS ===\n\n");

        if (tablaSimbolos.isEmpty()) {
            sb.append("(Sin variables declaradas)\n");
            return sb.toString();
        }

        for (Simbolo s : tablaSimbolos.values()) {
            sb.append("Nombre: ").append(s.getNombre()).append("\n");
            sb.append("Tipo:   ").append(s.getTipo()).append("\n");
            Object valor = s.getValor();
            sb.append("Valor:  ").append(valor != null ? valor.toString() : "(sin asignar)").append("\n");
            sb.append("----------------------------------------\n");
        }

        return sb.toString();
    }
}
