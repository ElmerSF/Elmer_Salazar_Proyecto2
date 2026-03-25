/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de validar bloques de código (While, For, If) usando:
 - TabladeExpresiones (regex) para la forma general de la línea
 - Lexer/Token/TokenType para la validación semántica de la condición
 - SymbolTable para tipos y declaraciones

Este análisis se ejecuta ANTES del análisis línea por línea (Validador) y se
enfoca en:
 - Bloques mal cerrados
 - Bloques vacíos
 - Estructuras incompletas
 - Condiciones mal formadas (versión inicial)
 - Prohibición de anidamiento de While, For e If según la rúbrica

Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo
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
    // MÉTODO PRINCIPAL: Analiza todas las líneas del archivo
    // ============================================================
    public void analizarBloques(String[] lineas) {

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i];
            String trim = linea.trim();
            int numeroLinea = i + 1;

            Expresion expr = clasificarLinea(trim);

            switch (expr) {

                case WHILE:
                    validarWhile(lineas, i);
                    break;

                case END_WHILE:
                    // End While sin While correspondiente
                    errorManager.agregarError(
                            ErrorCode.WEND_SIN_WHILE,
                            linea,
                            numeroLinea
                    );
                    break;

                case FOR:
                    validarFor(lineas, i);
                    break;

                case NEXT:
                    // Next sin For correspondiente
                    errorManager.agregarError(
                            ErrorCode.NEXT_SIN_FOR,
                            linea,
                            numeroLinea
                    );
                    break;

                case IF:
                    validarIf(lineas, i);
                    break;

                case ELSE:
                    // Else sin If correspondiente
                    errorManager.agregarError(
                            ErrorCode.ELSE_SIN_IF,
                            linea,
                            numeroLinea
                    );
                    break;

                case END_IF:
                    // End If sin If correspondiente
                    errorManager.agregarError(
                            ErrorCode.ENDIF_SIN_IF,
                            linea,
                            numeroLinea
                    );
                    break;

                default:
                    // Otras líneas no son responsabilidad de BlockAnalyzer
                    break;
            }
        }
    }

    // ============================================================
    // CLASIFICACIÓN DE LÍNEAS USANDO TABLADEEXPRESIONES (REGEX)
    // ============================================================
    private Expresion clasificarLinea(String linea) {

        for (Expresion e : Expresion.values()) {
            if (e == Expresion.DESCONOCIDO) continue;
            if (linea.matches(e.patron)) {
                return e;
            }
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
    // VALIDACIÓN DE BLOQUE WHILE
    // ============================================================
    private void validarWhile(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;
        String lineaWhile = lineas[indiceInicio];

        // ------------------------------------------------------------
        // 1. Validar condición del While usando tokens
        //    Forma general: While <var> <op> <entero>
        // ------------------------------------------------------------
        List<Token> tokens = lexer.tokenizar(lineaWhile);
        if (tokens.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // Buscar token WHILE
        int idxWhile = buscarToken(tokens, TokenType.Type.WHILE, 0);
        if (idxWhile == -1 || idxWhile == tokens.size() - 1) {
            errorManager.agregarError(
                    ErrorCode.WHILE_SIN_CONDICION,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // Variable después de WHILE
        int idxVar = idxWhile + 1;
        Token tokVar = tokens.get(idxVar);

        if (!tokVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        String nombreVar = tokVar.lexema;

        // Variable debe estar declarada
        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(
                    ErrorCode.WHILE_IDENTIFICADOR_NO_DECLARADO,
                    lineaWhile,
                    numeroLineaInicio
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
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // Buscar operador relacional
        int idxOp = buscarOperadorRelacional(tokens, idxVar + 1);
        if (idxOp == -1) {
            errorManager.agregarError(
                    ErrorCode.WHILE_OPERADOR_RELACIONAL_INVALIDO,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // Operando derecho (se espera número entero)
        int idxValor = idxOp + 1;
        if (idxValor >= tokens.size()) {
            errorManager.agregarError(
                    ErrorCode.WHILE_CONDICION_INVALIDA,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        Token tokValor = tokens.get(idxValor);
        if (!tokValor.es(TokenType.Type.NUMBER) || tokValor.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.WHILE_OPERANDO_INVALIDO,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar el End While correspondiente
        // ------------------------------------------------------------
        boolean encontradoEnd = false;
        int indiceEnd = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i];
            String trim = linea.trim();
            int numLinea = i + 1;

            Expresion expr = clasificarLinea(trim);

            if (expr == Expresion.WHILE) {
                // No se permiten While anidados
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        linea,
                        numLinea
                );
                return;
            }

            if (expr == Expresion.END_WHILE) {
                encontradoEnd = true;
                indiceEnd = i;
                break;
            }
        }

        if (!encontradoEnd) {
            errorManager.agregarError(
                    ErrorCode.WHILE_SIN_WEND,
                    lineaWhile,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista al menos una línea ejecutable
        // ------------------------------------------------------------
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceEnd; i++) {
            if (!esLineaVaciaOComentario(lineas[i])) {
                tieneCodigo = true;
                break;
            }
        }

        if (!tieneCodigo) {
            errorManager.agregarError(
                    ErrorCode.WHILE_VACIO,
                    lineaWhile,
                    numeroLineaInicio
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE FOR
    // ============================================================
    private void validarFor(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;
        String lineaFor = lineas[indiceInicio];

        List<Token> tokens = lexer.tokenizar(lineaFor);
        if (tokens.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VARIABLE,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // Buscar FOR
        int idxFor = buscarToken(tokens, TokenType.Type.FOR, 0);
        if (idxFor == -1 || idxFor == tokens.size() - 1) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VARIABLE,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // Variable de control
        int idxVar = idxFor + 1;
        Token tokVar = tokens.get(idxVar);

        if (!tokVar.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VARIABLE,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        String nombreVar = tokVar.lexema;

        if (!symbolTable.existe(nombreVar)) {
            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_DECLARADA,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        String tipoVar = symbolTable.getTipo(nombreVar);
        if (tipoVar == null ||
                (!tipoVar.equalsIgnoreCase("Integer") &&
                 !tipoVar.equalsIgnoreCase("Byte"))) {

            errorManager.agregarError(
                    ErrorCode.FOR_VARIABLE_NO_NUMERICA,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // '='
        int idxIgual = buscarToken(tokens, TokenType.Type.OP_ASSIGN, idxVar + 1);
        if (idxIgual == -1) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_IGUAL,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // Valor inicial
        int idxValorInicial = buscarNumero(tokens, idxIgual + 1);
        if (idxValorInicial == -1) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_INICIAL,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        Token tokIni = tokens.get(idxValorInicial);
        if (!tokIni.es(TokenType.Type.NUMBER) || tokIni.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_INICIAL_INVALIDO,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // TO
        int idxTo = buscarToken(tokens, TokenType.Type.TO, idxValorInicial + 1);
        if (idxTo == -1) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_TO,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // Valor final
        int idxValorFinal = buscarNumero(tokens, idxTo + 1);
        if (idxValorFinal == -1) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_VALOR_FINAL,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        Token tokFin = tokens.get(idxValorFinal);
        if (!tokFin.es(TokenType.Type.NUMBER) || tokFin.lexema.contains(".")) {
            errorManager.agregarError(
                    ErrorCode.FOR_VALOR_FINAL_INVALIDO,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar el Next correspondiente
        // ------------------------------------------------------------
        boolean encontradoNext = false;
        int indiceNext = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i];
            String trim = linea.trim();
            int numLinea = i + 1;

            Expresion expr = clasificarLinea(trim);

            if (expr == Expresion.FOR) {
                // No se permiten For anidados
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        linea,
                        numLinea
                );
                return;
            }

            if (expr == Expresion.NEXT) {
                encontradoNext = true;
                indiceNext = i;
                break;
            }
        }

        if (!encontradoNext) {
            errorManager.agregarError(
                    ErrorCode.FOR_SIN_NEXT,
                    lineaFor,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar que exista al menos una línea ejecutable
        // ------------------------------------------------------------
        boolean tieneCodigo = false;

        for (int i = indiceInicio + 1; i < indiceNext; i++) {
            if (!esLineaVaciaOComentario(lineas[i])) {
                tieneCodigo = true;
                break;
            }
        }

        if (!tieneCodigo) {
            errorManager.agregarError(
                    ErrorCode.FOR_VACIO,
                    lineaFor,
                    numeroLineaInicio
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE BLOQUE IF
    // ============================================================
    private void validarIf(String[] lineas, int indiceInicio) {

        int numeroLineaInicio = indiceInicio + 1;
        String lineaIf = lineas[indiceInicio];

        List<Token> tokens = lexer.tokenizar(lineaIf);
        if (tokens.isEmpty()) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_CONDICION,
                    lineaIf,
                    numeroLineaInicio
            );
            return;
        }

        // IF
        int idxIf = buscarToken(tokens, TokenType.Type.IF, 0);
        if (idxIf == -1) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_CONDICION,
                    lineaIf,
                    numeroLineaInicio
            );
            return;
        }

        // THEN
        int idxThen = buscarToken(tokens, TokenType.Type.THEN, idxIf + 1);
        if (idxThen == -1) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_THEN,
                    lineaIf,
                    numeroLineaInicio
            );
            return;
        }

        // Debe existir algo entre IF y THEN (condición no vacía)
        if (idxThen == idxIf + 1) {
            errorManager.agregarError(
                    ErrorCode.IF_SIN_CONDICION,
                    lineaIf,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 2. Buscar Else y End If correspondientes
        // ------------------------------------------------------------
        int indiceElse = -1;
        int indiceEndIf = -1;

        for (int i = indiceInicio + 1; i < lineas.length; i++) {

            String linea = lineas[i];
            String trim = linea.trim();
            int numLinea = i + 1;

            Expresion expr = clasificarLinea(trim);

            if (expr == Expresion.IF) {
                // No se permiten IF anidados
                errorManager.agregarError(
                        ErrorCode.BLOQUE_DESBALANCEADO,
                        linea,
                        numLinea
                );
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
            errorManager.agregarError(
                    ErrorCode.IF_SIN_ENDIF,
                    lineaIf,
                    numeroLineaInicio
            );
            return;
        }

        // ------------------------------------------------------------
        // 3. Validar líneas de código después de THEN y después de ELSE
        // ------------------------------------------------------------

        // Caso 1: IF ... THEN ... END IF (sin ELSE)
        if (indiceElse == -1) {

            boolean tieneCodigoThen = false;

            for (int i = indiceInicio + 1; i < indiceEndIf; i++) {
                if (!esLineaVaciaOComentario(lineas[i])) {
                    tieneCodigoThen = true;
                    break;
                }
            }

            if (!tieneCodigoThen) {
                errorManager.agregarError(
                        ErrorCode.IF_VACIO,
                        lineaIf,
                        numeroLineaInicio
                );
            }

            return;
        }

        // Caso 2: IF ... THEN ... ELSE ... END IF
        boolean tieneCodigoThen = false;
        boolean tieneCodigoElse = false;

        for (int i = indiceInicio + 1; i < indiceElse; i++) {
            if (!esLineaVaciaOComentario(lineas[i])) {
                tieneCodigoThen = true;
                break;
            }
        }

        for (int i = indiceElse + 1; i < indiceEndIf; i++) {
            if (!esLineaVaciaOComentario(lineas[i])) {
                tieneCodigoElse = true;
                break;
            }
        }

        if (!tieneCodigoThen) {
            errorManager.agregarError(
                    ErrorCode.IF_VACIO,
                    lineaIf,
                    numeroLineaInicio
            );
        }

        if (!tieneCodigoElse) {
            errorManager.agregarError(
                    ErrorCode.IF_VACIO,
                    lineas[indiceElse],
                    indiceElse + 1
            );
        }
    }

    // ============================================================
    // HELPERS PARA BÚSQUEDA EN LISTAS DE TOKENS
    // ============================================================
    private int buscarToken(List<Token> tokens, TokenType.Type tipo, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).es(tipo)) {
                return i;
            }
        }
        return -1;
    }

    private int buscarNumero(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.es(TokenType.Type.NUMBER)) {
                return i;
            }
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
