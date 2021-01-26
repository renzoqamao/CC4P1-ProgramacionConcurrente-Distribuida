package cliente;

public class Cliente {

	private static final String IP =  "25.15.114.100";
	private static final int PUERTO_CLIENTE = 2222;
	private TCPCliente tcpCliente = null;

	public static void main(String[] args) {
		Cliente cl = new Cliente();
		cl.inicio();
	}

	public void inicio() {
		new Thread(new Runnable() {
			public void run() {
				tcpCliente = new TCPCliente(IP, PUERTO_CLIENTE);
				tcpCliente.run();
			}
		}).start();
		
	}

}