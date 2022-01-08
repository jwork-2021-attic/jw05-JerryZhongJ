package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

class HorizontalBoundary extends Entity{

    HorizontalBoundary(World world) {
        super(world, EntityType.FAKE, Settings.BOUNDARY_LONG, Settings.BOUNDARY_SHORT);
        //TODO Auto-generated constructor stub
    }
    
}
