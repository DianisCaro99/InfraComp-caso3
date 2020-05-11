package clienteSinSeguridad;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


public class MnsSinSeguridad
{
	// -----------------------------------------------------------------
    // Mensajes del protocolo de comunicaci�n
    // -----------------------------------------------------------------
	public static final String HOLA = "HOLA";
	public static final String OK = "OK";
	public static final String ERROR = "ERROR";
	public static final String ALGORITMOS = "ALGORITMOS";
	public static final String SEPARADOR_PRINCIPAL = ":";
	// -----------------------------------------------------------------
    // Algoritmos para el manejo de confidencialidad e integridad
    // -----------------------------------------------------------------
	public static final String AES = "AES";
	public static final String RSA = "RSA";
	public static final String HMACSHA512 = "HMACSHA512";
	/**
	 * Mensaje de comunicaci�n inicial
	 * @return mensaje inicial
	 */
	public static String mns_inicComunicacion()
	{
		return HOLA;
	}
	/**
	 * Verifica si el mensaje enviado por el servidor es OK
	 * @param respuestaServ mensaje de respuesta del servidor
	 * @return True en caso de que el mensaje sea ERROR, False en caso contrario
	 */
	public static boolean verificarError(String respuestaServ)
	{
		if(respuestaServ==ERROR)
			return true;
		else
			return false;
	}
	/**
	 * Mensaje para notificar los algoritmos a usar por el servidor
	 * @return Mensaje de comunicaci�n
	 */
	public static String mns_algoritmos()
	{
		return ALGORITMOS+SEPARADOR_PRINCIPAL+AES+SEPARADOR_PRINCIPAL+RSA+
				SEPARADOR_PRINCIPAL+HMACSHA512;
	}
	/**
	 * Mensaje de verificaci�n: Comunicaci�n correcta
	 * @return mensaje de verificaci�n
	 */
	public static String mns_OK()
	{
		return OK;
	}
	/**
	 * Mensaje de verificaci�n: Comunicaci�n incorrecta
	 * @return mensaje de verificaci�n
	 */
	public static String mns_Error()
	{
		return ERROR;
	}
	/**
	 * Genera la tupla de llaves para el cliente
	 * @return Tupla de llave (privada-p�blica) del cliente
	 * @throws NoSuchAlgorithmException si la llave no se puede generar
	 */
	public static KeyPair llaveCliente() throws NoSuchAlgorithmException
	{
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance(RSA);
		kpGen.initialize(1024);
		return kpGen.generateKeyPair();
	}
}
