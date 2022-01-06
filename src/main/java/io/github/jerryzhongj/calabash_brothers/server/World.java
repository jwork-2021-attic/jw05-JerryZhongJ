package io.github.jerryzhongj.calabash_brothers.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 这个世界是连续的，以像素为长度单位
// 坐标以地面的中心为原点
public class World implements Serializable{
    private static final long serialVersionUID = 1403870569483406173L;

    enum WorldStatus{
        // END means the game finish, however STOP means world was forcely stop.
        SETUP, READY, RUNNING, END
    }

    @AllArgsConstructor
    static class Position{
        @Getter
        final double x, y;

        // public Position clone(){
        //     return new Position(x, y);
        // }

        Position add(double deltaX, double deltaY){
            return new Position(x + deltaX, y + deltaY);
        }

        Position setX(double newX){
            return new Position(newX, y);
        }

        Position setY(double newY){
            return new Position(x, newY);
        }
        /**
         * 
         * @param pos another position
         * @return the distance between these two postion
         */
        public double disFrom(Position pos){
            double deltaX = pos.x - x;
            double deltaY = pos.y - y;
            return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }

        public String toString(){
            return String.format("(%.2f, %.2f)", x, y);
        }
    }

    @AllArgsConstructor
    static class Velocity{
        @Getter
        final double vx, vy;

        Velocity add(double deltaVx, double deltaVy){
            return new Velocity( + deltaVx, vy + deltaVy);
        }

        Velocity setVx(double newVx){
            return new Velocity(newVx, vy);
        }

        Velocity setVy(double newVy){
            return new Velocity(vx, newVy);
        }

        public String toString(){
            return String.format("(%.2f, %.2f)", vx, vy);
        }

    }

    enum UpdateOrder{
        // Should start with 0
        // The later update covers the earlier.
        // Due to overlaying problem, we have to put adding entity the last one.
        BASIC(0), CALABASH_ACTION(1), CAlABASH_SUPER(2), REMOVE_ENTITY(3), VALIDATION_CHECK(4), ADD_ENTITY(5);
        @Getter
        final int priority;
        private UpdateOrder(int priority){
            this.priority = priority;
        }
        static int getTotalNum(){
            return UpdateOrder.values().length;
        }
    }

    enum UpdateType{
        ONESHOT, FINITE, INFINTE, BINDTO
    }

    // All changes happen here
    // New state is computed based on the older one.
    abstract class Update{
        
        final UpdateType type;
        // TimeUnit: millisecond
        int remain;
        // TODO: BINDTO
        Update(UpdateType type, int remain){
            this.type = type;
            this.remain = remain;
        }
        Update(UpdateType type){
            this(type, 0);
        }
        abstract void update();


        // Some update utils
        protected void addVelocity(MovableEntity me, double deltaVx, double deltaVy){
            Velocity v = newVelocities.get(me);
            if(v == null){
                v = getVelocity(me);
                if(v == null){
                    // TODO: Log
                    System.out.printf("Cannot set add v for %s: not found in this world.", me.getName());
                    return;
                }
            }
            newVelocities.put(me, v.add(deltaVx, deltaVy));
        }

        protected void setVelocity(MovableEntity me, Velocity newV){
            newVelocities.put(me, newV);
        }

        protected void setVelocityX(MovableEntity me, double vx){
            Velocity v = newVelocities.get(me);
            if(v == null){
                v = getVelocity(me);
                if(v == null){
                    // TODO: Log
                    System.out.printf("Cannot set vx for %s: not found in this world.", me.getName());
                    return;
                }
            }
            newVelocities.put(me, v.setVx(vx));
        }

        protected void setVelocityY(MovableEntity me, double vy){
            Velocity v = newVelocities.get(me);
            if(v == null){
                v = getVelocity(me);
                if(v == null){
                    // TODO: Log
                    System.out.printf("Cannot set vy for %s: not found in this world.", me.getName());
                    return;
                }
            }
            newVelocities.put(me, v.setVy(vy));
        }

