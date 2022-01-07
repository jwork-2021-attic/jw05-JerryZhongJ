package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.ThreadPool;

public class CalabashBroIII extends CalabashBro{

    CalabashBroIII(World world, String name) {
        super(world, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_III), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_III));
        protectFactor = Settings.CALABASH_III_INITIAL_PROTECT;
    }

    @Override
    synchronized public void superfy() {
        if(superMode == true)
            return;

        superMode = true;
        protectFactor = Settings.CALABASH_III_SUPER_PROTECT;

        ThreadPool.scheduled.schedule(()->{
            superMode = false;
            protectFactor = Settings.CALABASH_III_INITIAL_PROTECT;
        }, Settings.SUPER_TIME_LIMIT, TimeUnit.MILLISECONDS);
        
    }
    
}
