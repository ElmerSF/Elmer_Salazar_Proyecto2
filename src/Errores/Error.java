/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase que representa un error individual detectado durante el análisis del
programa. Cada error contiene:
 - un código (ErrorCode) que define el tipo de error,
 - el número de línea donde ocurrió,
 - y la línea original del archivo fuente para referencia.

Esta clase es utilizada por ErrorManager para almacenar y reportar errores
de forma ordenada y consistente.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Errores;

public class Error {

    // Código del error (enum ErrorCode), contiene número y mensaje asociado.
    private final ErrorCode codigo;

    // Número de línea donde ocurrió el error.
    private final int numeroLinea;

    // Texto exacto de la línea original del archivo fuente.
    private final String lineaOriginal;

    /**
     * Constructor del error.
     *
     * @param codigo        Código del error (enum ErrorCode)
     * @param numeroLinea   Línea donde se detectó el error
     * @param lineaOriginal Texto original de la línea para referencia
     */
    public Error(ErrorCode codigo, int numeroLinea, String lineaOriginal) {
        this.codigo = codigo;
        this.numeroLinea = numeroLinea;
        this.lineaOriginal = lineaOriginal;
    }

    /**
     * Devuelve el número del error según el enum ErrorCode.
     * Ejemplo: 101, 205, 310, etc.
     */
    public int getNumero() {
        return codigo.getCodigo();
    }

    /**
     * Devuelve el número de línea donde ocurrió el error.
     */
    public int getLinea() {
        return numeroLinea;
    }

    /**
     * Devuelve la descripción del error según ErrorCode.
     * Ejemplo: "Falta palabra reservada As", "Tipo inválido", etc.
     */
    public String getDescripcion() {
        return codigo.getMensaje();
    }

    /**
     * Devuelve la línea original del archivo donde ocurrió el error.
     * Esto permite mostrar el contexto exacto al usuario.
     */
    public String getLineaOriginal() {
        return lineaOriginal;
    }

    /**
     * Representación legible del error.
     * Formato:
     *   Error 102. Línea 0005. Tipo inválido → "Dim x As Entero"
     *
     * Se usa String.format("%04d") para mostrar el número de línea con
     * cuatro dígitos, lo cual facilita la lectura y el ordenamiento.
     */
    @Override
    public String toString() {
        return "Error " + getNumero() +
               ". Línea " + String.format("%04d", numeroLinea) +
               ". " + getDescripcion() +
               " → \"" + lineaOriginal + "\"";
    }
}
