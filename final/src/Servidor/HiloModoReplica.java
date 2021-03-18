package Servidor;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class HiloModoReplica {

	private boolean salir;

	private String ip;
	private int puerto;
	private BufferedReader in;
	private PrintWriter mOut;
	
	public BlockingQueue<String> peticionBackup = new LinkedBlockingQueue<String>();
	private TCPServidor tcpServidor;
	

	public HiloModoReplica(TCPServidor tcpServidor, String ip, int puerto) {
		this.tcpServidor = tcpServidor;
		this.ip = ip;
		this.puerto = puerto;
		this.salir = false;

	}

	public void run() {
		InetAddress direccionServidor;
		Socket socket;

		try {
			Thread.sleep(1000); // tiempo de espera 0.5s
			direccionServidor = InetAddress.getByName(ip);
			socket = new Socket(direccionServidor, puerto);
			System.out.println("Conectado a Servidor Lider");
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			
			conexionEstablecida();
			this.tcpServidor.estado = "CONECTADO";
			
			
			//ACEPTAR PETICIONES
			new Thread(new Runnable() {
				public void run() {
					while (!salir) {
						peticionesCliente();
					}
				}
			}).start();

			//ENVIAR RESPUESTAS : se conecta BD
			new Thread(new Runnable() {
				public void run() {
					while (!salir) {
						enviarRespuesta();
					}
				}
			}).start();

			
			while(!salir) {
				try {
					Thread.sleep(100);
					String mensaje = "Comprobacion;Bvivo";
					mOut.println(mensaje);
					if (mOut.checkError()) {
						salir = true;
						socket.close();
						}
				} catch (InterruptedException | IOException  e) {
					e.printStackTrace();
				}
			}
			System.out.println("finalizado");
		} catch (Exception e) {
			System.out.println("HiloModoReplica: ERROR: IP="+ this.ip+ " puerto: "+this.puerto);
		}

	}

	
	private void conexionEstablecida() throws IOException {
		String mensaje = "";
		while (!salir) {
			mensaje = in.readLine();
			if (mensaje != null && mensaje.contains("ID")) {
				String arregloMensaje[] = mensaje.split(";");
				this.tcpServidor.id = Integer.parseInt(arregloMensaje[1]);
				break;
			}
			if(mensaje!=null &&  mensaje.contains("MAXCLIENTE")) {
				String arregloMensaje[] = mensaje.split(";");
				this.tcpServidor.maxCliente = Integer.parseInt(arregloMensaje[1]);
			}
		}
		System.out.println("Servidor Replica con id: " + this.tcpServidor.id);
	}

	private void peticionesCliente()  {
		String mensaje = null;
		while (!salir) {
			try {
				mensaje = in.readLine();
				if (mensaje != null && !mensaje.split(";")[0].equals("Comprobacion")&& !mensaje.split(";")[0].equals("MAXCLIENTE")) {
					this.peticionBackup.put(mensaje);
					System.out.println("Peticion: " + mensaje);
				}
				if(mensaje!=null &&  mensaje.contains("MAXCLIENTE")) {
					String arregloMensaje[] = mensaje.split(";");
					this.tcpServidor.maxCliente = Integer.parseInt(arregloMensaje[1]);
				}
			} catch (IOException | InterruptedException e) {
				System.out.println("error peticion cliente");
				this.salir = true;
			}
		}
	}
	

	public void enviarRespuesta() {
		String respuesta="";
		
		while (!salir) {
			String peticion="peticion";
			
			do {
				peticion = this.peticionBackup.poll();					
			} while (peticion==null);
			
			
			//PROCESO DE BASE DE DATOS
			
			respuesta = peticion.split(";")[0]+";"+peticion.split(";")[1]+";" + peticion( (int) Integer.parseInt( peticion.split(";")[2].split(",")[1] , 10 ) );
		
			//enviar respuesta
				//para probar
				//String arreglo[] = peticion.split(";");
				//respuesta = arreglo[0]+";"+arreglo[1]+";RESPUESTA";
				
				
			enviarMensaje(respuesta);
			
			System.out.println("Enviando rpta a Balanceador: "+respuesta);
		}
	}

	
	public void enviarMensaje(String mensaje) {
		if (this.mOut != null && !this.mOut.checkError()) {
			this.mOut.println(mensaje);
			this.mOut.flush();
		}
	}
	
	//Conexión a la base de datos/
	public static final String URL = "jdbc:mysql://192.168.1.6:3306/Goshop";
	public static final String USERNAME = "replica";
	public static final String PASSWORD = "1234";
	
	public static Connection getConection(){
		Connection con = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(URL,USERNAME,PASSWORD);
			System.out.println("conexión exitosa");
		}catch(Exception e){
			System.out.println(e);
		}
		
		return con;
	}
	
	//mensaje:
	
	private String peticion(int id){
		String producto = " ";
		try{
			Connection con = null ;
			con = getConection();
			
			PreparedStatement ps;
			ResultSet res;
			
			ps = con.prepareStatement("SELECT * FROM productos where id=?");
			ps.setInt(1,id);
			
			res = ps.executeQuery();
			
			if(res.next()){
				producto= String.valueOf(res.getInt("id")) +","+res.getString("nombre")+","+String.valueOf(res.getFloat("precio"))+","+ res.getString("tipo");
				System.out.println("producto encontrado:"+producto);
				
			}else{
				System.out.println("error al encontrar producto");
			}
			con.close();			
		}catch(Exception e){
			System.out.println(e);
		}
		return producto;

	}
}