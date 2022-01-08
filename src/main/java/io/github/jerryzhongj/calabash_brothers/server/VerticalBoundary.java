package io.github.jerryzhongj.calabash_brothers.server;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;

class VerticalBoundary extends Entity{

    VerticalBoundary(World world) {
        super(world, EntityType.FAKE, Settings.BOUNDARY_SHORT, Settings.BOUNDARY_LONG);
        //TODO Auto-generated constructor stub
    }
    
}
