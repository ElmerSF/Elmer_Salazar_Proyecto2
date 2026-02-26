/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Enumeración que define todos los códigos de error utilizados por el analizador.
Cada error tiene:
 - un número único (código),
 - un mensaje descriptivo,
 - y pertenece a una categoría (Dim, Console.WriteLine, Module, End Module, etc.).

Esta estructura permite que el Validador y el ErrorManager trabajen con errores
de forma consistente, centralizada y fácil de mantener.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Errores;

public enum ErrorCode {

    // ============================================================
    // DIM — Errores relacionados con declaraciones de variables
    // ============================================================
    IDENTIFICADOR_INVALIDO(101, "Se esperaba un identificador después de 'Dim'."),
    FALTA_AS(102, "Se esperaba la palabra reservada 'As'."),
    TIPO_INVALIDO(103, "Tipo de dato inválido en declaración Dim."),
    VALOR_NO_COMPATIBLE(106, "El valor asignado no es compatible con el tipo declarado."),
    TOKENS_EXTRA(107, "Tokens extra después de la declaración de variable."),
    DIM_ANTES_DE_MODULE(108, "Las declaraciones Dim deben aparecer después del Module."),
    OPERANDO_NO_NUMERICO(109, "Operando no numérico en la operación matemática."),
    VARIABLE_NO_DECLARADA(110, "Se está utilizando una variable que no ha sido declarada."),
    OPERADOR_INVALIDO(111, "Operador inválido en la operación matemática."),
    OPERANDO_INVALIDO(112, "Operando inválido en la operación matemática."),
    USO_PALABRA_RESERVADA_COMO_TIPO(113, "Los tipos válidos son: Integer, String, Boolean y Byte."),
    USO_PALABRA_RESERVADA_COMO_IDENTIFICADOR(114, "No se puede usar una palabra reservada como identificador."),
    DECLARACION_INCOMPLETA(115, "Declaración Dim incompleta."),
    FALTA_IGUAL(116, "Se esperaba el operador '=' en la asignación."),
    FALTA_VALOR(117, "Se esperaba un valor después del '='."),
    IDENTIFICADOR_INICIA_CON_GUION_BAJO(118, "Un identificador no puede iniciar con '_'."),
    IDENTIFICADOR_INICIA_CON_NUMERO(119, "Un identificador no puede iniciar con un número."),
    IDENTIFICADOR_CON_ESPACIOS(120, "Un identificador no puede contener espacios."),

    // ============================================================
    // Console.WriteLine — Errores de impresión y sintaxis
    // ============================================================
    PARENTESIS_FALTANTE(121, "La sentencia Console.WriteLine debe incluir paréntesis de apertura y cierre."),
    PARENTESIS_VACIOS(122, "Los paréntesis de Console.WriteLine no pueden estar vacíos."),
    STRING_SIN_CERRAR(123, "La cadena dentro de Console.WriteLine debe iniciar y cerrar con comillas."),
    COMENTARIO_INVALIDO(124, "Los comentarios solo son válidos si la línea inicia con un apóstrofe (')."),

    // ============================================================
    // MODULE — Errores en la declaración del módulo principal
    // ============================================================
    MODULE_ANTES_DE_IMPORTS(130, "La sentencia Module debe aparecer después de Imports."),
    MODULE_SIN_IDENTIFICADOR(131, "Se esperaba un identificador después de 'Module'."),
    MODULE_ESPACIO_INCORRECTO(132, "Debe existir un único espacio entre 'Module' y el identificador."),

    // ============================================================
    // END MODULE — Errores en el cierre del módulo
    // ============================================================
    ENDMODULE_ESPACIO_INCORRECTO(140, "Debe existir un único espacio entre 'End' y 'Module'."),
    ENDMODULE_TIENE_TOKENS_EXTRA(141, "La sentencia 'End Module' no debe contener tokens adicionales."),
    ENDMODULE_DUPLICADO(142, "Se encontró más de un 'End Module' válido."),
    ENDMODULE_NO_ES_ULTIMA_LINEA(143, "'End Module' debe ser la última línea con contenido del archivo.");

    // ============================================================
    // CAMPOS INTERNOS
    // ============================================================
    private final int codigo;      // Número único del error
    private final String mensaje;  // Descripción del error

    /**
     * Constructor del enum.
     * @param codigo  Número del error
     * @param mensaje Mensaje descriptivo asociado
     */
    ErrorCode(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    /**
     * Devuelve el número del error.
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Devuelve el mensaje descriptivo del error.
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Representación legible del error.
     * Ejemplo: "Error 102. Se esperaba la palabra reservada 'As'."
     */
    @Override
    public String toString() {
        return "Error " + codigo + ". " + mensaje;
    }
}
