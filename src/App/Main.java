/*
UNED Informática Compiladores 3307
Estudiante: Elmer Eduardo Salazar Flores 3-0426-0158
I Cuatrimestre 2026
Clase principal donde se inicia

ASCII art basado en colecciones anónimas de:
ASCII Art Archive. (n.d.). ASCII Art Gallery. https://www.asciiart.eu/gallery
Modificado para uso en el Proyecto 1 de Compiladores (UNED).
*/

package App;

import Lexer.Token;
import Simbolos.SymbolTable;
import Archivos.FileManager;
import Lexer.Lexer;
import Validaciones.Validador;
import Errores.ErrorManager;
import Simbolos.TabladeExpresiones;

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

        //valida el tipo de extensión del archivo
        if (!archivo.toLowerCase().endsWith(".vb")) {
            System.out.println("El archivo debe tener extensión .vb");
            return;
        }
        //detalle informativo para el usuario
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

        // Infraestructura (instanciamos las diferentes clases)
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

        // Procesar línea por línea
        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i];
            int numeroLinea = i + 1;

            // 1. Clasificar línea completa
            TabladeExpresiones.Expresion tipoLinea = clasificarLinea(linea);

            // 2. Tokenizar
            List<Token> tokens = lexer.tokenizar(linea);

            // 3. Validar
            validador.validarLinea(tokens, linea, numeroLinea);
        }

        // Validación final del archivo
        validador.validarFinDeArchivo(lineas.length);

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
            //ser revisa con cual de los patrones se identifica
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
