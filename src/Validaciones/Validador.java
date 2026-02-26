/*
UNED Informática Compiladores 3307
Estudiante Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de validar las reglas sintácticas y semánticas del lenguaje
definido para el Proyecto 1. El Validador recibe los tokens generados por el
Lexer y aplica las reglas del enunciado: orden de Imports/Module, estructura
de End Module, declaraciones Dim, validación de Console.WriteLine, tipos de
datos, compatibilidad de asignaciones y detección de errores semánticos.

Cada error detectado se registra mediante ErrorManager, y la tabla de símbolos
(SymbolTable) se utiliza para validar variables declaradas y tipos asociados.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Validaciones;

import Lexer.Token;
import Lexer.TokenType;
import Simbolos.SymbolTable;
import Errores.ErrorCode;
import Errores.ErrorManager;
import java.util.List;
import java.util.ArrayList;

public class Validador {

    private final ErrorManager errorManager;   // Manejo centralizado de errores
    private final SymbolTable symbolTable;     // Tabla de símbolos para variables declaradas

    // Control del orden lógico del archivo
    private boolean moduleEncontrado = false;

    // Control de Imports y Module
    private boolean importsEncontrado = false;
    private boolean moduleValidado = false;

    // Control de End Module
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

        // Si la línea no tiene tokens, no hay nada que validar
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        // ------------------------------------------------------------
        // 1. Manejo de comentarios según los TOKENS, no por texto
        // ------------------------------------------------------------
        // Se busca el primer token que sea comentario. Todo lo que esté
        // después de él se descarta, y si aparece después de código,
        // se marca como COMENTARIO_INVALIDO.
        int indiceComentario = -1;
        for (int i = 0; i < tokens.size(); i++) {
            
            if (tokens.get(i).es(TokenType.Type.COMMENT)) {
                //Si se encuentra un comentario en la línea se asigna el valor de i 
                //indice de comentario
                indiceComentario = i;
                break;
            }
        }

        // Línea que es SOLO comentario → se ignora completamente
        //se encontró un solo comentario en la línea
        if (indiceComentario == 0) {
            return;
        }

        // Comentario después de código → error y se recorta la lista
        if (indiceComentario > 0) {
            errorManager.agregarError(ErrorCode.COMENTARIO_INVALIDO, linea, numeroLinea);

            // Se crea una nueva lista para evitar modificar la original
            tokens = new ArrayList<>(tokens.subList(0, indiceComentario));

            // Si después de recortar no queda nada, no hay más que validar
            if (tokens.isEmpty()) {
                return;
            }
        }

        Token primero = tokens.get(0);

        // ------------------------------------------------------------
        // 2. Validación de End Module
        // ------------------------------------------------------------
        if (esEndModule(tokens)) {
            validarEndModule(tokens, linea, numeroLinea);
            return;
        }

        // Código después de un End Module válido → error
        if (cantidadEndModule > 0) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_NO_ES_ULTIMA_LINEA,
                    linea,
                    numeroLinea
            );
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

        // Module encontrado
        if (primero.es(TokenType.Type.MODULE)) {
            moduleEncontrado = true;
            return;
        }

        // ------------------------------------------------------------
        // 5. Declaraciones Dim
        // ------------------------------------------------------------
        if (primero.es(TokenType.Type.DIM)) {

            // Dim antes de Module → error
            //ya no se sigue validando más esa declaración de DIM porque se tiene un error Primario
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
        }
    }

    // ============================================================
    // END MODULE
    // ============================================================
    private boolean esEndModule(List<Token> tokens) {

        // End Module debe tener al menos dos tokens
        if (tokens.size() < 2) return false;

        // Se valida la secuencia exacta: END MODULE
        return tokens.get(0).es(TokenType.Type.END) &&
               tokens.get(1).es(TokenType.Type.MODULE);
    }

    private void validarEndModule(List<Token> tokens, String linea, int numeroLinea) {

        // Validación del espacio exacto entre End y Module
        int indexEnd = linea.indexOf("End");
        int indexModule = linea.indexOf("Module");
        
        //se resta los indices de inicio y si la diferencia es distinta de 4
        if (indexModule - indexEnd != 4) {
            errorManager.agregarError(ErrorCode.ENDMODULE_ESPACIO_INCORRECTO, linea, numeroLinea);
            return;
        }

        // No debe haber tokens adicionales
        if (tokens.size() > 2) {
            errorManager.agregarError(ErrorCode.ENDMODULE_TIENE_TOKENS_EXTRA, linea, numeroLinea);
            return;
        }
        //esta "bandera" indica que se ha validado ya un token como End Module
        cantidadEndModule++;
        lineaEndModule = numeroLinea;
    }

    public void validarFinDeArchivo(int ultimaLineaConContenido) {

        // No apareció End Module
        if (cantidadEndModule == 0) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_NO_ES_ULTIMA_LINEA,
                    "Fin de archivo",
                    ultimaLineaConContenido
            );
            return;
        }

        // Apareció más de uno
        if (cantidadEndModule > 1) {
            errorManager.agregarError(
                    ErrorCode.ENDMODULE_DUPLICADO,
                    "Fin de archivo",
                    lineaEndModule
            );
        }

        // End Module no está en la última línea
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

        // Imports debe aparecer antes de Module
        if (!importsEncontrado) {
            errorManager.agregarError(ErrorCode.MODULE_ANTES_DE_IMPORTS, linea, numeroLinea);
            return;
        }

        // Module debe tener un identificador
        if (tokens.size() < 2) {
            errorManager.agregarError(ErrorCode.MODULE_SIN_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        Token identificador = tokens.get(1);

        // Validación del identificador
        if (!identificador.es(TokenType.Type.IDENTIFIER)) {
            errorManager.agregarError(ErrorCode.MODULE_SIN_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        // Evitar palabras reservadas como nombre de módulo
        if (identificador.es(TokenType.Type.MODULE) ||
            identificador.es(TokenType.Type.END) ||
            identificador.es(TokenType.Type.IF) ||
            identificador.es(TokenType.Type.THEN) ||
            identificador.es(TokenType.Type.ELSE) ||
            identificador.es(TokenType.Type.FUNCTION)) {

            errorManager.agregarError(ErrorCode.USO_PALABRA_RESERVADA_COMO_IDENTIFICADOR, linea, numeroLinea);
            return;
        }

        // Validación del espacio exacto entre "Module" y el nombre
        //le pedimos el número donde empieza module ejemplo 0
        //le pedimos el número donde empieza el proximo lexema 7
        //restamos la y debe dar 7 si da más que eso es que hay espacios en medio
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

        // Debe tener al menos: Console . WriteLine
        if (tokens.size() < 3) {
            return false;
        }

        return tokens.get(0).es(TokenType.Type.IDENTIFIER) &&
               tokens.get(0).lexema.equalsIgnoreCase("Console") &&
               tokens.get(1).lexema.equals(".") &&
               tokens.get(2).es(TokenType.Type.IDENTIFIER) &&
               tokens.get(2).lexema.equalsIgnoreCase("WriteLine");
    }

    private void validarConsoleWriteLine(List<Token> tokens, String linea, int numeroLinea) {

        // 1. Validación de strings dentro de los paréntesis
        for (int i = 3; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            // String mal cerrado
            if (t.es(TokenType.Type.STRING_LITERAL)) {
                if (!t.lexema.startsWith("\"") || !t.lexema.endsWith("\"")) {
                    errorManager.agregarError(ErrorCode.STRING_SIN_CERRAR, linea, numeroLinea);
                    return;
                }
            }

            // Comillas Unicode “ ” → error
            if (t.lexema.equals("“") || t.lexema.equals("”")) {
                errorManager.agregarError(ErrorCode.STRING_SIN_CERRAR, linea, numeroLinea);
                return;
            }
        }

        // 2. Validación del paréntesis de cierre
        Token ultimo = tokens.get(tokens.size() - 1);

        if (!ultimo.es(TokenType.Type.PAREN_CLOSE)) {
            errorManager.agregarError(ErrorCode.PARENTESIS_FALTANTE, linea, numeroLinea);
            return;
        }

        // 3. Paréntesis vacíos
        if (tokens.size() == 5) {
            errorManager.agregarError(ErrorCode.PARENTESIS_VACIOS, linea, numeroLinea);
            return;
        }

        // 4. Strings sin cerrar dentro de los paréntesis
        for (int i = 4; i < tokens.size() - 1; i++) {
            Token t = tokens.get(i);

            if (t.es(TokenType.Type.STRING_LITERAL)) {
                if (!t.lexema.startsWith("\"") || !t.lexema.endsWith("\"")) {
                    errorManager.agregarError(ErrorCode.STRING_SIN_CERRAR, linea, numeroLinea);
                    return;
                }
            }
        }
    }

    // ============================================================
    // Declaraciones Dim
    // ============================================================
    private void validarDeclaracionDim(List<Token> tokens, String linea, int numeroLinea) {

        // Dim debe tener al menos: Dim id As Tipo
        if (tokens.size() < 4) {
            errorManager.agregarError(ErrorCode.DECLARACION_INCOMPLETA, linea, numeroLinea);
            return;
        }
        //Pasamos a revisar el próximo token para validar
        Token identificador = tokens.get(1);

        // Validación del identificador
        if (!identificador.es(TokenType.Type.IDENTIFIER)) {

            // Palabras reservadas usadas como identificador
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

            // Identificador inválido por reglas del lenguaje
            String lex = identificador.lexema;

            if (lex.startsWith("_")) {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INICIA_CON_GUION_BAJO, linea, numeroLinea);

            } else if (lex.matches("^[0-9].*")) {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INICIA_CON_NUMERO, linea, numeroLinea);

            } else if (!identificador.es(TokenType.Type.IDENTIFIER)) {
                errorManager.agregarError(ErrorCode.IDENTIFICADOR_INVALIDO, linea, numeroLinea);
            }
        }

        Token asToken = tokens.get(2);

        // Identificador con espacios (ej: "mi var As Integer")
        //si lo que sigue es otro identificador quiere decir que hay un espacio y un identificador compuesto
        if (asToken.es(TokenType.Type.IDENTIFIER) &&
            tokens.size() > 3 &&
            tokens.get(3).es(TokenType.Type.AS)) {

            errorManager.agregarError(ErrorCode.IDENTIFICADOR_CON_ESPACIOS, linea, numeroLinea);
            return;
        }

        // Falta la palabra As
        if (!asToken.es(TokenType.Type.AS)) {
            errorManager.agregarError(ErrorCode.FALTA_AS, linea, numeroLinea);
        }

        Token tipo = tokens.get(3);
        String tipoLex = tipo.lexema;

        boolean tipoEsValido = esTipoValido(tipoLex);

        // Palabra reservada usada como tipo
        if (tipo.es(TokenType.Type.MODULE) ||
            tipo.es(TokenType.Type.END) ||
            tipo.es(TokenType.Type.IF) ||
            tipo.es(TokenType.Type.THEN) ||
            tipo.es(TokenType.Type.ELSE) ||
            tipo.es(TokenType.Type.FUNCTION) ||
            tipo.es(TokenType.Type.IMPORTS) ||
            tipo.es(TokenType.Type.DIM)) {

            errorManager.agregarError(ErrorCode.USO_PALABRA_RESERVADA_COMO_TIPO, linea, numeroLinea);
            return;
        }

        // Tipo inválido
        if (!tipoEsValido) {
            errorManager.agregarError(ErrorCode.TIPO_INVALIDO, linea, numeroLinea);
            return;
        }

        // Registrar variable en la tabla de símbolos
        symbolTable.registrar(identificador.lexema, tipoLex);

        // Asignación opcional
        if (tokens.size() > 4) {
            validarAsignacion(tokens, linea, numeroLinea, tipo);
        }

        // Validación de tokens extra sin operación
        boolean hayOperacion = false;

        for (int i = 5; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.es(TokenType.Type.OP_PLUS) ||
                t.es(TokenType.Type.OP_MINUS) ||
                t.es(TokenType.Type.OP_MULT) ||
                t.es(TokenType.Type.OP_DIV)) {
                hayOperacion = true;
                break;
            }
        }

        if (!hayOperacion && tokens.size() > 6) {
            errorManager.agregarError(ErrorCode.TOKENS_EXTRA, linea, numeroLinea);
        }
    }
    //tipos validos de variables
    private boolean esTipoValido(String tipo) {
        return tipo.equalsIgnoreCase("Integer") ||
               tipo.equalsIgnoreCase("String") ||
               tipo.equalsIgnoreCase("Boolean") ||
               tipo.equalsIgnoreCase("Byte");
    }

    private void validarAsignacion(List<Token> tokens, String linea, int numeroLinea, Token tipoDeclarado) {

        // Debe existir el operador "="
        if (!tokens.get(4).es(TokenType.Type.OP_ASSIGN)) {
            errorManager.agregarError(ErrorCode.FALTA_IGUAL, linea, numeroLinea);
            return;
        }

        // Falta el valor después del "="
        if (tokens.size() < 6) {
            errorManager.agregarError(ErrorCode.FALTA_VALOR, linea, numeroLinea);
            return;
        }

        // Asignación simple: Dim x As Integer = 5
        if (tokens.size() == 6) {
            validarCompatibilidad(tipoDeclarado, tokens.get(5), linea, numeroLinea);
            return;
        }

        // Asignación con operación matemática
        validarOperacionMatematica(tokens, linea, numeroLinea, tipoDeclarado);
    }

    private void validarCompatibilidad(Token tipo, Token valor, String linea, int numeroLinea) {

        String t = tipo.lexema.toLowerCase();

        switch (t) {

            case "integer":
                if (!valor.es(TokenType.Type.NUMBER) || valor.lexema.contains(".")) {
                    errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
                }
                break;

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

        // Recorre todos los tokens a partir del valor inicial (posición 5),
        // validando que la expresión matemática solo contenga:
        // - números
        // - operadores válidos (+, -, *, /)
        // - identificadores numéricos previamente declarados
        for (int i = 5; i < tokens.size(); i++) {

            Token t = tokens.get(i);

            // Operadores aritméticos válidos
            if (t.es(TokenType.Type.OP_PLUS) ||
                t.es(TokenType.Type.OP_MINUS) ||
                t.es(TokenType.Type.OP_MULT) ||
                t.es(TokenType.Type.OP_DIV)) {
                continue;
            }

            // Números válidos
            if (t.es(TokenType.Type.NUMBER)) {
                continue;
            }

            // Operador inválido detectado por el Lexer (por ejemplo "==")
            if (t.es(TokenType.Type.OP_INVALID)) {
                errorManager.agregarError(ErrorCode.OPERADOR_INVALIDO, linea, numeroLinea);
                continue;
            }

            // Operadores inválidos detectados por el lexema crudo
            // (casos como "==", "%", "%%" si llegaron tokenizados de esa forma)
            if (t.lexema.equals("==") ||
                t.lexema.equals("%")  ||
                t.lexema.equals("%%")) {

                errorManager.agregarError(ErrorCode.OPERADOR_INVALIDO, linea, numeroLinea);
                continue;
            }

            // Identificadores usados dentro de la operación
            if (t.es(TokenType.Type.IDENTIFIER)) {

                // Variable no declarada en la tabla de símbolos
                if (!symbolTable.existe(t.lexema)) {
                    errorManager.agregarError(ErrorCode.VARIABLE_NO_DECLARADA, linea, numeroLinea);
                    continue;
                }

                // Solo se permiten variables de tipo Integer o Byte en operaciones numéricas
                String tipoVar = symbolTable.tipoDe(t.lexema);

                if (!tipoVar.equals("integer") && !tipoVar.equals("byte")) {
                    errorManager.agregarError(ErrorCode.OPERANDO_NO_NUMERICO, linea, numeroLinea);
                }

                continue;
            }

            // Cualquier otro token que llegue aquí es un operando inválido
            errorManager.agregarError(ErrorCode.OPERANDO_INVALIDO, linea, numeroLinea);
        }

        // Si la variable declarada es de tipo String o Boolean,
        // no debería participar en operaciones matemáticas
        if (tipoDeclarado.lexema.equalsIgnoreCase("String") ||
            tipoDeclarado.lexema.equalsIgnoreCase("Boolean")) {

            errorManager.agregarError(ErrorCode.VALOR_NO_COMPATIBLE, linea, numeroLinea);
        }
    }
}
