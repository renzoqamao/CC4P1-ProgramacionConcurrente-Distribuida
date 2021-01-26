package replica;


public class Replica {

	private static final String IP = "25.15.114.100";
	private static final int PUERTO_REPLICA = 3333;
	private TCPReplica tcpReplica = null;

	public static void main(String[] args) {
		Replica r = new Replica();
		r.inicio();
	}

	public void inicio() {
		new Thread(new Runnable() {
			public void run() {
				/*envia la cantidad de cuentas y el monto por cuentas
				 * */
				tcpReplica = new TCPReplica(IP, PUERTO_REPLICA, 10000, 1000);
				tcpReplica.run();
			}
		}).start();	
	}

}