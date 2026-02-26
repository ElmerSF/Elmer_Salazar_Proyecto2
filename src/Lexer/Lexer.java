/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026
Clase para tokenizar cada línea del archivo fuente
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo
*/
package Lexer;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public Lexer() {}

    // ============================================================
    // MÉTODO PRINCIPAL
    // ============================================================
    public List<Token> tokenizar(String linea) {

        List<Token> tokens = new ArrayList<>();

        if (linea == null || linea.isEmpty()) {
            return tokens;
        }

        int i = 0;
        int n = linea.length();

        while (i < n) {

            char c = linea.charAt(i);

            // ------------------------------------------------------------
            // ESPACIOS EN BLANCO
            // ------------------------------------------------------------
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // ------------------------------------------------------------
            // COMENTARIOS (todo lo que sigue a ')
            // ------------------------------------------------------------
            if (c == '\'') {
                tokens.add(new Token(linea.substring(i), TokenType.Type.COMMENT));
                break;
            }

            // ------------------------------------------------------------
            // STRINGS ENTRE COMILLAS ASCII
            // ------------------------------------------------------------
            if (c == '"') {
                
                //se guarda la posición actual y empieza a recorrer cada caracter
                int inicio = i;
                i++;

                //StringBuilder es una clase mutable que permite construir cadenas de texto de forma eficiente.
                StringBuilder sb = new StringBuilder();
                
                //va guardando cada caracter
                sb.append('"');

                boolean cerrado = false;

                while (i < n) {
                    char d = linea.charAt(i);
                    sb.append(d);

                    //hasta que encuetre la comilla de cierre
                    if (d == '"') {
                        cerrado = true;
                        i++;
                        break;
                    }
                    i++;
                }
                    // Si 'cerrado' es true, la cadena es válida y se marca como STRING_LITERAL.
                    // Si 'cerrado' es false, significa que falta la comilla de cierre y se marca como UNKNOWN.
                tokens.add(new Token(sb.toString(),
                        cerrado ? TokenType.Type.STRING_LITERAL : TokenType.Type.UNKNOWN));
                continue;
            }

            // ------------------------------------------------------------
            // COMILLAS UNICODE (“ ”) → ERROR
            // ------------------------------------------------------------
            if (c == '“' || c == '”') {
                tokens.add(new Token(String.valueOf(c), TokenType.Type.UNKNOWN));
                i++;
                continue;
            }

            // ------------------------------------------------------------
            // IDENTIFICADORES QUE INICIAN CON "_"
            // ------------------------------------------------------------
            if (c == '_') {
                int inicio = i;
                while (i < n && (Character.isLetterOrDigit(linea.charAt(i)) || linea.charAt(i) == '_')) {
                    i++;
                }
                tokens.add(new Token(linea.substring(inicio, i), TokenType.Type.UNKNOWN));
                continue;
            }

            // ------------------------------------------------------------
            // NÚMEROS O IDENTIFICADORES QUE INICIAN CON NÚMERO
            // ------------------------------------------------------------
            if (Character.isDigit(c)) {

                int inicio = i;
                boolean tieneLetras = false;
                boolean tienePunto = false;

                while (i < n &&
                       (Character.isLetterOrDigit(linea.charAt(i)) ||
                        linea.charAt(i) == '_' ||
                        linea.charAt(i) == '.')) {

                    char d = linea.charAt(i);

                    if (Character.isLetter(d) || d == '_') {
                        tieneLetras = true;
                    }

                    if (d == '.') {
                        if (tienePunto) break;
                        tienePunto = true;
                    }

                    i++;
                }

                String lexema = linea.substring(inicio, i);
                
                //si tiene letras 
                if (tieneLetras) {
                    tokens.add(new Token(lexema, TokenType.Type.UNKNOWN));
                } else {
                    tokens.add(new Token(lexema, TokenType.Type.NUMBER));
                }

                continue;
            }

            // ------------------------------------------------------------
            // IDENTIFICADORES O PALABRAS RESERVADAS
            // ------------------------------------------------------------
            if (Character.isLetter(c)) {

                int inicio = i;
                  //mientras sea una letra digito o _
                while (i < n &&
                       (Character.isLetterOrDigit(linea.charAt(i)) ||
                        linea.charAt(i) == '_')) {
                    i++;
                }
                //palabra completa, y se pasa a mayúscula
                String lexema = linea.substring(inicio, i);
                String upper = lexema.toUpperCase();

                // Buscar si coincide con alguna palabra reservada del enum
                TokenType.Type tipo = obtenerPalabraReservada(upper);

                //si se retorno la palabra reservada se agrega
                if (tipo != null) {
                    tokens.add(new Token(lexema, tipo));
                    
                    //si el retorno fue null es un identificador
                } else {
                    tokens.add(new Token(lexema, TokenType.Type.IDENTIFIER));
                }

                continue;
            }

            // ------------------------------------------------------------
            // OPERADORES
            // ------------------------------------------------------------
            
            //si encontré un signo = y el próximo caracter es también un =
            //se encontró un == que no es un operador válido en vb
            if (c == '=' && i + 1 < n && linea.charAt(i + 1) == '=') {
                tokens.add(new Token("==", TokenType.Type.OP_INVALID));
                i += 2;
                continue;
            }

            switch (c) {
                case '=':
                    tokens.add(new Token("=", TokenType.Type.OP_ASSIGN));
                    i++;
                    continue;
                case '+':
                    tokens.add(new Token("+", TokenType.Type.OP_PLUS));
                    i++;
                    continue;
                case '-':
                    tokens.add(new Token("-", TokenType.Type.OP_MINUS));
                    i++;
                    continue;
                case '*':
                    tokens.add(new Token("*", TokenType.Type.OP_MULT));
                    i++;
                    continue;
                case '/':
                    tokens.add(new Token("/", TokenType.Type.OP_DIV));
                    i++;
                    continue;
                case '&':
                    tokens.add(new Token("&", TokenType.Type.OP_CONCAT));
                    i++;
                    continue;
            }

            // ------------------------------------------------------------
            // SÍMBOLOS
            // ------------------------------------------------------------
            if (c == '(') {
                tokens.add(new Token("(", TokenType.Type.PAREN_OPEN));
                i++;
                continue;
            }

            if (c == ')') {
                tokens.add(new Token(")", TokenType.Type.PAREN_CLOSE));
                i++;
                continue;
            }

            if (c == ',') {
                tokens.add(new Token(",", TokenType.Type.COMMA));
                i++;
                continue;
            }
        

            // ------------------------------------------------------------
            // CUALQUIER OTRO CARÁCTER → UNKNOWN
            // ------------------------------------------------------------
            tokens.add(new Token(String.valueOf(c), TokenType.Type.UNKNOWN));
            i++;
        }

        return tokens;
    }

    // ============================================================
    // DETECTAR PALABRAS RESERVADAS DEL ENUM
    // ============================================================
    private TokenType.Type obtenerPalabraReservada(String upper) {
        //se recorre todas las constantes de Enum
        for (TokenType.Type t : TokenType.Type.values()) {
           //si coincide lo retorna
            if (t.name().equals(upper)) {
                return t;
            }
        }
        return null;
    }
}
