package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import lombok.Getter;

class CalabashBroVI extends CalabashBro{
    
    @Getter
    private boolean invisible = false;
    
    CalabashBroVI(World world, String name) {
        super(world, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_VI), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_VI));
    }

    @Override
    public void superfy() {
        // TODO Auto-generated method stub
        
    }
    
}
