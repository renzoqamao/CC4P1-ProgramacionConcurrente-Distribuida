package Laboratorio.Lab02.V3_4.cliente;

import java.io.PrintWriter;
import java.util.Random;

public class Mapa {
	public static final int FILA = 10;
	public static final int COLUMNA = 10;
	char matriz[][] = new char[FILA][COLUMNA];
	public Bomberman jugadores[] = new Bomberman[4];

	// CAMBIO:POSICIONES INICIALES DE LOS JUGADORES
	int pX[] = { 0, 0, FILA - 1, FILA - 1, };
	int pY[] = { 0, COLUMNA - 1, 0, COLUMNA - 1 };

	char simbolos[] = { 'A', 'B', 'C', 'D', 'E' };
	char simbolos_bomba[] = { '1', '2', '3', '4' };

	//ULTIMO CAMBIO--------------
	PrintWriter mOut;
	int idCliente;

	Mapa(int idCliente, PrintWriter mOut) {
		this.mOut = mOut;
		this.idCliente = idCliente;
		crear_mapa();
	}
	//---------------------------

	/*
	 * Crear el mapa donde la posición de cada jugador son las esquinas y evita que
	 * sean muros y genera un pared(#) aleatoriamente
	 */
	public void crear_mapa() {
		Random r = new Random(4234);
		int num_rand;
		for (int x = 0; x < matriz.length; x++) {
			for (int y = 0; y < matriz[x].length; y++) {
				if (x == 0 & y == 0) {
					matriz[x][y] = ' ';
				} else if (x == 0 && y == matriz[x].length - 1) {
					matriz[x][y] = ' ';
				} else if (x == matriz.length - 1 & y == 0) {
					matriz[x][y] = ' ';
				} else if (x == matriz.length - 1 & y == matriz[x].length - 1) {
					matriz[x][y] = ' ';
				} else {
					// CAMBIO: TODOS LOS JUGADORES TENGA EL MISMO MAPA
					num_rand = r.nextInt(matriz.length) + 1;
					if (num_rand % 2 == 0) {
						matriz[x][y] = '#';
					} else if (num_rand % 3 == 0) {
						matriz[x][y] = '|';
					} else {
						matriz[x][y] = ' ';
					}
				}

			}
		}
	}

	// refresca el mapa
	public void actualizar_mapa() {
		// crear_mapa(); //CAMBIO
		llenar_bomberman();
		// llenar_bombas();
		accionesBombas();
		mostrar();
		System.out.println();
		System.out.println();

	}

	/* dibuja a los jugadores en la matriz en cualquier posición */
	public void llenar_bomberman() {
		int pos_x;
		int pos_y;
		int iterator = 0;
		while (iterator < jugadores.length && jugadores[iterator] != null) {
			if (jugadores[iterator].estado.equals("vivo")) {
				pos_x = jugadores[iterator].x;
				pos_y = jugadores[iterator].y;
				matriz[pos_x][pos_y] = jugadores[iterator].simbolo;
			}
			iterator++;
		}
	}

	// muestra las ACCIONES DE LA BOMBAS EN SUS DIFERENTES ESTADOS
	public void accionesBombas() {
		int posX;
		int posY;
		String estado;
		int i;
		for (i = 0; i < jugadores.length && jugadores[i] != null; i++) {
			estado = jugadores[i].bomba.estado;
			switch (estado) {
			case "cargando":
				if (!jugadores[i].sueltaBomba) {
					jugadores[i].colocarbomba();
					posX = jugadores[i].bomba.x;
					posY = jugadores[i].bomba.y;
					matriz[posX][posY] = jugadores[i].bomba.simbolo;
				}
				break;
			case "inicioExplocion":
				posX = jugadores[i].bomba.x;
				posY = jugadores[i].bomba.y;
				inicioExplocion(i, posX, posY);
				break;
			case "finExplocion":
				posX = jugadores[i].bomba.x;
				posY = jugadores[i].bomba.y;
				finExplocion(i, posX, posY);
				jugadores[i].bomba.estado = "desactivado";
				jugadores[i].sueltaBomba = false;
				break;
			}
		}
	}

