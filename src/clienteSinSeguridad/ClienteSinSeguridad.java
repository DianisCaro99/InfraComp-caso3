package clienteSinSeguridad;

import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Random;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class ClienteSinSeguridad 
{
	/**
	 * Socket de comunicaci�n
	 */
	private Socket socket;
	/**
	 * Identificador del cliente
	 */
	private  int id_cliente;
	/**
	 * Certificado digital del cliente
	 */
	private  X509Certificate certificadoCliente;
	/**
	 * Certificado digital del servidor
	 */
	private  X509Certificate certificadoServidor;
	/**
	 * Tupla de llave (p�blica-privada del cliente
	 */
	private  KeyPair keyPairCliente;
	/**
	 * Puerto de comunicaci�n entre cliente-servidor
	 */
	private static int puerto=3400;
	/**
	 * Host para la conexi�n
	 */
	private final static String HOST = "localhost"; 
	/**
	 * M�tdo main del cliente
	 * @param args
	 * @throws Exception
	 */
	private PrintWriter out;
	private BufferedReader br;
    private InputStream inS;
    private OutputStream outS;
	private SecretKey k_SC;

	public ClienteSinSeguridad()
	{
		try 
		{
            this.socket = new Socket(HOST, puerto);
            this.inS = this.socket.getInputStream();
            this.outS = this.socket.getOutputStream();
            this.br = new BufferedReader(new InputStreamReader(this.inS));
            this.out = new PrintWriter(this.outS, true);
        }
        catch (Exception e) {
            System.out.println("Fail Opening de Client Socket: " + e.getMessage());
        }
	}

	/**
	 * Transforma una cadena de caracteres en un certificado X509
	 * @param certServidor Cadena de caracteres que conforman el certificado
	 * @return Certificado digital de la forma X509
	 * @throws CertificateException En caso de que no se pueda formar el certificado correctamente
	 */
	private static X509Certificate convertirCertificado(String certServidor) throws CertificateException
	{
		byte[] certiServidorByte = new byte[520];
		certiServidorByte = DatatypeConverter.parseBase64Binary(certServidor);
		CertificateFactory creador = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certiServidorByte);
		return (X509Certificate)creador.generateCertificate(in);
	}
	/**
	 * Genera el certificado digital del cliente con base a una tupla de llave(p�blica-privada)
	 * @param kepair Tupla de llave (p�blica-privada) del cliente
	 * @return certificado digital del cliente
	 * @throws Exception En caso de que no se pueda crear correctamente el certificado
	 */
	private  X509Certificate generarCertificadoCliente(KeyPair kepair) throws Exception
	{
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.add(Calendar.YEAR, 10);
		X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(new X500Name("CN=localhost"), 
				BigInteger.valueOf(1), Calendar.getInstance().getTime(), 
				endCalendar.getTime(), new X500Name("CN=localhost"), 
				SubjectPublicKeyInfo.getInstance(keyPairCliente.getPublic().getEncoded()));
		ContentSigner contentsigner = new JcaContentSignerBuilder("SHA1withRSA").build(keyPairCliente.getPrivate());
		X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(contentsigner);
		return new JcaX509CertificateConverter().setProvider("BC").getCertificate(x509CertificateHolder);
	}
	// -----------------------------------------------------------------
	// Etapa1: Seleccionar algoritmos e iniciar sesi�n
	// -----------------------------------------------------------------
	public void etapa1() throws Exception
	{
		//Creaci�n del identificador del cliente
		Random numAleatorio = new Random();
		id_cliente = numAleatorio.nextInt(9999-1000+1) + 1000;

		//Asegurando conexion con el cliente
		System.out.println("Empezando cliente "+ id_cliente +" en puerto: " + puerto);        
		Security.addProvider((Provider)new BouncyCastleProvider());

		System.out.println("Cliente inicializado en el puerto: "+puerto);
		out.println(MnsSinSeguridad.mns_inicComunicacion());

		//Respuesta del servidor 
		String respuestaServidor = br.readLine();

		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicaci�n");
			socket.close();
		}
		else
		{
			System.out.println("Comenz� el protocolo de comunicaci�n");
		}

		out.println(MnsSinSeguridad.mns_algoritmos());

		respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicaci�n");
			socket.close();
		}
		else
		{
			System.out.println("Se enviaron los algoritmos seleccionados");
		}
	}
	// -----------------------------------------------------------------
	// Etapa2: Autenticaci�n de	cliente	y servidor
	// -----------------------------------------------------------------
	public void etapa2() throws Exception
	{
		//Creaci�n del par de llave p�blica y privada del del cliente
		try 
		{keyPairCliente = MnsSinSeguridad.llaveCliente();}
		catch (Exception e) 
		{System.out.println("Error en la creaci�n de la llave: " + e.getMessage());}

		//Creaci�n de certifado del cliente
		try 
		{certificadoCliente = generarCertificadoCliente(keyPairCliente);}
		catch (Exception e) 
		{System.out.println("Error en la creaci�n del certificado: " + e.getMessage());}

		//Env�o del certificado del cliente al servidor
		byte[] certificadoByte = certificadoCliente.getEncoded();
		String certificadoString = DatatypeConverter.printBase64Binary(certificadoByte);
		out.println(certificadoString);

		String respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicaci�n");
			socket.close();
		}
		else
		{
			System.out.println("Se env�o el certificado digital del cliente al servidor");
		}

		//Obtenci�n del certificado del servidor
		String strCertificadoServidor = br.readLine(); 
		System.out.println("Se recibi� el certificado digital del servidor");

		try 
		{
			out.println(MnsSinSeguridad.mns_OK());
			certificadoServidor = convertirCertificado(strCertificadoServidor);
		} 
		catch (Exception e) 
		{
			out.println(MnsSinSeguridad.mns_Error());
			socket.close();
		}

		//Obtenci�n la llave K_SC
		String strK_SC = br.readLine();
		try
		{
//			byte[] decodedKey = Base64.getDecoder().decode(strK_SC);
//			k_SC = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 
			System.out.println("Se recibi� correctamente K_SC");
		}
		catch (Exception e)
		{
			socket.close();
		}
		
		//Obtenci�n del reto del servidor
		String strReto = br.readLine();
		System.out.println("Se recibi� el reto");

		//Env�o del reto al servidor
		out.println(strReto);

		respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicaci�n");
			socket.close();
		}
		else
		{
			System.out.println("Se recibi� correctamente el reto en el servidor");
		}
	}
	// -----------------------------------------------------------------
	// Etapa3: Reporte y manejo	de la actualizaci�n
	// -----------------------------------------------------------------
	public  void etapa3() throws Exception
	{
		//Env�o de <idUsuario>
		out.println(id_cliente);
		System.out.println("Se env�o el identificador del cliente al servidor");

		//Recepci�n de <hhmm>
		String respuestaServidor = br.readLine();
		try 
		{
			verificarFormato(respuestaServidor);
			System.out.println("La hora enviada por el servidor es: "+ respuestaServidor);
			out.println(MnsSinSeguridad.mns_OK());
			System.out.println("Se termin� la ejecuci�n correctamente.");
			socket.close();
		} 
		catch (Exception e) 
		{
			out.println(MnsSinSeguridad.mns_Error());
			socket.close();
		}
	}

	public void verificarFormato(String hhmm) throws Exception
	{
		Integer.parseInt(hhmm);
	}

	public static void main(String[] args) throws Exception
	{
		final ClienteSinSeguridad client = new ClienteSinSeguridad();
		client.etapa1();
		client.etapa2();
		client.etapa3();
	}
}
