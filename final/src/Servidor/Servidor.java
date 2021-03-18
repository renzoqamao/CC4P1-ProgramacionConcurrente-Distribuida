package Servidor;

public class Servidor {
	TCPServidor tcpBalanceador;

	public static void main(String[] args) {
		Servidor b = new Servidor();
		b.iniciar();
	}

	public void iniciar() {
		new Thread(new Runnable() {
			public void run() {
				tcpBalanceador = new TCPServidor();
				tcpBalanceador.run();
			}
		}).start();
	}

}