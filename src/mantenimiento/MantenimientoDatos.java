package mantenimiento;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Clase que modela el mantenimiento de los datos de una tabla.
 * Esta clase se usará dentro de la primera opción del menu principal.
 * 
 * @author Alberto Lebrero Pizarro - 2ºDAM - CEU
 * @version 1.3
 */
public class MantenimientoDatos {
	private final Connection conexion;
	private final Scanner teclado;
	
	
	/**
	 * Constructor parametrizado que crea un objeto con una conexión y una entrada estándar.
	 * 
	 * @param conexion Conexión activa a la base de datos.
	 */
	public MantenimientoDatos(Connection conexion) {
		this.conexion = conexion;
		this.teclado = new Scanner(System.in);
	}
	
	
	/**
     * Añade una nueva fila a la tabla especificada, solicitando al usuario un código y un nombre.
     * 
     * @param nombreTabla Nombre de la tabla en la que se insertará la nueva fila.
     * @throws SQLException Si ocurre un error al acceder o modificar la base de datos.
     */
	public void annadirUnaFila(String nombreTabla) throws SQLException {
		System.out.println("Añadir una fila a la tabla " + nombreTabla.toUpperCase());
		
		Statement sentenciaDesplazableActualizable = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet tablaTemporal = sentenciaDesplazableActualizable.executeQuery("SELECT CODIGO, NOMBRE FROM " + nombreTabla.toUpperCase());
		
		
		int codigoSolicitado = solicitarEntero(teclado, "Introduce un código: ");
		String nombreSolicitado = solicitarCadena(teclado, "Introduce un nombre: ");
		
		//Me posiciono en la fila para insertar
		tablaTemporal.moveToInsertRow();
		tablaTemporal.updateInt("CODIGO", codigoSolicitado);
		tablaTemporal.updateString("NOMBRE", nombreSolicitado);
		tablaTemporal.insertRow();
		tablaTemporal.moveToCurrentRow();
		System.out.println("------------------------------------------");
		System.out.println("Fila añadida correctamente.");
		System.out.println("------------------------------------------");
		
		//Cierro los recursos utilizados
		tablaTemporal.close();
		sentenciaDesplazableActualizable.close();
	}
	
	
	/**
     * Muestra los datos de una fila específica de la tabla indicada, identificada por su código.
     * 
     * @param nombreTabla Nombre de la tabla de la cual se visualizará la fila.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
	public void visualizarLosDatosDeUnaFila(String nombreTabla) throws SQLException {
		System.out.println("\nVisualizar los datos de una fila de la tabla " + nombreTabla.toUpperCase());
		
		Statement sentenciaDesplazableActualizable = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet tablaTemporal = sentenciaDesplazableActualizable.executeQuery("SELECT CODIGO, NOMBRE FROM " + nombreTabla.toUpperCase());
		
		
		int codigoSolicitado = solicitarEntero(teclado, "\nIntroduce un código: ");
		boolean encontrado = false;
		
		//Me posiciono antes de la primera fila
		tablaTemporal.beforeFirst();
		while(tablaTemporal.next() && !encontrado) {
			if(tablaTemporal.getInt("CODIGO") == codigoSolicitado) {
				encontrado = true;
				System.out.printf("%-15s%-15s\n", "CÓDIGO", "NOMBRE");
				System.out.println("-".repeat(2*16));
				System.out.printf("%-15s%-15s\n\n", tablaTemporal.getInt("CODIGO"), tablaTemporal.getString("NOMBRE"));
			}
		}
		
		if (!encontrado) {
			System.out.println("\n------------------------------------------------------");
			System.out.println("No se encontró ninguna fila con el código " + codigoSolicitado);
			System.out.println("------------------------------------------------------");
		}
		
		//Cierro los recursos utilizados
		tablaTemporal.close();
		sentenciaDesplazableActualizable.close();
	}
	
	
    /**
     * Muestra todas las filas y columnas de la tabla especificada, ordenadas por el código de manera ascendente.
     * 
     * Este método obtiene los metadatos de la tabla para mostrar dinámicamente los nombres de las columnas
     * y los valores de cada fila. Si la tabla está vacía, se informa al usuario.
     * 
     * @param nombreTabla Nombre de la tabla de la cual se desea visualizar la información.
     * @throws SQLException Si ocurre un error al ejecutar la consulta SQL o al acceder a los datos de la tabla.
     */
	public void mostrarDatos(String nombreTabla) throws SQLException {
		Statement sentenciaDesplazableActualizable = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet tablaTemporal = sentenciaDesplazableActualizable.executeQuery("SELECT CODIGO, NOMBRE FROM " + nombreTabla.toUpperCase() + " ORDER BY CODIGO ASC");
		
		
		ResultSetMetaData metaDatosFilasTabla = tablaTemporal.getMetaData();
		int numeroDeColumnas = metaDatosFilasTabla.getColumnCount();
		
		System.out.println();
		//Muestro el nombre de las columnas
		for(int i = 1; i <= numeroDeColumnas; i++) {
			System.out.printf("%-15s", metaDatosFilasTabla.getColumnName(i));
		}
		System.out.println();
		System.out.println("-".repeat(numeroDeColumnas*16));
		
		tablaTemporal.beforeFirst();
		int numeroDeFilasConsultadas = 0;
		//Muestro los datos de cada una de las filas de la tabla en función de la consulta realizada.
		//Para cada fila...
		while(tablaTemporal.next()) {
			//Para cada columna...
			for(int columnaActual = 1; columnaActual <= numeroDeColumnas; columnaActual++) {
				System.out.printf("%-15s", tablaTemporal.getString(columnaActual));
			}
			System.out.println();
			numeroDeFilasConsultadas++;
		}
		System.out.println("-".repeat(numeroDeColumnas*16));

		if(numeroDeFilasConsultadas == 0) {
			System.out.println("\n-------------------------------------------------------------------");
			System.out.println("No se encontró ninguna fila");
			System.out.println("---------------------------------------------------------------------");
		}else {
			System.out.println("\n\n-- " + numeroDeFilasConsultadas + " filas consultadas --");
			System.out.println("---------------------------");
		}
		
		//Cierro los recursos utilizados
		tablaTemporal.close();
		sentenciaDesplazableActualizable.close();
	}
	
		
	/**
     * Muestra todas las filas de la tabla especificada.
     * 
     * @param nombreTabla Nombre de la tabla a visualizar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
	public void visualizarTodasLasFilas(String nombreTabla) throws SQLException {
		System.out.println("Visualizar todas las fila de la tabla " + nombreTabla.toUpperCase());
		mostrarDatos(nombreTabla);
	}
	
	
	/**
     * Modifica el nombre de una fila en la tabla indicada, identificada por su código.
     * 
     * @param nombreTabla Nombre de la tabla en la que se modificará dicha fila.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
	public void modificarElNombreDeUnaFila(String nombreTabla) throws SQLException {
		System.out.println("Modificar el nombre de una fila de la tabla " + nombreTabla.toUpperCase());
		
		Statement sentenciaDesplazableActualizable = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet tablaTemporal = sentenciaDesplazableActualizable.executeQuery("SELECT CODIGO, NOMBRE FROM " + nombreTabla);
		
		int codigoSolicitado = solicitarEntero(teclado, "Introduce el código del cual se quiere modificar el nombre: ");
		
		boolean encontrado = false;
		tablaTemporal.beforeFirst();
		
		while(tablaTemporal.next() && !encontrado) {
			if(tablaTemporal.getInt("CODIGO") == codigoSolicitado) {
				encontrado = true;
				String nuevoNombre = solicitarCadena(teclado, "Introduce le nuevo nombre: ");
				
				tablaTemporal.updateString("NOMBRE", nuevoNombre);
				tablaTemporal.updateRow();
				
				System.out.println("------------------------------------------");
				System.out.println("Fila modificada correctamente.");
				System.out.println("------------------------------------------");
			}
		}
		
		//Cierro los recursos utilizados
		tablaTemporal.close();
		sentenciaDesplazableActualizable.close();
	}
	
	
    /**
     * Elimina una fila de la tabla indicada según el código ingresado por el usuario.  
     * Antes de borrar la fila, se muestra la información correspondiente y se solicita confirmación al usuario.  
     * 
     * @param nombreTabla Nombre de la tabla de la cual se quiere eliminar una fila.  
     * @throws SQLException Si ocurre un error al acceder a la base de datos.  
     */
	public void eliminarUnaFila(String nombreTabla) throws SQLException {
		System.out.println("Eliminar una fila de la tabla " + nombreTabla.toUpperCase());
		
		Statement sentenciaDesplazableActualizable = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet tablaTemporal = sentenciaDesplazableActualizable.executeQuery("SELECT CODIGO, NOMBRE FROM " + nombreTabla);
		
		int codigoAEliminar = solicitarEntero(teclado, "Introduce el código a eliminar: ");
		
		boolean eliminado = false;
		tablaTemporal.beforeFirst();
		
		while(tablaTemporal.next() && !eliminado) {
			if(tablaTemporal.getInt("CODIGO") == codigoAEliminar) {
				System.out.println("Vas a proceder a eliminar a esta persona:");
				System.out.printf("\t%-15s%-15s\n", "CÓDIGO", "NOMBRE");
				System.out.println("\t" + "-".repeat(2*16));
				System.out.printf("\t%-15s%-15s\n\n", tablaTemporal.getInt("CODIGO"), tablaTemporal.getString("NOMBRE"));
				
				String confirmacion = solicitarCadena(teclado, "¿Estás seguro que deseas eliminarlo? (S/N): ");
				
				if(confirmacion.equalsIgnoreCase("s")) {
					tablaTemporal.deleteRow();
					eliminado = true;
					System.out.println("------------------------------------------");
					System.out.println("Fila eliminada correctamente.");
					System.out.println("------------------------------------------");
				}else {
					System.out.println("--------------------");
					System.out.println("Operación cancelada.");
					System.out.println("--------------------");
				}
			}
		}
		
		//Cierro los recursos utilizados
		tablaTemporal.close();
		sentenciaDesplazableActualizable.close();
	}
	
