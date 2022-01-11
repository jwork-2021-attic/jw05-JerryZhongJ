package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.server.World.Position;

public class CalabashBroVII extends CalabashBro{

    CalabashBroVII(World world, String name) {
        super(world, EntityType.CALABASH_BRO_VII, name, world.getLoader().loadEntityWidth(EntityType.CALABASH_BRO_VII), world.getLoader().loadEntityHeight(EntityType.CALABASH_BRO_VII));
    }

    
    @Override
    public void superMode(){
        super.superMode();
        World.Update update = world.new Update(World.UpdateType.FINITE){

            @Override
            void update() {
                Set<Entity> entities = world.getEntityAround(CalabashBroVII.this, position -> {
                    double x = position.x * (facingRight ? 1: -1);
                    double y = position.y;
                    return x >= 0 && x <= 400 && Math.abs(y) - 50 <= x;
                }); 

                for(Entity e : entities){
                    if(!(e instanceof MovableEntity))
                        continue;
                    MovableEntity me = (MovableEntity)e;
                    Position mePos = world.getPosition(me);
                    Position pos = world.getPosition(CalabashBroVII.this);
                    double deltaX = pos.x - mePos.x;
                    double deltaY = pos.y - mePos.y;
                    double speed = 500 - deltaX;
                    double vx = speed * deltaX / Math.sqrt(deltaX * deltaX + deltaY + deltaY);
                    double vy = speed * deltaY / Math.sqrt(deltaX * deltaX + deltaY + deltaY);
                    setVelocity(me, new World.Velocity(vx, vy));
                }
            }
        };
        update.remain = (int)Settings.DEFAULT_SUPER_TIME;
        world.registerUpdate(update, World.UpdateOrder.CAlABASH_SUPER);
    }
    
    
}
