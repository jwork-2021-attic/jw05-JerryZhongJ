package io.github.jerryzhongj.calabash_brothers.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


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

    @AllArgsConstructor
    public static class Position{
        @Getter
        @Setter
        double x, y;

        public Position clone(){
            return new Position(x, y);
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
    }

    @AllArgsConstructor
    public static class Velocity{
        @Getter
        @Setter
        double vx, vy;
        public Velocity clone(){
            return new Velocity(vx, vy);
        }
    }

    enum UpdateOrder{
        ADD_ENTITY(0), BASIC(1), CALABASH_ACTION(2), CAlABASH_SUPER(3), REMOVE_ENTITY(4), VALIDATION_CHECK(5);
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
        ONESHOT, FINITE, INFINTE
    }

    
    abstract class Update implements Comparable<Update>{
        
        final UpdateType type;
        // TimeUnit: millisecond
        int remain;
        Update(UpdateType type, int remain){
            this.type = type;
            this.remain = remain;
        }
        Update(UpdateType type){
            this(type, 0);
        }
        abstract void run();
        public int compareTo(Update c){
            return 0;
        }

        // Some update utils
        protected void setVelocityX(MovableEntity me, double vx){
            Velocity v = newVelocities.get(me);
            if(v == null){
                v = getVelocity(me);
                newVelocities.put(me, v);
            }
            
            v.vx = vx;
        }

        protected void setVelocityY(MovableEntity me, double vy){
            Velocity v = newVelocities.get(me);
            if(v == null){
                v = getVelocity(me);
                newVelocities.put(me, v);
            }
            v.vy = vy;
        }

        protected void setPositionX(MovableEntity me, double x){
            Position pos = newPositions.get(me);
            if(pos == null){
                pos = getPosition(me);
                newPositions.put(me, pos);
            }
            pos.x = x;
        }

        protected void setPositionY(MovableEntity me, double y){
            Position pos = newPositions.get(me);
            if(pos == null){
                pos = getPosition(me);
                newPositions.put(me, pos);
            }
            pos.y = y;
        }

        protected void resetVelocity(MovableEntity me){
            newVelocities.remove(me);
        }

        protected void resetPosition(MovableEntity me){
            newPositions.remove(me);
        }

    }
    @Getter
    private  Loader loader;

    // Info
    @Setter
    private double width;
    @Setter
    private double height;

    // A state of the world consists of positions and velocities
    private  Map<Entity, Position> positions = new HashMap<>();
    private  Map<MovableEntity, Velocity> velocities = new HashMap<>();

    // A temporary state when updating
    private  Map<MovableEntity, Position> newPositions = new HashMap<>();
    private  Map<MovableEntity, Velocity> newVelocities = new HashMap<>();
    private  List<Entity> removedEntities = new ArrayList<>();
    
    // Locks to  
    private  ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private  ReadWriteLock newStateLock = new ReentrantReadWriteLock();

    // Update means the change of state
    private  Queue<Update>[] updateQueues = new Queue[UpdateOrder.getTotalNum()];
    
    private Set<CalabashBro> livingCalabash = new HashSet<>();

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
    void setMap(Map<String, Position> map){
        for(Map.Entry<String, Position> record : map.entrySet()){
            String entityName = record.getKey();
            Position pos = record.getValue();
            switch(entityName){
                case "Concrete":
                    positions.put(new Concrete(this), pos.clone());
            }
        }
    }
    void setPlayers(){
        ;
    }
    /**
     * set players, register some infinite updates.
     */
    void ready(){

        // TODO: Randomly initial player's position

        // Register gravity
        registerUpdate(new Update(World.UpdateType.INFINTE){
            @Override
            void run() {
                for(Map.Entry<MovableEntity, Velocity> entry : velocities.entrySet()){
                    MovableEntity me = entry.getKey();
                    double vy = entry.getValue().vy;
                    if(vy >= -Settings.MAX_FALL_SPEED){
                        vy -= Settings.GRAVITY / Settings.FPS;
                        setVelocityY(me, vy);
                    }    
                }
            }

        }, World.UpdateOrder.BASIC);

        // Register position update
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void run() {
                for(Map.Entry<MovableEntity, Velocity> entry : velocities.entrySet()){
                    MovableEntity me = entry.getKey();
                    Velocity v = entry.getValue();
                    Position pos = getPosition(me);
                    
                    setPositionX(me, pos.x + v.vx / Settings.FPS);
                    setPositionY(me, pos.y + v.vy / Settings.FPS);
                    
                }
            }

        }, World.UpdateOrder.BASIC);

        // register collision check
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void run() {
                boolean valid = false;
                while(!valid)
                    valid = true;
                    for(MovableEntity candidate : newPositions.keySet()){
                        Position cPos = newPositions.get(candidate);
                        double cRadius = candidate.getRadius();
                        for(Entity e : positions.keySet()){
                            Position pos = newPositions.get(e);
                            if(pos == null)
                                pos = positions.get(e);
                            double dis = pos.disFrom(cPos);
                            if(dis <= cRadius + e.getRadius() && collide(candidate, e)){
                                resetPosition(candidate);
                                setVelocityX(candidate, 0);
                                setVelocityY(candidate, 0);
                                if(e instanceof MovableEntity){
                                    setVelocityX((MovableEntity)e, 0);
                                    setVelocityY((MovableEntity)e, 0);
                                }
                                break;
                            }
                                
                        }
                    }
                
            }

        }, World.UpdateOrder.VALIDATION_CHECK); 
    }

    /**
     * All updates happen here. There is no other way to change the state of the world.
     */
    void run(){
        
        ThreadPool.scheduled.scheduleAtFixedRate(()->{
            try{
                stateLock.readLock().lock();
                newStateLock.writeLock().lock();

                newPositions.clear();
                newVelocities.clear();
                removedEntities.clear();
                
                for(int i = 0;i < UpdateOrder.getTotalNum();i++){
                    Queue<Update> queue = updateQueues[i];
                    Iterator<Update> it = queue.iterator();
                    while(it.hasNext()){
                        Update update = it.next();
        
                        if(update.type == UpdateType.FINITE){
                            update.remain -= 1000 / 60;
                        }
        
                        if(update.type == UpdateType.ONESHOT || update.type == UpdateType.FINITE && update.remain <= 0){
                            it.remove();
                        }
                        
                        update.run();
        
                    }
                } 
            }catch(RuntimeException e){
                throw e;
            } finally{
                newStateLock.writeLock().unlock();
                stateLock.readLock().unlock();
            }

            
            // update
            try{
                stateLock.writeLock().lock();
                newStateLock.readLock().lock();
                for(Map.Entry<MovableEntity, Position> entry : newPositions.entrySet()){
                    positions.put(entry.getKey(), entry.getValue());
                }
            
                for(Map.Entry<MovableEntity, Velocity> entry : newVelocities.entrySet()){
                    velocities.put(entry.getKey(), entry.getValue());
                }

                // remove enities;
                for(Entity e:removedEntities){
                    positions.remove(e);
                    if(e instanceof MovableEntity){
                        velocities.remove((MovableEntity)e);
                    }
                }
            }catch(RuntimeException e){
                throw e;
            }finally{
                newStateLock.readLock().unlock();
                stateLock.writeLock().unlock();
            }

            

            if(livingCalabash.size() == 1){
                // TODO: End this world
            }
              
        }, 0, 1000 / Settings.FPS, TimeUnit.MILLISECONDS);
    }

    void stop(){
        // TODO 
    }

    void registerUpdate(Update e, UpdateOrder order){
        updateQueues[order.getPriority()].add(e);
    }

    /**
     * @param a Entity A
     * @param b Entity B
     * @return whether they collide
     */
    boolean collide(Entity a, Entity b){
        Position posA = null;
        Position posB = null;
        try{
            stateLock.readLock().lock();
            posA = positions.get(a);
            posB = positions.get(b);
        }catch(RuntimeException e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }
        
        if(posA == null || posB == null)
            throw new RuntimeException("No such entity in positions");
        
        int [][]boundA = a.getBoundary();
        int [][]boundB = b.getBoundary();
        for(int []coordA : boundA){
            for(int []coordB : boundB){
                if(Math.abs(coordB[0] - coordA[0] + posB.x - posA.x) < 1 &&
                   Math.abs(coordB[1] - coordA[1] + posB.y - posA.y) < 1)
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param me
     * @return the COPY velocity of specified entity.
     */
    protected Velocity getVelocity(MovableEntity me){

        Velocity v = null;
        try{
            stateLock.readLock().lock();
            v = velocities.get(me);
            
        }catch(RuntimeException e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }

        if(v == null)
            throw new RuntimeException("No such movable enitity in velocities");
        return v.clone();
    }


    /**
     * 
     * @param me
     * @return the COPY position of specified entity.
     */
    protected Position getPosition(MovableEntity me){

        Position pos = null;
        try{
            stateLock.readLock().lock();
            pos = positions.get(me);
            
        }catch(RuntimeException e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }

        if(pos == null)
            throw new RuntimeException("No such movable enitity in velocities");
        return pos.clone();
    }

    /**
     * 
     * @param e get enities around whom
     * @param condition in what area
     * @return
     */
    protected Set<Entity> getEntityAround(Entity e, Predicate<Position> condition){
        Position ePos = null;
        Set<Entity> result = new HashSet<>();

        try{
            stateLock.readLock().lock();
            ePos = positions.get(e);
            
        }catch(RuntimeException exception){
            throw exception;
        }finally{
            stateLock.readLock().unlock();
        }   

        if(ePos == null)
                throw new RuntimeException("No such entity in positions");
            for(Map.Entry<Entity, Position> entry : positions.entrySet()){
                Position pos = entry.getValue();
                if(condition.test(new Position(pos.x - ePos.x, pos.y - ePos.y))){
                    result.add(entry.getKey());
                }
            }
        return result;
    }
}
