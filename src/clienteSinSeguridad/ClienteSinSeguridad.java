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

import clienteConSeguridad.Mns_Alg;

public class ClienteSinSeguridad 
{
	/**
	 * Socket de comunicación
	 */
	private Socket socket;
	/**
	 * Identificador del cliente
	 */
	private static int id_cliente;
	/**
	 * Certificado digital del cliente
	 */
	private static X509Certificate certificadoCliente;
	/**
	 * Certificado digital del servidor
	 */
	private static X509Certificate certificadoServidor;
	/**
	 * Tupla de llave (pública-privada del cliente
	 */
	private static KeyPair keyPairCliente;
	/**
	 * Puerto de comunicación entre cliente-servidor
	 */
	private static int puerto;
	/**
	 * Host para la conexión
	 */
	private final static String HOST = "localhost"; 
	/**
	 * Métdo main del cliente
	 * @param args
	 * @throws Exception
	 */
	private PrintWriter writer;
	private BufferedReader br;
	private SecretKey k_SC;

	public ClienteSinSeguridad()
	{
		try 
		{
			this.socket = new Socket("localhost", 9999);
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
	 * Genera el certificado digital del cliente con base a una tupla de llave(pública-privada)
	 * @param kepair Tupla de llave (pública-privada) del cliente
	 * @return certificado digital del cliente
	 * @throws Exception En caso de que no se pueda crear correctamente el certificado
	 */
	private static X509Certificate generarCertificadoCliente(KeyPair kepair) throws Exception
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
	// Etapa1: Seleccionar algoritmos e iniciar sesión
	// -----------------------------------------------------------------
	public synchronized void etapa1() throws Exception
	{
		puerto = 3400;

		//Creación del identificador del cliente
		Random numAleatorio = new Random();
		id_cliente = numAleatorio.nextInt(9999-1000+1) + 1000;

		//Asegurando conexion con el cliente
		System.out.println("Empezando cliente "+ id_cliente +" en puerto: " + puerto);        
		Security.addProvider((Provider)new BouncyCastleProvider());

		//Preparando el socket para comunicación
		socket = new Socket(HOST, puerto);
		writer = new PrintWriter(socket.getOutputStream(), true);
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		System.out.println("Cliente inicializado en el puerto: "+puerto);
		writer.println(MnsSinSeguridad.mns_inicComunicacion());

		//Respuesta del servidor 
		String respuestaServidor = br.readLine();

		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicación");
			socket.close();
		}
		else
		{
			System.out.println("Comenzó el protocolo de comunicación");
		}

		writer.println(MnsSinSeguridad.mns_algoritmos());

		respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicación");
			socket.close();
		}
		else
		{
			System.out.println("Se enviaron los algoritmos seleccionados");
		}
	}
	// -----------------------------------------------------------------
	// Etapa2: Autenticación de	cliente	y servidor
	// -----------------------------------------------------------------
	public synchronized void etapa2() throws Exception
	{
		//Creación del par de llave pública y privada del del cliente
		try 
		{keyPairCliente = MnsSinSeguridad.llaveCliente();}
		catch (Exception e) 
		{System.out.println("Error en la creación de la llave: " + e.getMessage());}

		//Creación de certifado del cliente
		try 
		{certificadoCliente = generarCertificadoCliente(keyPairCliente);}
		catch (Exception e) 
		{System.out.println("Error en la creación del certificado: " + e.getMessage());}

		//Envío del certificado del cliente al servidor
		byte[] certificadoByte = certificadoCliente.getEncoded();
		String certificadoString = DatatypeConverter.printBase64Binary(certificadoByte);
		writer.println(certificadoString);

		String respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicación");
			socket.close();
		}
		else
		{
			System.out.println("Se envío el certificado digital del cliente al servidor");
		}

		//Obtención del certificado del servidor
		String strCertificadoServidor = br.readLine(); 
		System.out.println("Se recibió el certificado digital del servidor");

		try 
		{
			writer.println(MnsSinSeguridad.mns_OK());
			certificadoServidor = convertirCertificado(strCertificadoServidor);
		} 
		catch (Exception e) 
		{
			writer.println(MnsSinSeguridad.mns_Error());
			socket.close();
		}

		//Obtención la llave K_SC
		String strK_SC = br.readLine();
		try
		{
			byte[] decodedKey = Base64.getDecoder().decode(strK_SC);
			k_SC = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 
			System.out.println("Se recibió correctamente K_SC");
		}
		catch (Exception e)
		{
			socket.close();
		}

		//Obtención del reto del servidor
		String strReto = "000";
		System.out.println("Se recibió el reto");

		//Envío del reto al servidor
		writer.println(strReto);

		respuestaServidor = br.readLine();
		if(MnsSinSeguridad.verificarError(respuestaServidor))
		{
			System.out.println("Hubo un error en la comunicación");
			socket.close();
		}
		else
		{
			System.out.println("Se recibió correctamente el reto en el servidor");
		}
	}
	// -----------------------------------------------------------------
	// Etapa3: Reporte y manejo	de la actualización
	// -----------------------------------------------------------------
	public synchronized void etapa3() throws Exception
	{
		//Envío de <idUsuario>
		writer.println(id_cliente);
		System.out.println("Se envío el identificador del cliente al servidor");

		//Recepción de <hhmm>
		String respuestaServidor = br.readLine();
		try 
		{
			verificarFormato(respuestaServidor);
			System.out.println("La hora enviada por el servidor es: "+ respuestaServidor);
			writer.println(MnsSinSeguridad.mns_OK());
			System.out.println("Se terminó la ejecución correctamente.");
			socket.close();
		} 
		catch (Exception e) 
		{
			writer.println(MnsSinSeguridad.mns_Error());
			socket.close();
		}
	}

	public void verificarFormato(String hhmm) throws Exception
	{
		String[] hora_min = hhmm.split(":");
		Integer.parseInt(hora_min[0]);
		Integer.parseInt(hora_min[1]);
	}

	public static void main(String[] args) throws Exception
	{
		final ClienteSinSeguridad client = new ClienteSinSeguridad();
		client.etapa1();
		client.etapa2();
		client.etapa3();
	}
}
