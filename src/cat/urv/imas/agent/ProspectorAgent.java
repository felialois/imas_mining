/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.RequesterBehaviorProspector;
import cat.urv.imas.behaviour.coordinator.CyclicBehaviourProsCoor;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviourProsCoor;
import cat.urv.imas.onthology.MessageContent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.List;

/**
 *
 * @author felipe
 */
public class ProspectorAgent extends WorkerAgent{
    
    private long min_x;
    private long min_y;
    private long max_x;
    private long max_y;
    
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }
    
    public void set_location(String s){
        String[] ls = s.split("\\[")[1].split("\\]")[0].split(",");
        min_x=Long.parseLong(ls[0].replace(" ", ""));
        max_x=Long.parseLong(ls[1].replace(" ", ""));
        min_y=Long.parseLong(ls[2].replace(" ", ""));
        max_y=Long.parseLong(ls[3].replace(" ", ""));
        log("Location Received by Prospector");
    }
    
    @Override
    public void setup(){
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.PROSPECTOR.toString());
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
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.coordinator = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // Waits for the ready message from the coor
        ACLMessage ready;
        do {
            ready = receive();
        } while(ready == null ||
            !MessageContent.READY.equals(ready.getContent()));
        log("Prospector coor ready");
        
        // Send message to get the map
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
       
        RequesterBehaviorProspector rbp = new RequesterBehaviorProspector(this, mapRequest);
        
        // Send message to get the area
        ACLMessage areaRequest = new ACLMessage(ACLMessage.REQUEST);
        areaRequest.clearAllReceiver();
        areaRequest.addReceiver(this.coordinator);
        areaRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
         try {
            areaRequest.setContent(MessageContent.GET_AREA);
            log("Request message content");
        } catch (Exception e) {
            log("error in message creation prospector");
            e.printStackTrace();
        }
         
        RequesterBehaviorProspector getAreaBehavior
                = new RequesterBehaviorProspector(this, areaRequest);
        
        SequentialBehaviour seq_behaviour = new SequentialBehaviour();
        seq_behaviour.addSubBehaviour(rbp);
        seq_behaviour.addSubBehaviour(getAreaBehavior);
        this.addBehaviour(seq_behaviour);
        
        

    }
    
}
