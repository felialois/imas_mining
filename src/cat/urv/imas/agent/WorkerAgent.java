/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import jade.core.AID;

/**
 *
 * @author felipe
 */
public class WorkerAgent extends ImasAgent{
    
    protected AID coordinator;
    
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
