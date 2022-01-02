package io.github.jerryzhongj.calabash_brothers.server;

import java.util.concurrent.TimeUnit;

public class CalabashBroIII extends CalabashBro{

    CalabashBroIII(World world) {
        super(world, "III");
        protectFactor = Settings.BRO_III_INITIAL_PROTECT;
    }

    @Override
    void superfy() {
        if(superMode == true)
            return;

        superMode = true;
        protectFactor = Settings.BRO_III_SUPER_PROTECT;

        ThreadPool.scheduled.schedule(()->{
            superMode = false;
            protectFactor = Settings.BRO_III_INITIAL_PROTECT;
        }, Settings.SUPER_TIME_LIMIT, TimeUnit.MILLISECONDS);
        
    }
    
}