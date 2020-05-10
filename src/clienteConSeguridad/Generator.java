package clienteConSeguridad;

import uniandes.gload.core.Task;
import uniandes.gload.core.LoadGenerator;

public class Generator
{
    private LoadGenerator generator;
    
    public Generator() 
    {
        final Task work = this.createTask();
        final int numberOfTasks = 10;
        final int gapBetweenTasks = 1000;
        (this.generator = new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks)).generate();
    }
    
    private Task createTask() {
        return new ClientServerTask();
    }
    
    public static void main(final String... args) {
        final Generator gen = new Generator();
    }
}