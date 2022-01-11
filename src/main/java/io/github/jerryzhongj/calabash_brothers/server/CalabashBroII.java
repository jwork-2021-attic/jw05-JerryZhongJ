package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.server.World.Position;
import io.github.jerryzhongj.calabash_brothers.server.World.UpdateOrder;
import io.github.jerryzhongj.calabash_brothers.server.World.UpdateType;

public class CalabashBroII extends CalabashBro{

    public CalabashBroII(World world, String name) {
        super(world, EntityType.CALABASH_BRO_II, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_II), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_II));
        
    }

    @Override
    protected double getSpeed(){
        return Settings.CALABASH_II_SPEED;
    }

    @Override
    public void superMode(){
        super.superMode();
        World.Update update = world.new Update(World.UpdateType.FINITE){

            @Override
            void update() {
                Set<Entity> entities = world.getEntityAround(CalabashBroII.this, position -> {
                    return position.x * position.x + position.y + position.y <= Settings.CALABASH_II_RADIUS * Settings.CALABASH_II_RADIUS;
                }); 

                for(Entity e : entities){
                    if(!(e instanceof MovableEntity))
                        continue;
                    MovableEntity me = (MovableEntity)e;
                    setPosition(me, world.getPosition(me));
                    setVelocity(me, world.getVelocity(me));
                }
            }
        };
        update.remain = (int)Settings.DEFAULT_SUPER_TIME;
        world.registerUpdate(update, World.UpdateOrder.CAlABASH_SUPER);
    }
    
}
