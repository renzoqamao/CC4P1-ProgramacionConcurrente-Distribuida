package cliente;

import java.math.BigDecimal;
import java.math.MathContext;

class Hilo extends Thread {
	
	public BigDecimal minimo;
	public BigDecimal maximo;
	public BigDecimal n;
	public BigDecimal a;
	public String str;
	public BigDecimal b;
	public BigDecimal suma;
	public BigDecimal Dx;
	public BigDecimal x_eval;
	public BigDecimal altura;
	public BigDecimal area;
	public String[] list_terminos; 
	public String[][] termino=new String[20][20];
	//MathContext mc = MathContext.DECIMAL128;
	MathContext mc = new MathContext(60);
	/* El rango [minimo,maximo] el tramo de integral que tenemos que resolver
	 * El rango [a,b] es el tramo total de la integral que tenemos que resolver
	 * n : número de particiones totales.
	 * str : string de la función  : "1,2;4,1;1,0
	 * */
	public Hilo(BigDecimal minimo, BigDecimal maximo, BigDecimal n,BigDecimal a, BigDecimal b, String str) {
		this.minimo = minimo;
		this.maximo = maximo;
		this.n = n;
		this.a = a;
		this.b = b;
		this.str = str;
		this.suma = BigDecimal.ZERO;
		this.altura = BigDecimal.ZERO;
		this.area = BigDecimal.ZERO;
	    definir();
	    
	}
	/* Dx : diferencial de x , es un tramo muy pequeño
	 * x_eval :  primer x en evaluar en el tramo correspondiente.
	 * list_terminos : [ "1,2","4,1","1,0" ]
	 * */
	public void definir() {
		this.Dx = this.b.subtract(this.a, mc).divide(this.n, mc);
	    this.x_eval = this.Dx.multiply(this.minimo,mc).add(this.a, mc);
	    this.list_terminos = this.str.split(";");
	}
	/*Evalua X en la función dada.
	 * termino : multiplicador [0] y potencia[1] del x
	 * Retorna el valor de la función en ese punto.  
	 * */
	public BigDecimal function(BigDecimal x ) {
		BigDecimal function_value = BigDecimal.ZERO;
		for(int i=0; i<list_terminos.length;i++) {	
			this.termino[i] = this.list_terminos[i].split(",");
			     function_value=x.pow( Integer.parseInt(this.termino[i][1]), mc).multiply(new BigDecimal(this.termino[i][0]), mc).add(function_value,mc);
		}
		return function_value;
	}
	/* Metodo run heredado de Thread
	 * Calcula la integral en el rango de [minimo,maximo] 
	 * Almacena el valor de la integral en ese tramo en la variable suma.
	 * */
	public void run() {
		for(double i = this.minimo.intValue(); i<=this.maximo.intValue() ; i++) {
			this.altura = this.function(this.x_eval);
			this.area = this.Dx.multiply(this.altura, mc);
			this.suma = this.suma.add(this.area, mc);
			this.x_eval= this.x_eval.add( this.Dx, mc);
		}
	}
}
