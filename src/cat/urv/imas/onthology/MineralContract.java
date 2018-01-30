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
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.x;
        hash = 17 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MineralContract other = (MineralContract) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }
    

}
