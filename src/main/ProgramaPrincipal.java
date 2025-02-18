package main;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

import mantenimiento.MantenimientoDatos;

/**
 * Programa principal que comprueba el correcto funcionamiento de la clase MantenimientoDatos y de la aplicación en general.
 * 
 * @author Alberto Lebrero Pizarro - 2ºDAM - CEU
 * @version 1.3
 */
public class ProgramaPrincipal {

	/**
	 * Método principal que ejecuta la aplicación.
	 * 
	 * @param args Argumentos de entrada pasados como parámetros de entrada.
	 */
	public static void main(String[] args) {
		System.out.println("PRÁCTICA 5: MANTENIMIENTO DE TABLAS CON RESULTSET Y TRANSACCIONES.");
		String nombreTabla = "T_BLOQUEOS";
		
		boolean salir = false;
		Scanner teclado = new Scanner(System.in);

		while(!salir) {
			String nombreDeUsuario = MantenimientoDatos.solicitarCadena(teclado, "Introduce un usuario: ");
			String contrasennaDeUsuario = MantenimientoDatos.solicitarCadena(teclado, "Introduce una contraseña: ");

			try {
				Class.forName("oracle.jdbc.OracleDriver");
				
				//Obtengo la conexión a la BBDD de Oracle
				try(Connection conexion = DriverManager.getConnection
	                    ("jdbc:oracle:thin:"+nombreDeUsuario+"/"+contrasennaDeUsuario+"@//localhost:1521/XE")) {
					System.out.println("-----------------------------------------------------");
					System.out.println("Conectado con usuario " + nombreDeUsuario + " y contraseña " + contrasennaDeUsuario);
					System.out.println("-----------------------------------------------------");
					boolean salirMenu = false;
					while(!salirMenu) {
						menuPrincipal();
						int opcionEscogida = escogerOpcion("Escoge una opción: ", 1, 3);
						
						switch(opcionEscogida) {
							case 1:
								//Se realiza el mantenimiento exclusivamente de la tabla T_BLOQUEOS
								mantenimientoDeDatos(conexion, nombreTabla, new MantenimientoDatos(conexion));
								break;
							case 2:
								realizarLecturaNoRepetida(conexion, nombreDeUsuario, contrasennaDeUsuario, nombreTabla);
								break;
							case 3:
								salir = true;
								salirMenu = true;
								teclado.close();
								conexion.close();
								System.out.println("Hasta pronto!");
								break;
						}
					}
					
				}
				
			} catch (ClassNotFoundException cNFE) {
				System.out.println("Error al cargar el driver JDBC de Oracle.\n");
			} catch (SQLException sqlE) {
				sqlE.printStackTrace();
				System.out.println("Usuario y contraseña erróneos.\n");

			}
		}

	}
	
	/**
	 * Método que muestra el menú principal de la aplicación.
	 */
	private static void menuPrincipal() {
		System.out.println("***************************************************************");
		System.out.println("* 1. Mantenimiento de datos de T_BLOQUEOS usando ResultSet.   *");
		System.out.println("* 2. Lectura no repetida de la tabla T_BLOQUEOS.              *");
		System.out.println("* 3. Salir.                                                   *");
		System.out.println("***************************************************************");
	}
	
	///////////////////////////////////////////////////////////////////
	//Opción 1: Mantenimiento de datos de T_BLOQUEOS usando ResultSet.
	///////////////////////////////////////////////////////////////////

	/**
	 * Método que muestra el menu del mantenimiento de datos de la aplicación.
	 */
	private static void menuMantenimientoDeDatos() {
		System.out.println("\n\t\t\t Mantenimiento de datos de la tabla T_BLOQUEOS usando RsultSet.");
		System.out.println("**********************************************************************************************************************");
		System.out.println("**\t1. Anadir una fila a la tabla.                                                                              **");
		System.out.println("**\t2. Visualizar los datos de una fila de la tabla.                                                            **");
		System.out.println("**\t3. Visualizar todas las filas de la tabla.                                                                  **");
		System.out.println("**\t4. Modificar el nombre de una fila.                                                                         **");
		System.out.println("**\t5. Eliminar una fila de la tabla.                                                                           **");
		System.out.println("**\t6. Salir.                                                                                                   **");
		System.out.println("**********************************************************************************************************************");
	}

