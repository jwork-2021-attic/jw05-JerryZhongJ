package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.ThreadPool;
import io.github.jerryzhongj.calabash_brothers.server.World.UpdateOrder;
import io.github.jerryzhongj.calabash_brothers.server.World.UpdateType;
import lombok.Getter;


public abstract class CalabashBro extends MovableEntity{
    @Getter
    protected double hp = Settings.MAX_HP;
    @Getter
    protected double mp = 0;
    // facing left(false) or right(true)
    @Getter
    protected boolean facingRight = false;
    @Getter
    protected boolean superMode = false;
    @Getter
    protected String name;
    // None doesn't mean stand still, but means character is not trying to move.
    public enum Status{
        INERTIA, MOVING_LEFT, MOVING_RIGHT
    }
    @Getter
    protected Status movingStatus = Status.INERTIA;
    CalabashBro(World world, EntityType type, String name, double width, double height) {
        super(world, type, width, height);
        this.name = name;

        ThreadPool.scheduled.scheduleAtFixedRate(()->{
            synchronized(this){
                mp += Settings.MP_GROW_PER_SENCOND;
                if(mp >= Settings.MAX_MP)
                    mp = Settings.MAX_MP;
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    // Can be customized
    protected double getDamage(){
        return Settings.DEFAULT_DAMAGE;
    }

    protected double getProtect(){
        return 1.0;
    }

    protected double getSpeed(){
        return Settings.DEFAULT_SPEED;
    }

    protected long getSuperModeTime(){
        return Settings.DEFAULT_SUPER_TIME;
    }

    protected double getMpCost(){
        return Settings.DEFAULT_MP_COST;
    }

    // for World
    public synchronized void hurt(double damage){
        hp -= damage * getProtect();
        if(hp <= 0){
            // TODO: add corpe
            world.registerUpdate(world.new Update(UpdateType.ONESHOT){
                @Override
                void update() {
                    removeEntity(CalabashBro.this);
                }
                
            }, UpdateOrder.REMOVE_ENTITY);
        }
    }
    
    public boolean isAlive(){
        return hp > 0;
    }

    // for Controller
    public void punch(){
        final boolean facing = this.facingRight;
        Set<Entity> entities = world.getEntityAround(this, position -> {
            double x = position.x * ( facing ? 1 : -1 );
            double y = position.y;
            return Math.abs(y) <= Settings.ATTACK_HEIGHT  / 2 && x >= 0 && x <= Settings.ATTACK_LENGTH;
        });

        for(Entity entity : entities){
            if(entity instanceof CalabashBro){
                CalabashBro bro = (CalabashBro)entity;
                bro.hurt(getDamage());
                world.registerUpdate(world.new Update(World.UpdateType.ONESHOT){

                    @Override
                    void update() {
                        setVelocityX(bro, Settings.GET_PUNCH_VELOCITY_X * (facing?1:-1));
                        setVelocityY(bro, Settings.GET_PUNCH_VELOCITY_Y);
                    }

                }, World.UpdateOrder.CALABASH_ACTION);
            }
        }

    }

    public void moveLeft(){
        facingRight = false;
        movingStatus = Status.MOVING_LEFT;
    }

    public void moveRight(){
        facingRight = true;
        movingStatus = Status.MOVING_RIGHT;
    }

    public void jump(){
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityY(CalabashBro.this, getSpeed());
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    public void stop(){
        // Character is then not trying to move.
        movingStatus = Status.INERTIA;

        // And stop explcitly.
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityX(CalabashBro.this, 0);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    public synchronized void superMode(){
        if(superMode == true || mp < getMpCost())
            return;

        superMode = true;
        mp -= getMpCost();
        ThreadPool.scheduled.schedule(()->{
            superMode = false;
        }, getSuperModeTime(), TimeUnit.MILLISECONDS);
    }
    

}
