/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class CyclicBehaviourDiggerCoor extends CyclicBehaviour{
    
    Queue<AID> queueDiggers = new LinkedList<AID>();

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
        } else if(content.equals(MessageContent.GET_MOVEMENT)){
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try{
                boolean prospector = agent.getAssignedProspector();
                if(!prospector) {
                    reply.setContent(MessageContent.RANDOM);
                    agent.send(reply);
                    agent.log("Set movement random");
                } else{
                    queueDiggers.add(msg.getSender());
                    agent.log("Waiting for assigned prospector");
                }
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.send(reply);
                agent.errorLog(e.toString());
                e.printStackTrace();
            }
        } else if(content.startsWith(MessageContent.AREAS)) {
            agent.setAreas(content.substring(MessageContent.AREAS.length()+1));
        } else if(content.startsWith(MessageContent.METAL)) {
            // Empezar la subasta
        } else try {
            if(msg.getContentObject() instanceof AID) {
                AID prospector = (AID)msg.getContentObject();
                AID digger = queueDiggers.poll();
                
                // Message to digger with the AID of the prospector
                ACLMessage messageDigger = new ACLMessage(ACLMessage.INFORM);
                messageDigger.addReceiver(digger);
                try {
                    messageDigger.setContentObject(prospector);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                agent.send(messageDigger);
                agent.log("Sent assigned prospector to digger");
                
                // Message to prospector with the AID of the digger
                ACLMessage messageProspector = new ACLMessage(ACLMessage.INFORM);
                messageProspector.addReceiver(prospector);
                try {
                    messageProspector.setContentObject(digger);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                agent.send(messageProspector);
                agent.log("Sent assigned digger to prospector");
                
            } else{
                agent.errorLog("Error: " + content);
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(CyclicBehaviourDiggerCoor.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
}
