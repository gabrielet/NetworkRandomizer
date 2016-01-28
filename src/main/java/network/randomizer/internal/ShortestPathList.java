/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.LinkedList;

/**
 *
 * @author gabriele
 */
class ShortestPathList extends LinkedList{
    
    int length;
    /**
     * Creates a new instance of ShortestPathList
     */
    public ShortestPathList() {
        super();
        length = 0;
    }
    
    @Override
    public String toString() {
        String result = "";
        for (Object aThi : this) {
            result = result + "> " + ((MultiSPath) aThi).getName();
        }
        return result;
    }
    
    public String getSource(){
        return((MultiSPath)this.get(0)).getName();    	
    }
    
    public String getSourceDest(){
        return ((MultiSPath)this.get(0)).getName() + " " + ((MultiSPath)this.get(this.size()-1)).getName();  	
    }
    
    public int getlength() {
        return length;
    }
    
    public void setlength(int newlength) {
        length = newlength;
    }
}
