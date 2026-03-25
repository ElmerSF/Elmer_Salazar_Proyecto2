/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de validar bloques de código (While, For, If).
Este análisis se ejecuta ANTES del análisis línea por línea para detectar:

 - Bloques mal cerrados
 - Bloques vacíos
 - Estructuras incompletas
 - Condiciones mal formadas (versión alineada con rúbrica)

A diferencia del Validador, que trabaja instrucción por instrucción,
BlockAnalyzer trabaja a nivel de bloques, pero reutilizando:

 - Lexer (para obtener tokens por línea)
 - SymbolTable (para tipos y existencia de variables)
 - ErrorCode (para mantener la misma tipificación de errores)

No se duplican las validaciones ya realizadas en otras clases; aquí solo
se cubren los aspectos de estructura de bloques requeridos en el Proyecto 2.
*/

package Validaciones;

import Errores.ErrorManager;
import Errores.ErrorCode;
import Simbolos.SymbolTable;
import Lexer.Lexer;
import Lexer.Token;
import Lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class BlockAnalyzer {

    private final ErrorManager errorManager;
    private final SymbolTable symbolTable;
    private final Lexer lexer;

    public BlockAnalyzer(ErrorManager errorManager, SymbolTable symbolTable) {
        this.errorManager = errorManager;
        this.symbolTable = symbolTable;
        this.lexer = new Lexer();
    }

    // ============================================================
    // MÉTODO PRINCIPAL: Analiza todas las líneas del archivo
    // ============================================================
    public void analizarBloques(String[] lineas) {

        // Tokenizamos todas las líneas una sola vez para no repetir trabajo
        List<Token>[] tokensPorLinea = new ArrayList[lineas.length];

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            tokensPorLinea[i] = new ArrayList<>(lexer.tokenizar(linea));
        }

        for (int i = 0; i < lineas.length; i++) {

            List<Token> tokens = tokensPorLinea[i];

            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            // Comentarios de línea completa: no se analizan como bloque
            if (primero.es(TokenType.Type.COMMENT)) {
                continue;
            }

            // Detectar inicio de bloque WHILE
            if (primero.es(TokenType.Type.WHILE)) {
                validarWhile(lineas, tokensPorLinea, i);
            }

            // Detectar inicio de bloque FOR
            if (primero.es(TokenType.Type.FOR)) {
                validarFor(lineas, tokensPorLinea, i);
            }

            // Detectar inicio de bloque IF
            if (primero.es(TokenType.Type.IF)) {
                validarIf(lineas, tokensPorLinea, i);
            }
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE WHILE (Proyecto 2)
    // ============================================================
    private void validarWhile(String[] lineas,
                              List<Token>[] tokensPorLinea,
                              int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        List<Token> tokensLinea = tokensPorLinea[indiceInicio];

        // ------------------------------------------------------------
        // 1. Validar condición del While (While <var> <op> <entero>)
        // ------------------------------------------------------------
        // Rúbrica: variable declarada, tipo Integer, operador < > <= >= =, valor entero

        if (tokensLinea.size() < 4) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        Token tVar = tokensLinea.get(1);
        Token tOp  = tokensLinea.get(2);
        Token tVal = tokensLinea.get(3);

        // Variable debe ser identificador
        if (!tVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        String nombreVar = tVar.lexema;

        // Variable debe estar declarada
        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(
                    ErrorCode.WHILE_IDENTIFICADOR_NO_DECLARADO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Variable debe ser Integer o Byte
        String tipoVar = symbolTable.getTipo(nombreVar);
        if (tipoVar == null ||
            (!tipoVar.equalsIgnoreCase("Integer") &&
             !tipoVar.equalsIgnoreCase("Byte"))) {

            errorManager.agregarError(
                    ErrorCode.WHILE_OPERANDO_INVALIDO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Operador relacional válido
        if (!(tOp.es(TokenType.Type.OP_LT)  ||
              tOp.es(TokenType.Type.OP_GT)  ||
              tOp.es(TokenType.Type.OP_LTE) ||
              tOp.es(TokenType.Type.OP_GTE) ||
              tOp.es(TokenType.Type.OP_ASSIGN))) {

            errorManager.agregarError(
                    ErrorCode.WHILE_OPERADOR_RELACIONAL_INVALIDO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Valor entero
        if (!tVal.es(TokenType.Type.NUMBER) || tVal.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.WHILE_OPERANDO_INVALIDO,
                    lineas[indiceInicio],
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

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            // No se permiten While anidados (según rúbrica)
            if (primero.es(TokenType.Type.WHILE)) {
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        lineas[i],
                        i + 1
                );
                return;
            }

            if (esEndWhile(tokens)) {
                encontradoEnd = true;
                indiceEnd = i;
                break;
            }
        }

        if (!encontradoEnd) {
            errorManager.agregarError(
                    ErrorCode.WHILE_SIN_WEND,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista al menos una línea ejecutable
        // ------------------------------------------------------------
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceEnd; i++) {

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            // Se ignoran líneas vacías y comentarios
            if (primero.es(TokenType.Type.COMMENT)) {
                continue;
            }

            tieneCodigo = true;
            break;
        }

        if (!tieneCodigo) {
            errorManager.agregarError(
                    ErrorCode.WHILE_VACIO,
                    lineas[indiceInicio],
                    lineaInicio
            );
        }
    }

    private boolean esEndWhile(List<Token> tokens) {
        if (tokens.size() < 2) return false;
        return tokens.get(0).es(TokenType.Type.END) &&
               tokens.get(1).es(TokenType.Type.WHILE);
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE FOR (Proyecto 2)
    // ============================================================
    private void validarFor(String[] lineas,
                            List<Token>[] tokensPorLinea,
                            int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        List<Token> tokensLinea = tokensPorLinea[indiceInicio];

        // ------------------------------------------------------------
        // 1. Validar cabecera: For variable = valor_inicial To valor_final
        // ------------------------------------------------------------
        if (tokensLinea.size() < 6) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_FINAL,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        Token tFor  = tokensLinea.get(0);
        Token tVar  = tokensLinea.get(1);
        Token tEq   = tokensLinea.get(2);
        Token tIni  = tokensLinea.get(3);
        Token tTo   = tokensLinea.get(4);
        Token tFin  = tokensLinea.get(5);

        // Variable de control
        if (!tVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VARIABLE,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        String nombreVar = tVar.lexema;

        // Variable debe estar declarada
        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_DECLARADA,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Variable debe ser numérica
        String tipoVar = symbolTable.getTipo(nombreVar);
        if (tipoVar == null ||
            (!tipoVar.equalsIgnoreCase("Integer") &&
             !tipoVar.equalsIgnoreCase("Byte"))) {

            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_NUMERICA,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Debe existir '='
        if (!tEq.es(TokenType.Type.OP_ASSIGN)) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_IGUAL,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Valor inicial entero
        if (!tIni.es(TokenType.Type.NUMBER) || tIni.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_INICIAL_INVALIDO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Palabra reservada To
        if (!tTo.es(TokenType.Type.TO)) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_TO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Valor final entero
        if (!tFin.es(TokenType.Type.NUMBER) || tFin.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_FINAL_INVALIDO,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar el Next correspondiente
        // ------------------------------------------------------------
        boolean encontradoNext = false;
        int indiceNext = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            // No se permiten For anidados
            if (primero.es(TokenType.Type.FOR)) {
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        lineas[i],
                        i + 1
                );
                return;
            }

            if (primero.es(TokenType.Type.NEXT)) {
                encontradoNext = true;
                indiceNext = i;
                break;
            }
        }

        if (!encontradoNext) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_NEXT,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista al menos una línea ejecutable
        // ------------------------------------------------------------
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceNext; i++) {

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            if (primero.es(TokenType.Type.COMMENT)) {
                continue;
            }

            tieneCodigo = true;
            break;
        }

        if (!tieneCodigo) {
            errorManager.agregarError(
                    ErrorCode.FOR_VACIO,
                    lineas[indiceInicio],
                    lineaInicio
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE IF (Proyecto 2)
    // ============================================================
    private void validarIf(String[] lineas,
                           List<Token>[] tokensPorLinea,
                           int indiceInicio) {

        int lineaInicio = indiceInicio + 1;
        List<Token> tokensLinea = tokensPorLinea[indiceInicio];

        // ------------------------------------------------------------
        // 1. Validar que exista condición y THEN en la misma línea
        // ------------------------------------------------------------
        int indiceThen = -1;

        for (int i = 0; i < tokensLinea.size(); i++) {
            if (tokensLinea.get(i).es(TokenType.Type.THEN)) {
                indiceThen = i;
                break;
            }
        }

        if (indiceThen == -1) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_THEN,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // Debe haber al menos un token entre IF y THEN (condición no vacía)
        if (indiceThen == 1) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_CONDICION,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar Else (opcional) y End If correspondiente
        // ------------------------------------------------------------
        boolean encontradoEndIf = false;
        int indiceEndIf = -1;
        int indiceElse = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            // No se permiten IF anidados
            if (primero.es(TokenType.Type.IF)) {
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        lineas[i],
                        i + 1
                );
                return;
            }

            // Else
            if (primero.es(TokenType.Type.ELSE)) {
                if (indiceElse == -1) {
                    indiceElse = i;
                }
                continue;
            }

            // End If
            if (esEndIf(tokens)) {
                encontradoEndIf = true;
                indiceEndIf = i;
                break;
            }
        }

        if (!encontradoEndIf) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_ENDIF,
                    lineas[indiceInicio],
                    lineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista al menos una línea de código después de THEN
        // ------------------------------------------------------------
        int limiteThen = (indiceElse != -1) ? indiceElse : indiceEndIf;
        boolean tieneCodigoThen = false;

        for (int i = indiceInicio + 1; i < limiteThen; i++) {

            List<Token> tokens = tokensPorLinea[i];
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primero = tokens.get(0);

            if (primero.es(TokenType.Type.COMMENT)) {
                continue;
            }

            tieneCodigoThen = true;
            break;
        }

        if (!tieneCodigoThen) {
            errorManager.agregarError(
                    ErrorCode.IF_VACIO,
                    lineas[indiceInicio],
                    lineaInicio
            );
        }

        // ------------------------------------------------------------
        // 4. Si hay ELSE, validar que tenga al menos una línea de código
        // ------------------------------------------------------------
        if (indiceElse != -1) {

            boolean tieneCodigoElse = false;

            for (int i = indiceElse + 1; i < indiceEndIf; i++) {

                List<Token> tokens = tokensPorLinea[i];
                if (tokens == null || tokens.isEmpty()) {
                    continue;
                }

                Token primero = tokens.get(0);

                if (primero.es(TokenType.Type.COMMENT)) {
                    continue;
                }

                tieneCodigoElse = true;
                break;
            }

            if (!tieneCodigoElse) {
                errorManager.agregarError(
                        ErrorCode.IF_VACIO,
                        lineas[indiceElse],
                        indiceElse + 1
                );
            }
        }
    }

    private boolean esEndIf(List<Token> tokens) {
        if (tokens.size() < 2) return false;
        return tokens.get(0).es(TokenType.Type.END) &&
               tokens.get(1).es(TokenType.Type.IF);
    }
}
