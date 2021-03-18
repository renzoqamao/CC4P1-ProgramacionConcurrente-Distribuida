package Servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class HiloCliente implements Runnable {
	private Socket cliente;
	public int idCliente;
	public String estadoCliente;
	
	private TCPServidor tcpServidor;

	public PrintWriter mOut;
	public BufferedReader in;
	/*se pasa la cola de peticiones y el estado de cada hilo es conectado.*/
	public HiloCliente(TCPServidor tcpServidor, Socket cliente, int numeroDeCliente) {
		this.tcpServidor = tcpServidor;
		this.cliente = cliente;
		this.idCliente = numeroDeCliente;
	}
	/*envia el ID al comenzar
	 * Recibe las peticiones de los clientes.
	 * */
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);

			int comparar = idCliente(); //recibe id del cliente
			if(comparar==0) { 
				//ENVIA id PARA EL CLIENTE
				this.tcpServidor.maxCliente++;
				enviarMensaje("ID;"+this.tcpServidor.maxCliente);
				//ENVIA maxCliente  A TODAS LAS REPLICAS
				for (HiloReplica h : this.tcpServidor.hiloReplica) {
					h.enviarMensaje("MAXCLIENTE;"+this.tcpServidor.maxCliente);
				}
			}
			else {
				this.idCliente = comparar; //GUARDA id DEL CLIENTE
			}
	
			System.out.println("Cliente "+this.idCliente+" conectado a Balanceado "+this.tcpServidor.id);
			
			peticionDeCliente();
			
			
		} catch (Exception e) {
			//System.out.println("HiloCliente" + ": Error" + e);
		}
	}
	
	private int idCliente() throws IOException {
		String mensaje = "";
		int id = 0;
		boolean salir = false;
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje != null && mensaje.contains("ID")) {
				String arregloMensaje[] = mensaje.split(";");
				id = Integer.parseInt(arregloMensaje[1]);
				salir = true;
			}
		}
		return id;
	}
	
	
	/*Recibe las peticiones de lo clientes y las almacena en la cola de peticiones.
	 * */
	private void peticionDeCliente() throws IOException, InterruptedException  {
		String mensaje = null;
		boolean salir = false;
		while(!salir) {
			mensaje = in.readLine();
			if(mensaje!=null && !mensaje.split(";")[0].equals("Comprobacion")) {
				this.tcpServidor.colaPeticion.put(mensaje); //ALMACENA SOLICITUD EN COLA_PETICIONES
				System.out.println("Peticion del cliente "+this.idCliente+": "+ mensaje);
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