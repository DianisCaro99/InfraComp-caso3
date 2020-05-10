package clienteSinSeguridad;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	@Override
	public void execute() {
		final Cliente client = new Cliente();
		try {
			synchronized (client) {
				client.etapa1();
				client.etapa2();
				client.etapa3();
			}

		} catch (Exception e) {
			System.out.println("F amiguito");
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