        // protected void addPosition(MovableEntity me, double deltaX, double deltaY){
        //     Position v = newPositions.get(me);
        //     if(v == null){
        //         v = getPosition(me);
        //         if(v == null){
        //             // TODO: Log
        //             System.out.printf("Cannot set add v for %s: not found in this world.", me.getName());
        //             return;
        //         }
        //     }
        //     newPositions.put(me, v.add(deltaX, deltaY));
        // }

        protected void setPosition(Entity e, Position pos){
            newPositions.put(e, pos);
        }

        protected void setPositionX(Entity e, double x){
            Position pos = newPositions.get(e);
            if(pos == null){
                pos = getPosition(e);
                if(pos == null){
                    // TODO: Log
                    System.out.printf("Cannot set x for %s: not found in this world.", e.getName());
                    return;
                }
            }
            newPositions.put(e, pos.setX(x));
        }

        protected void setPositionY(Entity e, double y){
            Position pos = newPositions.get(e);
            if(pos == null){
                pos = getPosition(e);
                if(pos == null){
                    // TODO: Log
                    System.out.printf("Cannot set x for %s: not found in this world.", e.getName());
                    return;
                }
            }
            newPositions.put(e, pos.setY(y));
        }

        protected void addEntity(Entity e, Position pos){
            if(e instanceof MovableEntity){
                addEntityWithVelocity((MovableEntity)e, pos, new Velocity(0, 0));
            }

            if(!hasCollision(e, pos)){
                setPosition(e, pos);
                return;
            }
            
            // If it is delecate, destroy it right now
            if(e instanceof Delicate){
                removeEntity(e);
                return;
            }

            // Slightly move to avoid overlay, should work when overlaying with only one entity.
            // Try eight directions to find a suitable position.
            double w = e.getWidth();
            double h = e.getHeight();
            Position []newPositions = {pos.add(-w, 0), pos.add(+w, 0), pos.add(0, -h), pos.add(0, +h),
                pos.add(-w, -h), pos.add(-w, h), pos.add(w, -h), pos.add(w, h)};
            for(Position newPos : newPositions){
                if(!hasCollision(e, newPos)){
                    setPosition(e, pos);
                    return;
                }
            }

            // Give up. Try next time...
            registerUpdate(new Update(UpdateType.ONESHOT){
                @Override
                void update() {
                    addEntity(e, pos);
                    
                }
            }, UpdateOrder.ADD_ENTITY);
        }

        protected void addEntityWithVelocity(MovableEntity me, Position pos, Velocity v){
            if(!hasCollision(me, pos)){
                setPosition(me, pos);
                setVelocity(me, v);
                return;
            }
            
            // If it is delicate, destroy it right now
            if(me instanceof Delicate){
                removeEntity(me);
                return;
            }

            // Slightly move to avoid overlay, should work when overlaying with only one entity.
            // Try eight directions to find a suitable position.
            double w = me.getWidth();
            double h = me.getHeight();
            Position []newPositions = {pos.add(-w, 0), pos.add(+w, 0), pos.add(0, -h), pos.add(0, +h),
                pos.add(-w, -h), pos.add(-w, h), pos.add(w, -h), pos.add(w, h)};
            for(Position newPos : newPositions){
                if(!hasCollision(me, newPos)){
                    setPosition(me, pos);
                    setVelocity(me, v);
                    return;
                }
            }

            // Give up. Try next time...
            registerUpdate(new Update(UpdateType.ONESHOT){
                @Override
                void update() {
                    addEntityWithVelocity(me, pos, v);
                }
            }, UpdateOrder.ADD_ENTITY);
        }

        // e may haven't added to this world yet, in this case this method is used to trigger its destroy program.
        protected void removeEntity(Entity e){
            removedEntities.add(e);
            
            if(e instanceof CalabashBro)
                livingCalabash.remove((CalabashBro)e);
        }

    }
    
