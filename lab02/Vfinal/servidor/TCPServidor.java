package Laboratorio.Lab02.V3_4.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServidor {

	private static final int PUERTO = 4567;
	public ArrayList<TCPServidorHilo> hilosDeServidor = new ArrayList<TCPServidorHilo>();
	private static final int MAXIMO_CLIENTES = 4;

	public void run() {

		ServerSocket socketServidor;
		try {
			socketServidor = new ServerSocket(PUERTO);
			int numeroDeCliente = 0;
			while (numeroDeCliente <= MAXIMO_CLIENTES) {
				Socket cliente = socketServidor.accept();
				hilosDeServidor.add(new TCPServidorHilo(this, cliente, numeroDeCliente + 1));
				Thread hilo = new Thread(hilosDeServidor.get(numeroDeCliente));
				hilo.start();
				numeroDeCliente++;
			}
		} catch (IOException e) {
			System.out.println("Error" + e.getMessage());
		}

	}

	public void enviarMensajeTodos(String mensaje) {
		for (TCPServidorHilo hilo : this.hilosDeServidor) {
			if (hilo.mOut != null && !hilo.mOut.checkError()) {
				hilo.mOut.println(mensaje);
				hilo.mOut.flush();
			}
		}
	}

}
