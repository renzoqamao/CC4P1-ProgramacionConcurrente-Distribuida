package balanceador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class HiloReplica implements Runnable {
	private Socket cliente;
	public int id;
	public String estado;
	public String bloqueo;
	public String respuesta;

	private BlockingQueue<String> colaRespuesta;
	public PrintWriter mOut;
	public BufferedReader in;
	/*Todos los hilos al comenzar son libres y desbloqueados.
	 * */
	public HiloReplica(BlockingQueue<String> colaRespuesta, Socket cliente, int numeroDeCliente) {
		this.colaRespuesta = colaRespuesta;
		this.cliente = cliente;
		this.id = numeroDeCliente;
		this.estado = "LIBRE";
		this.bloqueo = "DESBLOQUEADO";
		this.respuesta = "-";
	}
	/*Se envia  el ID al comenzar.
	 * Ademas de las respuestas replicas.*/
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);
			
			enviarMensaje("ID "+this.id);
			respuestaReplica();
			
		} catch (Exception e) {
			//System.out.println("Hilo Replica " + "S: Error" + e);
		}

	}

	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}
	/*Lee el mensaje y busca que tipo de operacion es:
	 * Si es de "L"ectura almacena la respuesta en la cola de respuesta, despues de consultar pasa a estado libre.
	 * Si es de "A"ctualizacion almaena la respuesta en el atributo respuesta.
	 * */	
	private void respuestaReplica() throws IOException, InterruptedException  {
		String mensaje = null;
		boolean salir = false;
		while(!salir) {
			mensaje = this.in.readLine();
			if(mensaje!=null) {
				String tipoOperacion = mensaje.split("-")[1];//tipo de operacion
				if(tipoOperacion.equals("L")) {		
					this.colaRespuesta.put(mensaje); //ALMACENA SOLICITUD EN COLA_RESPUESTA		
					this.estado="LIBRE";
				}
				if(tipoOperacion.equals("A")) {
					this.respuesta = mensaje;	
				} 
				
				System.out.println("Mensaje de Replica: "+mensaje);
			}
		}
	}
	
}	
	
	
	