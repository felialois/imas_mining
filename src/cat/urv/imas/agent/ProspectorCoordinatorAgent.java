/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviourProsCoor;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.List;
import java.lang.Math;

/**
 *
 * @author felipe
 */
public class ProspectorCoordinatorAgent extends CoordinatorAgent{
    
    /**
     * Game settings in use.
     */
    private GameSettings game;
    
    /**
     * Coordinator agent id
     */
    private AID coordinatorAgent;
    
    @Override
    public void setGame(GameSettings game) {
        this.game = game;
        this.divideMap();
    }
        
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

        // search CoordinatorAgent
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
        this.addBehaviour(new RequesterBehaviourProsCoor(this, initialRequest));
        
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }
    
    public void divideMap(){        
        Cell[][] map = game.getMap();
        int prs = game.getNumberOfProspectors();
        long[] x_positions = new long[prs];
        long[] y_positions = new long[prs];

        long x_size = Math.round(map.length/ Math.sqrt(prs));
        long y_size = Math.round(map[0].length/ Math.sqrt(prs));
        for(int i=0;i<prs;i++){
            x_positions[i] = i*x_size;
            y_positions[i] = i*y_size;
        }
        
        log("Map divided");
    }
    
}
