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
        // VALIDACIÓN DE BLOQUE WHILE (Proyecto 2)
        // ============================================================
        private void validarWhile(String[] lineas, int indiceInicio, Lexer lexer) {

            int lineaInicio = indiceInicio + 1;
            String lineaWhile = lineas[indiceInicio].trim();

            // ------------------------------------------------------------
            // 1. Validar condición del While
            // ------------------------------------------------------------
            // Esperado: While variable <entero
            String[] partes = lineaWhile.split("\\s+");

            if (partes.length != 3) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "La condición del While es inválida.",
                        lineaInicio
                );
                return;
            }

            String variable = partes[1];
            String condicion = partes[2];

            // Validar que la variable exista y sea Integer
            if (!symbolTable.existe(variable)) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "La variable '" + variable + "' no ha sido declarada.",
                        lineaInicio
                );
                return;
            }

            if (!symbolTable.getTipo(variable).equalsIgnoreCase("Integer")) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "La variable '" + variable + "' debe ser de tipo Integer.",
                        lineaInicio
                );
                return;
            }

            // Validar operador y número entero
            if (!(condicion.contains("<") || condicion.contains(">") || condicion.contains("="))) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "La condición debe contener un operador válido (<, > o =).",
                        lineaInicio
                );
                return;
            }

            String[] partesCond = condicion.split("[<>=]");

            if (partesCond.length != 2) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "La condición del While está mal formada.",
                        lineaInicio
                );
                return;
            }

            String valor = partesCond[1];

            try {
                Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                errorManager.agregarError(
                        ErrorCode.WHILE_CONDICION_INVALIDA,
                        "El valor '" + valor + "' debe ser un número entero.",
                        lineaInicio
                );
                return;
            }

    // ------------------------------------------------------------
    // 2. Buscar el End While correspondiente
    // ------------------------------------------------------------
    boolean encontradoEnd = false;
    int indiceEnd = -1;

    for (int i = indiceInicio + 1; i < lineas.length; i++) {

        String linea = lineas[i].trim();

        // No se permiten While anidados
        if (linea.startsWith("While ")) {
            errorManager.agregarError(
                    ErrorCode.WHILE_SIN_END,
                    "No se permiten While anidados.",
                    lineaInicio
            );
            return;
        }

        if (linea.equalsIgnoreCase("End While")) {
            encontradoEnd = true;
            indiceEnd = i;
            break;
        }
    }

    if (!encontradoEnd) {
        errorManager.agregarError(
                ErrorCode.WHILE_SIN_END,
                "El bloque While no tiene su correspondiente 'End While'.",
                lineaInicio
        );
        return;
    }

    // ------------------------------------------------------------
    // 3. Validar que exista al menos una línea ejecutable
    // ------------------------------------------------------------
    boolean tieneCodigo = false;

    for (int i = indiceInicio + 1; i < indiceEnd; i++) {

        String linea = lineas[i].trim();

        if (!linea.isEmpty() && !linea.startsWith("'")) {
            tieneCodigo = true;
            break;
        }
    }

    if (!tieneCodigo) {
        errorManager.agregarError(
                ErrorCode.WHILE_VACIO,
                "El bloque While está vacío o solo contiene comentarios.",
                lineaInicio
        );
    }
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
