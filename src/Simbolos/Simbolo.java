/*UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase que representa un símbolo dentro de la tabla de símbolos.
Cada símbolo corresponde a una variable declarada en el programa,
almacenando su nombre, tipo y valor (si aplica).

Esta estructura permite realizar validaciones semánticas como:
 - Verificar si una variable existe
 - Consultar su tipo
 - Validar asignaciones
 - Validar condiciones en While, For e If
*/

package Simbolos;

public class Simbolo {

    private final String nombre;   // Nombre de la variable
    private final String tipo;     // Tipo declarado (Integer, String, Boolean, Byte)
    private Object valor;          // Valor asignado (opcional)

    /**
     * Constructor del símbolo.
     * @param nombre Nombre de la variable
     * @param tipo   Tipo declarado de la variable
     */
    public Simbolo(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = null;
    }

    /**
     * Devuelve el nombre del símbolo.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el tipo del símbolo.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Devuelve el valor actual de la variable.
     */
    public Object getValor() {
        return valor;
    }

    /**
     * Asigna un valor a la variable.
     */
    public void setValor(Object valor) {
        this.valor = valor;
    }
}
