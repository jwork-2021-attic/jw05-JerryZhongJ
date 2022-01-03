package io.github.jerryzhongj.calabash_brothers.sever;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.github.jerryzhongj.calabash_brothers.server.Loader;
import io.github.jerryzhongj.calabash_brothers.server.World;

/**
 * Unit test for simple App.
 */
public class WorldTest 
{
    
    @Test
    public void testLoadWorld()
    {
        Loader loader = new Loader();
        World world = loader.loadInitialWorld("simple");
        world.setPlayers();
        world.ready();
        world.resume();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            
        }
    }
}
