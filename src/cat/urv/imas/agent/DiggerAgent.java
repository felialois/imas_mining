/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.CyclicMessagingDigger;
import cat.urv.imas.behaviour.agent.RequesterBehaviorDigger;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author felipe
 */
public class DiggerAgent extends WorkerAgent{
    
    private String movement;
    private AID assigned_pros;
    
    public DiggerAgent() {
        super(AgentType.DIGGER);
    }
    
    @Override
    public void setup(){
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.DIGGER.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        // search for the correct coordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.coordinator = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // Waits for the ready message from the coor
        ACLMessage ready;
        do {
            ready = receive();
        } while(ready == null ||
            !MessageContent.READY.equals(ready.getContent()));
        log("Digger coor ready");
        
        ACLMessage mapRequest = new ACLMessage(ACLMessage.REQUEST);
        mapRequest.addReceiver(this.coordinator);
        mapRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        try {
            mapRequest.setContent(MessageContent.GET_MAP);
            log("Request message content");
        } catch (Exception e) {
            log("error in message creation digger");
            e.printStackTrace();
        }
        
        // Waits for the message of the Prospector Coordinator
        ACLMessage ready_pros;
        do {
            ready_pros = receive();
        } while(ready_pros == null ||
            !MessageContent.PROS_ASSIGNED.equals(ready_pros.getContent()));
        log("Pros coor ready");
        
        SequentialBehaviour seq_behaviour = new SequentialBehaviour();
        seq_behaviour.addSubBehaviour(new RequesterBehaviorDigger(this, mapRequest));
        seq_behaviour.addSubBehaviour(new CyclicMessagingDigger(this));
        this.addBehaviour(seq_behaviour);
    }
    
    public AID getCoordinator() {
        return coordinator;
    }
    
    public void setMovement(String movement) {
        this.movement = movement;
    }
    
    public String getMovement() {
        return movement;
    }


    public void setAssignedProspector(AID prospector) {
        this.assigned_pros = prospector;
    }
    
    public AID getAssignedProspector() {
        return assigned_pros;
    }
    
    public int[] randomMovementDigger(){
        int randomNumRow = ThreadLocalRandom.current().nextInt(-1, 2);
        int randomNumCol = ThreadLocalRandom.current().nextInt(-1, 2);
        int newRow=this.getRow()+randomNumRow;
        int newCol=this.getColumn()+randomNumCol;
        int result[] = new int[2];
        
        if(this.getGame().getMap()[newRow][newCol].getCellType().toString().equals("PATH"))
        {
            this.log("ROW "+this.getRow()+" COLUMN "+this.getColumn());
                                                
            result[0] = newRow;
            result[1] = newCol;
            
            return result;
        }
        
        return null;
    }    

    
}