/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase que define todos los tipos de tokens que el Lexer puede reconocer.
Cada tipo está asociado a un patrón (regex) que describe la forma que debe
tener el lexema para pertenecer a esa categoría. El Lexer utiliza estos
patrones para clasificar los fragmentos de texto extraídos del código fuente.
*/

package Lexer;

public class TokenType {

    // Enum que contiene todas las categorías de tokens del lenguaje.
    // Cada elemento incluye un patrón regex que describe su forma válida.
    public enum Type {

        // ============================================================
        // PALABRAS RESERVADAS
        // ============================================================
        MODULE("^MODULE$"),
        END("^END$"),
        SUB("^SUB$"),
        FUNCTION("^FUNCTION$"),
        DIM("^DIM$"),
        AS("^AS$"),
        IF("^IF$"),
        THEN("^THEN$"),
        ELSEIF("^ELSEIF$"),
        ELSE("^ELSE$"),
        RETURN("^RETURN$"),
        WHILE("^WHILE$"),
        IMPORTS("^IMPORTS$"),

        // Tipos de datos
        TYPE_INTEGER("^INTEGER$"),
        TYPE_STRING("^STRING$"),
        TYPE_BOOLEAN("^BOOLEAN$"),
        TYPE_BYTE("^BYTE$"),

        // ============================================================
        // IDENTIFICADORES Y LITERALES
        // ============================================================
        IDENTIFIER("^[A-Za-z][A-Za-z0-9_]*$"),   // Identificadores válidos
        NUMBER("^[0-9]+(\\.[0-9]+)?$"),          // Enteros o decimales
        STRING_LITERAL("^\"([^\"\\\\]|\\\\.)*\"$"), // Texto entre comillas

        // ============================================================
        // OPERADORES
        // ============================================================
        OP_ASSIGN("^=$"),
        OP_PLUS("^\\+$"),
        OP_MINUS("^-+$"),
        OP_MULT("^\\*$"),
        OP_DIV("^/$"),
        OP_CONCAT("^&$"),
        OP_INVALID("^==$"),   // patrón imposible, no es parte de vb

        // ============================================================
        // SÍMBOLOS
        // ============================================================
        PAREN_OPEN("^\\($"),
        PAREN_CLOSE("^\\)$"),
        COMMA("^,$"),

        // ============================================================
        // COMENTARIOS
        // ============================================================
        COMMENT("^'.*$"),

        // ============================================================
        // TOKENS INVÁLIDOS O DESCONOCIDOS
        // ============================================================
        UNKNOWN(".*");

        // Patrón regex asociado al tipo de token
        public final String patron;

        // Constructor del enum: asigna el patrón correspondiente
        Type(String patron) {
            this.patron = patron;
        }
    }
}
