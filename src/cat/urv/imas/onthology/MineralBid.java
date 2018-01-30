/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import jade.core.AID;

/**
 *
 * @author felipe
 */
public class MineralBid {
    
    private AID digger;
    private int proposal;
    
    public AID getDigger(){
        return digger;
    }
    
    public int getProposal(){
        return proposal;
    }
    
    public MineralBid(AID digger, int proposal){
        this.digger = digger;
        this.proposal = proposal;
    }
    
    @Override
    public boolean equals(Object o){
        MineralBid o2 = (MineralBid) o;
        return (o2.getDigger().equals(this.digger)) && 
                (o2.getProposal()==this.proposal);
    }
    
    
    
}
