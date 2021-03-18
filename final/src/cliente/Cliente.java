// PETICION: id_cliente;#operacion;peticion
// RESPUESTA: id_cliente;#operacion;respuesta

package cliente;

import java.util.ArrayList;
import java.util.Vector;

public class Cliente {

	private TCPCliente tcpCliente = null;
	
	private ArrayList<String> direcciones = new ArrayList<String>();
	public Vector<String> peticiones = new Vector<String>(20,5);
	public int id = 0;
	public static Cliente cl;
	public int nPeticion = 0;

	public static void main(String[] args) {
		cl = new Cliente();
		cl.inicio();
	}
	
	public void agregarDirecciones() {
		direcciones.add("192.168.1.101:2222");
		direcciones.add("192.168.1.102:2222");
		direcciones.add("192.168.1.103:2222");
		direcciones.add("192.168.1.104:2222");
	}

	public void inicio() {
		new Thread(new Runnable() {
			public void run() {
				agregarDirecciones();
				while(true) {
					int contador = 1;
					for (String d : direcciones) {
						String direccionServidor[] = d.split(":");
						tcpCliente = new TCPCliente(cl, direccionServidor[0], Integer.parseInt(direccionServidor[1]));
						tcpCliente.run();
						System.out.println("Cambiando a servidor "+contador);
						contador++;
					}				
				}
			}
		}).start();
		
		
	}
	
	

}