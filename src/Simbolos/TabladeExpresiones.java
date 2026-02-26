/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Tabla de expresiones que define patrones completos (regex) para reconocer
estructuras de líneas válidas dentro del lenguaje del Proyecto 1. A diferencia
del Lexer, que trabaja a nivel de tokens individuales, esta tabla permite
identificar líneas completas como declaraciones, bloques de control, llamadas
a funciones, etc.

Cada elemento del enum Expresion contiene un patrón regex que describe la forma
general de una instrucción. Esta tabla puede utilizarse para validaciones
adicionales, análisis estructural o diagnósticos.
Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo 
*/

package Simbolos;

public class TabladeExpresiones {

    public enum Expresion {

        // ============================================================
        // LÍNEAS VACÍAS
        // Coincide con líneas que contienen solo espacios o están vacías.
        // ============================================================
        LINEA_VACIA("^\\s*$"),

        // ============================================================
        // COMENTARIOS
        // Comentarios que inician con apostrofe (') opcionalmente precedidos
        // por espacios. Ejemplo:   ' Esto es un comentario
        // ============================================================
        COMENTARIO("^\\s*'.*$"),

        // ============================================================
        // IMPORTS
        // Reconoce instrucciones como:
        //   Imports System
        //   Imports System.IO
        //   Imports Mi.Namespace.Personalizado
        // ============================================================
        IMPORTS("^\\s*Imports\\s+[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*\\s*$"),

        // ============================================================
        // MODULE <Identificador>
        // Ejemplo:  Module MiPrograma
        // ============================================================
        MODULE("^\\s*Module\\s+[A-Za-z][A-Za-z0-9_]*\\s*$"),

        // ============================================================
        // END MODULE
        // Cierra el bloque principal del programa.
        // ============================================================
        END_MODULE("^\\s*End\\s+Module\\s*$"),

        // ============================================================
        // SUB <Identificador>()
        // Ejemplo:  Sub MiFuncion()
        // ============================================================
        SUB_DECLARACION("^\\s*Sub\\s+[A-Za-z][A-Za-z0-9_]*\\s*\\(.*\\)\\s*$"),

        // ============================================================
        // END SUB
        // ============================================================
        END_SUB("^\\s*End\\s+Sub\\s*$"),

        // ============================================================
        // FUNCTION <Identificador>(...) As Tipo
        // Ejemplo:
        //   Function Sumar(a As Integer, b As Integer) As Integer
        // ============================================================
        FUNCTION_DECLARACION(
            "^\\s*Function\\s+[A-Za-z][A-Za-z0-9_]*\\s*\\(.*\\)\\s*As\\s+(Integer|String|Boolean|Byte|Double)\\s*$"
        ),

        // ============================================================
        // END FUNCTION
        // ============================================================
        END_FUNCTION("^\\s*End\\s+Function\\s*$"),

        // ============================================================
        // RETURN <expresión>
        // Ejemplo:  Return x + 5
        // ============================================================
        RETURN("^\\s*Return\\s+.+$"),

        // ============================================================
        // IF <condición> THEN
        // Ejemplo:  If x > 5 Then
        // ============================================================
        IF("^\\s*If\\s+.+\\s+Then\\s*$"),

        // ============================================================
        // ELSEIF <condición> THEN
        // ============================================================
        ELSEIF("^\\s*ElseIf\\s+.+\\s+Then\\s*$"),

        // ============================================================
        // ELSE
        // ============================================================
        ELSE("^\\s*Else\\s*$"),

        // ============================================================
        // END IF
        // ============================================================
        END_IF("^\\s*End\\s+If\\s*$"),

        // ============================================================
        // DECLARACIÓN DIM
        // Ejemplo:
        //   Dim edad As Integer
        //   Dim nombre As String = "Juan"
        //
        // Solo permite tipos válidos del proyecto.
        // ============================================================
        DIM_DECLARACION(
            "^\\s*Dim\\s+[A-Za-z][A-Za-z0-9_]*\\s+As\\s+(Integer|String|Boolean|Byte)(\\s*=\\s*.+)?$"
        ),

        // ============================================================
        // ASIGNACIÓN SIMPLE
        // Ejemplo:  x = 10
        // ============================================================
        ASIGNACION("^\\s*[A-Za-z][A-Za-z0-9_]*\\s*=\\s*.+$"),

        // ============================================================
        // Console.WriteLine(...)
        // Ejemplo:  Console.WriteLine("Hola")
        // ============================================================
        CONSOLE_WRITELINE("^\\s*Console\\.WriteLine\\s*\\(.*\\)\\s*$"),

        // ============================================================
        // LLAMADA A FUNCIÓN
        // Ejemplo:  Sumar(5, 3)
        // ============================================================
        LLAMADA_FUNCION("^\\s*[A-Za-z][A-Za-z0-9_]*\\s*\\(.*\\)\\s*$"),

        // ============================================================
        // CUALQUIER OTRA LÍNEA
        // Patrón comodín para líneas que no coinciden con ninguna regla anterior.
        // ============================================================
        DESCONOCIDO(".*");

        // Patrón regex asociado a la expresión
        public final String patron;

        /**
         * Constructor del enum.
         * @param patron Expresión regular que define la estructura de la línea.
         */
        Expresion(String patron) {
            this.patron = patron;
        }
    }
}
