/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author santi
 */
public class RequesterBehaviourDiggerCoor extends RequesterBehaviour{

    public RequesterBehaviourDiggerCoor(CoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
    }
    
    @Override
    protected void handleInform(ACLMessage msg) {
        if(msg.getContent().startsWith(MessageContent.METAL)) {
            CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
            // Starts the auction
            agent.log("METAL found in " 
                    + msg.getContent().substring(MessageContent.METAL.length()+1) 
                    + ", starting auction");
            msg.clearAllReceiver();
            DFAgentDescription df = new DFAgentDescription();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER.toString());
            df.addServices(searchCriterion);
            DFAgentDescription[] agents;
            try {
                // Find all the diggers and add the message
                agents = DFService.search(myAgent, df);
                for(DFAgentDescription a: agents){
                    msg.addReceiver(a.getName());
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            myAgent.send(msg);
        }else{
            super.handleInform(msg);
        }
    }
}
