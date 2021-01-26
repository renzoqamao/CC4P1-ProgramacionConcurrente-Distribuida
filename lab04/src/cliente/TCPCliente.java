package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class TCPCliente {

	private String ip;
	private int puerto;
	private BufferedReader in;
	private PrintWriter mOut;

	private int[] cuentas;
	private int id;

	public TCPCliente(String ip, int puerto) {
		this.ip = ip;
		this.puerto = puerto;

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
			generandoCuentas(15, 100);
			peticion(500, 10);
			System.out.println("FINALIZO PETICIONES DE CLIENTE "+this.id);

		} catch (Exception e) {
			System.out.println("TCP Cliente " + ": Error " + e);
		}

	}
	/*metodo que recibe el ID enviado por el balanceador
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
		System.out.println("Cliente "+this.id);
	}
	/*metodo de genera un numero de  cantidadCuentas de cuentas 
	 * y una variable rango para que cada cuenta seleccione una cuenta diferente de la base de datos.()
	 * */
	private void generandoCuentas(int cantidadCuentas, int rango) {
		this.cuentas = new int[cantidadCuentas];
		Random r = new Random();
		int num_rand;
		for (int i = 0; i < cuentas.length; i++) {
			num_rand =   (r.nextInt(rango) + (this.id-1)*rango+1);
			cuentas[i] = num_rand;
		}
	}
	/*Metodo de generación de Peticiones de :
	 * L: lectura
	 * A : actualizaciones
	 * Cada clienta genera nPeticiones y el tiempo entre peticiones es de 0,01seg
	 * */
	private void peticion(int nPeticiones, int tiempoEntrePeticiones) throws IOException, InterruptedException {
		Random r = new Random();
		int num_rand;
		int nOperacion = 0;
		String peticion = "";
		for (int i = 1; i <= nPeticiones; i++) {
			num_rand = r.nextInt(10) + 1;
			nOperacion++;
			if (num_rand <= 6) { // operacion=L
				peticion = peticionLeer("L", nOperacion);
				enviarMensaje(peticion);
				System.out.println("petición: "+peticion);
				RecibirRespuesta();
			} else { // operacion=A
				peticion = peticionActualizar("A", nOperacion);
				enviarMensaje(peticion);
				System.out.println("petición: "+peticion);
				RecibirRespuesta();
			}
			Thread.sleep(tiempoEntrePeticiones);
		}
		enviarMensaje("FINALIZO");
	}
	/*Metodo para peticion tipo L
	 * construye el formato del mensaje
	 *  id-L-nOperacion;id_account(cuenta destino)
	 * */
	private String peticionLeer(String operacion, int nOperacion) {
		String id_solicitud = this.id + "-" + operacion +"-"+ nOperacion;
		int id_account = this.cuentas[nOperacion % this.cuentas.length];
		return id_solicitud + ";" + id_account;
	}
	/*
	 * Metodo para peticion tipo A
	 * construye el formato del mensaje
	 * id-A-nOperacion;De una cuenta ; Para otra cuenta ; cantidad_dinero;
	 * */
	private String peticionActualizar(String operacion, int nOperacion) {
		Random r = new Random();
		int num_rand;
		
		String id_solicitud;
		int de;
		int para;
		int cantidad_dinero;
		
		id_solicitud = this.id + "-" + operacion + "-"+nOperacion;
		de = this.cuentas[nOperacion % this.cuentas.length];

		do {
			num_rand = r.nextInt(10000) + 1;
		} while (num_rand == de); //verifica que no se envie a la misma cuenta
		para = num_rand;

		cantidad_dinero = r.nextInt(1000) + 1;
		
		return id_solicitud + ";" + de + ";" + para + ";" + cantidad_dinero;
	}

	
	/*Recibe las respuestas del Balanceador
	 * */
	private void RecibirRespuesta() throws IOException {
		String mensaje = null;
		boolean salir = false;
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje != null) {
				System.out.println("rpta: " + mensaje);
				salir = true;
			}
		}
	}
	/*Envia mensajes al Balanceador
	 * */
	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}
}