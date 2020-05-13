package clienteConSeguridad;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	@Override
	public void execute() {
		 Client2 client = new Client2();
		try {
			
				client.etapa1();
				client.etapa2();
				client.etapa3();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	@Override
	public void fail() {
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		System.out.println(Task.OK_MESSAGE);
	}
}