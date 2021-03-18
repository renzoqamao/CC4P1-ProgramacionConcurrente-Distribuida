package Servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class HiloReplica implements Runnable {

	private Socket cliente;
	public int id;
	public String estadoCliente;
	private String ipReplica;

	private TCPServidor tcpServidor;
	
	public PrintWriter mOut;
	public BufferedReader in;

	public HiloReplica(TCPServidor tcpServidor, Socket cliente, String ipReplica) {
		this.tcpServidor = tcpServidor;
		this.cliente = cliente;
		this.ipReplica = ipReplica;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);
			
			//ENVIA id a REPLICA
			int contador = 1;
			for (String d : this.tcpServidor.direcciones) {
				if(d.contains(this.ipReplica)) {
					this.id = contador;
					break;
				}
				contador++;	
			}
			enviarMensaje("ID;"+this.id);
			
			System.out.println("Replica "+this.id+" conectado a Balanceado ");
			
			respuestaReplica();

		} catch (Exception e) {
			//System.out.println("HiloCliente" + ": Error" + e);
		}
	}
	
	private void respuestaReplica() throws IOException, InterruptedException  {
		String mensaje = null;
		boolean salir = false;
		while(!salir) {
			mensaje = this.in.readLine();
			if(mensaje!=null && !mensaje.split(";")[0].equals("Comprobacion")) {
				this.tcpServidor.colaRespuesta.put(mensaje); //ALMACENA SOLICITUD EN COLA_RESPUESTA		
				System.out.println("Respuesta de Replica: "+mensaje);
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