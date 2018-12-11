/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ignacio
 */
public class Analizador {
    
    private static ArrayList<String> txtError;
    private static ArrayList<String> variable;
    private static ArrayList<String> tipoVar;
    private static ArrayList<String> valorVar;
    private static ArrayList<String> tokens;
    private static ArrayList<String> valorTokens;
    private static ArrayList<Integer> column1;
    private static ArrayList<String> column2;
    private static ArrayList<String> column3;
    private static ArrayList<String> column4;
    private static ArrayList<Integer> posDo;
    private static ArrayList<Integer> posWhile;
    private static Boolean errores;
    private static int numIDE, numOA, numPR, numCE, numCF, numOR, numOB;
    private static String linea;
    
    /**
     * @param codigo
     * @return 
     */
    public static Boolean analizar(String codigo) {
        
        // Objeto para la construcción de strings.
        StringBuilder stb = new StringBuilder();
        
        txtError = new ArrayList<>();
        variable = new ArrayList<>();
        tipoVar = new ArrayList<>();
        valorVar = new ArrayList<>();
        tokens = new ArrayList<>();
        valorTokens = new ArrayList<>();
        numIDE = 0; numOA = 0; numPR = 0; numCE = 0; numCF = 0; numOR = 0; numOB = 0;
        linea = System.getProperty("line.separator");
        
        // Variables que almacenaran informacion de la tripleta
        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        column3 = new ArrayList<>();
        column4 = new ArrayList<>();
        posDo = new ArrayList<>();
        posWhile = new ArrayList<>();
        
        // Variable que se encarga de identificar si hay errores para desplegar.
        errores = false;
        
        // Variable que almacena la posición.
        int pos = 0;
        
        // Antes de iniciar, se deben eliminar los tabs del código para evitar errores adicionales.
        codigo = codigo.replace("\t", "");
        
        // Se dívide el código por lineas.
        String[] lineaCodigo = codigo.split("\\n");
        
        // En caso de que el código se encuentre en una sola línea, se divide con el delimitador.
        if (lineaCodigo.length == 1) {
            lineaCodigo = codigo.split(";");
            
            // Ahora se debe volver a agregar el delimitador
            for (int i = 0; i < lineaCodigo.length; i++) {
                stb = new StringBuilder();
                stb.append(lineaCodigo[i]).append(";");
                
                // Se elimina el espacio en blanco al principio.
                if (i > 0) {
                    stb.deleteCharAt(0);
                }
                lineaCodigo[i] = stb.toString();
            }
            
        }
        
        // La verificación línea por línea empieza en esta parte
        for (int i = 0; i < lineaCodigo.length; i++) {
            
            // Variable que almacena el número de línea.
            int numLinea = i + 1;
            
            // Ahora la verificación por línea se checa por palabras divididas por espacios.
            String[] palabra = lineaCodigo[i].split("\\s");;
            
            // Análisis sin espacios
            if (lineaCodigo[i].contains("<=") || lineaCodigo[i].contains(">=")) {
                palabra = lineaCodigo[i].split("((?<=((int|float|double|do$|while)|(([<>][=])|([!=][=]))|([;)(}{])))|(?=((int|float|double|do|while)|(([<>][=])|([!=][=]))|([;)(}{]))))");
            }
            else if (lineaCodigo[i].contains("<") || lineaCodigo[i].contains(">") || lineaCodigo[i].contains("==")) {
                palabra = lineaCodigo[i].split("((?<=((int|float|double|do$|while)|(([<>])|([!=][=]))|([;)(}{])))|(?=((int|float|double|do|while)|(([<>])|([!=][=]))|([;)(}{]))))");
            }
            else if (lineaCodigo[i].contains("+") || lineaCodigo[i].contains("-") || lineaCodigo[i].contains("*") || lineaCodigo[i].contains("/") || lineaCodigo[i].contains("^")) {
                palabra = lineaCodigo[i].split("((?<=((int|float|double|do$|while)|([;)(}{=])|([+-/*])))|(?=((int|float|double|do|while)|([;)(}{=])|([+-/*]))))");
            }
            else {
                palabra = lineaCodigo[i].split("((?<=((int|float|double|do$|while)|([;)(}{=])))|(?=((int|float|double|do|while)|([;)(}{=]))))");
            }
            
            
            // Variable encargada de almacenar la variable base.
            String var = "";
            
            // Variable encargada de guardar el tipo de variable de la línea.
            String tipo = "null";
            
            // Variable encargada de guardar el valor de la variable.
            String valor = "null";
            
            // Bandera para saber si el tipo base ha sido asignado anteriormente.
            Boolean tipoBase = false;
            
            // Bandera para saber si la variable base ha sido asignada anteriormente.
            Boolean varBase = false;
            
            // Bandera para saber que parte del código define el valor de la variable.
            Boolean asignacion = false;
            
            // Bandera para saber si un error fue encontrado.
            Boolean error = false;
            
            for (int j = 0; j < palabra.length; j++) {
                
                // Si la palabra contiene espacios se eliminan.
                palabra[j] = palabra[j].replace(" ", "");
                
                // Si la palabra esta vacía, no tiene caso analizarla.
                if (palabra[j].isEmpty()) {
                    continue;
                }
                
                // Bandera para saber si el token es nuevo.
                Boolean nuevo = true;
                
                // El tipo de variable base sólo es leída y almacenada con la primera palabra.
                if (j == 0) {
                    
                    // Se declara la expresión regular para encontrar palabras reservadas que definen el tipo.
                    Pattern patron = Pattern.compile("int$|float$|double$");
                    Matcher coincidencia = patron.matcher(palabra[j]);
                    
                    // Sí coincide, el tipo es almacenado y se continua con la siguiente variable.
                    if (coincidencia.matches()) {
                        tipo = palabra[j];
                        tipoBase = true;
                        
                        // Se agrega el token.
                        if (numPR > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numPR++;
                                tokens.add("PR" +numPR);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numPR++;
                            tokens.add("PR" +numPR);
                            valorTokens.add(palabra[j]);
                        }
                        
                        pos++;
                        continue;
                    }
                }
                
                // Se verifica si se trata de las palabras reservadas do o while.
                Pattern patron = Pattern.compile("do$|while$");
                Matcher coincidencia = patron.matcher(palabra[j]);
                
                // Si coinicide, se verifica la siguiente palabra.
                if (coincidencia.matches()) {
                    
                    // Se agrega el token.
                        if (numPR > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numPR++;
                                tokens.add("PR" +numPR);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numPR++;
                            tokens.add("PR" +numPR);
                            valorTokens.add(palabra[j]);
                        }
                    
                        pos++;
                    continue;
                }
                
                // Se verifica si es el operador de asignación.
                patron = Pattern.compile("^[=]$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena el valor de la variable.
                if (coincidencia.matches()) {
                    asignacion = true;
                    
                    // Se inicia StringBuilder para empezar almacenar el valor de la variable.
                    stb = new StringBuilder();
                    
                    // Se registra el token de asignación.
                    tokens.add("OAS");
                    valorTokens.add("=");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es el delimitador.
                patron = Pattern.compile("^[;]$");
                coincidencia = patron.matcher(palabra[j]);
                
                if (coincidencia.matches()) {
                    
                    // Se registra el token de delimitador.
                    tokens.add("DEL");
                    valorTokens.add(";");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es un parentesis.
                patron = Pattern.compile("[(]$");
                coincidencia = patron.matcher(palabra[j]);
                
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, el parentesis se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se registra el token de parentesis.
                    tokens.add("PAR1");
                    valorTokens.add("(");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es un parentesis.
                patron = Pattern.compile("[)]$");
                coincidencia = patron.matcher(palabra[j]);
                
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, el parentesis se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se registra el token de parentesis.
                    tokens.add("PAR2");
                    valorTokens.add(")");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es un corchete.
                patron = Pattern.compile("[{]$");
                coincidencia = patron.matcher(palabra[j]);
                
                if (coincidencia.matches()) {
                    
                    // Se registra el token de delimitador.
                    tokens.add("COR1");
                    valorTokens.add("{");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es un corchete.
                patron = Pattern.compile("[}]$");
                coincidencia = patron.matcher(palabra[j]);
                
                if (coincidencia.matches()) {
                    
                    // Se registra el token de delimitador.
                    tokens.add("COR2");
                    valorTokens.add("}");
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si se trata de una constante entera.
                patron = Pattern.compile("^[-]?(\\d)+$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena la constante entera.
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, la constante se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se agrega el token.
                        if (numCE > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numCE++;
                                tokens.add("CE" +numCE);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numCE++;
                            tokens.add("CE" +numCE);
                            valorTokens.add(palabra[j]);
                        }
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si se trata de una constante flotante.
                patron = Pattern.compile("^[-]?(\\d)+(.(\\d)+)$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena la constante flotante.
                if (coincidencia.matches()) {
                    
                    // Se verifica que si existe incompatibilidad de tipos.
                    if (tipo.equalsIgnoreCase("int")) {
                        txtError.add("ERROR: En la línea " +numLinea+ " hay incompatibilidad de tipos: " +var+ " es un int y " +palabra[j]+ " es un float.");
                        
                        error = true;
                        errores = true;
                    }
                    
                    // En caso contrario, almacenar la constante flotante.
                    else if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se agrega el token.
                        if (numCF > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numCF++;
                                tokens.add("CF" +numCF);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numCF++;
                            tokens.add("CF" +numCF);
                            valorTokens.add(palabra[j]);
                        }
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si se trata de un operador aritmético.
                patron = Pattern.compile("^[+-/*]$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena al operador aritmético.
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, el operador aritmético se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Si se intenta dividir con una variable base int.
                    if (tipo.equalsIgnoreCase("int") && palabra[j].contains("/")) {
                        txtError.add("ERROR: En la línea " +numLinea+ " puede haber pérdida de información al dividir un int.");

                        error = true;
                        errores = true;
                    }
                    
                    // Se agrega el token.
                        if (numOA > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numOA++;
                                tokens.add("OA" +numOA);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numOA++;
                            tokens.add("OA" +numOA);
                            valorTokens.add(palabra[j]);
                        }
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si se trata de un operador relacional.
                patron = Pattern.compile("([<>][=]?)$|([!=][=])$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena al operador relacional.
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, el operador relacional se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se agrega el token.
                        if (numOR > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numOR++;
                                tokens.add("OR" +numOR);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numOR++;
                            tokens.add("OR" +numOR);
                            valorTokens.add(palabra[j]);
                        }
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si se trata de un operador booleano.
                patron = Pattern.compile("([&][&])$|([|][|])$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Se activa la bandera que almacena al operador booleano
                if (coincidencia.matches()) {
                    
                    // Al tratarse de una asignación, el operador booleano se almacena.
                    if (asignacion) {
                        stb.append(palabra[j]).append(" ");
                    }
                    
                    // Se agrega el token.
                        if (numOB > 0) {
                            // Primero debemos verificar que el token no exista.
                            // t guarda la posición del token a analizar.
                            int t = 0;
                            // tokensReg guarda el total de tokens registrados hasta el momento.
                            int tokensReg = tokens.size();
                            while (t < tokensReg) {
                                
                                // Si el token existe.
                                if (valorTokens.get(t).equals(palabra[j])) {
                                    // Se guarda el token existente.
                                    tokens.add(tokens.get(t));
                                    valorTokens.add(valorTokens.get(t));
                                    nuevo = false;
                                    break;
                                }
                                
                                t++;
                                
                            }
                            
                            if (nuevo) {
                                // Se guarda el nuevo token.
                                numOB++;
                                tokens.add("OB" +numOB);
                                valorTokens.add(palabra[j]);
                            }
                        }
                        // Si no hay ningún token, se registra el primero
                        else {
                            numOB++;
                            tokens.add("OB" +numOB);
                            valorTokens.add(palabra[j]);
                        }
                    
                    pos++;
                    continue;
                }
                
                // Se verifica si es una variable.
                patron = Pattern.compile("^[A-Za-z_$][\\w$]*$");
                coincidencia = patron.matcher(palabra[j]);
                
                // Sí coincide se asigna el tipo base si no ha sido declarado antes.
                if (coincidencia.matches()) {
                    
                    // Variable donde se almacena el tipo de la variable que se analiza.
                    String tipoPalabra = "null";
                    
                    // Se verifica si esta es la variable base de la línea, de ser así, se almacena.
                    if (tipoBase && !varBase) {
                        var = palabra[j];
                        tipoPalabra = tipo;
                        varBase = true;
                    }
                    
                    // Bandera indicadora de si se pasa a la siguiente variable.
                    Boolean salto = false;
                    
                    // Variable donde se almacena el valor de la variable que se analiza.
                    String valorPalabra = valor;
                    
                    // Se busca si la variable no ha sido registrada antes
                    for (int k = 0; k < variable.size(); k++) {
                        
                        // Si ha sido registrada antes.
                        if (variable.get(k).equals(palabra[j])) {
                            
                            // Se obtiene y almacena el tipo de la variable.
                            tipoPalabra = tipoVar.get(k);
                            
                            // Se analiza si la variable ha sido declarada previamente.
                            if (!tipo.equalsIgnoreCase("null") && !asignacion) {
                                txtError.add("ERROR: En la línea " +numLinea+ " la variable " + palabra[j] + " ha sido declarada previamente.");

                                error = true;
                                errores = true;
                            }
                            
                            // Se obtiene y almacena el valor de la variable.
                            valorPalabra = valorVar.get(k);
                            
                            // Se indica que la palabra se ha encontrado.
                            salto = true;
                            break;
                        }
                        
                    }
                    
                    // Se verifica de nuevo si esta es la variable de la base de línea pero sin declaración.
                    if (j == 0) {
                        var = palabra[j];
                        tipo = tipoPalabra;
                        varBase = true;
                    }
                    
                    // Se analiza si la variable no ha sido declarada.
                    if (tipoPalabra.equalsIgnoreCase("null")) {
                        txtError.add("ERROR: En la línea " +numLinea+ " la variable " + palabra[j] + " no está declarada.");
                        
                        error = true;
                        errores = true;
                    }
                    
                    // Se analiza si se esta intentando asignar una variable incompatible o no inicializada.
                    if (asignacion) {
                        
                        // Se analiza si el tipo base no es compatible con el tipo que se analiza.
                        if (tipo.equalsIgnoreCase("int") && (tipoPalabra.equalsIgnoreCase("double") || tipoPalabra.equalsIgnoreCase("float"))) {
                            txtError.add("ERROR: En la línea " +numLinea+ " hay incompatibilidad de tipos: " +var+ " es un int y " +palabra[j]+ " es " +tipoPalabra+ ".");

                            error = true;
                            errores = true;
                        }
                        
                        // Se analiza si la variable no se encuentra inicializada.
                        if (valorPalabra.equalsIgnoreCase("null")) {
                            txtError.add("ERROR: En la línea " +numLinea+ "la variable " + palabra[j] + " no está inicializada.");

                            error = true;
                            errores = true;
                        }
                        
                        // Si ningún error fue encontrado, se almacena en stb para después asignar el valor a la variable.
                        if (!error) {
                            stb.append(palabra[j]).append(" ");
                        }
                    }
                    
                    // Se agrega el token.
                    if (numIDE > 0) {
                        // Primero debemos verificar que el token no exista.
                        // t guarda la posición del token a analizar.
                        int t = 0;
                        // tokensReg guarda el total de tokens registrados hasta el momento.
                        int tokensReg = tokens.size();
                        while (t < tokensReg) {
                            
                            // Si el token existe.
                            if (valorTokens.get(t).equals(palabra[j])) {
                                // Se guarda el token existente.
                                tokens.add(tokens.get(t));
                                valorTokens.add(valorTokens.get(t));
                                nuevo = false;
                            
                                break;
                            }
                                
                            t++;
                                
                        }
                            
                        if (nuevo) {
                            // Se guarda el nuevo token.
                            numIDE++;
                            tokens.add("IDE" +numIDE);
                            valorTokens.add(palabra[j]);
                        }
                    }
                    // Si no hay ningún token, se registra el primero
                    else {
                        numIDE++;
                        tokens.add("IDE" +numIDE);
                        valorTokens.add(palabra[j]);
                    }
                    
                    // Se verifica la siguiente palabra si no es necesario agregar.
                    if (salto) {
                        pos++;
                        continue;
                    }
                    
                    // La variable se registra
                    variable.add(palabra[j]);
                    tipoVar.add(tipoPalabra);
                    valorVar.add(valorPalabra);
                    
                    pos++;
                    
                }
                
            }
            
            // Si la línea tuvo una asignación, esta se agrega en esta fase.
            if (asignacion) {
                
                // Si la línea no tuvo ningún error, se procede a la asignación.
                if (!error) {
                    
                    // Primero debemos encontrar la posición de la variable a asignar.
                    for (int j = 0; j < variable.size(); j++) {
                        
                        // Al encontrar la variable, se le asigna su nuevo valor.
                        if (variable.get(j).equalsIgnoreCase(var)) {
                            valorVar.set(j, stb.toString());
                            break;
                        }
                    }
                }
            }
        }
        
        // Ahora el código se optimiza
        optimizarCodigo();
        
        // Se genera la tripleta
        generarTriplo();
        
        // Se genera el ensamblador
        generarEnsamblador();
        
        return errores;
    }
    
    // Método para obtener las variables.
    public static String[] obtenerVariables() {
        
        // Número de variables registradas.
        int tamanio = variable.size();
        
        // Arreglo que almacena todas las variables.
        String[] variables = new String[tamanio];
        
        // Se recorre el ArrayList de variables.
        for (int i = 0; i < tamanio; i++) {
            variables[i] = variable.get(i);
        }
        
        // Se regresan todas las variables.
        return variables;
    }
    
    // Método para obtener el número de variables.
    public static int obtenerNumVariables() {
        return variable.size();
    }
    
    // Método para obtener el número de errores.
    public static int obtenerNumErrores() {
        return txtError.size();
    }
    
    // Método para obtener el número de tokens.
    public static int obtenerNumTokens() {
        return tokens.size();
    }
    
    // Método para obtener los tipos de variables.
    public static String[] obtenerTipoVar() {
        
        // Número de variables registradas.
        int tamanio = tipoVar.size();
        
        // Arreglo que almacena todas las variables.
        String[] tipos = new String[tamanio];
        
        // Se recorre el ArrayList de variables.
        for (int i = 0; i < tamanio; i++) {
            tipos[i] = tipoVar.get(i);
        }
        
        // Se regresan todas las variables.
        return tipos;
    }
    
    // Método para obtener los valores de las variables.
    public static String[] obtenerValoresVar() {
        
        // Número de variables registradas.
        int tamanio = valorVar.size();
        
        // Arreglo que almacena todas las variables.
        String[] valores = new String[tamanio];
        
        // Se recorre el ArrayList de variables.
        for (int i = 0; i < tamanio; i++) {
            valores[i] = valorVar.get(i);
        }
        
        // Se regresan todas las variables.
        return valores;
    }
    
    // Método para obtener el texto de los errores.
    public static String[] obtenerTxtError() {
        
        // Número de variables registradas.
        int tamanio = txtError.size();
        
        // Arreglo que almacena todas las variables.
        String[] erroresArray = new String[tamanio];
        
        // Se recorre el ArrayList de variables.
        for (int i = 0; i < tamanio; i++) {
            erroresArray[i] = txtError.get(i);
        }
        
        // Se regresan todas las variables.
        return erroresArray;
    }
    
    // Método para obtener los tokens.
    public static String[] obtenerTokens() {
        
        // Número de tokens registrados.
        int tamanio = tokens.size();
        
        // Arreglo que almacena todos los tokens.
        String[] tokensArray = new String[tamanio];
        
        // Se recorre el ArrayList de tokens.
        for (int i = 0; i < tamanio; i++) {
            tokensArray[i] = tokens.get(i);
        }
        
        // Se regresan todas las tokens.
        return tokensArray;
    }
    
    // Método para obtener los valores de los tokens.
    public static String[] obtenerValoresTokens() {
        
        // Número de valores de tokens registrados.
        int tamanio = valorTokens.size();
        
        // Arreglo que almacena todas las variables.
        String[] valorTokensArray = new String[tamanio];
        
        // Se recorre el ArrayList de variables.
        for (int i = 0; i < tamanio; i++) {
            valorTokensArray[i] = valorTokens.get(i);
        }
        
        // Se regresan todas las variables.
        return valorTokensArray;
    }
    
    // Método para guardar el archivo de texto.
    public static void guardarArchivo(String archivo, String texto) {
        
        try (FileWriter fw = new FileWriter(archivo)) {
            fw.write(texto);
        }
        catch (IOException ex) {
            System.out.println("No se pudo guardar el archivo.");
        }
    }
    
    // Metodo encargado de la optimización del código.
    public static void optimizarCodigo() {
        
        // Objeto para la construcción de strings.
        StringBuilder stb = new StringBuilder();
        
        // Listas que almacenaran los tokens optimizados.
        ArrayList<String> tokensOpt = new ArrayList<>();
        ArrayList<String> valorTokensOpt = new ArrayList<>();
        
        // Se recorren los tokens
        int tamanioTokens = tokens.size();
        
        for (int i = 0; i < tamanioTokens; i++) {
            String actualToken = tokens.get(i);
            String actualValor = valorTokens.get(i);
            String preToken = "";
            String preValor = "";
            String posToken = "";
            Boolean ultimo = true;
            
            // Se guarda el token anterior necesario para algunas comprobaciones.
            if (i > 0) {
                preToken = tokens.get(i-1);
                preValor = valorTokens.get(i-1);
            }
            
            // Se guarda el token posterior necesario para algunas comprobaciones.
            if (i < tamanioTokens-1) {
                posToken = tokens.get(i+1);
                ultimo = false;
            }
            
            // Si hay un delimitador.
            if (actualToken.equalsIgnoreCase("DEL")) {
                
                // Si no es el último token.
                if (!ultimo) {
                    
                    // Verificar si el siguiente token es un corchete.
                    if (posToken.equalsIgnoreCase("COR2")) {
                        
                        // Se agrega el delimitador tal cual.
                        stb.append(actualValor);
                        tokensOpt.add(actualToken);
                        valorTokensOpt.add(actualValor);
                    }
                    
                    // Si no es un corchete
                    else {
                        
                        // Se agrega el delimitador junto a un salto de línea.
                        stb.append(actualValor).append(linea);
                        tokensOpt.add(actualToken);
                        valorTokensOpt.add(actualValor);
                    }
                }
                
                // Si es el último token.
                else {
                    
                    // Se agrega tal cual el delimitador.
                    stb.append(actualValor);
                    tokensOpt.add(actualToken);
                    valorTokensOpt.add(actualValor);
                }
            }
            
            // Si hay una palabra reservada
            else if (actualToken.contains("PR")) {
                
                // Si el anterior token es un parentesis.
                if (preToken.equalsIgnoreCase("PAR2")) {
                    
                    // Se realiza un salto de línea
                    stb.append(linea);
                }
                
                // Se escribe la palabra del token y un espacio.
                stb.append(actualValor).append(" ");
                tokensOpt.add(actualToken);
                valorTokensOpt.add(actualValor);
                
            }
            
            // Si hay un corchete inicial
            else if (actualToken.equalsIgnoreCase("COR1")) {
                
                // Se escribe un salto de línea y se escribe el corchete inicial.
                stb.append(linea);
                stb.append(actualValor);
                tokensOpt.add(actualToken);
                valorTokensOpt.add(actualValor);
            }
            
            // Si hay un corchete final
            else if (actualToken.equalsIgnoreCase("COR2")) {
                
                // Se escribe el corchete final y un salto de línea.
                stb.append(actualValor);
                tokensOpt.add(actualToken);
                valorTokensOpt.add(actualValor);
                stb.append(linea);
            }
            
            // Instrucción 3: Si hay una asignación.
            else if (actualToken.equalsIgnoreCase("OAS")) {
                
                // Se debe agregar el operador.
                stb.append(actualValor);
                tokensOpt.add(actualToken);
                valorTokensOpt.add(actualValor);
                
                // Bandera indicadora de que se ha encontrado el reemplazo
                Boolean reemplazo = false;
                
                // Se almacenará el codigo que contiene el IDE anterior en esta variable.
                String preCodigo = "";
                
                // Se ignora la optimizacion si se trata de una asignacion simple.
                if (tokens.get(i+2).equalsIgnoreCase("DEL")) {
                    continue;
                }
                
                // Se analizan todas las variables registradas.
                for (int j = 0; j < variable.size(); j++) {
                    
                    // Si la variable contiene de valor 1, se ignora.
                    if (valorVar.get(j).equalsIgnoreCase("1 ") || valorVar.get(j).equalsIgnoreCase("null")) {
                        continue;
                    }
                    
                    // Si se encuentra la variable con el token anterior
                    if (preValor.equalsIgnoreCase(variable.get(j))) {
                        
                        // Se escribe el codigo que contiene.
                        preCodigo = valorVar.get(j);
                        
                        // Se rompe el ciclo.
                        break;
                    }
                    
                }
                
                // Ahora se debe analizar en todos los IDE su valor para la sustitución.
                for (int j = 0; j < variable.size(); j++) {
                    
                    // Se obtiene el valor de las variables y se compara si contienen alguna parte del codigo.
                    // De ser así (y que no sea la misma variable).
                    if (preCodigo.contains(valorVar.get(j)) && !preValor.equalsIgnoreCase(variable.get(j)) && !valorVar.get(j).equalsIgnoreCase("1 ") && !valorVar.get(j).equalsIgnoreCase("null")) {
                        
                        // Se guarda el caracter anterior
                        int charAnt = preCodigo.indexOf(valorVar.get(j)) - 1;
                        
                        // Pero si la posición es -1, se evita una posible excepcion.
                        if (charAnt == -1) {
                            charAnt = 0;
                        }
                        
                        // Si antes de la parte a reemplazar no hay un espacio, se ignora.
                        if (preCodigo.charAt(charAnt) != ' ' && charAnt != 0) {
                            continue;
                        }
                        
                        // Se sustituye la parte del código por la variable.
                        preCodigo = preCodigo.replace(valorVar.get(j), variable.get(j)+ " ");
                        
                        // Bandera para indicar que si se realizo una sustitucion.
                        reemplazo = true;
                    }
                }
                
                if (reemplazo) {
                    
                    // Se guardan los tokens correspondientes al codigo.
                    String[] preCodigoTokens = preCodigo.split("\\s");
                    
                    // Se verifica lo capturado.
                    for (int j = 0; j < preCodigoTokens.length ; j++) {
                        
                        // Si la palabra del código actual es 1 y a continuación hay una multiplicación, el 1 se elimina.
                        if (preCodigoTokens[j].equalsIgnoreCase("1") && (preCodigoTokens[j+1].equalsIgnoreCase("(") || preCodigoTokens[j+1].equalsIgnoreCase("*"))) {
                            preCodigoTokens[j] = "";
                                
                            // Si había un asterisco, se elimina de igual forma.
                            if (preCodigoTokens[j+1].equalsIgnoreCase("*")) {
                                preCodigoTokens[j] = "";
                                j++;
                            }
                                
                            // Se termina el ciclo
                            continue;
                        }
                        
                        // Se lee el arreglo de tokens
                        for (int k = 0 ; k < tokens.size() ; k ++) {
                            
                            // Si el codigo concuerda con el valor del token.
                            if (preCodigoTokens[j].equalsIgnoreCase(valorTokens.get(k))) {
                                
                                // Se almacena en la nueva lista de tokens.
                                tokensOpt.add(tokens.get(k));
                                valorTokensOpt.add(valorTokens.get(k));
                                
                                // Se termina el ciclo
                                break;
                            }
                        }
                    }
                    
                    // Se eliminan los espacios.
                    preCodigo = arregloATexto(preCodigoTokens);
                    stb.append(preCodigo);
                        
                    // Se salta todo el análisis de la línea (ya esta optimizado).
                    while (!tokens.get(i+1).equalsIgnoreCase("DEL")) {
                        i++;
                    }
                    
                }
                
                
            }
            
            // Con cualquier otro token, se escribe tal cual.
            else {
                stb.append(actualValor);
                tokensOpt.add(actualToken);
                valorTokensOpt.add(actualValor);
            }
            
            
        }
        
        // Se almacenan las listas con los nuevos tokens
        tokens = tokensOpt;
        valorTokens = valorTokensOpt;
        
        // Se guarda el código optimizado
        guardarArchivo("optimizado.txt", stb.toString());
    }
    
    public static void generarTriplo () {
        
        // Indica el numero de instruccion.
        int numIns = 1;
        
        // Numero de tripleta
        int numTrip = 1;
        
        // Numero de condicion
        int numCon = 1;
        
        // Pila que contiene la posición de llamada del do.
        Stack<Integer> insDo = new Stack<>();
        
        // Empieza la diversión, se verifica token por token.
        for (int i = 0; i < tokens.size(); i++) {
            
            // Si el token es una palabra reservada, se revisa de cual se trata.
            if(tokens.get(i).contains("PR")) {
                
                // Si hay un do, se guarda el número de instrucción.
                if (valorTokens.get(i).equalsIgnoreCase("do")) {
                    posDo.add(numIns);
                    insDo.push(numIns);
                }
                
                // Si hay un while
                else if (valorTokens.get(i).equalsIgnoreCase("while")) {
                    
                    // Se guarda el inicio del while
                    posWhile.add(numIns);
                    
                    // Se guarda en la tripleta la variable a comprobar
                    column1.add(numIns);
                    column2.add("T" +numTrip);
                    column3.add(valorTokens.get(i+2));
                    column4.add("=");
                    numIns++;
                    
                    // Se analiza con que se debe comparar
                    column1.add(numIns);
                    column2.add("T" +numTrip);
                    column3.add(valorTokens.get(i+4));
                    column4.add(valorTokens.get(i+3));
                    numIns++;
                    
                    // Si es verdadero, debe continuar con la siguiente instruccion.
                    int sigIns = numIns + 2;
                    column1.add(numIns);
                    column2.add("TR" +numCon);
                    column3.add("TRUE");
                    column4.add(insDo.pop()+"");
                    numIns++;
                    
                    // Si es falso, se debe regresar al anterior do
                    column1.add(numIns);
                    column2.add("TR" +numCon);
                    column3.add("FALSE");
                    column4.add(sigIns+"");
                    numIns++;
                    numTrip++;
                    numCon++;
                    
                }
                
                else {
                    continue;
                }
                
            }
            
            // Si el token es un operador de asignación.
            if(tokens.get(i).contains("OAS")) {
                // Se considera si hay un delimitador despúes de su siguiente token
                if (tokens.get(i+2).equalsIgnoreCase("DEL")) {
                    
                    // De ser así estamos ante una asignación simple y la tripleta.
                    column1.add(numIns);
                    column2.add(valorTokens.get(i-1));
                    column3.add(valorTokens.get(i+1));
                    column4.add("=");
                    numIns++;
                }
                
                // En este caso, tenemos una probable operación matemática.
                else {
                    
                    // Variable a la que se asignará el resultado final.
                    String variableAsig = valorTokens.get(i-1);
                    
                    // Se guarda en una lista nueva la operacion.
                    ArrayList<String> valoresTokensOp = new ArrayList<>();
                    
                    // Saltamos el guardado del operador de asignación.
                    i++;
                    
                    while (!tokens.get(i).equalsIgnoreCase("DEL")) {
                        valoresTokensOp.add(valorTokens.get(i));
                        i++;
                    }
                    
                    // Se va recorriendo la lista hasta que no queden variables por comparar
                    while (valoresTokensOp.size() > 1) {
                        
                        // Si hay un parentesis en la operación.
                        if (valoresTokensOp.indexOf(")") != -1) {
                            
                            // Se almacenan las posiciones.
                            int posFin = valoresTokensOp.indexOf(")");
                            int posIni = posFin - 2;
                            
                            // Se revisa que antes dos tokens anteriores no sean "(".
                            if (!valoresTokensOp.get(posIni).equalsIgnoreCase("(")) {
                                
                                // Si hay una multiplicación en la operación.
                                if (valoresTokensOp.lastIndexOf("*") != -1) {

                                    // Se almacena la última variable en el triplo y se elimina de la lista.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("*")+1));
                                    column4.add("=");
                                    numIns++;

                                    // En el triplo se almacena la operacion realizada.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("*")-1));
                                    column4.add(valoresTokensOp.get(valoresTokensOp.lastIndexOf("*")));
                                    numIns++;

                                    // Se agrega a la lista el triplo realizado.
                                    valoresTokensOp.set(valoresTokensOp.lastIndexOf("*"),"T" +numTrip);

                                    // Se aumenta el contador del triplo
                                    numTrip++;

                                    continue;
                                }

                                // Si hay una division en la operación.
                                else if (valoresTokensOp.lastIndexOf("/") != -1) {

                                    // Se almacena la última variable en el triplo y se elimina de la lista.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("/")+1));
                                    column4.add("=");
                                    numIns++;

                                    // En el triplo se almacena la operacion realizada.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("/")-1));
                                    column4.add(valoresTokensOp.get(valoresTokensOp.lastIndexOf("/")));
                                    numIns++;

                                    // Se agrega a la lista el triplo realizado.
                                    valoresTokensOp.set(valoresTokensOp.lastIndexOf("/"),"T" +numTrip);

                                    // Se aumenta el contador del triplo
                                    numTrip++;

                                    continue;
                                }

                                // Si solo quedan sumas y restas.
                                else {

                                    // Se almacena la última variable en el triplo y se elimina de la lista.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                                    column4.add("=");
                                    numIns++;

                                    // En el triplo se almacena la operacion realizada.
                                    column1.add(numIns);
                                    column2.add("T" +numTrip);
                                    column4.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                                    column3.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                                    numIns++;

                                    // Se agrega a la lista el triplo realizado.
                                    valoresTokensOp.add("T" +numTrip);

                                    // Se aumenta el contador del triplo
                                    numTrip++;
                                }
                                
                            }
                            
                            // En este caso, la operación no tiene mas operaciones alrededor
                            else {
                                
                                // Primero debemos comprobar que no estemos en el final del lado derecho.
                                if (posFin < valoresTokensOp.size()-1) {
                                    
                                    // Se verifica que haya una suma, resta o parentesis final del lado derecho
                                    if (valoresTokensOp.get(posFin+1).equalsIgnoreCase("+") || valoresTokensOp.get(posFin+1).equalsIgnoreCase("-") || valoresTokensOp.get(posFin+1).equalsIgnoreCase(")")) {
                                        
                                        // De ser así, se elimina el parentesis
                                        valoresTokensOp.remove(posFin);
                                    }
                                    
                                    // En cualquier otro caso, se cambia el parentesis por una multiplicación
                                    else {
                                        valoresTokensOp.set(posFin, "*");
                                    }
                                        
                                }
                                
                                // Al ser el final, solamente se elimina el parentesis.
                                else {
                                    valoresTokensOp.remove(posFin);
                                }
                                
                                // Ahora debemos comprobar que no estemos al principio del lado izquierdo.
                                if (posIni > 0) {
                                    // Se verifica que haya una suma, resta o parentesis del lado izquierdo.
                                    if (valoresTokensOp.get(posIni-1).equalsIgnoreCase("+") || valoresTokensOp.get(posIni-1).equalsIgnoreCase("-") || valoresTokensOp.get(posIni-1).equalsIgnoreCase(")") || valoresTokensOp.get(posIni-1).equalsIgnoreCase("(")) {
                                        
                                        // De ser así, se elimina el parentesis
                                        valoresTokensOp.remove(posIni);
                                    }
                                    
                                    // En cualquier otro caso, se cambia el parentesis por una multiplicación
                                    else {
                                        valoresTokensOp.set(posIni, "*");
                                    }
                                    
                                }
                                
                                // Al ser el principio, solamente se elimina el parentesis.
                                else {
                                    valoresTokensOp.remove(posIni);
                                }
                                
                            }
                            
                            continue;
                        }
                        
                        // Si hay una multiplicación en la operación.
                        else if (valoresTokensOp.lastIndexOf("*") != -1) {
                            
                            // Se almacena la última variable en el triplo y se elimina de la lista.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("*")+1));
                            column4.add("=");
                            numIns++;

                            // En el triplo se almacena la operacion realizada.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("*")-1));
                            column4.add(valoresTokensOp.get(valoresTokensOp.lastIndexOf("*")));
                            numIns++;

                            // Se agrega a la lista el triplo realizado.
                            valoresTokensOp.set(valoresTokensOp.lastIndexOf("*"),"T" +numTrip);
                            
                            // Se aumenta el contador del triplo
                            numTrip++;
                            
                            continue;
                        }
                        
                        // Si hay una division en la operación.
                        else if (valoresTokensOp.lastIndexOf("/") != -1) {
                            
                            // Se almacena la última variable en el triplo y se elimina de la lista.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("/")+1));
                            column4.add("=");
                            numIns++;

                            // En el triplo se almacena la operacion realizada.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column3.add(valoresTokensOp.remove(valoresTokensOp.lastIndexOf("/")-1));
                            column4.add(valoresTokensOp.get(valoresTokensOp.lastIndexOf("/")));
                            numIns++;

                            // Se agrega a la lista el triplo realizado.
                            valoresTokensOp.set(valoresTokensOp.lastIndexOf("/"),"T" +numTrip);
                            
                            // Se aumenta el contador del triplo
                            numTrip++;
                            
                            continue;
                        }
                        
                        // Si solo quedan sumas y restas.
                        else {
                            
                            // Se almacena la última variable en el triplo y se elimina de la lista.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column3.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                            column4.add("=");
                            numIns++;

                            // En el triplo se almacena la operacion realizada.
                            column1.add(numIns);
                            column2.add("T" +numTrip);
                            column4.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                            column3.add(valoresTokensOp.remove(valoresTokensOp.size()-1));
                            numIns++;

                            // Se agrega a la lista el triplo realizado.
                            valoresTokensOp.add("T" +numTrip);
                            
                            // Se aumenta el contador del triplo
                            numTrip++;
                        }
                    }
                    
                    // El resultado final es almacenado en la variable
                    column1.add(numIns);
                    column2.add(variableAsig);
                    column3.add(valoresTokensOp.get(0));
                    column4.add("=");
                    numIns++;
                    
                }
            }
        }
        
        // Ahora se genera el texto de la tripleta.
        StringBuilder texto = new StringBuilder();
        
        for (int i = 0; i < column1.size(); i++) {
            texto.append(column1.get(i)).append("\t")
                    .append(column2.get(i)).append("\t")
                    .append(column3.get(i)).append("\t")
                    .append(column4.get(i)).append(linea);
        }
        
        // Se guarda el archivo.
        guardarArchivo("tripleta.txt", texto.toString());
    }
    
    public static void generarEnsamblador() {
        
        // Se crean variables referentes a los registros.
        String AH = "";
        String AL = "";
        String BH = "";
        String BL = "";
        String CL = "";
        
        // Variables con el numero de ciclo y condicion.
        int numCiclo = 1;
        int numCon = 1;
        
        // Se guarda la posición del primer do y while.
        int insDo = 0;
        int insWhile = 0;
        try {
            insDo = posDo.remove(0) - 1;
            insWhile = posWhile.remove(0) - 1;
        }
        catch (IndexOutOfBoundsException ex) {
            // No hay ciclos.
            insDo = -1;
            insWhile = -1;
        }
        
        // Pila que almacena siempre el ultimo ciclo.
        Stack<Integer> ultCiclo = new Stack<>();
        
        // En dado caso de que la pila se vacíe antes, se usa este String.
        String ultCiclo2 = "";
        
        // Variable que contendrá el código ensamblador
        StringBuilder texto = new StringBuilder();
        
        // Lo divertido del ensamblador comienza en esta parte.
        for (int i = 0; i < column1.size(); i++) {
            
            // Se verifica si estamos ante una instruccion do.
            if (i == insDo) {
                
                ultCiclo2 = "Ciclo" + numCiclo;
                texto.append(ultCiclo2).append(":").append(linea);
                ultCiclo.push(numCiclo);
                numCiclo++;
                
                // Se elimina la posicion de este do.
                try {
                    while (insDo == posDo.get(0) - 1) {
                        insDo = posDo.remove(0) - 1;
                    }
                    insDo = posDo.remove(0) - 1;
                }
                catch (IndexOutOfBoundsException ex) {
                    // No hay más ciclos.
                    insDo = -1;
                }
            }
            
            // Si estamos ante una instruccion while.
            if (i == insWhile) {
                
                texto.append("Condicion").append(numCon).append(":").append(linea);
                numCon++;
                
                /*
                // Se limpia lo que habia en AL.
                if (!AL.isEmpty()) {
                    texto.append("\tMOV AL, 0;").append(linea);
                    AL = "";
                }
                */
                
                // Se pasa la variable a comparar a AL.
                texto.append("\tMOV AL, ").append(column3.get(i)).append(";").append(linea);
                AL = column3.get(i);
                i++;
                
                // Ahora se compara lo que tiene AL.
                texto.append("\tCMP AL, ").append(column3.get(i)).append(";").append(linea);
                AL = column3.get(i);
                
                // Se procede a leer que comparación se esta realizando.
                // Si se trata de un menor.
                if (column4.get(i).equalsIgnoreCase("<")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJL Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJL ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                    
                }
                
                // Si se trata de un menor o igual que.
                else if (column4.get(i).equalsIgnoreCase("<=")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJLE Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJLE ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                }
                
                // Si se trata de un mayor.
                else if (column4.get(i).equalsIgnoreCase(">")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJG Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJG ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                }
                
                // Si se trata de un mayor o igual que.
                else if (column4.get(i).equalsIgnoreCase(">=")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJGE Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJGE ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                }
                
                // Si se trata de un igual que.
                else if (column4.get(i).equalsIgnoreCase("==")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJE Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJE ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                }
                
                // Si se trata de un diferente de.
                else if (column4.get(i).equalsIgnoreCase("!=")) {
                    try {
                        ultCiclo.lastElement();
                        texto.append("\tJNE Ciclo").append(ultCiclo.pop()).append(";").append(linea);
                    }
                    catch (EmptyStackException | NoSuchElementException ex) {
                        texto.append("\tJNE ").append(ultCiclo2).append(";").append(linea);
                        ultCiclo2 = "";
                    }
                }
                
                // Se saltan las dos lineas que contienen la instrucción while
                i = i + 2;
                
                // Se elimina la posicion de este while.
                try {
                    insWhile = posWhile.remove(0) - 1;
                }
                catch (IndexOutOfBoundsException ex) {
                    // No hay más ciclos.
                    insWhile = -1;
                }
                
                // Si a continuación no nos encontramos con un while o un do,
                // se considera que es continuación del ciclo anterior.
                if (i + 1 != insDo && i + 1 != insWhile && i + 1 < column1.size()) {
                    if (!ultCiclo2.isEmpty()) {
                        texto.append("\tJMP ").append(ultCiclo2).append("pr2").append(";").append(linea)
                                .append(ultCiclo2).append("pr2").append(":").append(linea);
                    }
                    else {
                        texto.append("\tJMP Final;").append(linea)
                                .append("Final:").append(linea);
                    }
                }
            }
            
            // Se verifica que el operador es una asignación.
            else if (!column2.get(i).contains("T") && !column3.get(i).contains("T")) {
                
                texto.append("\tMOV ").append(column2.get(i)).append(", ").append(column3.get(i)).append(";").append(linea);
                
            }
            
            // Si solamente la columna 2 no contiene una variable temporal, estamos ante una asignación.
            else if (!column2.get(i).contains("T")) {
                
                // Se mueve de BL el resultado a la variable correspondiente.
                texto.append("\tMOV ").append(column2.get(i)).append(", BL;").append(linea);
                
                // Se limpia la variable para detectar que lo que almacena BH ya no es importante.
                BL = "";
                
            }
            
            // De no ser ningun caso anterior, estamos ante una operación aritmetica.
            else {
                
                // Si BL se encuentra vacía, se procede a realizar la operación.
                if (BL.isEmpty()) {
                    
                    // Se mueve a BL el primer número.
                    texto.append("\tMOV BL, ").append(column3.get(i)).append(";").append(linea);
                    BL = column3.get(i);
                    
                    // Se mueve al siguiente número.
                    i++;
                    
                    // Si no estamos ante una división, se utiliza el registro BH
                    if (!column4.get(i).equalsIgnoreCase("/")) {
                        texto.append("\tMOV BH, ").append(column3.get(i)).append(";").append(linea);
                        BH = column3.get(i);
                    }
                    
                    // Pero si se trata de una división.
                    else {
                        
                        // Se limpia la parte alta de AX si no se encuentra vacía.
                        if (!AH.isEmpty()) {
                            texto.append("\tMOV AH, 0;").append(linea);
                            AH = "";
                        }
                        
                        // Se pasa a AL el número a dividir.
                        texto.append("\tMOV AL, BL;").append(linea);
                        
                        // Se pasa a CL el divisor.
                        texto.append("\tMOV CL, ").append(column3.get(i)).append(";").append(linea);
                        CL = column3.get(i);
                        
                        // Se realiza la división y se indica los registros con información.
                        texto.append("\tDIV CL;").append(linea);
                        AH = "Residuo";
                        AL = "Resultado";
                        
                        // Se guarda el resultado en BL.
                        texto.append("\tMOV BL, AL;").append(linea);
                        BL = CL;
                    }
                    
                    // Si se trata de una multiplicación.
                    if (column4.get(i).equalsIgnoreCase("*")) {
                        texto.append("\tMUL BL, BH;").append(linea);
                        BL = "BL * BH";
                    }
                    
                    // Si se trata de una resta.
                    else if (column4.get(i).equalsIgnoreCase("-")) {
                        texto.append("\tSUB BL, BH;").append(linea);
                        BL = "BL - BH";
                    }
                    
                    // Si se trata de una suma.
                    else if (column4.get(i).equalsIgnoreCase("+")) {
                        texto.append("\tADD BL, BH;").append(linea);
                        BL = "BL + BH";
                    }
                }
                
                // Si BL tiene guardada información, se utiliza la misma para seguir utilizandose.
                else {
                    
                    // Se salta la instrucción que hace un guardado de dos temporales.
                    i++;
                    
                    // Si no estamos ante una división, se utiliza el registro BH
                    if (!column4.get(i).equalsIgnoreCase("/")) {
                        texto.append("\tMOV BH, ").append(column3.get(i)).append(";").append(linea);
                        BH = column3.get(i);
                    }
                    
                    // Pero si se trata de una división.
                    else {
                        
                        // Se limpia la parte alta de AX si no se encuentra vacía.
                        if (!AH.isEmpty()) {
                            texto.append("\tMOV AH, 0;").append(linea);
                            AH = "";
                        }
                        
                        // Se pasa a AL el número a dividir.
                        texto.append("\tMOV AL, BL;").append(linea);
                        
                        // Se pasa a CL el divisor.
                        texto.append("\tMOV CL, ").append(column3.get(i)).append(";").append(linea);
                        CL = column3.get(i);
                        
                        // Se realiza la división y se indica los registros con información.
                        texto.append("\tDIV CL;").append(linea);
                        AH = "Residuo";
                        AL = "Resultado";
                        
                        // Se guarda el resultado en BL.
                        texto.append("\tMOV BL, AL;").append(linea);
                        BL = CL;
                    }
                    
                    // Si se trata de una multiplicación.
                    if (column4.get(i).equalsIgnoreCase("*")) {
                        texto.append("\tMUL BL, BH;").append(linea);
                        BL = "BL * BH";
                    }
                    
                    // Si se trata de una resta.
                    else if (column4.get(i).equalsIgnoreCase("-")) {
                        texto.append("\tSUB BL, BH;").append(linea);
                        BL = "BL - BH";
                    }
                    
                    // Si se trata de una suma.
                    else if (column4.get(i).equalsIgnoreCase("+")) {
                        texto.append("\tADD BL, BH;").append(linea);
                        BL = "BL + BH";
                    }
                    
                }
                
            }
            
            // Si la siguiente instrucción es un ciclo.
            if (i + 1 == insDo) {
                
                // Se escribe un salto hacia el ciclo.
                texto.append("\tJMP Ciclo").append(numCiclo).append(";").append(linea);
                
            }
            
            // Si la siguiente instrucción es una condicion.
            if (i + 1 == insWhile) {
                
                // Se escribe un salto hacia el ciclo.
                texto.append("\tJMP Condicion").append(numCon).append(";").append(linea);
                
            }
            
            // Si estamos en la ultima linea.
            if (i + 1 == column1.size()) {
                texto.append("\tFIN");
            }
            
        }
        
        // Se guarda el archivo final.
        guardarArchivo("ensamblador.txt", texto.toString());
    }
    
    // Método para convertir un arreglo a texto.
    public static String arregloATexto (String[] arreglo) {
        StringBuilder stb = new StringBuilder();
        
        for (String texto : arreglo) {
            stb.append(texto);
        }
        
        return stb.toString();
    }
    
}
