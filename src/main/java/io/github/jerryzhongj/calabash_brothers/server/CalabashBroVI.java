package io.github.jerryzhongj.calabash_brothers.server;

import lombok.Getter;

class CalabashBroVI extends CalabashBro{
    
    @Getter
    private boolean invisible = false;
    
    CalabashBroVI(World world, String name) {
        super(world, name);
    }

    @Override
    public void superfy() {
        // TODO Auto-generated method stub
        
    }
    
}
