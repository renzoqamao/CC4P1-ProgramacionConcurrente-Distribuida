package Laboratorio.Lab02.V3_4.cliente;

import java.util.Scanner;

public class Jugador {

	private static final String IP = "192.168.1.123";
	private static final int PUERTO = 4567;
	private static final String FINALIZA_JUEGO_CLIENTE = "FINAL";// FALTA VERIFICAR
	private TCPJugador tcpJugador;
	Scanner sc;
	Scanner sc_ip;
	char ids[] = new char[4];
//	Mapa campo = new Mapa(1);

	public static void main(String[] args) {
		Jugador j = new Jugador();
		j.inicio();

	}

	public void inicio() {
		new Thread(new Runnable() {
			public void run() {
				tcpJugador = new TCPJugador(IP, PUERTO);
				tcpJugador.run();
			}
		}).start();
		teclado();
	}

	private void teclado() {
		sc = new Scanner(System.in);
		String mensaje = sc.nextLine();

		while (!mensaje.equals(FINALIZA_JUEGO_CLIENTE)) {
			tcpJugador.enviarMensaje(mensaje); // envia mensaje al servidor
			mensaje = sc.nextLine();
		}

	}

}
