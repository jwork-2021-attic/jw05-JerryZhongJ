package io.github.jerryzhongj.calabash_brothers.server;


import lombok.Getter;


class Entity {
    

    @Getter
    private final double height;
    @Getter
    private final double width;
    protected final World world;
    @Getter
    private final String name;
    // @Getter
    // private final double radius;
    Entity(World world, String name, double height, double width) {
        this.height = height;
        this.width = width;
        this.world = world;
        this.name = name;

        // check connective
        // for(int i = 0;i < boundary.length - 1;i++){
        //     int[] coord1 = boundary[i];
        //     int[] coord2 = boundary[i+1];
            
        //     if(coord1.length != 2 && coord2.length != 2)
        //         throw new RuntimeException("Boundary of" + name + "should be a list of coordinates");

        //     if(coord1[0] == coord2[0] - 1 && coord1[1] == coord2[1] || coord1[0] == coord2[0]  && coord1[1] == coord2[1] + 1 || coord1[0] == coord2[0] + 1 && coord1[1] == coord2[1] || coord1[0] == coord2[0]  && coord1[1] == coord2[1] - 1)
        //         ;
        //     else
        //         throw new RuntimeException("Boundary of" + name + "doesn't meet 4-connectivity");

        // }

        // // get radius
        // double maxDis = 0;
        // for(int i = 0;i < boundary.length;i++){
        //     int []coord = boundary[i];
            
        //     int x0 = coord[0];
        //     int x1 = x0 + 1;

        //     int y0 = coord[1];
        //     int y1 = y0 + 1;

        //     int x2_0 = x0 * x0;
        //     int x2_1 = x1 * x1; 
        //     int y2_0 = y0 * y0;
        //     int y2_1 = y1 * y1; 
        //     // 4 corner
        //     double d0 = Math.sqrt(x2_0 + y2_0);
        //     double d1 = Math.sqrt(x2_1 + y2_0);
        //     double d2 = Math.sqrt(x2_0 + y2_1);
        //     double d3 = Math.sqrt(x2_1 + y2_1);

        //     if(d0 > maxDis)
        //         maxDis = d0;
        //     if(d1 > maxDis)
        //         maxDis = d1;
        //     if(d2 > maxDis)
        //         maxDis = d2;
        //     if(d3 > maxDis)
        //         maxDis = d3;

        // }
        // radius = maxDis;
    }


}
