/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.CyclicBehaviourDiggerCoor;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviourDiggerCoor;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
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
public class DiggerCoordinatorAgent extends CoordinatorAgent{
    
    /**
     * Game settings in use.
     */
    private GameSettings game;
    
    
    /**
     * Coordinator agent id
     */
    private AID coordinatorAgent;
    
    private int numProspectors;
    private int numDiggers;
    private int numAreas;
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
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

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        initialRequest.addReceiver(UtilsAgents.searchAgent(this, searchCriterion));
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //we add a behaviour that sends the message and waits for an answer
        SequentialBehaviour seq_behaviour = new SequentialBehaviour();
        seq_behaviour.addSubBehaviour(new RequesterBehaviourDiggerCoor(this, initialRequest));
        seq_behaviour.addSubBehaviour(new CyclicBehaviourDiggerCoor(this));
        this.addBehaviour(seq_behaviour);
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }
    
    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    @Override
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Sets the number areas divided by the prospector coordinator
     * @param numAreas: number of areas 
     */
    public void setNumAreas(int numAreas) {
        this.numAreas = numAreas;
        log("Number of areas: " + numAreas);
    }
}
