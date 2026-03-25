/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

BlockAnalyzer — Versión parchada y final
----------------------------------------
Valida bloques WHILE, FOR e IF usando:
 - Regex (TabladeExpresiones) para estructura general
 - Tokens (Lexer) para semántica de condición
 - SymbolTable para tipos y declaraciones
 - ErrorManager para ignorar líneas dañadas

Este analizador:
 - NO duplica validaciones del Validador
 - NO analiza líneas con errores previos
 - NO genera falsos positivos
 - Cumple la rúbrica del Proyecto 2
*/

package Validaciones;

import Errores.ErrorCode;
import Errores.ErrorManager;
import Lexer.Lexer;
import Lexer.Token;
import Lexer.TokenType;
import Simbolos.SymbolTable;
import Simbolos.TabladeExpresiones.Expresion;

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
    // Helper: ignorar líneas dañadas por el Validador
    // ============================================================
    private boolean ignorarLinea(int numeroLinea) {
        return errorManager.hayErroresEnLinea(numeroLinea);
    }

    // ============================================================
    // MÉTODO PRINCIPAL
    // ============================================================
    public void analizarBloques(String[] lineas) {

        for (int i = 0; i < lineas.length; i++) {

            int numeroLinea = i + 1;

            // Si la línea ya tiene errores → ignorar
            if (ignorarLinea(numeroLinea)) {
                continue;
            }

            String linea = lineas[i];
            String trim = linea.trim();

            Expresion expr = clasificarLinea(trim);

            switch (expr) {

                case WHILE:
                    validarWhile(lineas, i);
                    break;

                case FOR:
                    validarFor(lineas, i);
                    break;

                case IF:
                    validarIf(lineas, i);
                    break;

                // Estos ya no deben generar errores falsos
                case END_WHILE:
                case NEXT:
                case ELSE:
                case END_IF:
                    break;

                default:
                    break;
            }
        }
    }

    // ============================================================
    // CLASIFICACIÓN POR REGEX
    // ============================================================
    private Expresion clasificarLinea(String linea) {
        for (Expresion e : Expresion.values()) {
            if (e == Expresion.DESCONOCIDO) continue;
            if (linea.matches(e.patron)) return e;
        }
        return Expresion.DESCONOCIDO;
    }

    private boolean esLineaVaciaOComentario(String linea) {
        String trim = linea.trim();
        return trim.isEmpty()
                || trim.matches(Expresion.LINEA_VACIA.patron)
                || trim.matches(Expresion.COMENTARIO.patron);
    }

    // ============================================================
    // VALIDACIÓN WHILE
    // ============================================================
    private void validarWhile(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;

        // Si la línea ya tiene errores → ignorar
        if (ignorarLinea(numeroLineaInicio)) return;

        String lineaWhile = lineas[indiceInicio];
        List<Token> tokens = lexer.tokenizar(lineaWhile);

        if (tokens.isEmpty()) {
            errorManager.agregarError(ErrorCode.WHILE_CONDICION_INVALIDA, lineaWhile, numeroLineaInicio);
            return;
        }

        int idxWhile = buscarToken(tokens, TokenType.Type.WHILE, 0);
        if (idxWhile == -1) return;

        // Variable
        int idxVar = idxWhile + 1;
        if (idxVar >= tokens.size()) return;

        Token tokVar = tokens.get(idxVar);
        if (!tokVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(ErrorCode.WHILE_CONDICION_INVALIDA, lineaWhile, numeroLineaInicio);
            return;
        }

        String nombreVar = tokVar.lexema;

        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(ErrorCode.WHILE_IDENTIFICADOR_NO_DECLARADO, lineaWhile, numeroLineaInicio);
            return;
        }

        String tipoVar = symbolTable.getTipo(nombreVar);
        if (!tipoVar.equalsIgnoreCase("Integer") && !tipoVar.equalsIgnoreCase("Byte")) {
            errorManager.agregarError(ErrorCode.WHILE_OPERANDO_INVALIDO, lineaWhile, numeroLineaInicio);
            return;
        }

        // Operador
        int idxOp = buscarOperadorRelacional(tokens, idxVar + 1);
        if (idxOp == -1) {
            errorManager.agregarError(ErrorCode.WHILE_OPERADOR_RELACIONAL_INVALIDO, lineaWhile, numeroLineaInicio);
            return;
        }

        // Valor
        int idxVal = idxOp + 1;
        if (idxVal >= tokens.size()) {
            errorManager.agregarError(ErrorCode.WHILE_CONDICION_INVALIDA, lineaWhile, numeroLineaInicio);
            return;
        }

        Token tokVal = tokens.get(idxVal);
        if (!tokVal.es(TokenType.Type.NUMBER) || tokVal.lexema.contains(".")) {
            errorManager.agregarError(ErrorCode.WHILE_OPERANDO_INVALIDO, lineaWhile, numeroLineaInicio);
            return;
        }

        // Buscar End While
        boolean encontradoEnd = false;
        int indiceEnd = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            int numLinea = i + 1;

            if (ignorarLinea(numLinea)) continue;

            String trim = lineas[i].trim();
            Expresion expr = clasificarLinea(trim);

            if (expr == Expresion.WHILE) {
                errorManager.agregarError(ErrorCode.BLOQUE_DESBALANCEADO, lineas[i], numLinea);
                return;
            }

            if (expr == Expresion.END_WHILE) {
                encontradoEnd = true;
                indiceEnd = i;
                break;
            }
        }

        if (!encontradoEnd) {
            errorManager.agregarError(ErrorCode.WHILE_SIN_WEND, lineaWhile, numeroLineaInicio);
            return;
        }

        // Bloque vacío
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceEnd; i++) {
            if (!ignorarLinea(i + 1) && !esLineaVaciaOComentario(lineas[i])) {
                tieneCodigo = true;
                break;
            }
        }

        if (!tieneCodigo) {
            errorManager.agregarError(ErrorCode.WHILE_VACIO, lineaWhile, numeroLineaInicio);
        }
    }

    // ============================================================
    // VALIDACIÓN FOR
    // ============================================================
    private void validarFor(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;
        if (ignorarLinea(numeroLineaInicio)) return;

        String lineaFor = lineas[indiceInicio];
        List<Token> tokens = lexer.tokenizar(lineaFor);

        int idxFor = buscarToken(tokens, TokenType.Type.FOR, 0);
        if (idxFor == -1) return;

        // Variable
        int idxVar = idxFor + 1;
        if (idxVar >= tokens.size()) return;

        Token tokVar = tokens.get(idxVar);
        if (!tokVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(ErrorCode.FOR_SIN_VARIABLE, lineaFor, numeroLineaInicio);
            return;
        }

        String nombreVar = tokVar.lexema;

        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(ErrorCode.FOR_VARIABLE_NO_DECLARADA, lineaFor, numeroLineaInicio);
            return;
        }

        String tipoVar = symbolTable.getTipo(nombreVar);
        if (!tipoVar.equalsIgnoreCase("Integer") && !tipoVar.equalsIgnoreCase("Byte")) {
            errorManager.agregarError(ErrorCode.FOR_VARIABLE_NO_NUMERICA, lineaFor, numeroLineaInicio);
            return;
        }

        // '='
        int idxIgual = buscarToken(tokens, TokenType.Type.OP_ASSIGN, idxVar + 1);
        if (idxIgual == -1) {
            errorManager.agregarError(ErrorCode.FOR_SIN_IGUAL, lineaFor, numeroLineaInicio);
            return;
        }

        // Valor inicial
        int idxIni = buscarNumero(tokens, idxIgual + 1);
        if (idxIni == -1) {
            errorManager.agregarError(ErrorCode.FOR_SIN_VALOR_INICIAL, lineaFor, numeroLineaInicio);
            return;
        }

        Token tokIni = tokens.get(idxIni);
        if (!tokIni.es(TokenType.Type.NUMBER) || tokIni.lexema.contains(".")) {
            errorManager.agregarError(ErrorCode.FOR_VALOR_INICIAL_INVALIDO, lineaFor, numeroLineaInicio);
            return;
        }

        // TO
        int idxTo = buscarToken(tokens, TokenType.Type.TO, idxIni + 1);
        if (idxTo == -1) {
            errorManager.agregarError(ErrorCode.FOR_SIN_TO, lineaFor, numeroLineaInicio);
            return;
        }

        // Valor final
        int idxFin = buscarNumero(tokens, idxTo + 1);
        if (idxFin == -1) {
            errorManager.agregarError(ErrorCode.FOR_SIN_VALOR_FINAL, lineaFor, numeroLineaInicio);
            return;
        }

        Token tokFin = tokens.get(idxFin);
        if (!tokFin.es(TokenType.Type.NUMBER) || tokFin.lexema.contains(".")) {
            errorManager.agregarError(ErrorCode.FOR_VALOR_FINAL_INVALIDO, lineaFor, numeroLineaInicio);
            return;
        }

        // Buscar Next
        boolean encontradoNext = false;
        int indiceNext = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            int numLinea = i + 1;

            if (ignorarLinea(numLinea)) continue;

            Expresion expr = clasificarLinea(lineas[i].trim());

            if (expr == Expresion.FOR) {
                errorManager.agregarError(ErrorCode.BLOQUE_DESBALANCEADO, lineas[i], numLinea);
                return;
            }

            if (expr == Expresion.NEXT) {
                encontradoNext = true;
                indiceNext = i;
                break;
            }
        }

        if (!encontradoNext) {
            errorManager.agregarError(ErrorCode.FOR_SIN_NEXT, lineaFor, numeroLineaInicio);
            return;
        }

        // Bloque vacío
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceNext; i++) {
            if (!ignorarLinea(i + 1) && !esLineaVaciaOComentario(lineas[i])) {
                tieneCodigo = true;
                break;
            }
        }

        if (!tieneCodigo) {
            errorManager.agregarError(ErrorCode.FOR_VACIO, lineaFor, numeroLineaInicio);
        }
    }

    // ============================================================
    // VALIDACIÓN IF
    // ============================================================
    private void validarIf(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;
        if (ignorarLinea(numeroLineaInicio)) return;

        String lineaIf = lineas[indiceInicio];
        List<Token> tokens = lexer.tokenizar(lineaIf);

        int idxIf = buscarToken(tokens, TokenType.Type.IF, 0);
        if (idxIf == -1) return;

        int idxThen = buscarToken(tokens, TokenType.Type.THEN, idxIf + 1);
        if (idxThen == -1) {
            errorManager.agregarError(ErrorCode.IF_SIN_THEN, lineaIf, numeroLineaInicio);
            return;
        }

        if (idxThen == idxIf + 1) {
            errorManager.agregarError(ErrorCode.IF_SIN_CONDICION, lineaIf, numeroLineaInicio);
            return;
        }

        // Buscar Else y End If
        int indiceElse = -1;
        int indiceEndIf = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            int numLinea = i + 1;

            if (ignorarLinea(numLinea)) continue;

            Expresion expr = clasificarLinea(lineas[i].trim());

            if (expr == Expresion.IF) {
                errorManager.agregarError(ErrorCode.BLOQUE_DESBALANCEADO, lineas[i], numLinea);
                return;
            }

            if (expr == Expresion.ELSE && indiceElse == -1) {
                indiceElse = i;
                continue;
            }

            if (expr == Expresion.END_IF) {
                indiceEndIf = i;
                break;
            }
        }

        if (indiceEndIf == -1) {
            errorManager.agregarError(ErrorCode.IF_SIN_ENDIF, lineaIf, numeroLineaInicio);
            return;
        }

        // Validar THEN
        boolean tieneCodigoThen = false;

        for (int i = indiceInicio + 1; i < (indiceElse == -1 ? indiceEndIf : indiceElse); i++) {
            if (!ignorarLinea(i + 1) && !esLineaVaciaOComentario(lineas[i])) {
                tieneCodigoThen = true;
                break;
            }
        }

        if (!tieneCodigoThen) {
            errorManager.agregarError(ErrorCode.IF_VACIO, lineaIf, numeroLineaInicio);
        }

        // Validar ELSE
        if (indiceElse != -1) {

            boolean tieneCodigoElse = false;

            for (int i = indiceElse + 1; i < indiceEndIf; i++) {
                if (!ignorarLinea(i + 1) && !esLineaVaciaOComentario(lineas[i])) {
                    tieneCodigoElse = true;
                    break;
                }
            }

            if (!tieneCodigoElse) {
                errorManager.agregarError(ErrorCode.IF_VACIO, lineas[indiceElse], indiceElse + 1);
            }
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private int buscarToken(List<Token> tokens, TokenType.Type tipo, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).es(tipo)) return i;
        }
        return -1;
    }

    private int buscarNumero(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).es(TokenType.Type.NUMBER)) return i;
        }
        return -1;
    }

    private int buscarOperadorRelacional(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.es(TokenType.Type.OP_LT) ||
                t.es(TokenType.Type.OP_GT) ||
                t.es(TokenType.Type.OP_LTE) ||
                t.es(TokenType.Type.OP_GTE) ||
                t.es(TokenType.Type.OP_ASSIGN) ||
                t.es(TokenType.Type.OP_NEQ)) {
                return i;
            }
        }
        return -1;
    }
}
