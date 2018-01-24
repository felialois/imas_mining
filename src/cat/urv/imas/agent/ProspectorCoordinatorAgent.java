/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.CyclicBehaviourProsCoor;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviourProsCoor;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
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
public class ProspectorCoordinatorAgent extends CoordinatorAgent{
    
    /**
     * Game settings in use.
     */
    private GameSettings game;
    
    /**
     * Coordinator agent id
     */
    private AID coordinatorAgent;
    
    private final int nAreas = 4;
    
    private long[] x_min_positions;
    private long[] y_min_positions;
    private long[] x_max_positions;
    private long[] y_max_positions;
    private int actArea;
    
    @Override
    public void setGame(GameSettings game) {
        this.game = game;
        actArea = 0;
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
        SequentialBehaviour seq_behaviour = new SequentialBehaviour();
        seq_behaviour.addSubBehaviour(new RequesterBehaviourProsCoor(this, initialRequest));
        seq_behaviour.addSubBehaviour(new CyclicBehaviourProsCoor(this));
        this.addBehaviour(seq_behaviour);
        
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }
    
    public void divideMap(){        
        Cell[][] map = game.getMap();
        
        int size_x = map.length;
        int size_y = map[0].length;
        int prs = game.getNumberOfProspectors();
        x_min_positions = new long[prs];
        y_min_positions = new long[prs];
        x_max_positions = new long[prs];
        y_max_positions = new long[prs];

        long x_size = Math.round(size_x/ Math.sqrt(nAreas));
        long y_size = Math.round(size_y/ Math.sqrt(nAreas));
        
        int i = 0;
        for(long x=0; x<size_x; x+=x_size){
            for (long y=0; y<size_y; y+=y_size){
                x_min_positions[i] = x;
                y_min_positions[i] = y;
                x_max_positions[i] = x+x_size;
                y_max_positions[i] = y+y_size;
                i++;
            }
            
        }
        
        for(int k=0; k<prs;k++){
            log("Area " + k);
            log("X:"+Long.toString(x_min_positions[k])
                    +" , "+Long.toString(x_max_positions[k]));
            log("Y:"+Long.toString(y_min_positions[k])
                    +" , "+Long.toString(y_max_positions[k]));            
        }
        log("Map divided");
    }
    
    /**
     * This method returns the next available area for the prospectors
     * @return Array with the bounds of the area of the prospector
     * [minX, maxX, minY, maxY]
     */
    public long[] getNextArea() {
        long[] bounds = new long[4];
        bounds[0] = x_min_positions[actArea%x_min_positions.length];
        bounds[1] = x_max_positions[actArea%x_max_positions.length];
        bounds[2] = y_min_positions[actArea%y_min_positions.length];
        bounds[3] = y_max_positions[actArea%y_max_positions.length];
        return bounds;
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
     * Gets the coordinator agent
     * @return AID of the coordinator agent
     */
    public AID getCoordinatorAgent(){
        return coordinatorAgent;
    }
}
