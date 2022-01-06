package io.github.jerryzhongj.calabash_brothers.server;

import java.util.Set;

import lombok.Getter;

abstract class CalabashBro extends MovableEntity{
    @Getter
    protected double hp = Settings.INITIAL_HP;
    @Getter
    protected double mp = 0;
    protected double speed = Settings.DEFAULT_SPEED;
    protected double buff = 1;
    protected double protectFactor = 1;
    // facing left(false) or right(true)
    protected boolean facing = false;
    protected boolean superMode = false;

    CalabashBro(World world, String number) {
        super(world, "Calabash Brother " + number, Settings.CALABASH_HEIGHT, Settings.CALABASH_WIDTH);
        //TODO Auto-generated constructor stub
    }

    private double getDamage(){
        return Settings.DEFAULT_DAMAGE * buff;
    }

    // for World
    synchronized public void hurt(double hurt){
        hp -= hurt * protectFactor;
    }
    
    public boolean isAlive(){
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
                    void update() {
                        setVelocityX(bro, Settings.GET_PUNCH_VELOCITY_X * (facing?1:-1));
                        setVelocityY(bro, Settings.GET_PUNCH_VELOCITY_Y);
                    }

                }, World.UpdateOrder.CALABASH_ACTION);
            }
        }

    }

    public void moveLeft(){
        facing = false;
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityX(CalabashBro.this, -speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    public void moveRight(){
        facing = true;
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityX(CalabashBro.this, speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    public void jump(){
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityY(CalabashBro.this, speed);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    public void stop(){
        world.registerUpdate(world.new Update(World.UpdateType.ONESHOT) {
            @Override
            void update() {
                setVelocityX(CalabashBro.this, 0);
            }
            
        }, World.UpdateOrder.CALABASH_ACTION);
    }

    abstract public void superfy();

}
