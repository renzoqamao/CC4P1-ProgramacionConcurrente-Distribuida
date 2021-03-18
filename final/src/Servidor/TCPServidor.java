package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPServidor {

	public int id;
	public int maxCliente;
	public String estado;
	public ArrayList<String> direcciones = new ArrayList<String>();

	private static final int PUERTO_CLIENTE = 2222;
	private static final int MAXIMO_CLIENTES = 10;
	private static final int PUERTO_REPLICA = 3333;
	private static final int MAXIMO_REPLICAS = 10;

	public ArrayList<HiloCliente> hiloCliente = new ArrayList<HiloCliente>();
	public ArrayList<HiloReplica> hiloReplica = new ArrayList<HiloReplica>();
	
	public BlockingQueue<String> colaPeticion = new LinkedBlockingQueue<String>();
	public BlockingQueue<String> colaRespuesta = new LinkedBlockingQueue<String>();


	public TCPServidor() {
		this.id = 0;
		this.estado = "DESCONECTADO";
		this.maxCliente = 0;
	}

	public void run() {
		agregarDirecciones();
		eleccionLider();
		
	}

	private void eleccionLider() {
		int contador =0;
		int limite = this.direcciones.size();
		while(contador<limite) {
			modoReplica(this.direcciones.get(contador)); // BUSCO SERVIDOR LIBER
			contador++;
			if(this.estado.equals("CONECTADO")) {
				limite = this.id-1;
				contador=0;
				this.estado = "DESCONECTADO";
			}
		}
		System.out.println("voy a ser un servidor");
		modoServidor();// SOY EL SERVIDOR LIDER
	}
	
	public void agregarDirecciones() {
		direcciones.add("192.168.1.101:3333");
		direcciones.add("192.168.1.102:3333");
		direcciones.add("192.168.1.103:3333");
		direcciones.add("192.168.1.104:3333");
	}

	public void modoServidor() {
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

	public void modoReplica(String direccion) {

		String ip = direccion.split(":")[0];
		int puerto = Integer.parseInt(direccion.split(":")[1]);
		System.out.println("ip: "+ip+" puerto: "+puerto);
		HiloModoReplica replica = new HiloModoReplica(this, ip, puerto);
		replica.run();
	}

	public void generadorHilosClientes() {

		ServerSocket socketServidor;
		try {
			socketServidor = new ServerSocket(PUERTO_CLIENTE);
			int numeroDeCliente = 0;
			while (numeroDeCliente <= MAXIMO_CLIENTES) {
				Socket cliente = socketServidor.accept();
				numeroDeCliente++;
				hiloCliente.add(new HiloCliente(this, cliente, numeroDeCliente));
				Thread hilo = new Thread(hiloCliente.get(numeroDeCliente - 1));
				System.out.println("cliente "+numeroDeCliente);
				hilo.start();
				
			}
		} catch (IOException e) {
			System.out.println("Error de hilos de Clientes" + e.getMessage());
		}
	}

	public void generadorHilosReplica() {

		ServerSocket socketServidor;
		try {
			socketServidor = new ServerSocket(PUERTO_REPLICA);
			int numeroReplica = 0;// id del lider
			while (numeroReplica <= MAXIMO_REPLICAS) {
				Socket replica = socketServidor.accept();
				numeroReplica++;
				hiloReplica.add(new HiloReplica(this, replica, replica.getInetAddress().toString().split("/")[1] ));
				Thread hilo = new Thread(hiloReplica.get(numeroReplica - 1 ));
				hilo.start();
			}
		} catch (IOException e) {
			System.out.println("Error de hilos de Replica" + e.getMessage());
		}
	}

	public void envioRespuestaCliente() {
		boolean salir = false;
		while (!salir) {
			String respuesta;
			do {
				respuesta = this.colaRespuesta.poll();//					
			} while (respuesta==null);
			int nCliente = (int ) Integer.parseInt(respuesta.split(";")[0]);
			for (HiloCliente h : hiloCliente) {
				if(h.idCliente==nCliente) {
					h.enviarMensaje(respuesta);
					System.out.println("Enviando rpta a Cliente " + nCliente + ":"+respuesta);
					break;
				}
			}
		}
	}
	
	public void enviarPeticionReplica() {
		String mensaje = null;
		boolean salir = false;
		Random r = new Random();
		int num_rand;
		try {
			while (!salir) {
				Thread.sleep(1); 
				if(hiloReplica.size()>0) {
					num_rand = r.nextInt(hiloReplica.size());
					mensaje = this.colaPeticion.take();// SACA UN ELEMENTO DE LA COLA
					this.hiloReplica.get(num_rand).enviarMensaje(mensaje);
					System.out.println("Enviando peticion a R " + num_rand+": "+ mensaje);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
