package servidorConSeguridad;

import java.lang.management.ManagementFactory;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class MonitorDesempeNo 
{
	private double tiempoResp;
	private double inicio;
	private long fin;
	// -----------------------------------------------------------------
	// Monitor de desempeño: Tiempo de respuesta de una transacción
	// -----------------------------------------------------------------
	public void iniciarTiempoRespuesta()
	{
		inicio = System.currentTimeMillis();
	}

	public double terminarTiempoRespuesta()
	{
		fin = System.currentTimeMillis();
		tiempoResp = (fin - inicio);
		return tiempoResp;
	}
	// -----------------------------------------------------------------
	// Monitor de desempeño: Uso de CPU
	// -----------------------------------------------------------------
	public double getSystemCpuLoad() throws Exception 
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}
	
}
