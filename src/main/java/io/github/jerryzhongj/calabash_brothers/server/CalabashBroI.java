package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

class CalabashBroI extends CalabashBro{

    
    CalabashBroI(World world, String name) {
        super(world, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_I), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_I));
        buff = Settings.CALABASH_I_BUFF;
    }

    @Override
    public void superfy() {
        ;
        
    }
    
}
