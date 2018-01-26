/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.agent.WorkerAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author felipe
 */
public class RequesterBehaviorDigger extends RequesterAgentBehaviour{

    public RequesterBehaviorDigger(WorkerAgent agent, 
            ACLMessage requestMsg) {
        super(agent, requestMsg);
    }
    
    @Override
    protected void handleInform(ACLMessage msg) {
        if(msg.getContent().startsWith(MessageContent.GET_AREA)) {
            
            
            
        }
        else{
            super.handleInform(msg);
        }
    }
    
}
