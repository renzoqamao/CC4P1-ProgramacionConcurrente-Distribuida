package replica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class TCPReplica {
	private String ip;
	private int puerto;
	private BufferedReader in;
	private PrintWriter mOut;
	/* Array de String para escribir en el archivo csv
	 * */
	private String[] archivoCuentas = null;
	/*id de la replica
	 * cantidad de cuentas
	 * monto inicial de cada acuenta*/
	private int id;
	private int nCuentas;
	private int montoInicial;

	public TCPReplica(String ip, int puerto, int nCuentas, int montoInicial) {
		this.ip = ip;
		this.puerto = puerto;
		this.nCuentas = nCuentas;
		this.montoInicial = montoInicial;
	}

	public void run() {
		InetAddress direccionServidor;
		Socket socket;
		try {
			direccionServidor = InetAddress.getByName(ip);
			socket = new Socket(direccionServidor, puerto);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			
			conexionEstablecida();
			crearArchivoCuentas(nCuentas, montoInicial);
			this.archivoCuentas = new String[nCuentas];
			ficheroEnMemoria("src/replica/replica" + this.id + ".csv");
			enviarRespuestaABalanceador();

		} catch (Exception e) {
			System.out.println("TCP Replica " + ": Error " + e);
		}

	}
	/* Recibe el id que le envia el Balanceador
	 * */
	private void conexionEstablecida() throws IOException {
		String mensaje = "";
		boolean salir = false;
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje!=null && mensaje.contains("ID")) {
				String arregloMensaje[] = mensaje.split(" ");
				this.id = Integer.parseInt(arregloMensaje[1]);
				salir = true;
			}
		}
		System.out.println("Replica "+ this.id);
	}
	/*Envia la respuesta al balanceador
	 * 
	 * */
	private void enviarRespuestaABalanceador() throws IOException {
		String mensaje = null;
		boolean salir = false;
		int id_account = 0;
		String tipoOperacion = "";
		while (!salir) {
			mensaje = in.readLine();
			System.out.println("Recibe peticion: "+ mensaje);
			if (mensaje != null) {
				if(mensaje.equals("FINALIZO")) {
					montoTotal();
					System.out.println("FINALIZADO OPERACIONES DE TODOS LOS CLIENTES");
					break;
				}
				tipoOperacion = mensaje.split("-")[1];
				if (tipoOperacion.equals("L")) 
					enviarSaldo(mensaje);
				if (tipoOperacion.equals("A")) 
					realizarTransaccion(mensaje);
			}
		}
	}
	
	/* Se crea el archivo csv con los montos iniciales
	 * */
	private void crearArchivoCuentas(int nCuentas, int montoInicial) {
		PrintWriter writer = null;
		int idCuenta = 0;
		try {
			writer = new PrintWriter("src/replica/replica" + this.id + ".csv", "UTF-8");
			while (idCuenta < nCuentas) {
				idCuenta++;
				writer.println(idCuenta + "," + montoInicial);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*se pasa la informacion del fichero a un array.
	 * */
	public void ficheroEnMemoria(String ubicacion) {
		File archivo = new File(ubicacion);
		FileReader fr;
		BufferedReader br;
		String current = "";
		int contador = 0;
		try {
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);
			while ((current = br.readLine()) != null) {
				this.archivoCuentas[contador] = current;
				contador++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*Es escribe la informacion del array al fichero csv.
	 * */
	public  void escribirArchivo(String ubicacion)  {
		File archivo = new File(ubicacion);
		FileWriter fw = null;
		BufferedWriter bf = null;
		try {
			fw = new FileWriter(archivo);
			bf = new  BufferedWriter(fw);
			for (String cuentas : archivoCuentas) {
				bf.write(cuentas+"\n");
			}
			bf.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/*Realiza la transacción y envia el mensaje al Balanceador
	 * Peticiones tipo A
	 * */
	private void realizarTransaccion(String mensaje) {
		String[] arreglo = mensaje.split(";");
		String id_solicitud = arreglo[0];
		int cuenta_origen = Integer.parseInt(arreglo[1]);
		int cuenta_destino = Integer.parseInt(arreglo[2]);
		int dinero_solicitado = Integer.parseInt(arreglo[3]);
		int saldo_cuenta_origen;
		int saldo_cuenta_destino;
		if (cuenta_destino <= archivoCuentas.length) { // si existe cuenta destino
			saldo_cuenta_origen = Integer.parseInt(archivoCuentas[cuenta_origen - 1].split(",")[1]);
			saldo_cuenta_destino = Integer.parseInt(archivoCuentas[cuenta_destino - 1].split(",")[1]);
			if (dinero_solicitado <= saldo_cuenta_origen) { // saldo suficiente
				archivoCuentas[cuenta_origen - 1] = cuenta_origen + "," + (saldo_cuenta_origen - dinero_solicitado);
				archivoCuentas[cuenta_destino - 1] = cuenta_destino + "," + (saldo_cuenta_destino + dinero_solicitado);
				enviarMensaje(id_solicitud + "," + archivoCuentas[cuenta_origen - 1]
						+ "," + archivoCuentas[cuenta_destino - 1]);
				System.out.println("Enviar Saldo: "+id_solicitud + "," + archivoCuentas[cuenta_origen - 1]
						+ "," + archivoCuentas[cuenta_destino - 1]);
			} else {
				enviarMensaje( id_solicitud + ", SALDO INSUFICIENTE");
				System.out.println("Enviando Saldo: "+id_solicitud + ", SALDO INSUFICIENTE");
			}
		} else {
			enviarMensaje(id_solicitud + ", NO EXISTE CUENTA DESTINO");
			System.out.println("Enviando Saldo: "+id_solicitud + ", NO EXISTE CUENTA DESTINO");
		}
	}
	/*Metodo para peticion tipo L
	 * */
	private void enviarSaldo(String mensaje) {
		int id_account;
		int saldo = 0;
		id_account = Integer.parseInt(mensaje.split(";")[1]);
		saldo = Integer.parseInt(archivoCuentas[id_account - 1].split(",")[1]);
		enviarMensaje(mensaje + "," + saldo);
		System.out.println("Enviar Saldo: "+mensaje + "," + saldo);
	}
	/*Metodo que escribe en el csv los datos del array ademas que suma el monto total de la base de datos para comprobar*/
	private void montoTotal() {
		int saldoTotal = 0;
		escribirArchivo("src/replica/replica" + this.id + ".csv");
		for (String cuentas : archivoCuentas) {
			saldoTotal += Integer.parseInt(cuentas.split(",")[1]);
		}
		System.out.println("-------------------------------------------");
		System.out.println("		SALDO TOTAL: "+ saldoTotal);
	}
	/*envia mensaje al balanceador*/
	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}

}
