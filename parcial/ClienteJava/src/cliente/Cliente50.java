package cliente;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Scanner;



class Cliente50 {
	/*variable donde almacena las suma de los demás*/
    BigDecimal suma =BigDecimal.ZERO;
    /* cliente encargado de recibir el mensaje del servidor */
    TCPClient50 mTcpClient;
    /*scanner para lectura por teclado no lo usamos*/
    Scanner sc;
    static final String HOST = "192.168.1.5";
    /* variable con el número de hilos para calcular el tramo de integral*/
	public int n_hilos = 4;
	/*MathContext.DECIMAL128 */
	//MathContext mc = MathContext.DECIMAL128;
	MathContext mc = new MathContext(60);
	/*tiempos*/
	//long inicioTiempo;
	//long finalTiempo;
    public static void main(String[] args) {
        Cliente50 objcli = new Cliente50();
        objcli.iniciar();
    }
    /*el cliente inicia en paralelo el metodo para recibir mensajes del servidor HOST
     * además que recibir como argumento un objeto interfaz
     * esta interfaz recibe el mensaje */
    void iniciar() {
    	
        new Thread(
        		new Runnable() {
        			@Override
        			public void run() {
        				mTcpClient = new TCPClient50(HOST,
                        new TCPClient50.OnMessageReceived() {
        					@Override
        					public void messageReceived(String message) {
        						ClienteRecibe(message);
        					}
        				}
        				);
        				mTcpClient.run();
        			}
          }
        ).start();
        //---------------------------

       String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Cliente bandera 01");
        while (!salir.equals("s")) {
            salir = sc.nextLine();
            ClienteEnvia(salir);
        }
        System.out.println("Cliente bandera 02");
		
    }

    void ClienteRecibe(String llego) {
    	System.out.println("entro a clienteRecibe");
    	this.suma =BigDecimal.ZERO;
        System.out.println("CLINTE50 El mensaje::" + llego);
        if (llego.trim().contains("envio")) {
            String arrayString[] = llego.split("\\s+");
            BigDecimal min = new BigDecimal(arrayString[1]);
            BigDecimal max = new BigDecimal(arrayString[2]);
            BigDecimal n = new BigDecimal(arrayString[3]);
            BigDecimal a = new BigDecimal(arrayString[4]);
            BigDecimal b = new BigDecimal(arrayString[5]);
            String fun = arrayString[6];
            System.out.println("la funcion :" + fun+"\n " + "a :" + a +"\n" +" b:" + b + "\n"+"n:" +n+"\n" + "minimo:"+ min +"maximo:"+max);
            /* aqui calculamos un tramo de la integral */
            BigDecimal[] prefijos = indices_intervalo(max,min,this.n_hilos);
            System.out.println("prefijos:"+prefijos);
            Hilo[] hilos = new Hilo[n_hilos];
            //inicioTiempo = System.currentTimeMillis();
            for(int i=0; i<this.n_hilos;i++) {
        		hilos[i] = new Hilo(prefijos[i],prefijos[i+1],n,a,b,fun);
        		System.out.println("Hilo "+i+":" + "prefijo inicio: "+ prefijos[i]+" prefijo final :"+prefijos[i+1]);
        		hilos[i].run();
        		try {
					hilos[i].join();
				} catch (InterruptedException e) {
					// TODO Bloque catch generado automáticamente
					e.printStackTrace();
				}
        		System.out.println("hilo "+i+" : "+hilos[i].getName());
            }
            for(int i=0; i<this.n_hilos;i++) {
            	System.out.println("hilo "+i+"  suma : "+ hilos[i].suma);
            	this.suma = hilos[i].suma.add(this.suma, mc);
            }
            System.out.println("suma en el cliente :" + this.suma.toString());
            //Envia el mensaje al servidor
            ClienteEnvia(this.suma.toString());
            //finalTiempo = System.currentTimeMillis();
            //System.out.printf("Tiempo en Serie: %6f s \n",((double)(finalTiempo- inicioTiempo))/1000);
        }
    }
    
    
    /**el cliente envia un mensaje con objeto de la clase TCPClient50*/
    void ClienteEnvia(String envia) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(envia);
        }
    } 
    /*funcion que calcula los indices del intervalo en el cual van a trabajar los hilos
     * parte un intervalo en nro_particiones 
     * El intervalo que vamos a pasar es maximo-minimo -> max-min
     * */
    public BigDecimal[] indices_intervalo(BigDecimal maximo, BigDecimal minimo,int numero_particiones) {
    	//System.out.println("llego1");
    	BigDecimal intervalo = maximo.subtract(minimo, mc);
    	//System.out.println("llego2");
    	BigDecimal nro_particiones = new BigDecimal(String.valueOf(numero_particiones));
    	//System.out.println("llego3");
    	int cociente = (int)( intervalo.doubleValue() / nro_particiones.intValue()) ;
    	//System.out.println("llego4");
    	BigDecimal Bcociente = new BigDecimal(String.valueOf(cociente));
    	//System.out.println("llego5");
    	BigDecimal residuo =  intervalo.remainder(nro_particiones);
    	BigDecimal[] r = new BigDecimal[numero_particiones];
    	//System.out.println("llego6");
    	for(int i=0 ; i<numero_particiones ; i++) {
    		r[i] = Bcociente;
    		if(residuo.doubleValue()>0) {
    			r[i].add(BigDecimal.ONE, mc);
    			residuo.subtract(BigDecimal.ONE, mc);
    		}
    	}
    	//System.out.println("llego7");
    	BigDecimal[] prefijos = new BigDecimal[r.length+1];
    	prefijos[0]= BigDecimal.ZERO;
    	for (int i = 1; i < prefijos.length; i++) {
			prefijos[i] = prefijos[i-1].add( r[i-1] , mc);
		}
    	//System.out.println("llego8");
    	for (int i = 0; i < prefijos.length; i++) {
			prefijos[i] = prefijos[i].add(minimo,  mc);
		}
    	return prefijos;
    }
    
}
