/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026

Clase principal del proyecto. 
Se encarga de:
 - Leer el archivo fuente .vb
 - Ejecutar el análisis léxico (Lexer)
 - Ejecutar el análisis sintáctico/semántico por línea (Validador)
 - Ejecutar el análisis de estructuras de control (BlockAnalyzer)
 - Generar archivos de depuración (tokens, símbolos, clasificación)
 - Generar el archivo .log con todos los errores detectados

Este flujo respeta el orden lógico de un compilador:
 1. Tokenización
 2. Validación sintáctica y semántica de línea
 3. Validación de bloques (While, For, If)
 4. Reporte de errores

Se usó apoyo de IA para revisión y pruebas del código así como ordenarlo.
*/


package App;

import Lexer.Token;
import Simbolos.SymbolTable;
import Archivos.FileManager;
import Lexer.Lexer;
import Validaciones.Validador;
import Errores.ErrorManager;
import Simbolos.TabladeExpresiones;
import Validaciones.BlockAnalyzer;

import java.util.List;

public class Main {

    // Manejo global de errores
    private static final ErrorManager errorManager = new ErrorManager();
    private static final String directorio = System.getProperty("user.dir");

    // Activar/desactivar archivos de depuración
    private static final boolean GENERAR_TOKENS = false;
    private static final boolean GENERAR_SIMBOLOS = false;
    private static final boolean GENERAR_CLASIFICACION = false;

    public static void main(String[] args) {

        // Encabezado
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.println("\033[32m\t Analizador Léxico   Proyecto 1 UNED Estudiante: Elmer Salazar (3-426-158)");
        System.out.println("\033[0m --------------------------------------------------------------------------------------------------------------\n");

        // Validar argumentos
        if (args.length == 0) {
            System.out.println("No se indicó ningún archivo como argumento.");
            return;
        }

        String archivo = args[0];

        // valida el tipo de extensión del archivo
        if (!archivo.toLowerCase().endsWith(".vb")) {
            System.out.println("El archivo debe tener extensión .vb");
            return;
        }

        // detalle informativo para el usuario
        System.out.println("Ubicación actual: " + directorio);
        System.out.println("Archivo recibido: " + archivo + "\n");

        // ASCII ART
        System.out.println("\033[32m                          .-\"\"\"-.");
        System.out.println("\033[32m                         / .===. \\");
        System.out.println("\033[32m                         \\\\ 6 6 \\\\");
        System.out.println("\033[32m                         ( \\___/ )");
        System.out.println("\033[32m    _________________ooo__\\_____/_____________________");
        System.out.println("\033[32m   /                                                  \\");
        System.out.println("\033[36m   |ANALIZADOR LÉXICO   Proyecto 1 Compiladores 03307  |");
        System.out.println("\033[32m   \\______________________________ooo_________________/");
        System.out.println("\033[32m                         |  |  |");
        System.out.println("\033[32m                         |_ | _|");
        System.out.println("\033[32m                         |  |  |");
        System.out.println("\033[32m                         |__|__|");
        System.out.println("\033[32m                         /-'Y'-\\");
        System.out.println("\033[32m                        (__/ \\__)");
        System.out.println("\033[0m");

        // Infraestructura
        FileManager fm = new FileManager();
        Lexer lexer = new Lexer();
        SymbolTable symbolTable = new SymbolTable();
        Validador validador = new Validador(errorManager, symbolTable);

        // Leer archivo fuente
        String[] lineas = fm.leerArchivo(archivo);

        if (lineas == null) {
            System.out.println("No se pudo leer el archivo.");
            return;
        }
        if (lineas.length == 0) {
            System.out.println("El archivo está vacío.");
            return;
        }

        // Crear archivo .log con numeración
        String archivoLog = fm.crearArchivoLog(archivo, lineas);

        // ------------------------------------------------------------
        // BARRA DE PROGRESO 
        // ------------------------------------------------------------
        mostrarBarraProgreso();

        // ------------------------------------------------------------
        // PROCESO PRINCIPAL: LÉXICO + SINTÁCTICO/SEMÁNTICO POR LÍNEA
        // ------------------------------------------------------------
        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i];
            int numeroLinea = i + 1;

            // 1. (Opcional) Clasificación por regex
            // TabladeExpresiones.Expresion tipoLinea = clasificarLinea(linea);

            // 2. Tokenizar
            List<Token> tokens = lexer.tokenizar(linea);

            // 3. Validar línea (Proyecto 1 + reglas base de Proyecto 2)
            validador.validarLinea(tokens, linea, numeroLinea);
        }

        // Validación final del archivo (End Module, etc.)
        validador.validarFinDeArchivo(lineas.length);

        // ------------------------------------------------------------
        // PROYECTO 2: ANÁLISIS DE BLOQUES (WHILE / FOR / IF)
        // Se ejecuta DESPUÉS de que la tabla de símbolos está llena
        // y de que las validaciones de línea ya se realizaron.
        // ------------------------------------------------------------
        BlockAnalyzer blockAnalyzer = new BlockAnalyzer(errorManager, symbolTable);
        blockAnalyzer.analizarBloques(lineas);

        // Escribir errores
        fm.escribirErrores(archivoLog, errorManager);

        // ------------------------------------------------------------
        // ARCHIVOS OPCIONALES DE DEPURACIÓN
        // ------------------------------------------------------------
        if (GENERAR_TOKENS) {
            fm.escribirTokensPorLinea(archivo, lineas, lexer);
        }

        if (GENERAR_SIMBOLOS) {
            fm.escribirTablaSimbolos(archivo, symbolTable);
        }

        if (GENERAR_CLASIFICACION) {
            fm.escribirClasificacionLineas(archivo, lineas);
        }

        System.out.println("\n\nAnálisis completado. Archivo log generado: " + archivoLog);
    }

    // ============================================================
    // CLASIFICAR LÍNEA SEGÚN TABLADEEXPRESIONES
    // ============================================================
    private static TabladeExpresiones.Expresion clasificarLinea(String linea) {
        for (TabladeExpresiones.Expresion exp : TabladeExpresiones.Expresion.values()) {
            if (linea.matches(exp.patron)) {
                return exp;
            }
        }
        return TabladeExpresiones.Expresion.DESCONOCIDO;
    }

    // ============================================================
    // BARRA DE PROGRESO APOYO VISUAL
    // ============================================================
    public static void mostrarBarraProgreso() {
        System.out.print("\nProcesando: \033[32m");
        for (int i = 0; i < 35; i++) {
            System.out.print(">");
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {}
        }
        System.out.println("\033[0m 100%");
    }
}
