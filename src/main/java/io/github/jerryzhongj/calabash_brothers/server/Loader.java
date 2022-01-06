package io.github.jerryzhongj.calabash_brothers.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;




public class Loader {

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
                        world.setMap(tokens[0], new World.Position(x, y));
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        
        // Add boundary to this world
        for(double i =  0;i + Settings.BOUNDARY_LONG < width;i += Settings.BOUNDARY_LONG){
            double x = -width / 2 + i + Settings.BOUNDARY_LONG / 2;
            world.setMap("Horizontal Boundary", new World.Position(x, -Settings.BOUNDARY_SHORT / 2));
            world.setMap("Horizontal Boundary", new World.Position(x, height + Settings.BOUNDARY_SHORT / 2));
        }
        for(double i = 0;i + Settings.BOUNDARY_LONG < height;i += Settings.BOUNDARY_LONG){
            world.setMap("Vertical Boundary", new World.Position(-width / 2 - Settings.BOUNDARY_SHORT / 2, i + Settings.BOUNDARY_LONG / 2));
            world.setMap("Vertical Boundary", new World.Position(width / 2 + Settings.BOUNDARY_SHORT / 2, i + Settings.BOUNDARY_LONG / 2));
        }



        // Configure world
        
        
        return world;
    }

    public World loadSavedWorld(String backUpName){
        // TODO
        return null;
    }
}
