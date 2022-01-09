package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;

public class Earth extends Entity{

    public Earth(World world) {
        super(world, EntityType.Earth, world.getLoader().loadEntityWidth(EntityType.Earth), world.getLoader().loadEntityHeight(EntityType.Earth));
    }
    
}
