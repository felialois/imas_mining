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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author santi
 */
public class RequesterBehaviourCoor extends RequesterBehaviour{

    public RequesterBehaviourCoor(CoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
    }
    
    @Override
    protected void handleInform(ACLMessage msg) {
        if(msg.getContent().startsWith(MessageContent.METAL)) {
            CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
            // Sends the message to the digger coordinator
            agent.log("METAL found in " 
                    + msg.getContent().substring(MessageContent.METAL.length()+1) 
                    + ", send message to digger coordinator");
            msg.clearAllReceiver();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
            msg.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
            myAgent.send(msg);
        }else{
            super.handleInform(msg);
        }
    }
}
