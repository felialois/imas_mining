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
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        super.handleInform(msg);
    }
    
}
