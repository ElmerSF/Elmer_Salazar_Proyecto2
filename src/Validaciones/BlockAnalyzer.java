/*
UNED Informática Compiladores 3307
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

        Lexer lexer = new Lexer(); // reservado por si se amplía a tokens luego

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            // Detectar inicio de bloque WHILE
            if (linea.startsWith("While ")) {
                validarWhile(lineas, i);
            }

            // Detectar inicio de bloque FOR
            if (linea.startsWith("For ")) {
                validarFor(lineas, i);
            }

            // Detectar inicio de bloque IF
            if (linea.startsWith("If ")) {
                validarIf(lineas, i);
            }
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE WHILE (Proyecto 2)
    // ============================================================
    private void validarWhile(String[] lineas, int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        String lineaWhile = lineas[indiceInicio].trim();

        // ------------------------------------------------------------
        // 1. Validar condición del While (forma básica: While <var> <op> <valor>)
        // ------------------------------------------------------------
        String[] partes = lineaWhile.split("\\s+");

        if (partes.length != 3) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        String variable = partes[1];
        String condicion = partes[2];

        // Variable no declarada
        if (!symbolTable.existe(variable)) {
            errorManager.agregarError(
                    ErrorCode.WHILE_IDENTIFICADOR_NO_DECLARADO,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        // Variable debe ser numérica (Integer o Byte)
        String tipoVar = symbolTable.getTipo(variable);
        if (tipoVar == null ||
            (!tipoVar.equalsIgnoreCase("Integer") &&
             !tipoVar.equalsIgnoreCase("Byte"))) {

            errorManager.agregarError(
                    ErrorCode.WHILE_OPERANDO_INVALIDO,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        // Operador relacional inválido
        if (!(condicion.contains("<") || condicion.contains(">") || condicion.contains("="))) {
            errorManager.agregarError(
                    ErrorCode.WHILE_OPERADOR_RELACIONAL_INVALIDO,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        String[] partesCond = condicion.split("[<>=]");

        if (partesCond.length != 2) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        String valor = partesCond[1];

        try {
            Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            errorManager.agregarError(
                    ErrorCode.WHILE_OPERANDO_INVALIDO,
                    lineaWhile,
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
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        linea,
                        i + 1
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
                    ErrorCode.WHILE_SIN_WEND,
                    lineaWhile,
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
                    lineaWhile,
                    lineaInicio
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE FOR
    // ============================================================
    private void validarFor(String[] lineas, int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        String lineaFor = lineas[indiceInicio].trim();
        boolean encontradoNext = false;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.equalsIgnoreCase("Next")) {
                encontradoNext = true;

                // Bloque vacío: For ... Next en líneas consecutivas
                if (i == indiceInicio + 1) {
                    errorManager.agregarError(
                            ErrorCode.FOR_VACIO,
                            lineaFor,
                            lineaInicio
                    );
                }
                return;
            }
        }

        // No se encontró Next correspondiente
        errorManager.agregarError(
                ErrorCode.FOR_SIN_NEXT,
                lineaFor,
                lineaInicio
        );
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE IF
    // ============================================================
    private void validarIf(String[] lineas, int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        String lineaIf = lineas[indiceInicio].trim();
        boolean encontradoEndIf = false;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.equalsIgnoreCase("End If")) {
                encontradoEndIf = true;

                // IF vacío: If ... End If en líneas consecutivas
                if (i == indiceInicio + 1) {
                    errorManager.agregarError(
                            ErrorCode.IF_VACIO,
                            lineaIf,
                            lineaInicio
                    );
                }
                return;
            }
        }

        // No se encontró End If correspondiente
        errorManager.agregarError(
                ErrorCode.IF_SIN_ENDIF,
                lineaIf,
                lineaInicio
        );
    }
}
