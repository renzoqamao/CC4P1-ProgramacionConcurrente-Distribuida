package Laboratorio.Lab02.V3_4.cliente;

public class Bomba {
	int x;
	int y;

	// boolean explota = false;
	// boolean cargando = false;
	char simbolo;

	// Estados de la bomba: {desactivado, cargando, inicioExplocion, finExplocion}
	String estado = "desactivado";

	public Bomba(char simbolo) {
		// this.explota = false;
		this.simbolo = simbolo;
	}
	/*
	 * public Bomba(int x, int y,char simbolo){ this.x = x; this.y = y; this.explota
	 * = false; this.simbolo=simbolo; }
	 */

	// la bomba explota despues de timer seg
//	public void contador_explota(int timer) {
//		// timer es el número de segundos
//		try {
//			this.cargando = true;
//			Thread.sleep(timer*1000);
//			this.explota = true;
//			this.cargando = false;
//			Thread.sleep((timer-2)*1000);
//		} catch (InterruptedException e) {
//			// TODO Bloque catch generado automáticamente
//			e.printStackTrace();
//		}	
//	}

}
