package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

class Concrete extends Entity{

    Concrete(World world) {
        super(world, "Concrete", world.getLoader().loadEntityWidth(EntityType.CONCRETE), world.getLoader().loadEntityHeight(EntityType.CONCRETE));
        //TODO Auto-generated constructor stub
    }
    
}
