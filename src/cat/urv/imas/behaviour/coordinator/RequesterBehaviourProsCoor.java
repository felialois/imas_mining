/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import static jade.lang.acl.ACLParserConstants.AID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class RequesterBehaviourProsCoor extends RequesterBehaviour{

    public RequesterBehaviourProsCoor(CoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
    }
    
    @Override
    protected void handleInform(ACLMessage msg) {
        if(msg.getContent().startsWith(MessageContent.METAL)) {
            CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
            // Sends the message to the digger coordinator
            agent.log("METAL found in " 
                    + msg.getContent().substring(MessageContent.METAL.length()+1) 
                    + ", send message to coordinator");
            msg.clearAllReceiver();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            msg.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
            myAgent.send(msg);
        }else{
            super.handleInform(msg);
        }
    }
}
