/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.RequesterBehaviorProspector;
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
public class ProspectorAgent extends WorkerAgent{
    
    private long min_x;
    private long min_y;
    private long max_x;
    private long max_y;
    
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }
    
    public void set_location(long[] s){
        min_x=s[0];
        max_x=s[1];
        min_y=s[2];
        max_y=s[3];
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
        
        RequesterBehaviorProspector rbp = new RequesterBehaviorProspector(this,mapRequest);
        this.addBehaviour(rbp);
        
        

    }
    
}