    @Getter
    private  Loader loader;

    private WorldStatus status = WorldStatus.SETUP;

    private ScheduledFuture<?> running = null;

    // Info
    @Setter
    private double width = 0;
    @Setter
    private double height = 0;

    // A state of the world consists of positions and velocities
    private  Map<Entity, Position> positions = new HashMap<>();
    private  Map<MovableEntity, Velocity> velocities = new HashMap<>();

    // A temporary state when updating, this should not be visible to others.
    private  Map<Entity, Position> newPositions = new HashMap<>();
    private  Map<MovableEntity, Velocity> newVelocities = new HashMap<>();
    private  List<Entity> removedEntities = new ArrayList<>();
    
    // Lock. To keep consistence of the state, considering it as a whole.
    private  ReadWriteLock stateLock = new ReentrantReadWriteLock();
    

    // Update means the change of state
    private  Queue<Update>[] updateQueues = new Queue[UpdateOrder.getTotalNum()];
    
    private List<CalabashBro> livingCalabash = new LinkedList<>();

    public World(Loader loader) {
        this.loader = loader;

        for(int i = 0;i < UpdateOrder.getTotalNum();i++){
            updateQueues[i] = new ConcurrentLinkedQueue<>();
        }
        
    }

    /**
     * 
     * @param map consists of unmovable entity, including the boundary of this world.
     */
    void setMap(String entityName, Position pos){
        switch(entityName){
            case "Concrete":
                positions.put(new Concrete(this), pos);
                break;
            case "Vertical Boundary":
                positions.put(new VerticalBoundary(this), pos);
                break;
            case "Horizontal Boundary":
                positions.put(new HorizontalBoundary(this), pos);
                break;
        }
        
    }

    public void setPlayers(){
        // TODO
        CalabashBroI broI = new CalabashBroI(this);
        CalabashBroIII broIII = new CalabashBroIII(this);
        livingCalabash.add(broI);
        livingCalabash.add(broIII);
    }