	public void inicioExplocion(int id, int pos_x, int pos_y) {
		int arriba = pos_x - 1;
		int abajo = pos_x + 1;
		int izquierda = pos_y - 1;
		int derecha = pos_y + 1;

		// verifica si alcanza a un jugador
		bombaAlcanzaJugador(pos_x, pos_y, arriba, abajo, izquierda, derecha);

		// si el lugar esta libre
		lugarLibre(pos_x, pos_y, arriba, abajo, izquierda, derecha);

		// bomba alcanza pared rompible
		paredRomplible(pos_x, pos_y, arriba, abajo, izquierda, derecha);

	}

	//ULTIMO CAMBO---------------------
	private void bombaAlcanzaJugador(int pos_x, int pos_y, int arriba, int abajo, int izquierda, int derecha) {
		int i;
		for (i = 0; i < jugadores.length && jugadores[i] != null; i++) {
			if (jugadores[i].estado.equals("vivo")) {
				if (jugadores[i].x == pos_x && jugadores[i].y == pos_y) {
					jugadores[i].morir();
					matriz[pos_x][pos_y] = '%';
					enviarMensaje(i, jugadores[i].estado.toUpperCase());//-------------------------------
				}
				if (arriba >= 0 && arriba == jugadores[i].x && pos_y == jugadores[i].y) {
					jugadores[i].morir();
					matriz[arriba][pos_y] = '%';
					enviarMensaje(i, jugadores[i].estado.toUpperCase());//---------------------------
				}
				if (abajo < matriz.length && abajo == jugadores[i].x && pos_y == jugadores[i].y) {
					jugadores[i].morir();
					matriz[abajo][pos_y] = '%';
					enviarMensaje(i, jugadores[i].estado.toUpperCase());//---------------------------
				}
				if (izquierda >= 0 && pos_x == jugadores[i].x && izquierda == jugadores[i].y) {
					jugadores[i].morir();
					matriz[pos_x][izquierda] = '%';
					enviarMensaje(i, jugadores[i].estado.toUpperCase());//--------------------------
				}
				if (derecha < matriz[0].length && pos_x == jugadores[i].x && derecha == jugadores[i].y) {
					jugadores[i].morir();
					matriz[pos_x][derecha] = '%';
					enviarMensaje(i, jugadores[i].estado.toUpperCase());//---------------------------
				}
			}

		}

	}
	//---------------------------------------------------------
	
