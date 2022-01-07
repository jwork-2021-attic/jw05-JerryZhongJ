package io.github.jerryzhongj.calabash_brothers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import io.github.jerryzhongj.calabash_brothers.server.World;




public class Loader {
    private Map<String, Object> cachedPool = new HashMap<>();
    // public int[][] loadBoundary(String entityName){
    //     int[][] cache = (int[][])cachedPool.get("Boundary:"+entityName);
    //     if(cache != null)
    //         return cache;
    //     LinkedList<int[]> tmp = new LinkedList<>();
    //     try (InputStream in = getClass().getResourceAsStream("/Boundaries/"+entityName);
    //         BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {

    //         String line = null;
    //         while((line = reader.readLine()) != null){
    //             String []coordStr = line.split("\\s");
    //             int[] coord = {Integer.valueOf(coordStr[0]), Integer.valueOf(coordStr[1])};
    //             tmp.add(coord);
    //         }

    //         cache = tmp.toArray(new int[tmp.size()][]);
    //         cachedPool.put("Boundary:"+entityName, cache);
            

    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //         System.exit(1);
    //     }
    //     return cache;
    // }

    public World loadInitialWorld(String mapName){
 
        World world = new World(this);
        double width = 0;
        double height = 0;
        try(InputStream in = getClass().getResourceAsStream("/Maps/"+mapName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))){
            String line = null;
            while((line = reader.readLine()) != null){
                String[] tokens = line.split("\\s*:\\s*");
                switch(tokens[0]){
                    case "width":
                        width = Double.valueOf(tokens[1]);
                        world.setWidth(width);
                        break;
                    case "height":
                        height = Double.valueOf(tokens[1]);
                        world.setHeight(height);
                        break;
                    default:
                        String[] coord = tokens[1].split("\\s");
                        Double x = Double.valueOf(coord[0]);
                        Double y = Double.valueOf(coord[1]);
                        world.setMap(tokens[0], x, y);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        for(double i =  0;i + Settings.BOUNDARY_LONG < width;i += Settings.BOUNDARY_LONG){
            double x = -width / 2 + i + Settings.BOUNDARY_LONG / 2;
            world.setMap("Horizontal Boundary", x, -Settings.BOUNDARY_SHORT / 2);
            world.setMap("Horizontal Boundary", x, height + Settings.BOUNDARY_SHORT / 2);
        }
        for(double i = 0;i + Settings.BOUNDARY_LONG < height;i += Settings.BOUNDARY_LONG){
            world.setMap("Vertical Boundary", -width / 2 - Settings.BOUNDARY_SHORT / 2, i + Settings.BOUNDARY_LONG / 2);
            world.setMap("Vertical Boundary", width / 2 + Settings.BOUNDARY_SHORT / 2, i + Settings.BOUNDARY_LONG / 2);
        }


        return world;
    }

    public World loadSavedWorld(String backUpName){
        // TODO
        return null;
    }

    public double loadEntityWidth(EntityType type){
        Double width = (Double)cachedPool.get("EntityWidth:"+type.getName());
        if(width != null)
            return width;
        else{
            loadEntityInfo(type);
            return (Double)cachedPool.get("EntityWidth:"+type.getName());
        }
    }

    public double loadEntityHeight(EntityType type){
        Double height = (Double)cachedPool.get("EntityHeight:"+type.getName());
        if(height != null)
            return height;
        else{
            loadEntityInfo(type);
            return (Double)cachedPool.get("EntityHeight:"+type.getName());
        }
    }

    public double loadEntityOffsetX(EntityType type){
        Double offsetx = (Double)cachedPool.get("EntityOffsetX:"+type.getName());
        if(offsetx != null)
            return offsetx;
        else{
            loadEntityInfo(type);
            return (Double)cachedPool.get("EntityOffsetX:"+type.getName());
        }
    }

    public double loadEntityOffsetY(EntityType type){
        Double offsety = (Double)cachedPool.get("EntityOffsetY:"+type.getName());
        if(offsety != null)
            return offsety;
        else{
            loadEntityInfo(type);
            return (Double)cachedPool.get("EntityOffsetY:"+type.getName());
        }
    }
    
    private void loadEntityInfo(EntityType type){
        String name = type.getName();
        try(InputStream in = getClass().getResourceAsStream("/Entities/"+name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))){
            String line = reader.readLine();
            String[] tokens = line.split("\\s");
            cachedPool.put("EntityWidth:"+name, Double.valueOf(tokens[0]));
            cachedPool.put("EntityHeight:"+name, Double.valueOf(tokens[1]));
            cachedPool.put("EntityOffsetX:"+name, Double.valueOf(tokens[2]));
            cachedPool.put("EntityOffsetY:"+name, Double.valueOf(tokens[3]));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
