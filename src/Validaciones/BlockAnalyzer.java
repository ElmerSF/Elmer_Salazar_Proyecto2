/*UNED Informática Compiladores 3307
Estudiante Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase encargada de validar bloques de código
*/
package Validaciones;

import Errores.ErrorManager;
import Simbolos.SymbolTable;
import Lexer.Token;
import java.util.List;

public class BlockAnalyzer {

    private final ErrorManager errorManager;
    private final SymbolTable symbolTable;

    public BlockAnalyzer(ErrorManager errorManager, SymbolTable symbolTable) {
        this.errorManager = errorManager;
        this.symbolTable = symbolTable;
    }

    public void analizarBloques(List<List<Token>> lineasTokenizadas) {
        // Aquí luego metemos While, For, If
    }
}
