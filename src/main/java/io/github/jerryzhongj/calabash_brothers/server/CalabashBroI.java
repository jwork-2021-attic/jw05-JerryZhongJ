package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

public class CalabashBroI extends CalabashBro{

    
    public CalabashBroI(World world, String name) {
        super(world, EntityType.CALABASH_BRO_I, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_I), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_I));
        
    }

    @Override
    protected double getDamage(){
        if(superMode)
            return Settings.CALABASH_I_SUPER_DAMAGE;
        else
            return  Settings.CALABASH_I_DAMAGE;
    }

    
}
