package Laboratorio.Lab02.V3_4.servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServidorHilo implements Runnable {
	private static final String CLIENTE_ACEPTA_JUEGO = "OK";
	private static final String ES_UN_CLIENTE = "CLIENTE";
	private static final String ID_CLIENTE = "ID";
	private static final String CLIENTE_MUERTO = "MUERTO";
	private static final String GAME_OVER = "PERDI";
	private static final String GANADOR = "GANE";
	private TCPServidor hilosDeServidor;
	private Socket cliente;
	private int numeroDeCliente;

	public PrintWriter mOut;
	public BufferedReader in;
	
	//ULTIMO CAMBO---------------------
	private String estadoJuego = "";//---------------------------
	private boolean fuera = false;
	//--------------------------------

	public TCPServidorHilo(TCPServidor hilosDeServidor, Socket cliente, int numeroDeCliente) {
		this.hilosDeServidor = hilosDeServidor;
		this.cliente = cliente;
		this.numeroDeCliente = numeroDeCliente;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);

			// El cliente avisa a los demas que se acaba de conectar.
			enviarMensaje(ID_CLIENTE + " " + this.numeroDeCliente);
			hilosDeServidor.enviarMensajeTodos(ES_UN_CLIENTE + " " + this.numeroDeCliente);
			System.out.println(ES_UN_CLIENTE + " " + String.valueOf(this.numeroDeCliente));

			clienteAceptaPartida();
			enviarMovimientosDelCliente();
			
			
			//ULTIMO CAMBO---------------------
			if(this.estadoJuego.equals(CLIENTE_MUERTO)) {
				String mensaje = "";
				String mensajeRecibido[] = mensaje.split("\\s+");;
				while(mensajeRecibido.length !=2) {
					mensaje = in.readLine();
					mensajeRecibido = mensaje.split("\\s+");		
					hilosDeServidor.enviarMensajeTodos(this.numeroDeCliente + " " + mensaje);
				}
				enviarMensaje(GAME_OVER);
				System.out.println("CLIENTE "+this.numeroDeCliente+" PERDIO EL JUEGO");
			}
			if(this.estadoJuego.equals(GANADOR)) {
				enviarMensaje(GANADOR);
				System.out.println("CLIENTE "+this.numeroDeCliente+" GANO EL JUEGO");				
			}
			
			//------------------------------------------

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("TCP Server" + "S: Error" + e);
		}

	}

	// espera que el cliente acepte la partida
	private void clienteAceptaPartida() throws IOException {
		String acepta = "";
		while (true) {
			acepta = in.readLine();
			if (acepta.equals(CLIENTE_ACEPTA_JUEGO)) {
				System.out.println("Cliente " + this.numeroDeCliente + " se une a la partida");
				break;
			}
		}
	}

	//ULTIMO CAMBO---------------------
	// Envia los movimiento de un cliente a todos los demas
	private void enviarMovimientosDelCliente() throws IOException {
		String mensaje = "";
		while (!mensaje.equals(CLIENTE_MUERTO) && clientesVivos()>1) {
			mensaje = in.readLine();
			System.out.println(this.numeroDeCliente + " " + mensaje);
			hilosDeServidor.enviarMensajeTodos(this.numeroDeCliente + " " + mensaje);
		}
		if (mensaje.equals(CLIENTE_MUERTO)) {
			this.estadoJuego = CLIENTE_MUERTO;
			this.fuera = true;
		}
		else if(clientesVivos()<=1) {
			this.estadoJuego = GANADOR;			
		}

	}
	//-------------------------------------
	
	//ULTIMO CAMBO---------------------
	private int clientesVivos() {
		int nclientes = 0;
		for (TCPServidorHilo hilo : this.hilosDeServidor.hilosDeServidor) {
			if(!hilo.fuera)
				nclientes++;
		}
		return nclientes;
	}
	//--------------------------------

	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}

}
