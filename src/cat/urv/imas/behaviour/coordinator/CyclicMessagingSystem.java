/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author santi
 */
public class CyclicMessagingSystem extends CyclicBehaviour{

    public CyclicMessagingSystem(Agent a) {
        super(a);
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        SystemAgent agent = (SystemAgent)this.getAgent();
        if(content.startsWith(MessageContent.EXTRACT_METAL)) {
            String coord_string = content.substring(MessageContent.EXTRACT_METAL.length() + 1);
            String[] coord = coord_string.split(",");
            agent.extractMetal(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
        } else if(content.startsWith(MessageContent.METAL_TO_MC)) {
            String coord_string = content.substring(MessageContent.METAL_TO_MC.length() + 1);
            String[] coord = coord_string.split(",");
            agent.metalToMC(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]));
        }
    }
    
}
