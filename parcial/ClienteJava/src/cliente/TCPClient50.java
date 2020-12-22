package cliente;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient50 {
	/*servermsj : mensaje del servidor
	 * SERVERIP : IP HOST
	 * SERVERPORT : 4444
	 * mMessageListener : un objeto tipo interface
	 * mRun : boleano , mientras sea true va a seguir ejecutandose.
	 * out : se encarga de imprimir datos.
	 * in : se encarga de leer el datos almacenado en un buffer.
	 * */
    private String servermsj;
    public  String SERVERIP;
    public static final int SERVERPORT = 8000;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    PrintWriter out;
    BufferedReader in;
    /*constructor 
     * OnMessageReceived es una interface */
    public TCPClient50(String ip,OnMessageReceived listener) {
        SERVERIP = ip;
        mMessageListener = listener;
    }
    /*metodo para enviar mensaje */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
    /*metodo para para el cliente*/
    public void stopClient(){
        mRun = false;
    }
    /*metodo para correr el objeto
     * este se encarga de conectarse con el servidor*/
    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            System.out.println("TCP Client"+ "C: Conectando...");
            Socket socket = new Socket(serverAddr, SERVERPORT);
            try {
            	/*out va a tener el valor de lo que se envia por el socket  cliente ->servidor*/
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8")), true);
                System.out.println("TCP Client"+ "C: Sent.");
                System.out.println("TCP Client"+ "C: Done.");
                /*in se encarga de almacenar lo que recibe por el socket  servidor -> cliente */
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF8"));
                System.out.println("entrada in:");
                while (mRun) {
                	
                    servermsj = in.readLine();
                    if(servermsj.equals("salir")) {
                    	stopClient();
                    	this.sendMessage("salir");
                    	continue;
                    }
                    
                    if (servermsj != null && mMessageListener != null) {
                        mMessageListener.messageReceived(servermsj);
                    }
                    servermsj = null;
                }
            } catch (Exception e) {
                System.out.println("TCP"+ "S: Error"+e);

            } finally {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("TCP"+ "C: Error"+ e);
        }
    }
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}