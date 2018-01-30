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
public class MineralContract {
    
    private int x;
    private int y;
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    } 
    
    public MineralContract(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString(){
        return x+" , "+y;
    }
    
    @Override
    public boolean equals(Object o){
        MineralContract o2 = (MineralContract) o;
        return (o2.getX()==this.x) && (o2.getY()==this.y);
    }
    
}
