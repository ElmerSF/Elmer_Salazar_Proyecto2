package Validaciones;

import Lexer.Token;
import Lexer.TokenType;
import Simbolos.SymbolTable;
import Errores.ErrorCode;
import Errores.ErrorManager;
import java.util.List;
import java.util.ArrayList;

public class Validador {

    private final ErrorManager errorManager;
    private final SymbolTable symbolTable;

    private boolean moduleEncontrado = false;
    private boolean importsEncontrado = false;
    private boolean moduleValidado = false;

    private int cantidadEndModule = 0;
    private int lineaEndModule = -1;

    public Validador(ErrorManager errorManager, SymbolTable symbolTable) {
        this.errorManager = errorManager;
        this.symbolTable = symbolTable;
    }

    // ============================================================
    // MÉTODO PRINCIPAL DE VALIDACIÓN POR LÍNEA
    // ============================================================
    public void validarLinea(List<Token> tokens, String linea, int numeroLinea) {

        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        // ------------------------------------------------------------
        // 1. Manejo de comentarios
        // ------------------------------------------------------------
        int indiceComentario = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).es(TokenType.Type.COMMENT)) {
                indiceComentario = i;
                break;
            }
        }

        if (indiceComentario == 0) {
            return; // comentario válido
        }

        if (indiceComentario > 0) {
            errorManager.agregarError(ErrorCode.COMENTARIO_INVALIDO, linea, numeroLinea);
            tokens = new ArrayList<>(tokens.subList(0, indiceComentario));
            if (tokens.isEmpty()) return;
        }

        Token primero = tokens.get(0);

        // ------------------------------------------------------------
        // 2. End Module
        // ------------------------------------------------------------
        if (esEndModule(tokens)) {
            validarEndModule(tokens, linea, numeroLinea);
            return;
        }

        if (cantidadEndModule > 0) {
            errorManager.agregarError(ErrorCode.ENDMODULE_NO_ES_ULTIMA_LINEA, linea, numeroLinea);
            cantidadEndModule = 0;
            lineaEndModule = -1;
        }

        // ------------------------------------------------------------
        // 3. Imports
        // ------------------------------------------------------------
        if (primero.es(TokenType.Type.IMPORTS)) {
            importsEncontrado = true;
            return;
        }

        // ------------------------------------------------------------
        // 4. Module
        // ------------------------------------------------------------
        if (primero.es(TokenType.Type.MODULE)) {
            validarModule(tokens, linea, numeroLinea);
            return;
        }

        // ------------------------------------------------------------
        // 5. Dim
        // ------------------------------------------------------------
        if (primero.es(TokenType.Type.DIM)) {

            if (!moduleEncontrado) {
                errorManager.agregarError(ErrorCode.DIM_ANTES_DE_MODULE, linea, numeroLinea);
                return;
            }

            validarDeclaracionDim(tokens, linea, numeroLinea);
            return;
        }

        // ------------------------------------------------------------
        // 6. Console.WriteLine
        // ------------------------------------------------------------
        if (esConsoleWriteLine(tokens)) {
            validarConsoleWriteLine(tokens, linea, numeroLinea);
            return;
        }

        // ------------------------------------------------------------
        // 7. Estructuras de control (Proyecto 2)
        // ------------------------------------------------------------
        if (primero.es(TokenType.Type.IF)) return;
        if (primero.es(TokenType.Type.ELSEIF)) return;
        if (primero.es(TokenType.Type.ELSE)) return;

        if (primero.es(TokenType.Type.END) &&
            tokens.size() > 1 &&
            tokens.get(1).es(TokenType.Type.IF)) return;

        if (primero.es(TokenType.Type.WHILE)) return;

        if (primero.es(TokenType.Type.END) &&
            tokens.size() > 1 &&
            tokens.get(1).es(TokenType.Type.WHILE)) return;

        if (primero.es(TokenType.Type.FOR)) return;
        if (primero.es(TokenType.Type.NEXT)) return;

        // ------------------------------------------------------------
        // 8. PARCHE NUEVO — LÍNEAS INVÁLIDAS O COMENTARIOS SIN '
        // ------------------------------------------------------------
        String trimmed = linea.trim();

        if (!trimmed.isEmpty()
                && !trimmed.startsWith("'")
                && !esLineaValida(tokens)) {

            errorManager.agregarError(
                    ErrorCode.LINEA_INVALIDA_O_COMENTARIO_SIN_APOSTROFE,
                    linea,
                    numeroLinea
            );
        }
    }

    // ============================================================
    // MÉTODO AUXILIAR — Determina si una línea es válida
    // ============================================================
    private boolean esLineaValida(List<Token> tokens) {

        Token primero = tokens.get(0);

        return primero.es(TokenType.Type.IMPORTS)
            || primero.es(TokenType.Type.MODULE)
            || primero.es(TokenType.Type.DIM)
            || primero.es(TokenType.Type.IDENTIFIER)
            || primero.es(TokenType.Type.IF)
            || primero.es(TokenType.Type.ELSEIF)
            || primero.es(TokenType.Type.ELSE)
            || primero.es(TokenType.Type.END)
            || primero.es(TokenType.Type.WHILE)
            || primero.es(TokenType.Type.FOR)
            || primero.es(TokenType.Type.NEXT)
            || primero.es(TokenType.Type.STRING_LITERAL);
    }

    // ============================================================
    // END MODULE
    // ============================================================
    private boolean esEndModule(List<Token> tokens) {
        if (tokens.size() < 2) return false;
        return tokens.get(0).es(TokenType.Type.END) &&
               tokens.get(1).es(TokenType.Type.MODULE);
    }

    private void validarEndModule(List<Token> tokens, String linea, int numeroLinea) {

        int indexEnd = linea.indexOf("End");
        int indexModule = linea.indexOf("Module");

        if (indexModule - indexEnd != 4) {
            errorManager.agregarError(ErrorCode.ENDMODULE_ESPACIO_INCORRECTO, linea, numeroLinea);
            return;
        }

        if (tokens.size() > 2) {
            errorManager.agregarError(ErrorCode.ENDMODULE_TIENE_TOKENS_EXTRA, linea, numeroLinea);
            return;
        }

        cantidadEndModule++;
        lineaEndModule = numeroLinea;
    }

    public void validarFinDeArchivo(int ultimaLineaConContenido) {

        if (cantidadEndModule == 0) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_NO_ES_ULTIMA_LINEA,
                    "Fin de archivo",
                    ultimaLineaConContenido
            );
            return;
        }

        if (cantidadEndModule > 1) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_DUPLICADO,
                    "Fin de archivo",
                    lineaEndModule
            );
        }

        if (lineaEndModule != ultimaLineaConContenido) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_NO_ES_ULTIMA_LINEA,
                    "Fin de archivo",
                    ultimaLineaConContenido
            );
        }
    }

    // ============================================================
    // MODULE
    // ============================================================
    private void validarModule(List<Token> tokens, String linea, int numeroLinea) {

        if (!importsEncontrado) {
            errorManager.agregarError(ErrorCode.MODULE_ANTES_DE_IMPORTS, linea, numeroLinea);
            return;
        }

        if (tokens.size() < 2) {
            errorManager.agregarError(ErrorCode.MODULE_SIN_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        Token identificador = tokens.get(1);

        if (!identificador.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(ErrorCode.MODULE_SIN_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        if (identificador.es(TokenType.Type.MODULE) ||
            identificador.es(TokenType.Type.END) ||
            identificador.es(TokenType.Type.IF) ||
            identificador.es(TokenType.Type.THEN) ||
            identificador.es(TokenType.Type.ELSE) ||
            identificador.es(TokenType.Type.FUNCTION)) {

            errorManager.agregarError(ErrorCode.USO_PALABRA_RESERVADA_COMO_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        int indexModule = linea.indexOf("Module");
        int indexIdent = linea.indexOf(identificador.lexema);

        if (indexIdent - indexModule != 7) {
            errorManager.agregarError(ErrorCode.MODULE_ESPACIO_INCORRECTO, linea, numeroLinea);
            return;
        }

        moduleValidado = true;
        moduleEncontrado = true;
    }

    // ============================================================
    // Console.WriteLine
    // ============================================================
    private boolean esConsoleWriteLine(List<Token> tokens) {

        if (tokens.size() < 3) return false;

        return tokens.get(0).es(TokenType.Type.IDENTIFIER) &&
               tokens.get(0).lexema.equalsIgnoreCase("Console") &&
               tokens.get(1).lexema.equals(".") &&
               tokens.get(2).es(TokenType.Type.IDENTIFIER) &&
               tokens.get(2).lexema.equalsIgnoreCase("WriteLine");
    }

    private void validarConsoleWriteLine(List<Token> tokens, String linea, int numeroLinea) {

        for (int i = 3; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            if (t.es(TokenType.Type.STRING_LITERAL)) {
                if (!t.lexema.startsWith("\"") || !t.lexema.endsWith("\"")) {
                    errorManager.agregarError(ErrorCode.STRING_SIN_CERRAR, linea, numeroLinea);
                    return;
                }
            }

            if (t.lexema.equals("“") || t.lexema.equals("”")) {
                errorManager.agregarError(ErrorCode.STRING_SIN_CERRAR, linea, numeroLinea);
                return;
            }
        }

        Token ultimo = tokens.get(tokens.size() - 1);

        if (!ultimo.es(TokenType.Type.PAREN_CLOSE)) {
            errorManager.agregarError(ErrorCode.PARENTESIS_FALTANTE, linea, numeroLinea);
            return;
        }

        if (tokens.size() == 5) {
            errorManager.agregarError(ErrorCode.PARENTESIS_VACIOS, linea, numeroLinea);
            return;
        }
    }

    // ============================================================
    // Declaraciones Dim
    // ============================================================
    private void validarDeclaracionDim(List<Token> tokens, String linea, int numeroLinea) {

        if (tokens.size() < 4) {
            errorManager.agregarError(ErrorCode.DECLARACION_INCOMPLETA, linea, numeroLinea);
            return;
        }

        Token identificador = tokens.get(1);
        boolean identificadorEsValido = identificador.es(TokenType.Type.IDENTIFIER);

        if (!identificadorEsValido) {

            if (identificador.es(TokenType.Type.MODULE) ||
                identificador.es(TokenType.Type.END) ||
                identificador.es(TokenType.Type.IF) ||
                identificador.es(TokenType.Type.THEN) ||
                identificador.es(TokenType.Type.ELSE) ||
                identificador.es(TokenType.Type.FUNCTION) ||
                identificador.es(TokenType.Type.IMPORTS) ||
                identificador.es(TokenType.Type.DIM) ||
                identificador.es(TokenType.Type.AS)) {

                errorManager.agregarError(ErrorCode.USO_PALABRA_RESERVADA_COMO_IDENTIFICADOR, linea, numeroLinea);
            }

            String lex = identificador.lexema;

            if (lex.startsWith("_")) {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INICIA_CON_GUION_BAJO, linea, numeroLinea);

            } else if (lex.matches("^[0-9].*")) {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INICIA_CON_NUMERO, linea, numeroLinea);

            } else {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INVALIDO, linea, numeroLinea);
            }
        }

        Token asToken = tokens.get(2);

        if (asToken.es(TokenType.Type.IDENTIFIER) &&
            tokens.size() > 3 &&
            tokens.get(3).es(TokenType.Type.AS)) {

            errorManager.agregarError(ErrorCode.IDENTIFICADOR_CON_ESPACIOS, linea, numeroLinea);
            return;
        }

        if (!asToken.es(TokenType.Type.AS)) {
            errorManager.agregarError(ErrorCode.FALTA_AS, linea, numeroLinea);
            return;
        }

        Token tipo = tokens.get(3);
        String tipoLex = tipo.lexema;

        boolean tipoEsValido = esTipoValido(tipoLex);

        if (!tipoEsValido) {
            errorManager.agregarError(ErrorCode.TIPO_INVALIDO, linea, numeroLinea);
            return;
        }

        if (!identificadorEsValido) {
            return;
        }

        symbolTable.agregar(identificador.lexema, tipoLex);

        if (tokens.size() > 4) {
            validarAsignacion(tokens, linea, numeroLinea, tipo);
        }
    }

    private boolean esTipoValido(String tipo) {
        return tipo.equalsIgnoreCase("Integer") ||
               tipo.equalsIgnoreCase("String") ||
               tipo.equalsIgnoreCase("Boolean") ||
               tipo.equalsIgnoreCase("Byte");
    }

    private void validarAsignacion(List<Token> tokens, String linea, int numeroLinea, Token tipoDeclarado) {

        if (!tokens.get(4).es(TokenType.Type.OP_ASSIGN)) {
            errorManager.agregarError(ErrorCode.FALTA_IGUAL, linea, numeroLinea);
            return;
        }

        if (tokens.size() < 6) {
            errorManager.agregarError(ErrorCode.FALTA_VALOR, linea, numeroLinea);
            return;
        }

        if (tokens.size() == 6) {
            validarCompatibilidad(tipoDeclarado, tokens.get(5), linea, numeroLinea);
            return;
        }

        validarOperacionMatematica(tokens, linea, numeroLinea, tipoDeclarado);
    }

    private void validarCompatibilidad(Token tipo, Token valor, String linea, int numeroLinea) {

        String t = tipo.lexema.toLowerCase();

        switch (t) {

            case "integer":
            case "byte":
                if (!valor.es(TokenType.Type.NUMBER) || valor.lexema.contains(".")) {
                    errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
                }
                break;

            case "string":
                if (!valor.es(TokenType.Type.STRING_LITERAL)) {
                    errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
                }
                break;

            case "boolean":
                if (!valor.lexema.equalsIgnoreCase("True") &&
                    !valor.lexema.equalsIgnoreCase("False")) {
                    errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
                }
                break;

            default:
                errorManager.agregarError(ErrorCode.TIPO_INVALIDO, linea, numeroLinea);
        }
    }

    private void validarOperacionMatematica(List<Token> tokens, String linea, int numeroLinea, Token tipoDeclarado) {

        for (int i = 5; i < tokens.size(); i++) {

            Token t = tokens.get(i);

            if (t.es(TokenType.Type.OP_PLUS) ||
                t.es(TokenType.Type.OP_MINUS) ||
                t.es(TokenType.Type.OP_MULT) ||
                t.es(TokenType.Type.OP_DIV)) continue;

            if (t.es(TokenType.Type.NUMBER)) continue;

            if (t.es(TokenType.Type.OP_INVALID)) {
                errorManager.agregarError(ErrorCode.OPERADOR_INVALIDO, linea, numeroLinea);
                continue;
            }

            if (t.lexema.equals("==") ||
                t.lexema.equals("%")  ||
                t.lexema.equals("%%")) {

                errorManager.agregarError(ErrorCode.OPERADOR_INVALIDO, linea, numeroLinea);
                continue;
            }

            if (t.es(TokenType.Type.IDENTIFIER)) {

                if (!symbolTable.existe(t.lexema)) {
                    errorManager.agregarError(ErrorCode.VARIABLE_NO_DECLARADA, linea, numeroLinea);
                    continue;
                }

                String tipoVar = symbolTable.getTipo(t.lexema);

                if (tipoVar == null ||
                    (!tipoVar.equalsIgnoreCase("Integer") &&
                     !tipoVar.equalsIgnoreCase("Byte"))) {

                    errorManager.agregarError(ErrorCode.OPERANDO_NO_NUMERICO, linea, numeroLinea);
                }

                continue;
            }

            errorManager.agregarError(ErrorCode.OPERANDO_INVALIDO, linea, numeroLinea);
        }

        if (tipoDeclarado.lexema.equalsIgnoreCase("String") ||
            tipoDeclarado.lexema.equalsIgnoreCase("Boolean")) {

            errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
        }
    }
}