	//ULTIMO CAMBO---------------------
	public void enviarMensaje(int i, String mensaje) {
		if(i+1==this.idCliente && this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}
	
	//-----------------------------------

	private void lugarLibre(int pos_x, int pos_y, int arriba, int abajo, int izquierda, int derecha) {
		matriz[pos_x][pos_y] = '%';

		if (arriba >= 0 && matriz[arriba][pos_y] == ' ') {
			matriz[arriba][pos_y] = '%';
		}
		if (abajo < matriz.length && matriz[abajo][pos_y] == ' ') {
			matriz[abajo][pos_y] = '%';
		}
		if (izquierda >= 0 && matriz[pos_x][izquierda] == ' ') {
			matriz[pos_x][izquierda] = '%';
		}
		if (derecha < matriz[0].length && matriz[pos_x][derecha] == ' ') {
			matriz[pos_x][derecha] = '%';
		}
	}

	private void paredRomplible(int pos_x, int pos_y, int arriba, int abajo, int izquierda, int derecha) {

		if (arriba >= 0 && matriz[arriba][pos_y] == '#') {
			matriz[arriba][pos_y] = ' ';
		}
		if (abajo < matriz.length && matriz[abajo][pos_y] == '#') {
			matriz[abajo][pos_y] = ' ';
		}
		if (izquierda >= 0 && matriz[pos_x][izquierda] == '#') {
			matriz[pos_x][izquierda] = ' ';
		}
		if (derecha < matriz[0].length && matriz[pos_x][derecha] == '#') {
			matriz[pos_x][derecha] = ' ';
		}
	}

	public void finExplocion(int idCliente, int pos_x, int pos_y) {
		int arriba = pos_x - 1;
		int abajo = pos_x + 1;
		int izquierda = pos_y - 1;
		int derecha = pos_y + 1;

		// si el lugar esta libre
		matriz[pos_x][pos_y] = ' ';

		if (matriz[pos_x][pos_y] == '%' && matriz[pos_x][pos_y] == jugadores[idCliente].bomba.simbolo) {
			matriz[pos_x][pos_y] = ' ';
		}
		if (arriba >= 0 && matriz[arriba][pos_y] == '%') {
			matriz[arriba][pos_y] = ' ';
		}
		if (abajo < matriz.length && matriz[abajo][pos_y] == '%') {
			matriz[abajo][pos_y] = ' ';
		}
		if (izquierda >= 0 && matriz[pos_x][izquierda] == '%') {
			matriz[pos_x][izquierda] = ' ';
		}
		if (derecha < matriz[0].length && matriz[pos_x][derecha] == '%') {
			matriz[pos_x][derecha] = ' ';
		}
	}

	// muestra el mapa
	public void mostrar() {
		for (int x = 0; x < matriz.length; x++) {
			for (int y = 0; y < matriz[x].length; y++) {
				System.out.print(matriz[x][y]);
			}
			System.out.println("");
		}
	}

	// CAMBIO: BORRAR LA POSICION ACTUAL DEL JUGADOR AL MOVERSE
	public void borrarPosicion(int idJugador) {
		int pxJugador = jugadores[idJugador].x;
		int pyJugador = jugadores[idJugador].y;

		boolean hayBomba = false;
		for (int i = 0; i < simbolos_bomba.length; i++) {
			if (matriz[pxJugador][pyJugador] == simbolos_bomba[i]) {
				hayBomba = true;
				break;
			}
		}

		if (!hayBomba) {
			matriz[pxJugador][pyJugador] = ' ';
		}
	}

	// colision
	public boolean pared(int x, int y) {
		if (matriz[x][y] != '#' && matriz[x][y] != '|') {
			return false;
		} else {
			return true;
		}
	}

	// CAMBIO: AL INICIAR LA PARTIDA SE CREAN LOS JUGADORES=BOMBERMAN
	public void insertar_jugador(int id) {
		for (int i = 0; i < id; i++) {
			jugadores[i] = new Bomberman(pX[i], pY[i], simbolos[i], simbolos_bomba[i]);
		}
	}
	

//	// ataques a un jugador con id tal.
//	public void ataques(int id) {
//		int i = 0, j = 0;
//		int pos_x, pos_y;
//		while (i < jugadores.length && jugadores[i] != null) {
//			while (j < jugadores.length && jugadores[i].bomba.explota != false) {
//				pos_x = jugadores[i].bomba.x;
//				pos_y = jugadores[i].bomba.y;
//				int arriba = pos_x - 1;
//				int abajo = pos_x + 1;
//				int izquierda = pos_y - 1;
//				int derecha = pos_y + 1;
//				// la bomba revienta arriba,abajo,derecha e izquierda
//				if (arriba == jugadores[id].x && pos_y == jugadores[id].y) {
//					jugadores[id].morir();
//				} else if (abajo == jugadores[id].x && pos_y == jugadores[id].y) {
//					jugadores[id].morir();
//				} else if (pos_x == jugadores[id].x && izquierda == jugadores[id].y) {
//					jugadores[id].morir();
//				} else if (pos_x == jugadores[id].x && derecha == jugadores[id].y) {
//					jugadores[id].morir();
//				} else {
//					System.out.println("no haz muerto");
//				}
//				j++;
//			}
//			j = 0;
//			i++;
//		}
//	}

}

//	// muestra las bombas que han sido activadas
//	public void llenar_bombas() {
//		int pos_x;
//		int pos_y;
//		int iterator = 0;
//		while (iterator < jugadores.length && jugadores[iterator] != null
//				&& jugadores[iterator].bomba.cargando != false) {
//			pos_x = jugadores[iterator].x;
//			pos_y = jugadores[iterator].y;
//			matriz[pos_x][pos_y] = jugadores[iterator].bomba.simbolo;
//			iterator++;
//		}
//	}

// crear e insertar un jugador en el mapa inicio del juego x,y depende del id.
//	public void insertar_jugador( int id) {
//		for (int i = 0; i < id; i++) {
//			jugadores[i] = new Bomberman(x, y, simbolos[id], simbolos_bomba[id]);			
//		}
//	}