/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.InfoAgent;


/**
 *
 * @author felipe
 */
public class WorkerAgent extends ImasAgent{
    
    protected AID coordinator;
    protected GameSettings game;
    protected int column, row;  
    
    public void setGame(GameSettings game) {
        this.game = game;
    }
    
    public GameSettings getGame() {
        return game;
    }
    
    public AID getCoordinator(){
        return coordinator;
    }
    
    public void setCoordinator(AID coordinator){
        this.coordinator = coordinator;
    }
    
    
    public WorkerAgent(AgentType type) {
        super(type);
        this.row=0;
        this.column=0;
        //System.out.println(this.game.getDiggersCapacity());
        //this.column = this.game.getAgentList().get(this.type).get(Integer.valueOf(this.getName().substring(3))).getCol();
        //this.row = this.game.getAgentList().get(this.type).get(Integer.valueOf(this.getName().substring(3))).getRow();
    }
    
    public int getColumn()
    {
        return this.column;
    }
    
    public int getRow()
    {
        return this.row;
    }
    
    public void setColumn(int column)
    {
        this.column=column;
    }
    
    public void setRow(int row)
    {
        this.row=row;
    }
    
}
