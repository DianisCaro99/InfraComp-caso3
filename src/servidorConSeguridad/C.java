package servidorConSeguridad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
	
public class C 
{
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer;
	private static KeyPair keyPairServidor; 

	public static void main(String[] args) throws Exception
	{
		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int ip = Integer.parseInt(br.readLine());
		
		/***** N�mero de threads del tamaNo definido *****/
		System.out.println(MAESTRO + "Establezca el n�mero de threads para el servidor:");
		int numThreads = Integer.parseInt(br.readLine());
		
		System.out.println(MAESTRO+ "Ingrese el nombre del archivo para guardar los datos (sin .csv)");
		String nomArchivo = br.readLine();
		
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		
				
		// Crea el archivo de log
		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor); 
		String ruta = "./resultadosConSeguridad.txt";
		   
        file = new File(ruta);
        if (!file.exists())
        {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        fw.close();
        
        File file2 = new File("./datosConSeguridad/"+nomArchivo+".csv");
		if (!file2.exists())
		{
			file2.createNewFile();
		}

        D.init(certSer, keyPairServidor,file);
        
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
        
		/***** Creaci�n del Pool de Threads *****/
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

		try
		{
			for (int i=0;true;i++) 
			{
					Socket sc = ss.accept();
					System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
					executorService.execute(new D(sc, i, file2));
			}
		} 
		catch (Exception e) 
		{
			System.out.println(MAESTRO + "Error creando el socket cliente.");
			e.printStackTrace();
		}
		
	}
}
