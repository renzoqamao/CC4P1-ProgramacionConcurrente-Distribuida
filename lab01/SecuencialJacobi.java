package Secuencial;

import java.util.Random;

public class SecuencialJacobi {
//	static final int L = 6000;
	static final int L = 1000;
	
	public static void main(String[] args) {
		long inicioTiempo;
		long finalTiempo;
		
		double error = 0.009;
		boolean convergencia;

		double[][] A = new double[L][L];
		double[] X = new double [L];
		double[] b = new double[L];
		
		
//		//Iniciando 
		matrizDoble(A, 10);
		matrizLineal(X, 11);
		matrizLineal(b, 12);
		
		
		
		//COMPRUEBA SI RESUELVE CORRECTAMENTE
//		double[][] A = new double[][]{
//									{4, 1, 1, 1},
//									{2, 5, 0, -1},
//									{3, 2, 6, 0},
//									{2, 1, 1, 7}};
//		double[] X = new double[] {1,2, 3, 15};
//		double[] b = new double[] {8, 11, 15, 9};
		
		
		
		
		inicioTiempo = System.currentTimeMillis();
		convergencia= jacobi(A, X, b, error);
		finalTiempo = System.currentTimeMillis();
		
		if(convergencia==true)
			System.out.printf("Tiempo en Secuencial: %6f s \n",((double)(finalTiempo - inicioTiempo))/1000);
		else
			System.out.println("Diverge");;
		
		//MUESTRA X
		/*System.out.println("X:");
		for (int j = 0; j < X.length; j++) {
			System.out.println(X[j] + " ");
		}*/
		System.out.println();
		System.out.println("fin");

	}
	
	
	public static boolean jacobi(double[][] A, double[] X, double[] b, double error) {
		double[] nuevoX = new double[X.length];
		double a ;
		int contador = 0;
		 do {
			 contador++;
			 if(contador!=1)
				 System.arraycopy(nuevoX, 0, X, 0, X.length);
			 // BLOQUE QUE SE PUEDE PARALELIZAR
			 for (int i = 0; i < A.length; i++) {
				a = 0;
				for (int j = 0; j < A.length; j++) {
					if(i!=j) 
						a+= A[i][j]*X[j];
				}
				nuevoX[i] = (b[i]-a)/A[i][i];
			}
//			System.out.println(criterioParado(nuevoX, X));
		}while(criterioParado(nuevoX, X)>error && criterioParado(nuevoX, X)<1.3E30);
		 if (criterioParado(nuevoX, X)>=1.3E30)  
			 return false;//diverge
		 else {			 
			 System.arraycopy(nuevoX, 0, X, 0, X.length);
			 return true;//converge
		 }
	}
	
	
	public static double criterioParado(double[] nuevoX, double[] X) {
		double resultado = 0;
		resultado = Math.abs(X[0]-nuevoX[0]);
		for (int i = 1; i < nuevoX.length; i++) {
			if(resultado< Math.abs(X[i]-nuevoX[i]))
				resultado = Math.abs(X[i]-nuevoX[i]);
		}
		return resultado;
	}
	

	public static void imprimirMatriz(double[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz[0].length; j++) {
				System.out.print(matriz[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void matrizDoble(double[][] arreglo, int semilla) {
		Random numeroAletorio = new Random(semilla);
		for (int i = 0; i < arreglo.length; i++) {
			for (int j = 0; j < arreglo[0].length; j++) {
				arreglo[i][j] = numeroAletorio.nextInt(9)+1;
			}
		}
		//volviendo Matriz de diagonal estrictamente dominante
		
		double suma;
		for (int i = 0; i < arreglo.length; i++) {
			suma = 0;
			for (int j = 0; j < arreglo[0].length; j++) {
				if(i!=j)
				suma+=Math.abs(arreglo[i][j]);
			}
			arreglo[i][i] = suma + i +1;
		}
	}
	
	public static void matrizLineal(double[] arreglo, int semilla) {
		Random numeroAletorio = new Random(semilla);
			for (int i = 0; i < arreglo.length; i++) {
				arreglo[i] = numeroAletorio.nextInt(9)+1;
			}
		
	}
}
