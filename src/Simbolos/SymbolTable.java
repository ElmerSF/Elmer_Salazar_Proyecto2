/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Tabla de símbolos utilizada por el Validador para almacenar las variables
declaradas en el programa y su tipo asociado. Esta estructura permite realizar
validaciones semánticas como: verificar si una variable ya fue declarada,
consultar su tipo, y comprobar compatibilidad de tipos durante asignaciones
y operaciones matemáticas.

La tabla se implementa mediante un HashMap para permitir búsquedas rápidas
(O(1) promedio) y se normalizan los nombres a minúsculas para evitar problemas
de sensibilidad a mayúsculas/minúsculas.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Simbolos;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // Mapa que almacena pares: nombreVariable → tipoDeclarado
    // Se usa String → String porque los tipos del lenguaje son simples (Integer, String, etc.)
    private final Map<String, String> tabla;

    /**
     * Constructor de la tabla de símbolos.
     * Inicializa el HashMap donde se almacenarán las variables declaradas.
     */
    public SymbolTable() {
        this.tabla = new HashMap<>();
    }

    // ============================================================
    // REGISTRO DE VARIABLES
    // ============================================================

    /**
     * Registra una variable en la tabla de símbolos.
     *
     * @param nombre Nombre del identificador tal como aparece en el código.
     * @param tipo   Tipo declarado (Integer, String, Boolean, Byte).
     *
     * El nombre y el tipo se almacenan en minúsculas para garantizar
     * comparaciones consistentes sin importar cómo lo escribió el usuario.
     */
    public void registrar(String nombre, String tipo) {
        tabla.put(nombre.toLowerCase(), tipo.toLowerCase());
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    /**
     * Verifica si una variable ya fue declarada previamente.
     *
     * @param nombre Nombre del identificador a consultar.
     * @return true si existe en la tabla, false en caso contrario.
     *
     * Se convierte a minúsculas para evitar problemas de sensibilidad
     * entre "Edad", "edad" o "EDAD".
     */
    public boolean existe(String nombre) {
        return tabla.containsKey(nombre.toLowerCase());
    }

    /**
     * Devuelve el tipo asociado a una variable previamente declarada.
     *
     * @param nombre Nombre del identificador.
     * @return Tipo en minúsculas (integer, string, boolean, byte),
     *         o null si la variable no existe.
     */
    public String tipoDe(String nombre) {
        return tabla.get(nombre.toLowerCase());
    }

    // ============================================================
    // DEPURACIÓN / DEBUG
    // ============================================================

    /**
     * Representación legible de la tabla de símbolos.
     * Útil para depuración o para mostrar el contenido de la tabla
     * durante pruebas o diagnósticos.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TABLA DE SÍMBOLOS:\n");

        for (Map.Entry<String, String> entry : tabla.entrySet()) {
            sb.append("  ")
              .append(entry.getKey())
              .append(" : ")
              .append(entry.getValue())
              .append("\n");
        }

        return sb.toString();
    }
}
