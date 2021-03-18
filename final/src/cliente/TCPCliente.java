package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPCliente {

	private boolean salir;

	private String ip;
	private int puerto;
	private BufferedReader in;
	private PrintWriter mOut;

	private Cliente cliente;

	public TCPCliente(Cliente cliente, String ip, int puerto) {
		this.cliente = cliente;
		this.ip = ip;
		this.puerto = puerto;
		this.salir = false;

	}

	public void run() {
		InetAddress direccionServidor;
		Socket socket;

		try {
			Thread.sleep(1000); // tiempo de espera 1s
			direccionServidor = InetAddress.getByName(ip);
			socket = new Socket(direccionServidor, puerto);
			System.out.println("Conectado a servidor");
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			
			enviarMensaje("ID;" + this.cliente.id);
			if (this.cliente.id == 0)
				idCliente();
			else { 
				//ENVIA TODAS LAS PETICIONES FALTANTES: Cuando se desconecta un balanceador
				for (int i = 0; i < this.cliente.peticiones.size(); i++) {
					enviarMensaje(this.cliente.peticiones.elementAt(i));
				}		
			}

			// PETICIONES DEL CLIENTE
			new Thread(new Runnable() {
				public void run() {
					try {
						peticion(50, 1000);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();

			// RESPUESTAS PARA EL CLIENTE
			new Thread(new Runnable() {
				public void run() {
					try {
						RecibirRespuesta();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			
			//COMPROBAR PETICIONES
			new Thread(new Runnable() {
				public void run() {
					try {
						comprobarPeticiones();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			while(!salir) {
				try {
					Thread.sleep(100);
					String mensaje = "Comprobacion;Bvivo";
					mOut.println(mensaje);
					if (mOut.checkError()) {
						salir = true;
						socket.close();
						}
				} catch (InterruptedException | IOException  e) {
					e.printStackTrace();
				}
			}
			System.out.println("finalizado");

		} catch (Exception e) {
//			System.out.println("TCP Cliente " + ": Error " + e);
		}

	}

	/*
	 * metodo que recibe el ID enviado por el balanceador
	 */
	private void idCliente() throws IOException {
		String mensaje = "";
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje != null && mensaje.contains("ID")) {
				String arregloMensaje[] = mensaje.split(";");
				this.cliente.id = Integer.parseInt(arregloMensaje[1]);
				break;
			}
		}
		System.out.println("Cliente " + this.cliente.id);
	}

	// mensaje: id_cliente;#operacion;peticion
	private void peticion(int nPeticiones, int tiempoEntrePeticiones) throws IOException, InterruptedException {
		String mensaje = "PETICION";//SE MODIFICA EL MENSAJE
		String peticion = "";
		System.out.println("nPeticones: "+ nPeticiones);
		for (int i = 1; i <= nPeticiones; i++) {
			this.cliente.nPeticion++;
			mensaje = "BUY"+","+ String.valueOf( (int) Math.floor( Math.random()*20+1));
			//ENVIAR PETICON
				//PARA PROBAR
				peticion = this.cliente.id + ";" + this.cliente.nPeticion + ";" + mensaje;
				
				
			this.cliente.peticiones.addElement(peticion);
			enviarMensaje(peticion);
			Thread.sleep(tiempoEntrePeticiones);
			
			System.out.println("peticion: "+peticion);
		}
		System.out.println("FINALIZO");
	}

	// mensaje: id_cliente;#operacion;respuesta
	private void RecibirRespuesta() throws IOException {
		String mensaje = null;
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje != null) {
				String nOperacion = mensaje.split(";")[1]; //numero de operacion
				//verifica si existe el numero de operacion
				for (int i = 0; i < this.cliente.peticiones.size(); i++) {
					if (this.cliente.peticiones.elementAt(i).contains(";"+nOperacion+";")) {
						this.cliente.peticiones.removeElementAt(i); //elimina peticion de la lista
						System.out.println("rpta: " + mensaje);
						break;
					}
				}
			}
		}
	}

	// cada 5s 10 elementos
	private void comprobarPeticiones() throws InterruptedException {
		while (!salir) {
			Thread.sleep(5000);
			int nComprobaciones = (this.cliente.peticiones.size()>10) ? 10:this.cliente.peticiones.size();
			for (int i = 0; i < nComprobaciones; i++) {
				enviarMensaje(this.cliente.peticiones.elementAt(i));
			}				
		}
	}

	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}	

}