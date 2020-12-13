package Laboratorio.Lab02.V3_4.servidor;

import java.util.Scanner;

public class Servidor {
	TCPServidor tcpServidor;
	Scanner sc;

	public static final String INICIO_JUEGO_SERVIDOR = "INICIO";
	public static final String FINALIZA_JUEGO_SERVIDOR = "FINAL";// FALTA VERIFICAR

	public static void main(String[] args) {
		Servidor s = new Servidor();
		s.iniciar();
	}

	public void iniciar() {

		new Thread(new Runnable() {
			public void run() {
				tcpServidor = new TCPServidor();
				tcpServidor.run();
			}
		}).start();

		teclado();
	}

	private void teclado() {
		sc = new Scanner(System.in);
		String mensaje = sc.nextLine();

		while (!mensaje.equals(FINALIZA_JUEGO_SERVIDOR)) {
			if (mensaje.equals(INICIO_JUEGO_SERVIDOR)) {
				tcpServidor.enviarMensajeTodos(mensaje);
			}
			mensaje = sc.nextLine();
		}
	}

}
