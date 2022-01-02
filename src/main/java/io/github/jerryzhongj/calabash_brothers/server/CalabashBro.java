package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;

import lombok.Getter;

abstract class CalabashBro extends MovableEntity{
    @Getter
    protected double hp = 0;
    @Getter
    protected double mp = 0;
    protected double speed = Settings.DEFAULT_SPEED;
    protected double buff = 1;
    protected double protectFactor = 1;
    // facing left(false) or right(true)
    protected boolean facing = false;
    protected boolean superMode = false;

    CalabashBro(World world, String number) {
        super(world, "Calabash Brother " + number, world.getLoader().loadBoundary("Calabash Brother"));
        //TODO Auto-generated constructor stub
    }

    // for World
    void hurt(double hurt){
        hp -= hurt * protectFactor;
    }
    double getDamage(){
        return Settings.DEFAULT_DAMAGE * buff;
    }
    boolean isAlive(){
        return hp > 0;
    }
    // for Controller
    void punch(){
        final boolean facing = this.facing;
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
                    void run() {
                        setVelocityX(bro, Settings.GET_PUNCH_VELOCITY_X * (facing?1:-1));
                        setVelocityY(bro, Settings.GET_PUNCH_VELOCITY_Y);
                    }

                }, World.UpdateOrder.CALABASH_ACTION);
            }
        }

    }

    void moveLeft(){
        facing = false;
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void run() {
                setVelocityX(CalabashBro.this, -speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    void moveRight(){
        facing = true;
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void run() {
                setVelocityX(CalabashBro.this, speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    void jump(){
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void run() {
                setVelocityY(CalabashBro.this, speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    void stop(){
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void run() {
                setVelocityX(CalabashBro.this, 0);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    abstract void superfy();

}
