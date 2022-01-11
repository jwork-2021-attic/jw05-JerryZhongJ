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

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Loader;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.ThreadPool;
import io.github.jerryzhongj.calabash_brothers.server.CalabashBro.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


// 这个世界是连续的，以像素为长度单位
// 坐标以地面的中心为原点
public class World implements Serializable{
    private static final long serialVersionUID = 1403870569483406173L;

    public enum WorldStatus{
        // END means the game finish, however STOP means world was forcely stop.
        PAUSE, RUNNING, END
    }

    @AllArgsConstructor
    public static class Position{
        public final double x, y;

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
    public static class Velocity{
        
        public final double vx, vy;

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

    public enum UpdateOrder{
        // Should start with 0
        // The later update covers the earlier.
        // Due to overlaying problem, we have to put adding entity the last one.
        BASIC(0), CALABASH_ACTION(1), CAlABASH_SUPER(2), REMOVE_ENTITY(3), VALIDATION_CHECK(4), ADD_ENTITY(5);
        @Getter
        private final int priority;
        private UpdateOrder(int priority){
            this.priority = priority;
        }
        public static int getTotalNum(){
            return UpdateOrder.values().length;
        }
    }

    public enum UpdateType{
        ONESHOT, FINITE, INFINTE, BINDTO
    }

    // All changes happen here
    // New state is computed based on the older one.
    public abstract class Update{
        
