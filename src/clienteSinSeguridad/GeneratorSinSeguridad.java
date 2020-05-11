package clienteSinSeguridad;

import uniandes.gload.core.Task;
import uniandes.gload.core.LoadGenerator;

public class GeneratorSinSeguridad
{
    private LoadGenerator generator;
    
    public GeneratorSinSeguridad() 
    {
        final Task work = this.createTask();
        final int numberOfTasks = 10;
        final int gapBetweenTasks = 1000;
        (this.generator = new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks)).generate();
    }
    
    private Task createTask() {
        return new ClientServerTaskSinSeguridad();
    }
    
    public static void main(final String... args) {
        final GeneratorSinSeguridad gen = new GeneratorSinSeguridad();
    }
}