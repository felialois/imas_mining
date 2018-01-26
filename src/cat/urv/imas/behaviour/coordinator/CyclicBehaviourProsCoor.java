/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class CyclicBehaviourProsCoor extends CyclicBehaviour{

    public CyclicBehaviourProsCoor(Agent a) {
        super(a);
        try {
            // Send message informing the workers that coordinator is ready
            ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
            ready.setContent(MessageContent.READY);
            ready.clearAllReceiver();
            
            DFAgentDescription DFDescription = new DFAgentDescription();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.PROSPECTOR.toString());
            DFDescription.addServices(searchCriterion);
            
            DFAgentDescription[] prospectors = DFService.search(myAgent, DFDescription);
            
            for(int i = 0; i < prospectors.length; i++)
                ready.addReceiver(prospectors[i].getName());
            
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
        ProspectorCoordinatorAgent agent = (ProspectorCoordinatorAgent)this.getAgent();
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
        } else if(content.equals(MessageContent.GET_AREA)) {
            ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
            try{
                reply.setContentObject(agent.getNextArea());
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.errorLog(e.toString());
                e.printStackTrace();
            }
            agent.send(reply);
        } else if(content.startsWith(MessageContent.METAL)) {
            ACLMessage msgMetal = new ACLMessage(ACLMessage.INFORM);
            msgMetal.setContent(content);
            //msgMetal.addReceiver(agent.getCoordinatorAgent());
            agent.send(msgMetal);
        } else{
            agent.errorLog("Error: " + content);
        }
    }
    
}
