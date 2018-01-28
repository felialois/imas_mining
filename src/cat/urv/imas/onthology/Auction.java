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
public class Auction {
    
    private long x;
    private long y;
    
    public Auction(long x, long y){
        this.x = x;
        this.y = y;
    }
    
    public long getX(){
        return x;
    }
    
    public long getY(){
        return y;
    }
    
    @Override
    public String toString(){
        return x+","+y;
    }
    
}
