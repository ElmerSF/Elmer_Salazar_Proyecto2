/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase que representa un token generado por el Lexer. Un token es la unidad
mínima del lenguaje: puede ser un identificador, número, palabra reservada,
operador, símbolo, etc. Cada token almacena el texto exacto encontrado en
el código fuente (lexema) y su categoría (type), definida en el enum TokenType.
*/
package Lexer;

public class Token {

    // Texto literal tal como aparece en el código fuente.
    public final String lexema;

    // Categoría del token según el enum TokenType (IDENTIFIER, NUMBER, etc.).
    public final TokenType.Type type;

    // Constructor: recibe el lexema y el tipo asignado por el Lexer.
    public Token(String lexema, TokenType.Type type) {
        this.lexema = lexema;
        this.type = type;
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    /**
     * Verifica si el token pertenece a un tipo específico.
     * Ejemplo: token.es(TokenType.Type.IDENTIFIER)
     */
    public boolean es(TokenType.Type tipo) {
        return this.type == tipo;
    }

    /**
     * Verifica simultáneamente el tipo y el lexema del token.
     * Útil para palabras reservadas o símbolos específicos.
     * Ejemplo: token.es("RESERVED_WORD", "Module")
     */
    public boolean es(String tipo, String valor) {
        return this.type.name().equalsIgnoreCase(tipo)
                && this.lexema.equalsIgnoreCase(valor);
    }

    /**
     * Devuelve una representación legible del token.
     * Ejemplo: [IDENTIFIER : "x"]
     */
    @Override
    public String toString() {
        return "[" + type + " : \"" + lexema + "\"]";
    }
}