    /**
     * set players, register some infinite updates.
     */
    public void ready(){

        if(width == 0 || height == 0 || positions.isEmpty() || livingCalabash.isEmpty()){
            System.out.println("Setup not finished!");
            return;
        }

        for(CalabashBro bro : livingCalabash){
            velocities.put(bro, new Velocity(0,0));
            double randomX;
            double randomY;
            Position pos;
            do{
                randomX = -width / 2 + width * 0.5 * Math.random();
                randomY = height * 0.5 * Math.random();
                pos = new Position(randomX, randomY);
            }while(hasCollision(bro, pos));
            positions.put(bro, pos);
        }

        // Register gravity
        registerUpdate(new Update(World.UpdateType.INFINTE){
            @Override
            void update() {
                for(Map.Entry<MovableEntity, Velocity> entry : velocities.entrySet()){
                    MovableEntity me = entry.getKey();
                    double vy = entry.getValue().vy;
                    if(vy >= -Settings.MAX_FALL_SPEED){
                        vy -= Settings.GRAVITY / Settings.UPDATE_RATE;
                        setVelocityY(me, vy);
                    }    
                }
            }

        }, World.UpdateOrder.BASIC);

        // Register position update
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void update() {
                for(Map.Entry<MovableEntity, Velocity> entry : velocities.entrySet()){
                    MovableEntity me = entry.getKey();
                    Velocity v = entry.getValue();
                    Position pos = getPosition(me);
                    Position newPosition = new Position(pos.x + v.vx / Settings.UPDATE_RATE, pos.y + v.vy / Settings.UPDATE_RATE);
                    setPosition(me, newPosition);
                    
                }
            }

        }, World.UpdateOrder.BASIC);

        // register collision check
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void update() {
                
                LinkedList<MovableEntity> collideEntities = new LinkedList<>();
                do{
                    collideEntities.clear();

                    // We assume that last state is valid, then all the collisions are caused by enitities which change their positions.
                    for(Entity candidate : newPositions.keySet()){
                        if(!(candidate instanceof MovableEntity))
                            continue;
                        // Removed entities will not be considered.
                        if(removedEntities.contains(candidate))
                            continue;

                        Position pos = newPositions.get(candidate);
                        Velocity v = getVelocity(candidate);        // New positions are calculated based on old velocity;
                        //T B R L
                        int collision = getCollision(candidate, pos);
                        boolean reset = false;
                        if((collision & 1) != 0 && v.vx < 0){
                            setVelocityX((MovableEntity)candidate, 0);
                            reset = true;
                        }
                        if((collision & 2) != 0 && v.vx > 0){
                            setVelocityX((MovableEntity)candidate, 0);
                            reset = true;
                        }
                        if((collision & 4) != 0 && v.vy < 0){
                            setVelocityY((MovableEntity)candidate, 0);
                            reset = true;
                        }
                        if((collision & 8) != 0 && v.vy > 0){
                            setVelocityY((MovableEntity)candidate, 0);
                            reset = true;
                        }

                        if(reset)
                            collideEntities.add((MovableEntity)candidate);
                    }

                    // Reset position based on changed velocity
                    for(MovableEntity me : collideEntities){
                        Velocity v = newVelocities.get(me);
                        Position pos = getPosition(me);
                        Position newPosition = new Position(pos.x + v.vx / Settings.UPDATE_RATE, pos.y + v.vy / Settings.UPDATE_RATE);
                        setPosition(me, newPosition);

                        if(me instanceof Delicate){
                            // Remove delicate entity.
                            removeEntity(me);
                        }
                    }
                }while(!collideEntities.isEmpty());
                    
                
            }

        }, World.UpdateOrder.VALIDATION_CHECK); 

        status = WorldStatus.READY;
    }

    
    // All updates happen here. There is no other way to change the state of the world.
    // Updates must happen sequently.
    synchronized private void update(){
        
        try{
            stateLock.readLock().lock();

            newPositions.clear();
            newVelocities.clear();
            removedEntities.clear();
            
            for(int i = 0;i < UpdateOrder.getTotalNum();i++){

                Queue<Update> queue = updateQueues[i];
                Iterator<Update> it = queue.iterator();
                while(it.hasNext()){
                    Update update = it.next();
    
                    if(update.type == UpdateType.FINITE){
                        update.remain -= 1000 / Settings.UPDATE_RATE;
                    }
    
                    if(update.type == UpdateType.ONESHOT || update.type == UpdateType.FINITE && update.remain <= 0){
                        it.remove();
                    }
                    
                    update.update();
    
                }
            } 
        }catch(RuntimeException e){
            throw e;
        } finally{
            
            stateLock.readLock().unlock();
        }

        
        // Do Update
        try{
            stateLock.writeLock().lock();
            
            for(Map.Entry<Entity, Position> entry : newPositions.entrySet()){
                positions.put(entry.getKey(), entry.getValue());
            }
        
            for(Map.Entry<MovableEntity, Velocity> entry : newVelocities.entrySet()){
                velocities.put(entry.getKey(), entry.getValue());
            }

            // Remove enities;
            for(Entity e:removedEntities){
                positions.remove(e);
                if(e instanceof MovableEntity){
                    velocities.remove((MovableEntity)e);
                }

                if(e instanceof Destroyable){
                    ((Destroyable)e).destroy();
                }

                // TODO: Remove events that bind to this entity.
            }
        }catch(RuntimeException e){
            throw e;
        }finally{
            
            stateLock.writeLock().unlock();
        }
        
        // Only one character left, end this world.
       
        if(livingCalabash.size() == 1){
            System.out.printf("%s win\n", livingCalabash.get(0).getName());
            end();
        }

        // DEBUG
        for(CalabashBro calabash : livingCalabash){
            System.out.printf("%s\tpos: %s\tv: %s\thp: %.1f\n", calabash.getName(), positions.get(calabash).toString(), velocities.get(calabash).toString(), calabash.getHp());
        }
        // for(Map.Entry<Entity, Position> entry : positions.entrySet()){
        //     System.out.printf("%s\tpos: %s\n", entry.getKey().getName(), entry.getValue().toString());
        // }

    }
    
    public void resume(){
        if(status != WorldStatus.READY){
            System.out.println("This world is not ready!");
            return;
        }
        
        running = ThreadPool.scheduled.scheduleAtFixedRate(() -> {
            // TODO: exception handler
            try{
                this.update();
            }catch(RuntimeException e){
                e.printStackTrace();
            }
            
        }, 0, 1000 / Settings.UPDATE_RATE, TimeUnit.MILLISECONDS);
        
    }

    public void pause(){
        if(status != WorldStatus.RUNNING){
            System.out.println("This world is not running!");
            return;
        }
        running.cancel(false);
        running = null;
        status = WorldStatus.READY;
    }

    private void end(){
        if(status != WorldStatus.RUNNING){
            System.out.println("This world is not running!");
            return;
        }
        running.cancel(false);
        running = null;
        status = WorldStatus.END;
    }

    void registerUpdate(Update e, UpdateOrder order){
        updateQueues[order.getPriority()].add(e);
    }

    
     // return the direction A and B collide (from A's view) at the specified position.
     // T B R L
     // We assume that all entities are rectangular
    private int collide(Entity a, Position posA, Entity b, Position posB){
        double deltaX = Math.abs(posA.x - posB.x);
        double deltaY = Math.abs(posA.y - posB.y);
        double leastX = (a.getWidth() + b.getWidth()) / 2;
        double leastY = (a.getHeight() + b.getHeight()) / 2;
        double x = deltaX - leastX;
        double y = deltaY - leastY;
        if(x <= 0 && y <= 0){
            if(x * leastY < y * leastX){
                // more inside in x-direction
                // Top or Bottome
                if(posA.y < posB.y)
                    return 8;
                else
                    return 4;
            }else{
                // Left or Right
                if(posA.x < posB.x)
                    return 2;
                else
                    return 1;
            }
        }else
            return 0;

    }

    private int getCollision(Entity e, Position pos){
        int collision = 0;
        for(Entity e2 : positions.keySet()){

            if(removedEntities.contains(e2))
                continue;

            Position pos2 = newPositions.get(e2);
            if(pos2 == null)
                pos2 = getPosition(e2);
            
            collision |= collide(e, pos, e2, pos2);
                
        }
        return collision;
    }
    
    private boolean hasCollision(Entity e, Position pos){
        return getCollision(e, pos) != 0;
    }

    /**
     * 
     * @param me
     * @return null if no such entity.
     */
    Velocity getVelocity(Entity me){

        Velocity v = null;
        try{
            stateLock.readLock().lock();
            v = velocities.get(me);
            
        }catch(RuntimeException e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }

        return v;
    }


    /**
     * 
     * @param me
     * @return null if no such entity
     */
    Position getPosition(Entity me){

        Position pos = null;
        try{
            stateLock.readLock().lock();
            pos = positions.get(me);
            
        }catch(RuntimeException e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }

        return pos;
    }

    /**
     * 
     * @param e get enities around whom
     * @param condition in what area
     * @return
     */
    Set<Entity> getEntityAround(Entity e, Predicate<Position> condition){
        Position ePos = null;
        Set<Entity> result = new HashSet<>();

        ePos = getPosition(e);

        if(ePos != null){
            for(Map.Entry<Entity, Position> entry : positions.entrySet()){
                Position pos = entry.getValue();
                if(condition.test(new Position(pos.x - ePos.x, pos.y - ePos.y))){
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }
}
