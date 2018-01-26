/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.RequesterBehaviorDigger;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author felipe
 */
public class DiggerAgent extends WorkerAgent{
    
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
        mapRequest.clearAllReceiver();
        mapRequest.addReceiver(this.coordinator);
        mapRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        try {
            mapRequest.setContent(MessageContent.GET_MAP);
            log("Request message content");
        } catch (Exception e) {
            log("error in message creation prospector");
            e.printStackTrace();
        }
        
        RequesterBehaviorDigger rbp = new RequesterBehaviorDigger(this, mapRequest);
        this.addBehaviour(rbp);
    }
    
}
