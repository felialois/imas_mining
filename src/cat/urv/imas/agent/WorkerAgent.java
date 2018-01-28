/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import java.util.List;

/**
 *
 * @author felipe
 */
public class WorkerAgent extends ImasAgent{
    
    protected AID coordinator;
    private GameSettings game;
    private int column, row;  
    
    public void setGame(GameSettings game) {
        this.game = game;
    }
    
    public AID getCoordinator(){
        return coordinator;
    }
    
    public void setCoordinator(AID coordinator){
        this.coordinator = coordinator;
    }
    
    
    public WorkerAgent(AgentType type) {
        super(type);
        this.column = this.game.getAgentList().get(this.type).get(Integer.valueOf(this.getName().substring(3))).getCol();
        this.row = this.game.getAgentList().get(this.type).get(Integer.valueOf(this.getName().substring(3))).getRow();
    }
    
    public void moveRight(){
        // TODO: Check if movement is valid (Path cell, Not occupied...)
        this.column ++;
        // TODO: Update map
    }
    
    /*public void moveLeft(){
        this.column --;
    }
    public void moveUp(){
        this.row --;
    }
    public void moveDown(){
        this.row ++;
    }*/
    
}
