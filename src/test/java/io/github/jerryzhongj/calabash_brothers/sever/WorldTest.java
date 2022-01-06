package io.github.jerryzhongj.calabash_brothers.sever;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.github.jerryzhongj.calabash_brothers.server.CalabashBro;
import io.github.jerryzhongj.calabash_brothers.server.CalabashBroI;
import io.github.jerryzhongj.calabash_brothers.server.Loader;
import io.github.jerryzhongj.calabash_brothers.server.World;

/**
 * Unit test for simple App.
 */
public class WorldTest 
{
    
    @Test
    public void testWorld()
    {
        Loader loader = new Loader();
        World world = loader.loadInitialWorld("default");
        CalabashBroI b1 = new CalabashBroI(world, "Bro1");
        CalabashBroI b2 = new CalabashBroI(world, "Bro2");
        world.setPlayer(b1, 50, 50);
        world.setPlayer(b2, -50, 50);
        world.ready();
        world.resume();
        try {
            Thread.sleep(5000);
            while(true){
                b1.moveLeft();
                Thread.sleep(500);
                b1.punch();
                b1.stop();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }
}
