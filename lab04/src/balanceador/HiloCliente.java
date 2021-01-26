package balanceador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class HiloCliente implements Runnable {
	private Socket cliente;
	public int id;
	public String estadoCliente;

	private BlockingQueue<String> colaPeticion;
	public PrintWriter mOut;
	public BufferedReader in;
	/*se pasa la cola de peticiones y el estado de cada hilo es conectado.*/
	public HiloCliente(BlockingQueue<String> solicitud, Socket cliente, int numeroDeCliente) {
		this.colaPeticion = solicitud;
		this.cliente = cliente;
		this.id = numeroDeCliente;
		this.estadoCliente = "CONECTADO";
	}
	/*envia el ID al comenzar
	 * Recibe las peticiones de los clientes.
	 * */
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);
			enviarMensaje("ID "+this.id);
			System.out.println("Cliente "+this.id+" conectado");
			peticionDeCliente();
		} catch (Exception e) {
			//System.out.println("HiloCliente" + ": Error" + e);
		}
	}
	
	/*Recibe las peticiones de lo clientes y las almacena en la cola de peticiones.
	 * */
	private void peticionDeCliente() throws IOException, InterruptedException  {
		String mensaje = null;
		boolean salir = false;
		while(!salir) {
			mensaje = this.in.readLine();
			if(mensaje!=null) {
				this.colaPeticion.put(mensaje); //ALMACENA SOLICITUD EN COLA_PETICIONES
				System.out.println("Recibiendo peticion de cliente: "+ mensaje);
			}
		}
	}
	
	
	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}

}