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
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;

/**
 *
 * @author santi
 */
public class CyclicBehaviourProsCoor extends CyclicBehaviour{

    public CyclicBehaviourProsCoor(Agent a) {
        super(a);
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
            agent.send(reply);
            agent.log("Map sent to prospector");
        } else if(content.equals(MessageContent.GET_AREA)) {
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try{
                reply.setContent(MessageContent.GET_AREA
                        +Arrays.toString(agent.getNextArea(msg.getSender())));
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.errorLog(e.toString());
                e.printStackTrace();
            }
            agent.send(reply);
            agent.log("Assigned area");
            if(agent.checkAllProsAssigned()) {
                // Send message informing the diggers that all the prospectors 
                // were assigned
                ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
                ready.setContent(MessageContent.PROS_ASSIGNED);
                ready.clearAllReceiver();
                DFAgentDescription DFDescription = new DFAgentDescription();
                ServiceDescription searchCriterion = new ServiceDescription();
                searchCriterion.setType(AgentType.DIGGER.toString());
                DFDescription.addServices(searchCriterion);

                DFAgentDescription[] diggers;
                try {
                    diggers = DFService.search(agent, DFDescription);

                    for(int i = 0; i < diggers.length; i++)
                        ready.addReceiver(diggers[i].getName());

                } catch (FIPAException e) {
                    e.printStackTrace();
                }

                agent.send(ready);
                agent.log("All prospectors assigned");
            }
        } else if(content.startsWith(MessageContent.METAL)) {
            ACLMessage msgMetal = new ACLMessage(ACLMessage.INFORM);
            msgMetal.setContent(content);
            msgMetal.addReceiver(agent.getCoordinatorAgent());
            agent.send(msgMetal);
            agent.log("Metal found in "+content);
        } else if(content.equals(MessageContent.GET_PROS)) {
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try{
                reply.setContentObject(agent.getNextPros());
            } catch(Exception e) {
                e.printStackTrace();
            }
            agent.send(reply);
            agent.log("Sent assigned prospector");
        } else if(content.equals(MessageContent.GET_PROS_BY_AREA)) {
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try{
                reply.setContentObject(agent.getNextProsByArea());
            } catch(Exception e) {
                e.printStackTrace();
            }
            agent.send(reply);
            agent.log("Sent assigned prospector by area");
        } else{
            agent.errorLog("Error: " + content);
        }
    }
    
}
