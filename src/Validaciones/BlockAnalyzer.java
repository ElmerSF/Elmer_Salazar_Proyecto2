/*UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de validar bloques de código (While, For, If).
Este análisis se ejecuta ANTES del análisis línea por línea para detectar:

 - Bloques mal cerrados
 - Bloques vacíos
 - Estructuras incompletas
 - Condiciones mal formadas (versión inicial)
 

*/

package Validaciones;

import Errores.ErrorManager;
import Errores.ErrorCode;
import Simbolos.SymbolTable;
import Lexer.Lexer;

public class BlockAnalyzer {

    private final ErrorManager errorManager;
    private final SymbolTable symbolTable;

    public BlockAnalyzer(ErrorManager errorManager, SymbolTable symbolTable) {
        this.errorManager = errorManager;
        this.symbolTable = symbolTable;
    }

    // ============================================================
    // MÉTODO PRINCIPAL: Analiza todas las líneas del archivo
    // ============================================================
    public void analizarBloques(String[] lineas) {

        Lexer lexer = new Lexer();

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();
            int numeroLinea = i + 1;

            // Detectar inicio de bloque WHILE
            if (linea.startsWith("While ")) {
                validarWhile(lineas, i, lexer);
            }

            // Detectar inicio de bloque FOR
            if (linea.startsWith("For ")) {
                validarFor(lineas, i, lexer);
            }

            // Detectar inicio de bloque IF
            if (linea.startsWith("If ")) {
                validarIf(lineas, i, lexer);
            }
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE WHILE
    // ============================================================
    private void validarWhile(String[] lineas, int indiceInicio, Lexer lexer) {

        int lineaInicio = indiceInicio + 1;
        boolean encontradoEnd = false;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            // Detectar cierre del bloque
            if (linea.equalsIgnoreCase("End While")) {
                encontradoEnd = true;

                // Validar bloque vacío
                if (i == indiceInicio + 1) {
                    errorManager.agregarError(
                            ErrorCode.WHILE_VACIO,
                            "El bloque While está vacío.",
                            lineaInicio
                    );
                }
                return;
            }
        }

        // Si llegamos aquí, nunca se encontró End While
        errorManager.agregarError(
                ErrorCode.WHILE_SIN_END,
                "El bloque While no tiene su correspondiente 'End While'.",
                lineaInicio
        );
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE FOR
    // ============================================================
    private void validarFor(String[] lineas, int indiceInicio, Lexer lexer) {

        int lineaInicio = indiceInicio + 1;
        boolean encontradoNext = false;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.equalsIgnoreCase("Next")) {
                encontradoNext = true;

                if (i == indiceInicio + 1) {
                    errorManager.agregarError(
                            ErrorCode.FOR_VACIO,
                            "El bloque For está vacío.",
                            lineaInicio
                    );
                }
                return;
            }
        }

        errorManager.agregarError(
                ErrorCode.FOR_SIN_NEXT,
                "El bloque For no tiene su correspondiente 'Next'.",
                lineaInicio
        );
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE IF
    // ============================================================
    private void validarIf(String[] lineas, int indiceInicio, Lexer lexer) {

        int lineaInicio = indiceInicio + 1;
        boolean encontradoEndIf = false;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.equalsIgnoreCase("End If")) {
                encontradoEndIf = true;

                if (i == indiceInicio + 1) {
                    errorManager.agregarError(
                            ErrorCode.IF_VACIO,
                            "El bloque If está vacío.",
                            lineaInicio
                    );
                }
                return;
            }
        }

        errorManager.agregarError(
                ErrorCode.IF_SIN_ENDIF,
                "El bloque If no tiene su correspondiente 'End If'.",
                lineaInicio
        );
    }
}
