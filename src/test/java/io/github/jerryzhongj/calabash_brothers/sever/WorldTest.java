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
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testLoadWorld()
    {
        Loader loader = new Loader();
        World world = loader.loadInitialWorld("simple");
    }
}