	/**
	 * Solicita al usuario ingresar un número entero positivo de tipo primitivo.
	 * 
	 * @param teclado Objeto Scanner utilizado para leer el número entero escrito por el usuario.
	 * @param mensaje Mensaje de texto que se mostrará al usuario solicitando el dato de tipo primitivo int.
	 * @return Devuelve el valor entero si se cumplen las condiciones.
	 * @throws InputMismatchException Si el usuario ingresa cualquier dato distinto al solicitado.
	 */
    private static int solicitarEntero(Scanner teclado, String mensaje) {
        int numero;
        do {
            System.out.print(mensaje);
            while (!teclado.hasNextInt()) {
                System.out.println("Debe ingresar un número entero positivo.");
                teclado.next();
            }
            numero = teclado.nextInt();
        } while (numero < 0);
        teclado.nextLine();
        return numero;
    }
	
	/**
	 * Solicita al usuario ingresar una cadena de carcateres (String).
	 * 
	 * @param teclado Objeto Scanner utilizado para leer el número entero escrito por el usuario.
	 * @param mensaje Mensaje de texto que se mostrará al usuario solicitando el dato de tipo String.
	 * @return Un String con la cadena proporcionada por el usuario.
	 * @throws NoSuchElementException Si no se encontró ninguna cadena tras la solicitud.
	 */
	public static String solicitarCadena(Scanner teclado, String mensaje) throws NoSuchElementException {
		String cadenaIngresada;
		
        do {
        	System.out.print(mensaje);
        	cadenaIngresada = teclado.nextLine().trim();
			
        	if(cadenaIngresada.isEmpty()) {
        		System.out.println("\tLa cadena no puede estar vacía.");
        	}
        	
        }while((cadenaIngresada.isEmpty()));
        		
        return cadenaIngresada;
    }
	
}
