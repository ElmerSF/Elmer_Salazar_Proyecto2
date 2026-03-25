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

        // Reservado por si se amplía a análisis por tokens más adelante
        Lexer lexer = new Lexer();

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            // Ignorar líneas vacías y comentarios
            if (linea.isEmpty() || linea.startsWith("'")) {
                continue;
            }

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

        // Esperamos algo como: While contador < 5  (3 o 4 tokens según espacios)
        if (partes.length < 3) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        // Soportar tanto "While contador<5" como "While contador < 5"
        String variable;
        String operadorYValor;

        if (partes.length == 3) {
            // Ej: While contador<5
            variable = partes[1];
            operadorYValor = partes[2];
        } else {
            // Ej: While contador < 5
            variable = partes[1];
            operadorYValor = partes[2] + partes[3];
        }

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
        if (!(operadorYValor.contains("<") ||
              operadorYValor.contains(">") ||
              operadorYValor.contains("="))) {

            errorManager.agregarError(
                    ErrorCode.WHILE_OPERADOR_RELACIONAL_INVALIDO,
                    lineaWhile,
                    lineaInicio
            );
            return;
        }

        String[] partesCond = operadorYValor.split("[<>=]");

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
        // 2. Buscar el End While correspondiente y validar anidamientos
        // ------------------------------------------------------------
        boolean encontradoEnd = false;
        int indiceEnd = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.isEmpty() || linea.startsWith("'")) {
                continue;
            }

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
    // VALIDACIÓN DE BLOQUE FOR (Proyecto 2)
    // ============================================================
    private void validarFor(String[] lineas, int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        String lineaFor = lineas[indiceInicio].trim();

        // ------------------------------------------------------------
        // 1. Validar cabecera del For:
        //    For variable = valor_inicial To valor_final
        // ------------------------------------------------------------
        if (!lineaFor.contains("=")) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_IGUAL,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        String cabecera = lineaFor.substring(3).trim(); // quitar "For"
        String[] partesIgual = cabecera.split("=", 2);

        if (partesIgual.length < 2) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_INICIAL,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        String nombreVar = partesIgual[0].trim();
        String resto = partesIgual[1].trim();

        if (nombreVar.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VARIABLE,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // Variable declarada
        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_DECLARADA,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // Variable numérica
        String tipoVar = symbolTable.getTipo(nombreVar);
        if (tipoVar == null ||
                (!tipoVar.equalsIgnoreCase("Integer") &&
                 !tipoVar.equalsIgnoreCase("Byte"))) {

            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_NUMERICA,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // Debe existir la palabra To
        if (!resto.contains("To")) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_TO,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        String[] partesTo = resto.split("\\bTo\\b", 2);

        if (partesTo.length < 2) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_FINAL,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        String valorInicialStr = partesTo[0].trim();
        String valorFinalStr = partesTo[1].trim();

        if (valorInicialStr.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_INICIAL,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        if (valorFinalStr.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_FINAL,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // Validar que los valores sean enteros
        boolean valorInicialValido = true;
        boolean valorFinalValido = true;

        try {
            Integer.parseInt(valorInicialStr);
        } catch (NumberFormatException e) {
            valorInicialValido = false;
        }

        try {
            Integer.parseInt(valorFinalStr);
        } catch (NumberFormatException e) {
            valorFinalValido = false;
        }

        if (!valorInicialValido && !valorFinalValido) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALORES_NO_NUMERICOS,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        if (!valorInicialValido) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_INICIAL_INVALIDO,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        if (!valorFinalValido) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_FINAL_INVALIDO,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar el Next correspondiente, validar anidamientos y bloque vacío
        // ------------------------------------------------------------
        boolean encontradoNext = false;
        int indiceEnd = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.isEmpty() || linea.startsWith("'")) {
                continue;
            }

            // No se permiten For anidados
            if (linea.startsWith("For ")) {
                errorManager.agregarError(
                        ErrorCode.FOR_ANIDADO,
                        linea,
                        i + 1
                );
                return;
            }

            if (linea.equalsIgnoreCase("Next")) {
                encontradoNext = true;
                indiceEnd = i;
                break;
            }
        }

        if (!encontradoNext) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_NEXT,
                    lineaFor,
                    lineaInicio
            );
            return;
        }

        // Bloque vacío: For ... Next en líneas consecutivas (sin código entre medio)
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
                    ErrorCode.FOR_VACIO,
                    lineaFor,
                    lineaInicio
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE IF (Proyecto 2)
    // ============================================================
    private void validarIf(String[] lineas, int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        String lineaIf = lineas[indiceInicio].trim();

        // ------------------------------------------------------------
        // 1. Validar cabecera del If:
        //    If condicion Then
        // ------------------------------------------------------------
        if (!lineaIf.contains("Then")) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_THEN,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        // Extraer la parte entre "If" y "Then"
        String cabecera = lineaIf.substring(2).trim(); // quitar "If"
        String[] partesThen = cabecera.split("\\bThen\\b", 2);

        if (partesThen.length == 0 || partesThen[0].trim().isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_CONDICION,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        String condicion = partesThen[0].trim();

        // Validación básica de condición: esperamos algo como "contador = 3"
        String[] tokensCond = condicion.split("\\s+");

        if (tokensCond.length != 3) {
            errorManager.agregarError(
                    ErrorCode.IF_CONDICION_INVALIDA,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        String variable = tokensCond[0];
        String operador = tokensCond[1];
        String valor = tokensCond[2];

        // Variable declarada
        if (!symbolTable.existe(variable)) {
            errorManager.agregarError(
                    ErrorCode.IF_IDENTIFICADOR_NO_DECLARADO,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        // Operador relacional válido
        if (!(operador.equals("=") ||
              operador.equals("<") ||
              operador.equals(">") ||
              operador.equals("<=") ||
              operador.equals(">=") ||
              operador.equals("<>"))) {

            errorManager.agregarError(
                    ErrorCode.IF_OPERADOR_RELACIONAL_INVALIDO,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        // Validar operando según tipo de variable
        String tipoVar = symbolTable.getTipo(variable);

        if (tipoVar != null && (tipoVar.equalsIgnoreCase("Integer") ||
                                tipoVar.equalsIgnoreCase("Byte"))) {

            // Debe ser un número
            try {
                Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                errorManager.agregarError(
                        ErrorCode.IF_OPERANDO_INVALIDO,
                        lineaIf,
                        lineaInicio
                );
                return;
            }

        } else if (tipoVar != null && tipoVar.equalsIgnoreCase("Boolean")) {

            if (!(valor.equalsIgnoreCase("True") || valor.equalsIgnoreCase("False"))) {
                errorManager.agregarError(
                        ErrorCode.IF_OPERANDO_INVALIDO,
                        lineaIf,
                        lineaInicio
                );
                return;
            }

        } else {
            // Tipo no soportado para condición
            errorManager.agregarError(
                    ErrorCode.IF_CONDICION_NO_BOOLEAN,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar End If, validar anidamientos y bloques Then/Else
        // ------------------------------------------------------------
        boolean encontradoEndIf = false;
        int indiceEndIf = -1;
        int indiceElse = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.isEmpty() || linea.startsWith("'")) {
                continue;
            }

            // No se permiten If anidados
            if (linea.startsWith("If ")) {
                errorManager.agregarError(
                        ErrorCode.IF_ANIDADO,
                        linea,
                        i + 1
                );
                return;
            }

            if (linea.equalsIgnoreCase("Else")) {
                if (indiceElse == -1) {
                    indiceElse = i;
                }
            }

            if (linea.equalsIgnoreCase("End If")) {
                encontradoEndIf = true;
                indiceEndIf = i;
                break;
            }
        }

        if (!encontradoEndIf) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_ENDIF,
                    lineaIf,
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista código después de Then (bloque Then)
        // ------------------------------------------------------------
        boolean tieneCodigoThen = false;
        int limiteThen = (indiceElse != -1) ? indiceElse : indiceEndIf;

        for (int i = indiceInicio + 1; i < limiteThen; i++) {

            String linea = lineas[i].trim();

            if (!linea.isEmpty() && !linea.startsWith("'")) {
                tieneCodigoThen = true;
                break;
            }
        }

        if (!tieneCodigoThen) {
            errorManager.agregarError(
                    ErrorCode.IF_VACIO,
                    lineaIf,
                    lineaInicio
            );
        }

        // ------------------------------------------------------------
        // 4. Validar bloque Else (si existe)
        // ------------------------------------------------------------
        if (indiceElse != -1) {

            // Else no debe tener tokens extra
            String lineaElse = lineas[indiceElse].trim();
            if (!lineaElse.equalsIgnoreCase("Else")) {
                errorManager.agregarError(
                        ErrorCode.ELSE_CON_TOKENS_EXTRA,
                        lineaElse,
                        indiceElse + 1
                );
            }

            boolean tieneCodigoElse = false;

            for (int i = indiceElse + 1; i < indiceEndIf; i++) {

                String linea = lineas[i].trim();

                if (!linea.isEmpty() && !linea.startsWith("'")) {
                    tieneCodigoElse = true;
                    break;
                }
            }

            if (!tieneCodigoElse) {
                errorManager.agregarError(
                        ErrorCode.ELSE_VACIO,
                        lineaElse,
                        indiceElse + 1
                );
            }
        }
    }
}
