/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class CyclicMessagingPros extends CyclicBehaviour{

    public CyclicMessagingPros(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        ProspectorAgent agent = (ProspectorAgent)this.getAgent();
        try {
            System.out.println(msg.getContentObject());
            if(msg.getContentObject() instanceof AID) {
                if(!agent.diggerIsAssigned()){
                    agent.setAssignedDigger((AID)msg.getContentObject());
                    agent.actionTurn();
                }
            } else if(msg.getContentObject() instanceof GameSettings){
                agent.setGame((GameSettings)msg.getContentObject());
                agent.actualizePos();
                agent.actionTurn();
            } else{
                agent.errorLog("Error: " + content);
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(CyclicMessagingPros.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int result[] = agent.randomMovementProspector();
            
        if(result!=null)
        {
            agent.setRow(result[0]);
            agent.setColumn(result[1]);
        }

    }
    
}
