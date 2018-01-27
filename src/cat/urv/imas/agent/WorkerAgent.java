/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;

/**
 *
 * @author felipe
 */
public class WorkerAgent extends ImasAgent{
    
    protected AID coordinator;
    
    private GameSettings game;
    
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
    }
    
}
