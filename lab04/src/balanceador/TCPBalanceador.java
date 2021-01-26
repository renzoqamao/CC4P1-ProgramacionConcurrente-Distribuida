package balanceador;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPBalanceador {

	private static final int PUERTO_CLIENTE = 2222;
	private static final int MAXIMO_CLIENTES = 5;
	private static final int PUERTO_REPLICA = 3333;
	private static final int MAXIMO_REPLICAS = 5;
	/*Array de Hilos CLientes para que se comunique con los Clientes
	 * Array de HIlos Replica para que se comunique con las replicas
	 * Cola de Peticiones
	 * Cola para las Respuestas
	 * */
	private ArrayList<HiloCliente> hiloCliente = new ArrayList<HiloCliente>();
	private ArrayList<HiloReplica> hiloReplica = new ArrayList<HiloReplica>();
	private BlockingQueue<String> colaPeticion = new LinkedBlockingQueue<String>();
	private BlockingQueue<String> colaRespuesta = new LinkedBlockingQueue<String>();

	public void run() {
		/*Al iniciar el Balanceador se genera los Hilos para clientes y replicas
		 * Asi mismo se genera un hilo para enviarRespuesta a Cliente y otro hilo para enviarPeticion a Replica
		 * */
		new Thread(new Runnable() {
			public void run() {
				generadorHilosClientes();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				generadorHilosReplica();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				envioRespuestaCliente();
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run() {
				enviarPeticionReplica();
			}
		}).start();

	}
	
	/*se genera Hilos para cada cliente utilizando el mismo socket del serverSocket
	 * Cada hilo es almacenado en el arrayList y se crea un hilo unico con ese array de hilos para luego iniciarlo
	 * Cada hilo creado se le pasa la cola de peticiones, el socket y el número del cliente que va a utilizar ese hilo */
	public void generadorHilosClientes() {

		ServerSocket socketServidor;
		try {
			socketServidor = new ServerSocket(PUERTO_CLIENTE);
			int numeroDeCliente = 0;
			while (numeroDeCliente <= MAXIMO_CLIENTES) {
				Socket cliente = socketServidor.accept();
				numeroDeCliente++;
				hiloCliente.add(new HiloCliente(colaPeticion, cliente, numeroDeCliente));
				Thread hilo = new Thread(hiloCliente.get(numeroDeCliente-1));
				hilo.start();
			}
		} catch (IOException e) {
			System.out.println("Error" + e.getMessage());
		}
	}
	
	/*Se genera Hilos para cada Replica utilizando el mismo socket del serversocket
	 * Cada hilo es amalcenado en el arrayList y se rea un hilo unico con ese array de hilos para luego iniciarlo
	 * Cada hilo creado se le pasa la cola de Respuestas, el socket y el número de Replica.
	 * */
	public void generadorHilosReplica() {

		ServerSocket socketServidor;
		try {
			socketServidor = new ServerSocket(PUERTO_REPLICA);
			int numeroDeCliente = 0;
			while (numeroDeCliente <= MAXIMO_REPLICAS) {
				Socket cliente = socketServidor.accept();
				numeroDeCliente++;
				hiloReplica.add(new HiloReplica(colaRespuesta, cliente, numeroDeCliente));
				Thread hilo = new Thread(hiloReplica.get(numeroDeCliente-1));
				hilo.start();
			}
		} catch (IOException e) {
			System.out.println("Error" + e.getMessage());
		}
	}
	/*metodo que envia las respuesta a los clientes
	 * Envia la respuesta que esta en la cabeza de la cola.
	 * Del mensaje se extrae el id del cliente al que hay que enviarlo.
	 * Y el hilo se encarga de enviarlo.
	 * */
	public void envioRespuestaCliente() {
		boolean salir = false;
		while (!salir) {
			String respuesta;
			do {
				respuesta = this.colaRespuesta.poll();//					
			} while (respuesta==null);
			int nCliente = Integer.parseInt(respuesta.split("-")[0]);
			for (HiloCliente hilo : this.hiloCliente) {
				if (nCliente == hilo.id) {
					hilo.enviarMensaje(respuesta);
					System.out.println("Enviando rpta a Cliente " + nCliente + ":"+respuesta);
					break;
				}
			}
		}
	}
	
	/*metodo que envia la peticion a la replica.
	 * envia la peticion que se encuentra en la cabeza de la cola.
	 * Envia un petición que no halla Finalizado.
	 * */
	public void enviarPeticionReplica() {
		String mensaje = null;
		boolean salir = false;
		int contador_final = 0;
		String tipoOperacion = " ";
		try {
			while (!salir) {
				mensaje = this.colaPeticion.take();// SACA UN ELEMENTO DE LA COLA
				
				if(mensaje.equals("FINALIZO")) {
					contador_final++;
					if(contador_final==this.hiloCliente.size()) {
						for (HiloReplica hilo : hiloReplica) {
							hilo.enviarMensaje("FINALIZO");
							System.out.println("Mensaje a replica "+ hilo.id + " : FINALIZO");
						}break;
					}
					continue;
				}
				tipoOperacion = mensaje.split("-")[1];// tipo de operacion
				if (tipoOperacion.equals("L")) {
					enviarPeticionLectura(mensaje);
					//recibirRespuestaReplica();
				}
				if (tipoOperacion.equals("A")) {
					enviarPeticionActualizacion(mensaje);
					recibirRespuestaReplica();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* Peticion Lectura 
	 * Busca un hilo Libre y que este desbloqueado y le envia el mensaje
	 * */
	private void enviarPeticionLectura(String mensaje) {
		boolean salir = false;
		while(!salir) {
			for (HiloReplica hilo : this.hiloReplica) {
				if (hilo.estado.equals("LIBRE") && hilo.bloqueo.equals("DESBLOQUEADO")) {
					hilo.estado = "OCUPADO";
					hilo.enviarMensaje(mensaje);
					System.out.println("Enviando peticion a R"+hilo.id + ": "+mensaje);
					salir = true;
					break;
				}
			}
		}
	}
	/*Recibe la Respuesta de la Replica. Cuando todos las replicas estan libres
	 * se agrega la respuesta a la cola de respuestas.
	 * 
	 * */
	private void recibirRespuestaReplica() throws InterruptedException {
		//recibe respuesta de replica
		boolean todos_libres = false;
		int contador = 0;
		while (!todos_libres) {
			for (HiloReplica hilo : this.hiloReplica) {
				if (!hilo.respuesta.equals("-")) //todos los hilos tienen almacenado una respuesta
				{
					contador++;
				}
					
			}
			if (contador == hiloReplica.size()) {
				todos_libres = true;
			}
			contador = 0;
		}
		agregarAColaRespuesta();
		// resetea la respuesta de todos los hilos
		for (HiloReplica hilo : this.hiloReplica) {
			hilo.respuesta = "-";
		}
		//libera todos los hilos y los desbloquea.
		for (HiloReplica hilo : this.hiloReplica) {
			hilo.estado = "LIBRE";
			hilo.bloqueo = "DESBLOQUEADO";
		}
		
	}
	/*agrega la respuesta obtenida a la cola para posterioremente enviar al cliente.*/
	private void agregarAColaRespuesta() throws InterruptedException {
		// Verificando si los datos estan corruptos
		boolean corrupcionDatos = false;
		String aux = this.hiloReplica.get(0).respuesta;
		for (HiloReplica hilo : this.hiloReplica) {
			if (!aux.equals(hilo.respuesta))
				corrupcionDatos = true;
		}

		if (corrupcionDatos) {
			this.colaRespuesta.put(aux.split(";")[0] + ";" + "DATOS CORRUPTOS");			
		}
		else {
			// ALMACENA SOLICITUD EN COLA_PETICIONES
			this.colaRespuesta.put(hiloReplica.get(0).respuesta); 
		}

	}
	/*Peticion de Actualizacion
	 * Busca que todos esten libres y desbloqueados para poder actualizar.
	 * Actualiza todos a la vez.
	 * */
	private void enviarPeticionActualizacion(String mensaje) {
		boolean todos_libres = false;
		int contador = 0;
		while (!todos_libres) {
			for (HiloReplica hilo : this.hiloReplica) {
				if (hilo.estado.equals("LIBRE") && hilo.bloqueo.equals("DESBLOQUEADO")) {
					contador++;
				}
			}
			if (contador == hiloReplica.size()) {
				todos_libres = true;
			}
			contador = 0;
		}
		
		for (HiloReplica hilo : this.hiloReplica) {
			hilo.enviarMensaje(mensaje);
			hilo.estado = "OCUPADO";
			hilo.bloqueo = "BLOQUEADO";
			System.out.println("Enviando peticion a R "+hilo.id+": "+mensaje);
		}
		
	}


}
