/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author santi
 */
public class CyclicBehaviourDiggerCoor extends CyclicBehaviour{

    public CyclicBehaviourDiggerCoor(Agent a) {
        super(a);
        try {
            // Send message informing the workers that coordinator is ready
            ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
            ready.setContent(MessageContent.READY);
            ready.clearAllReceiver();
            
            DFAgentDescription DFDescription = new DFAgentDescription();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER.toString());
            DFDescription.addServices(searchCriterion);
            DFAgentDescription[] diggers = DFService.search(myAgent, DFDescription);
            
            for(int i = 0; i < diggers.length; i++)
                ready.addReceiver(diggers[i].getName());
            
            myAgent.send(ready);
            
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
        if(content.equals(MessageContent.GET_MAP)) {
            ACLMessage reply = msg.createReply();
            try{
                reply.setContentObject(agent.getGame());
                reply.setPerformative(ACLMessage.INFORM);
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.errorLog(e.toString());
                e.printStackTrace();
            }
            agent.send(msg);
        } else if(content.startsWith(MessageContent.AREAS)) {
            agent.setNumAreas(Integer.parseInt(content.substring(MessageContent.AREAS.length()+1)));
        } else if(content.startsWith(MessageContent.METAL)) {
            // Empezar la subasta
        } else{
            agent.errorLog("Error: " + content);
        }
    }
    
}
