/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

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
