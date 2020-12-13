package LaboratorioN01;

class HiloMatriz extends Thread {
	private int matrizA[][];
	private int matrizB[][];
	private int matrizResultado[][];
	private int inicioFila;
	private int finalFila;

	public HiloMatriz(int[][] matrizA, int[][] matrizB, int[][] matrizResultado, int inicioFila, int finalFila) {
		
		this.matrizA = matrizA;
		this.matrizB = matrizB;
		this.matrizResultado = matrizResultado;
		this.inicioFila = inicioFila;
		this.finalFila = finalFila;

	}
	

	public void run()
	{
		for (int i = this.inicioFila; i <= this.finalFila; i++) {
			for (int j = 0; j < matrizResultado.length; j++) {
				for (int k = 0; k < matrizResultado.length; k++) {
					matrizResultado[i][j] += matrizA[i][k] * matrizB[k][j];
				}
			}
		}
		
		System.out.println("paso "+ this.inicioFila);
	}
}