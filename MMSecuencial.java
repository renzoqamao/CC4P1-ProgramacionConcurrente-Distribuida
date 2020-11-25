package Secuencial;

import java.util.Random;

public class MMSecuencial {
	static final int L = 500;
	public static void main(String[] Args) {
		
		long inicioTiempoSerie;
		long finalTiempoSerie;
		
		int[][] matrizA = new int[L][L];
		int[][] matrizB = new int[L][L];
		int[][] matrizC = new int[L][L];

		matrizAleatoria(matrizA, 18);
		matrizAleatoria(matrizB, 14);
		
		inicioTiempoSerie = System.currentTimeMillis();
		for (int i = 0; i < L; i++) {
            for (int j = 0; j < L; j++) {
                for (int k = 0; k < L; k++) {
                    matrizC[i][j] += matrizA[i][k] * matrizB[k][j];
                }
            }
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
//		System.out.println("matriz final:");
//		imprimirMatriz(matrizC);
		System.out.printf("Tiempo en Serie: %s s \n",((double)(finalTiempoSerie - inicioTiempoSerie))/1000);
		

	}

	public static void imprimirMatriz(int[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz[0].length; j++) {
				System.out.print(matriz[i][j]+" ");
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
