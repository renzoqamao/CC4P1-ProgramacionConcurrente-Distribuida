package Laboratorio.Lab02.V3_4.cliente;

public class Bomberman {
	int x;
	int y;
	char simbolo;
	int id;
	Bomba bomba;

	String estado;
	boolean sueltaBomba;

	public Bomberman(int x, int y, char simbolo, char bomba_simbolo) {
		this.x = x;
		this.y = y;
		this.simbolo = simbolo;
		this.bomba = new Bomba(bomba_simbolo);
		this.estado = "vivo";
		this.sueltaBomba = false;
	}

	public void izquierda() {
		this.y = this.y - 1;
	}

	public void derecha() {
		this.y = this.y + 1;
	}

	public void arriba() {
		this.x = this.x - 1;
	}

	public void abajo() {
		this.x = this.x + 1;
	}

	/* el bomberman muere */
	public void morir() {
		// this.simbolo=' ';
		this.estado = "muerto";
	}

	/* permite colocar bombas y explotan 5 seg despues y la recarga es 5 es seg */
	public void colocarbomba() {
		this.bomba.x = this.x;
		this.bomba.y = this.y;
		this.sueltaBomba = true;
		// this.bomba.contador_explota(5);
		// this.bomba.explota=false;

	}

}
