package balanceador;

public class Balanceador {
	TCPBalanceador tcpBalanceador;

	public static void main(String[] args) {
		Balanceador b = new Balanceador();
		b.iniciar();
	}

	public void iniciar() {
		new Thread(new Runnable() {
			public void run() {
				tcpBalanceador = new TCPBalanceador();
				tcpBalanceador.run();
			}
		}).start();
	}

}