        public final UpdateType type;
        // TimeUnit: millisecond
        public int remain;
        // TODO: BINDTO
        public Update(UpdateType type, int remain){
            this.type = type;
            this.remain = remain;
        }
        public Update(UpdateType type){
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
                    System.out.printf("Cannot set add v for %s: not found in this world.", me.getType().getName());
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
                    System.out.printf("Cannot set vx for %s: not found in this world.", me.getType().getName());
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
                    System.out.printf("Cannot set vy for %s: not found in this world.", me.getType().getName());
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
                    System.out.printf("Cannot set x for %s: not found in this world.", e.getType().getName());
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
                    System.out.printf("Cannot set x for %s: not found in this world.", e.getType().getName());
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

    private WorldStatus status = WorldStatus.PAUSE;

    private ScheduledFuture<?> running = null;

    // Info
    @Setter
    private double width = 0;
    @Setter
    private double height = 0;
    @Setter
    private String background;

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
        
        // Register gravity
        registerUpdate(new Update(World.UpdateType.INFINTE){
            @Override
            void update() {
                for(Map.Entry<MovableEntity, Velocity> entry : velocities.entrySet()){
                    MovableEntity me = entry.getKey();
                    double vy = entry.getValue().vy;
                    if(vy >= -Settings.MAX_SPEED){
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


        // Register character moving
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void update() {
                    for(CalabashBro bro:livingCalabash){
                        switch(bro.movingStatus){
                            case MOVING_LEFT:
                                setVelocityX(bro, -Settings.DEFAULT_SPEED);
                                break;
                            case MOVING_RIGHT:
                                setVelocityX(bro, Settings.DEFAULT_SPEED);
                                break;
                            case INERTIA:
                                // Then this character should keep his intertia
                                break;
                        }
                    }
                
            }

        }, World.UpdateOrder.CALABASH_ACTION);
        // register collision check
        registerUpdate(new Update(World.UpdateType.INFINTE){

            @Override
            void update() {
                
                LinkedList<MovableEntity> collideEntities = new LinkedList<>();
                do{
                    collideEntities.clear();

                    for(Entity candidate : newPositions.keySet()){
                        if(!(candidate instanceof MovableEntity))
                            continue;
                        // Removed entities will not be considered.
                        if(removedEntities.contains(candidate))
                            continue;
                        MovableEntity me = (MovableEntity)candidate;
                        Position pos = newPositions.get(candidate);
                        Velocity v = newVelocities.get(candidate);
                        if(v == null)
                            v = getVelocity(candidate);

                        //T B R L
                        int collision = getCollision(candidate, pos);
                        boolean reset = false;

                        double fraction = Settings.FRACTION / Settings.UPDATE_RATE;
                        double yAfterFraction = (Math.abs(v.vy) - fraction > 0? Math.abs(v.vy) - fraction : 0) * (v.vy > 0?1:-1);
                        double xAfterFraction = (Math.abs(v.vx) - fraction > 0? Math.abs(v.vx) - fraction : 0) * (v.vx > 0?1:-1);
                        if((collision & 1) != 0 && v.vx < 0){
                            setVelocity(me, new Velocity(0, yAfterFraction));
                            reset = true;
                        }
                        if((collision & 2) != 0 && v.vx > 0){
                            setVelocity(me, new Velocity(0, yAfterFraction));
                            reset = true;
                        }

                        // If character is running, then there is no fraction effect.
                        if((collision & 4) != 0 && v.vy < 0){
                            if(me instanceof CalabashBro && ((CalabashBro)me).movingStatus != Status.INERTIA)
                                setVelocityY(me, 0);
                            else
                                setVelocity(me, new Velocity(xAfterFraction, 0));
                            reset = true;
                        }
                        if((collision & 8) != 0 && v.vy > 0){
                            if(me instanceof CalabashBro && ((CalabashBro)me).movingStatus != Status.INERTIA)
                                setVelocityY(me, 0);
                            else
                                setVelocity(me, new Velocity(xAfterFraction, 0));
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
    }

    /**
     * 
     * @param map consists of unmovable entity, including the boundary of this world.
     */
    public void setMap(String entityName, double x, double y){
        Position pos = new Position(x, y);
        switch(entityName){
            case "Earth":
                positions.put(new Earth(this), pos);
                break;
            case "Vertical Boundary":
                positions.put(new VerticalBoundary(this), pos);
                break;
            case "Horizontal Boundary":
                positions.put(new HorizontalBoundary(this), pos);
                break;
            // TODO: more types
        }
        
    }

    public void setPlayers(Player []players){
        for(Player player : players){
            setPlayer(player);
        }
    }

    public void setPlayer(Player player){
        // TODO: more brothers
        CalabashBro bro = null;
        String name = player.getName();
        switch(player.getCalabashType()){
            case CALABASH_BRO_I:
                bro = new CalabashBroI(this, name);
                break;
            case CALABASH_BRO_II:
                bro = new CalabashBroII(this, name);
                break;
            case CALABASH_BRO_III:
                bro = new CalabashBroIII(this, name);
                break;
            case CALABASH_BRO_VI:
                bro = new CalabashBroVI(this, name);
                break;
            case CALABASH_BRO_VII:
                bro = new CalabashBroVII(this, name);
                break;
            default:
                return;
        }
        player.control(bro);
        addCalabash(bro);
        
    }
    
    public void addCalabash(CalabashBro bro){
        livingCalabash.add(bro);
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
    
    // All updates happen here. There is no other way to change the state of the world.
    // Updates must happen sequently.
    private void update(){
        if(status != WorldStatus.RUNNING)
            return;

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
        }catch(Exception e){
            e.printStackTrace();
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
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }finally{
            
            stateLock.writeLock().unlock();
        }
        
        // Only one character left, end this world.
       
        if(livingCalabash.size() < 2){
            end();
        }

        // // DEBUG
        // for(CalabashBro calabash : livingCalabash){
        //     System.out.printf("%s\tpos: %s\t\tv: %s\t\thp: %.1f\n", calabash.getName(), positions.get(calabash).toString(), velocities.get(calabash).toString(), calabash.getHp());
        // }
       
        
        // for(Map.Entry<Entity, Position> entry : positions.entrySet()){
        //     System.out.printf("%s\tpos: %s\n", entry.getKey().getName(), entry.getValue().toString());
        // }

    }
    
    public void resume(){
        if(status != WorldStatus.PAUSE)
            System.out.println("Can not resume: This world is running or has ended");

        if(width == 0 || height == 0 || positions.isEmpty() || livingCalabash.isEmpty()){
            System.out.println("Setup not finished!");
            return;
        }
        status = WorldStatus.RUNNING;
        
        running = ThreadPool.scheduled.scheduleAtFixedRate(() -> {
            // TODO: exception handler
            try{
                update();
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
        status = WorldStatus.PAUSE;
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

    public void registerUpdate(Update e, UpdateOrder order){
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
        double x = leastX - deltaX;
        double y = leastY - deltaY;
        if(x >= 0 && y >= 0){
            if(x  > y){
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
            if(e2 == e)
                continue;

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
    public Velocity getVelocity(Entity me){

        Velocity v = null;
        try{
            stateLock.readLock().lock();
            v = velocities.get(me);
            
        }catch(Exception e){
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
    public Position getPosition(Entity me){

        Position pos = null;
        try{
            stateLock.readLock().lock();
            pos = positions.get(me);
            
        }catch(Exception e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }

        return pos;
    }

    /**
     * 
     * @param calabashBro get enities around whom
     * @param condition in what area
     * @return
     */
    public Set<Entity> getEntityAround(CalabashBro calabashBro, Predicate<Position> condition){
        Position ePos = null;
        Set<Entity> result = new HashSet<>();
        try{
            stateLock.readLock().lock();
            ePos = positions.get(calabashBro);

            if(ePos != null){
                for(Map.Entry<Entity, Position> entry : positions.entrySet()){
                    Position pos = entry.getValue();
                    Entity entity = entry.getKey();
                    if(entity != calabashBro && condition.test(new Position(pos.x - ePos.x, pos.y - ePos.y))){
                        result.add(entry.getKey());
                    }
                }
            }
        }catch(Exception re){
            throw re;
        }finally{
            stateLock.readLock().unlock();
        }
        
        return result;
    }

    public SnapShot getSnapShot(){
        SnapShot snapshot = new SnapShot();

        snapshot.width = width;
        snapshot.height = height;
        snapshot.background = background;

        // No boundary
        try{
            stateLock.readLock().lock();
            for(Map.Entry<Entity, Position> entry : positions.entrySet()){
                Entity e = entry.getKey();
                Position pos = entry.getValue();
                if(e.getType() == EntityType.FAKE)
                    continue;
                snapshot.positions.put(e, pos);
            }
        }catch(Exception e){
            throw e;
        }finally{
            stateLock.readLock().unlock();
        }
        
        for(CalabashBro bro:livingCalabash){
            snapshot.calabashes.put(bro,  new SnapShot.CalabashStatus(bro.getHp(), bro.getMp(), bro.isFacingRight(), bro.isSuperMode()));
        }

        if(livingCalabash.size() == 1)
            snapshot.winner = livingCalabash.get(0).getName();
        
        if(livingCalabash.size() == 0)
            snapshot.winner = "nobody";

        return snapshot;
    }
}
