package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import lombok.Getter;

public class CalabashBroVI extends CalabashBro{
    

    
    public CalabashBroVI(World world, String name) {
        super(world, EntityType.CALABASH_BRO_VI,name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_VI), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_VI));
    }

    public boolean isInvisible(){
        return superMode;
    }

    
    
}
