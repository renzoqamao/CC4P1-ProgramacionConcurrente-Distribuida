package LaboratorioN02;

public class HiloJacobi extends Thread {
	private double[][] A;
	private double[] b;
	private double[] X;
	private double[] nuevoX;
	private int inicioFila;
	private int finalFila;
	
	public HiloJacobi(double[][] A, double[] X, double[] b, double[]nuevoX, int inicioFila, int finalFila) {
		this.A = A;
		this.b = b;
		this.X = X;
		this.nuevoX = nuevoX;
		this.inicioFila = inicioFila;
		this.finalFila = finalFila;
	}
	public void run() {
		double a ;
		for (int i = this.inicioFila; i<=this.finalFila; i++) {
			a = 0;
			for (int j = 0; j < A.length; j++) {
				if(i!=j) 
					a+= A[i][j]*X[j];
			}
			nuevoX[i] = (b[i]-a)/A[i][i];
		}
//		System.out.println("hilos");
	}
}
