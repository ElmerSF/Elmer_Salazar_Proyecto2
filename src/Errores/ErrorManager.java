/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de administrar todos los errores detectados durante el análisis.
Su función es centralizar el registro de errores, almacenarlos en una lista y
finalmente escribirlos en un archivo de salida (.log). Esto permite separar la
lógica de validación (Validador) del manejo de errores, siguiendo buenas
prácticas de diseño.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Errores;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ErrorManager {

    // Lista donde se almacenan todos los errores detectados.
    // Se usa ArrayList para mantener el orden en que ocurrieron.
    private final List<Error> errores = new ArrayList<>();

    // ============================================================
    // REGISTRO DE ERRORES
    // ============================================================

    /**
     * Agrega un error a la lista de errores.
     *
     * @param codigo      Código del error (enum ErrorCode)
     * @param linea       Línea original donde ocurrió el error
     * @param numeroLinea Número de línea en el archivo fuente
     *
     * Se crea un objeto Error y se almacena en la lista. El orden en que se
     * agregan los errores se conserva para el reporte final.
     */
    public void agregarError(ErrorCode codigo, String linea, int numeroLinea) {
        errores.add(new Error(codigo, numeroLinea, linea));
    }

    // ============================================================
    // CONSULTA DE ERRORES
    // ============================================================

    /**
     * Devuelve la lista completa de errores registrados.
     * Útil para depuración o para mostrar errores en consola.
     */
    public List<Error> getErrores() {
        return errores;
    }

    // ============================================================
    // GENERACIÓN DEL ARCHIVO LOG
    // ============================================================

    /**
     * Escribe todos los errores en un archivo .log.
     *
     * @param nombreArchivo Nombre del archivo donde se guardará el reporte.
     *
     * Si no hay errores, se escribe un mensaje indicando que el análisis
     * terminó sin problemas. Si hay errores, cada uno se imprime usando
     * su método toString(), que ya formatea la salida de manera uniforme.
     */
    public void escribirLog(String nombreArchivo) {
        try {
            FileWriter fw = new FileWriter(nombreArchivo);
            PrintWriter pw = new PrintWriter(fw);

            if (errores.isEmpty()) {
                pw.println("No se encontraron errores.");
            } else {
                for (Error e : errores) {
                    pw.println(e.toString());
                }
            }

            pw.close();
            fw.close();

        } catch (Exception e) {
            // Si ocurre un error al escribir el archivo, se muestra en consola.
            System.out.println("Error al escribir archivo log: " + e.getMessage());
        }
    }
}
