package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

class Earth extends Entity{

    Earth(World world) {
        super(world, EntityType.Earth, world.getLoader().loadEntityWidth(EntityType.Earth), world.getLoader().loadEntityHeight(EntityType.Earth));
        //TODO Auto-generated constructor stub
    }
    
}
