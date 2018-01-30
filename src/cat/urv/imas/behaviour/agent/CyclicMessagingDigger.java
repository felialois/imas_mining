/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerAgent;
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
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class CyclicMessagingDigger extends CyclicBehaviour{
    
    public CyclicMessagingDigger(Agent a) {
        super(a);
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        
        // Sends a message to the digger coordinator to get the movement
        ACLMessage movementRequest = new ACLMessage(ACLMessage.REQUEST);
        movementRequest.addReceiver(agent.getCoordinator());
        movementRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        movementRequest.setContent(MessageContent.GET_MOVEMENT);
        agent.send(movementRequest);
        agent.log("Request movement");
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        if(content.startsWith(MessageContent.CONTRACT_ASIGN)) {
            String[] location = content.replace(MessageContent.CONTRACT_ASIGN, "").split(",");
            agent.log("Bid Won Location "+location[0]+" ,"+location[1]);
            agent.actState = DiggerAgent.DiggerState.GOING_TO_DIG;
            agent.restartContractNetBehaviour();
        } else if(msg.getContent().equals(MessageContent.RANDOM)) {
            agent.setMovement(MessageContent.RANDOM);
            agent.log("Set movement random");
        } else if(msg.getContent().contains(MessageContent.CONTRACT_REJECT)) {
            agent.log("Bid Lost");
            agent.restartContractNetBehaviour();
        }else try {
            if(msg.getContentObject() instanceof AID) {
                agent.setMovement("Follow prospector");
                agent.setAssignedProspector((AID)msg.getContentObject());
                agent.log("Following prospector");
            } else{
                agent.errorLog("Error: " + content);
            }
        } catch (Exception e) {
            System.out.print("ERROR :"+msg.getContent());
            e.printStackTrace();
        }
        
        if(agent.getMovement().equals(MessageContent.RANDOM))
        {
            int result[] = new int[2];
            result=agent.randomMovementDigger();
            
            if(result!=null)
            {
                agent.setRow(result[0]);
                agent.setColumn(result[1]);
            }

            
        }
        
    }
    
}
