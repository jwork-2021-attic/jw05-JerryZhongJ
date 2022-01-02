package io.github.jerryzhongj.calabash_brothers.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;




public class Loader {
    private final Map<String, Object> cachedPool = new HashMap<>();
   

    public int[][] loadBoundary(String entityName){
        int[][] cache = (int[][])cachedPool.get("Boundary:"+entityName);
        if(cache != null)
            return cache;
        LinkedList<int[]> tmp = new LinkedList<>();
        try (InputStream in = getClass().getResourceAsStream("/Boundaries/"+entityName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {

            String line = null;
            while((line = reader.readLine()) != null){
                String []coordStr = line.split("\\s");
                int[] coord = {Integer.valueOf(coordStr[0]), Integer.valueOf(coordStr[1])};
                tmp.add(coord);
            }

            cache = tmp.toArray(new int[tmp.size()][]);
            cachedPool.put("Boundary:"+entityName, cache);
            

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        return cache;
    }

    public World loadInitialWorld(String mapName){
        Map<String, World.Position> cacheMap = (Map<String, World.Position>)cachedPool.get("Map:"+mapName);
        Double cacheWidth = (Double)cachedPool.get("Map Width:"+mapName);
        Double cacheHeight = (Double)cachedPool.get("Map Height:"+mapName);
        World world = new World(this);
        if(cacheMap == null){
            cacheMap = new HashMap<>();
            try(InputStream in = getClass().getResourceAsStream("/Maps/"+mapName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))){
                String line = null;
                while((line = reader.readLine()) != null){
                    String[] tokens = line.split("\\s*:\\s*");
                    switch(tokens[0]){
                        case "width":
                            cacheWidth = Double.valueOf(tokens[1]);
                            break;
                        case "height":
                            cacheHeight = Double.valueOf(tokens[1]);
                            break;
                        default:
                            String[] coord = tokens[1].split("\\s");
                            Double x = Double.valueOf(coord[0]);
                            Double y = Double.valueOf(coord[1]);
                            cacheMap.put(tokens[0], new World.Position(x, y));
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
                System.exit(1);
            }
            
            // Add boundary to this world
            for(double i =  -cacheWidth / 2;i + Settings.BOUDARY_LONG < cacheWidth / 2;i += Settings.BOUDARY_LONG){
                cachedPool.put("Horizontal Boundary", new World.Position(i + Settings.BOUDARY_LONG / 2, -Settings.BOUDARY_SHORT / 2));
                cachedPool.put("Horizontal Boundary", new World.Position(i + Settings.BOUDARY_LONG / 2, cacheHeight + Settings.BOUDARY_SHORT / 2));
            }
            for(double i = 0;i + Settings.BOUDARY_LONG < cacheHeight;i += Settings.BOUDARY_LONG){
                cachedPool.put("Vertical Boundary", new World.Position(-cacheWidth / 2 - Settings.BOUDARY_SHORT / 2, i + Settings.BOUDARY_LONG / 2));
                cachedPool.put("Vertical Boundary", new World.Position(cacheWidth / 2 + Settings.BOUDARY_SHORT / 2, i + Settings.BOUDARY_LONG / 2));
            }

            cachedPool.put("Map Width:"+mapName, cacheWidth);
            cachedPool.put("Map Height:"+mapName, cacheHeight);
            cachedPool.put("Map:"+mapName, cacheMap);
        }

        // Configure world
        world.setWidth(cacheWidth);
        world.setHeight(cacheHeight);
        world.setMap(cacheMap);
        return world;
    }

    public World loadSavedWorld(String backUpName){
        // TODO
        return null;
    }
}
