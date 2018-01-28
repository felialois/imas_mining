/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

/**
 *
 * @author felipe
 */
public class Offer {
    private final long x;
    private final long y;
    private final int remainingSpace;
    
    public Offer(long x, long y, int remainingSpace){
        this.x = x;
        this.y = y;
        this.remainingSpace = remainingSpace;
    }
    
    public long getX(){
        return x;
    }
    
    public long getY(){
        return y;
    }
    
    public int getSpace(){
        return remainingSpace;
    }
    
    
}