	/**
	 * Realiza el mantenimiento de los datos de la tabla especificada.
	 * 
	 * @param conexion Conexión a la base de datos.
	 * @param nombreTabla Nombre de la tabla a mantener.
	 * @param mantenimiento Objeto de la clase MantenimientoDatos para acceder a los métodos correspondientes.
	 * @throws SQLException Si ocurre un error al acceder a la base de datos.
	 */
	private static void mantenimientoDeDatos(Connection conexion, String nombreTabla, MantenimientoDatos mantenimiento) throws SQLException {
		boolean volver = false;
				
		while(!volver) {
			menuMantenimientoDeDatos();
			int opcionEscogida = escogerOpcion("Escoge una opción: ", 1, 6);
			
			switch(opcionEscogida) {
				case 1:
					mantenimiento.annadirUnaFila(nombreTabla);
					break;
				case 2:
					mantenimiento.visualizarLosDatosDeUnaFila(nombreTabla);
					break;
				case 3:
					mantenimiento.visualizarTodasLasFilas(nombreTabla);
					break;
				case 4:
					mantenimiento.modificarElNombreDeUnaFila(nombreTabla);
					break;
				case 5:
					mantenimiento.eliminarUnaFila(nombreTabla);
					break;
				case 6:
					volver = true;
					break;
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////
	//Opción 2: Lectura no repetida de la tabla T_BLOQUEOS.
	///////////////////////////////////////////////////////////////////
	
	/**
	 * Realiza una lectura no repetida de la tabla especificada utilizando dos conexiones
	 * a la base de datos con el mismo usuario y configuración de aislamiento de transacciones.
	 * 
	 * Se ejecuta una consulta inicial con la primera conexión, luego se actualiza un registro
	 * con la segunda conexión y se confirma la transacción (commit). Posteriormente, se vuelve a consultar
	 * el mismo registro con ambas conexiones para observar el comportamiento de lectura.
	 * 
	 * @param conn                 Conexión a la base de datos.
	 * @param nombreDeUsuario      Nombre del usuario de la base de datos.
	 * @param contrasennaDeUsuario Contraseña del usuario de la base de datos.
	 * @param nombreTabla          Nombre de la tabla sobre la que se realiza la operación.
	 * @throws SQLException Si ocurre un error durante la ejecución de operaciones SQL.
	 */
	private static void realizarLecturaNoRepetida(Connection conn, String nombreDeUsuario, String contrasennaDeUsuario, String nombreTabla) throws SQLException {
		Connection conexion_1 = DriverManager.getConnection("jdbc:oracle:thin:"+nombreDeUsuario+"/"+contrasennaDeUsuario+"@//localhost:1521/XE");
		Connection conexion_2 = DriverManager.getConnection("jdbc:oracle:thin:"+nombreDeUsuario+"/"+contrasennaDeUsuario+"@//localhost:1521/XE");

		conexion_1.setAutoCommit(false);
		conexion_2.setAutoCommit(false);
		
		//Tipo de bloqueo
		conexion_1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conexion_2.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		PreparedStatement sentenciaPreparada_1 = conexion_1.prepareStatement("SELECT * FROM " + nombreTabla.toUpperCase() + " WHERE CODIGO = ?");
		sentenciaPreparada_1.setInt(1, 20);
		ResultSet tablaTemporalTransaccion_1 = sentenciaPreparada_1.executeQuery();
		
		if(tablaTemporalTransaccion_1.next()) {
			System.out.printf("%-15s%-15s\n", "CÓDIGO", "NOMBRE");
			System.out.println("-".repeat(2*16));
			System.out.printf("%-15s%-15s\n\n", tablaTemporalTransaccion_1.getInt("CODIGO"), tablaTemporalTransaccion_1.getString("NOMBRE"));
		}
		
		PreparedStatement sentenciaPreparada_2 = conexion_2.prepareStatement("UPDATE " + nombreTabla.toUpperCase() + " SET NOMBRE = 'Cambio de nombre' WHERE CODIGO = ?");
		sentenciaPreparada_2.setInt(1, 20);
		@SuppressWarnings("unused")
		int filaModificada = sentenciaPreparada_2.executeUpdate();
		
		conexion_2.commit();
		
		String consultaSQL = "SELECT * FROM " + nombreTabla.toUpperCase() + " WHERE CODIGO = 20";		
		ResultSet tablaTemporalTransaccion_2 = conexion_2.createStatement().executeQuery(consultaSQL);
		
		if(tablaTemporalTransaccion_2.next()) {
			System.out.printf("%-15s%-15s\n", "CÓDIGO", "NOMBRE");
			System.out.println("-".repeat(2*16));
			System.out.printf("%-15s%-15s\n\n", tablaTemporalTransaccion_2.getInt("CODIGO"), tablaTemporalTransaccion_2.getString("NOMBRE"));
		}
		
		tablaTemporalTransaccion_1 = sentenciaPreparada_1.executeQuery();
		if(tablaTemporalTransaccion_1.next()) {
			System.out.printf("%-15s%-15s\n", "CÓDIGO", "NOMBRE");
			System.out.println("-".repeat(2*16));
			System.out.printf("%-15s%-15s\n\n", tablaTemporalTransaccion_1.getInt("CODIGO"), tablaTemporalTransaccion_1.getString("NOMBRE"));
		}
		
		
		//Libero los recursos en el orden de creación.
		tablaTemporalTransaccion_2.close();
		sentenciaPreparada_2.close();
		tablaTemporalTransaccion_1.close();
		sentenciaPreparada_1.close();
		conexion_2.close();
		conexion_1.close();
	}
	
	
	/**
	 * Escoge la opción el que el usuario selecciona por teclado y realiza la 
	 * operación pertinente. Si el usuario escoge una opción no válida mostrará 
	 * un mensaje de error por pantalla.
	 * 
	 * @param mensajeParaSolicitarDato Mensaje que se imprimirá por pantalla para solicitar el dato al usuario.
	 * @param minValor Mínimo valor a ser seleccionado.
	 * @param maxValor Máximo valor a ser seleccionado.
	 * 
	 * @throws InputMismatchException Si el token recuperado no coincide con el patrón del tipo 
	 * 		   esperado (int).
	 * @throws InputMismatchException Si la entrada no es un número entero.
	 */
	private static int escogerOpcion(String mensajeParaSolicitarDato, int minValor, int maxValor) throws InputMismatchException {
		int opcionEscogida = 0;
		@SuppressWarnings("resource")
		Scanner teclado = new Scanner(System.in);;
		
		do {
			System.out.print(mensajeParaSolicitarDato);
			opcionEscogida = teclado.nextInt();
			
			//Limpio el buffer de entrada
			teclado.nextLine();
			
			if((opcionEscogida < minValor) || (opcionEscogida > maxValor)) {
				System.out.println("Opción incorrecta. Por favor, seleccione una opción válida.\n");
			}
		} while((opcionEscogida < minValor) || (opcionEscogida > maxValor));
				
		return opcionEscogida;
	}

}
