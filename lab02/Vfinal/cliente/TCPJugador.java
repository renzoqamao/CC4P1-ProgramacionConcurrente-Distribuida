package Laboratorio.Lab02.V3_4.cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPJugador {

	private static final String INICIA_JUEGO_SERVIDOR = "INICIO";
	private static final String CLIENTE_ACEPTA_JUEGO = "OK";
	private static final String ES_UN_CLIENTE = "CLIENTE";
	private static final String ID_CLIENTE = "ID";
	private static final String GAME_OVER = "PERDI";
	private static final String GANADOR = "GANE";
	private String ip;
	private int puerto;
	private BufferedReader in;
	private PrintWriter mOut;

	private Mapa mapa;
	// CAMBIO: AGREGE ID CLIENTE
	private int id;
	
	//ULTIMO CAMBO---------------------
	private String estadoJuego = "";
	//---------------------------

	public TCPJugador(String ip, int puerto) {
		this.ip = ip;
		this.puerto = puerto;
	}

	public void run() {
		InetAddress direccionServidor;
		Socket socket;
		try {
			direccionServidor = InetAddress.getByName(ip);
			socket = new Socket(direccionServidor, puerto);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

			recibeJugadoresCreados(); // inicio del juego
			recibeMensajeJugadores();
			
			//ULTIMO CAMBO---------------------
				//fin de juego
			if(this.estadoJuego.equals(GAME_OVER))
				System.out.println("PERDI JUEGO");
			else if(this.estadoJuego.equals(GANADOR))
				System.out.println("GANE JUEGO");
			//------------------------------------
			
			
		} catch (Exception e) {
			System.out.println("TCP Jugador " + ": Error " + e);
		}

	}

	// Recibe jugadores hasta que el servidor inicia la partida
	private void recibeJugadoresCreados() throws IOException {
		String mensaje = "";
		String numeroJugadores = "0";
		while (!mensaje.equals(INICIA_JUEGO_SERVIDOR)) {
			mensaje = in.readLine();

			// CAMBIO: AGREGE ID DEL PROPIO CLIENTE
			if (mensaje.contains(ID_CLIENTE)) {
				String arregloMensaje[] = mensaje.split("\\s+");
				this.id = Integer.parseInt(arregloMensaje[1]);
			}

			if (mensaje.contains(ES_UN_CLIENTE)) {
				String arregloMensaje[] = mensaje.split("\\s+");
				numeroJugadores = arregloMensaje[1];
			}
			if (mensaje.contains(INICIA_JUEGO_SERVIDOR)) {
				enviarMensaje(CLIENTE_ACEPTA_JUEGO);	
				//ULTIMO CAMBO---------------------
				mapa = new Mapa(this.id, this.mOut);// se crea el mapa al instanciar
				ingresaJugadores(numeroJugadores);
				//---------------------------------
			}
		}
		
	}
	//ULTIMO CAMBO---------------------
	private void recibeMensajeJugadores() throws IOException {
		String mensaje = ""; // forma: "id"+" "+"movimiento"
		
		while (!mensaje.equals(GAME_OVER) && !mensaje.equals(GANADOR)) { 
			mensaje = in.readLine();
			String mensajeRecibido[] = mensaje.split("\\s+");
			if (mensajeRecibido.length >= 2) {
				modificandoEscenario(mensaje);

			}
		}
		this.estadoJuego = mensaje;
	}
	//------------------------------------------

	// LOGICA DEL JUEGO---------------------------------------------
	public void ingresaJugadores(String mensaje) {
		int idCliente = Integer.parseInt(mensaje);
		mapa.insertar_jugador(idCliente);
		mapa.actualizar_mapa();
		System.out.println();
		System.out.println();
		

	}
	

	// CAMBIO
	public void modificandoEscenario(String mensaje) {
		String mensajeCliente[] = mensaje.split("\\s+");
		int idCliente = Integer.parseInt(mensajeCliente[0]) - 1;
		String movimiento = mensajeCliente[1];

		// MENSAJE QUE RECIBE: idCliente movimiento
		movimientoJugador(idCliente, movimiento);

		// MENSAJE QUE ENVIA: idCliente bomba estado
		if ("e".contains(movimiento.toLowerCase()) && this.id == idCliente + 1) {
			jugadorEnviaEstadoBomba(movimiento);
		}

		// MENSAJE QUE RECIBE: id_cliente bomba estado
		if (mensajeCliente.length == 3) {
			String bomba = mensajeCliente[1];
			String estado = mensajeCliente[2];
			recibeEstadoBomba(idCliente, bomba, estado);
		}

	}

	private void movimientoJugador(int idCliente, String movimiento) {
		int x = mapa.jugadores[idCliente].x;
		int y = mapa.jugadores[idCliente].y;
		// movimiento de un jugador
		switch (movimiento.toLowerCase()) {
		case "w":
			if (x - 1 >= 0 && !mapa.pared(x - 1, y)) {
				mapa.borrarPosicion(idCliente);
				mapa.jugadores[idCliente].arriba();
			}
			break;
		case "s":
			if (x + 1 < mapa.matriz[0].length && !mapa.pared(x + 1, y)) {
				mapa.borrarPosicion(idCliente);
				mapa.jugadores[idCliente].abajo();
			}
			break;
		case "d":
			if (y + 1 < mapa.matriz.length && !mapa.pared(x, y + 1)) {
				mapa.borrarPosicion(idCliente);
				mapa.jugadores[idCliente].derecha();
			}
			break;
		case "a":
			if (y - 1 >= 0 && !mapa.pared(x, y - 1)) {
				mapa.borrarPosicion(idCliente);
				mapa.jugadores[idCliente].izquierda();
			}
			break;
		}
		if ("wsda".contains(movimiento.toLowerCase()))
			mapa.actualizar_mapa();
	}

	private void jugadorEnviaEstadoBomba(String movimiento) {
		new Thread(new Runnable() {
			public void run() {
				int tiempoCarga = 3;
				int tiempoExplocion = 2;
				try {
					enviarMensaje("bomba " + "cargando");
					Thread.sleep(tiempoCarga * 1000);
					enviarMensaje("bomba " + "inicioExplocion");
					Thread.sleep(tiempoExplocion * 1000);
					enviarMensaje("bomba " + "finExplocion");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void recibeEstadoBomba(int idJugadorConBomba, String bomba, String estado) {
		if (bomba.equals("bomba")) {
			if (estado.equals("cargando")) {
				mapa.jugadores[idJugadorConBomba].bomba.estado = "cargando";
//				mapa.jugadores[idCliente].colocarbomba();
//				mapa.actualizar_mapa();
			} else if (estado.equals("inicioExplocion")) {
				mapa.jugadores[idJugadorConBomba].bomba.estado = "inicioExplocion";
				// mapa.ataques(id);
			} else if (estado.equals("finExplocion")) {
				mapa.jugadores[idJugadorConBomba].bomba.estado = "finExplocion";
			}
			mapa.actualizar_mapa();
		}
	}

	////// -----------------
	public  void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}

}
