package LaboratorioN01;

import java.util.Random;

public class PrincipalMultiplicacionMatriz {
	static final int L = 2000;
	static final int h = 8;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long inicioTiempoSerie;
		long finalTiempoSerie;
		
		int[][] matrizA = new int[L][L];
		int[][] matrizB = new int[L][L];
		int[][] matrizResultado = new int[L][L];
		
		//auxiliares
		int[][] copiaMatrizA = new int[L][L];
		int[][] copiaMatrizB = new int[L][L];

		Thread[] hilos = new Thread[h];
		
		///-------------
		matrizAleatoria(matrizA, 18);
//		System.out.println("Matriz A:");
//		imprimirMatriz(matrizA);
//		System.out.println();
		
		matrizAleatoria(matrizB, 14);
//		System.out.println("Matriz B:");
//		imprimirMatriz(matrizB);
//		System.out.println();
		
		//-----------
		inicioTiempoSerie = System.currentTimeMillis();
		int longitud = L/h;
		for (int i = 0; i < h; i++) {
//			System.arraycopy(matrizA, 0, copiaMatrizA, 0, L*L);
//			System.arraycopy(matrizA, 0, copiaMatrizB, 0, L*L);
			hilos[i] = new HiloMatriz(matrizA, matrizB, matrizResultado, i*longitud, i*longitud + longitud-1);
			hilos[i].start();
		}

		try {
			
			for (int i = 0; i < h; i++)
			{
				hilos[i].join();
				//System.out.println("entro "+i);
			}
			
		} catch (InterruptedException e) {
			System.out.println("Hilo interrumpido");
		}
		
		finalTiempoSerie = System.currentTimeMillis();
		
//		System.out.println("matriz A:");
//		imprimirMatriz(matrizA);
//		System.out.println();
//		
//		System.out.println("matriz B:");
//		imprimirMatriz(matrizB);
//		System.out.println();
//		
		
		
		System.out.printf("Tiempo en Serie: %6f s \n",((double)(finalTiempoSerie - inicioTiempoSerie))/1000);
		
//		System.out.println("Matriz Resultante:");
//		imprimirMatriz(matrizResultado);
		
		
	}

	public static void imprimirMatriz(int[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz[0].length; j++) {
				System.out.print(matriz[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void matrizAleatoria(int[][] arreglo, int semilla) {
		Random numeroAletorio = new Random(semilla);
		for (int i = 0; i < arreglo.length; i++) {
			for (int j = 0; j < arreglo[0].length; j++) {
				arreglo[i][j] = numeroAletorio.nextInt(9)+1;
			}
		}
	}

}
