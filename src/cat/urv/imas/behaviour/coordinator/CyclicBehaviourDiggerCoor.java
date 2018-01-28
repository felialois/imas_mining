/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.List;

/**
 *
 * @author santi
 */
public class CyclicBehaviourDiggerCoor extends CyclicBehaviour{

    public CyclicBehaviourDiggerCoor(Agent a) {
        super(a);
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
            agent.send(reply);
            
            agent.addDigger(msg.getSender());
            agent.log("Map sent to digger");
        } else if(content.startsWith(MessageContent.AREAS)) {
            agent.setAreas(content.substring(MessageContent.AREAS.length()+1));
        } else if(content.startsWith(MessageContent.METAL)) {
            // Empezar la subasta
            
            long x_location = Long.parseLong(content.split(",")[1]);
            long y_location = Long.parseLong(content.split(",")[2]);
            
            

            
            List<AID> diggers = agent.getDiggers();
            for (AID digger:diggers){
                // Create a message to communicate the auction start
                ACLMessage auctionStarted = new ACLMessage(ACLMessage.INFORM);
                auctionStarted.addReceiver(digger);
                auctionStarted.setContent(
                        MessageContent.AUCTION_START);
                auctionStarted.setProtocol(
                        FIPANames.InteractionProtocol.FIPA_REQUEST);
                
                
            }  
        } else if(content.startsWith(MessageContent.AUCTION_BID)) {
            // Adds the bid to the list
            
              
        } else{
            agent.errorLog("Error: " + content);
        }
    }
    
}
