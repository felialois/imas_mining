/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.Arrays;
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
            if(msg.getContentObject() instanceof AID) {
                agent.setAssignedDigger((AID)msg.getContentObject());
            } else{
                agent.errorLog("Error: " + content);
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(CyclicMessagingPros.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
