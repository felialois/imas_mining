/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.ContractNetInitiatorBehaviour;
import cat.urv.imas.behaviour.coordinator.CyclicBehaviourDiggerCoor;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviourDiggerCoor;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MineralBid;
import cat.urv.imas.onthology.MineralContract;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author felipe
 */
public class DiggerCoordinatorAgent extends CoordinatorAgent {

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
    private int actDigger;

    private int numAreas;
    private long[] x_min_positions;
    private long[] y_min_positions;
    private long[] x_max_positions;
    private long[] y_max_positions;
    private Set<AID> diggers;
    private Map<MineralContract, List<MineralBid>> contracts;

    private Behaviour seq_behaviour;

    @Override
    public void setGame(GameSettings game) {
        this.game = game;

        try {
            // Send message informing the workers that coordinator is ready
            ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
            ready.setContent(MessageContent.READY);
            ready.clearAllReceiver();

            DFAgentDescription DFDescription = new DFAgentDescription();
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER.toString());
            DFDescription.addServices(searchCriterion);

            DFAgentDescription[] diggers = DFService.search(this, DFDescription);

            for (int i = 0; i < diggers.length; i++) {
                ready.addReceiver(diggers[i].getName());
            }

            send(ready);

        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // Check how many diggers and prospectors are there in the map
        numDiggers = game.getNumberOfDiggers();
        numProspectors = game.getNumberOfProspectors();
        
        Map<AgentType, List<Cell>> agentList = game.getAgentList();
        for (Map.Entry<AgentType, List<Cell>> entry : agentList.entrySet()) {
            AgentType type = entry.getKey();
            if (type.equals(AgentType.DIGGER)) {
                numDiggers = entry.getValue().size();
            } else if (type.equals(AgentType.PROSPECTOR)) {
                numProspectors = entry.getValue().size();
            }
        }
        log("Diggers: " + numDiggers + " Prospectors: " + numProspectors);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        numProspectors = 0;
        numDiggers = 0;
        actDigger = 0;
        
        diggers = new HashSet<>();
        contracts = new HashMap<>();


        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.DIGGER_COORDINATOR.toString());
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
        seq_behaviour = new SequentialBehaviour();
        SequentialBehaviour seq = (SequentialBehaviour) seq_behaviour;
        seq.addSubBehaviour(new RequesterBehaviourDiggerCoor(this, initialRequest));
        seq.addSubBehaviour(new CyclicBehaviourDiggerCoor(this));
        this.addBehaviour(seq);
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
     *
     * @param numAreas: number of areas
     */
    public void setAreas(String areas) {
        String[] difAreas = areas.split(".");
        numAreas = difAreas.length;
        x_min_positions = new long[numAreas];
        x_max_positions = new long[numAreas];
        y_min_positions = new long[numAreas];
        y_max_positions = new long[numAreas];
        for (int i = 0; i < difAreas.length; i++) {
            String[] values = difAreas[i].split(",");
            x_min_positions[i] = Integer.parseInt(values[0]);
            x_max_positions[i] = Integer.parseInt(values[1]);
            y_min_positions[i] = Integer.parseInt(values[0]);
            y_max_positions[i] = Integer.parseInt(values[1]);
        }
    }

    public boolean getAssignedProspector() {
        actDigger++;
        if (actDigger > numProspectors) {
            return false;
        } else {
            ACLMessage prospectorRequest = new ACLMessage(ACLMessage.REQUEST);
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
            prospectorRequest.addReceiver(UtilsAgents.searchAgent(this, searchCriterion));
            if (numDiggers >= numProspectors) {
                prospectorRequest.setContent(MessageContent.GET_PROS);
            } else {
                prospectorRequest.setContent(MessageContent.GET_PROS_BY_AREA);
            }
            send(prospectorRequest);
            return true;
        }
    }

    /**
     * Getter of the number of diggers
     *
     * @return number of diggers
     */
    public int getNumDiggers() {
        return numDiggers;
    }

    /**
     * Getter of the number of prospectors
     *
     * @return number of prospectors
     */
    public int getNumProspectors() {
        return numProspectors;
    }

    public void addDigger(AID digger) {
        this.diggers.add(digger);
        log("Digger Added to Coordinator" + digger);
    }
    
    public void addContract(int x, int y){
        log("Contract added in :" + x+" , "+y);

        contracts.put(new MineralContract(x,y), new ArrayList<MineralBid>());
    }
    
    public void addContractBid(int x, int y, AID digger, int proposal){
        log("Bid made to contract:" + x+" , "+y);
        MineralContract mc = new MineralContract(x,y);
        if(contracts.containsKey(mc)){
            List<MineralBid> get = contracts.get(mc);
            get.add(new MineralBid(digger, proposal));
            contracts.put(new MineralContract(x,y), get);
        }else{
            System.out.print("Contract "+mc+" is not in map.");
        }
            
            
        
    }

    public void startAuction(int xLocation, int yLocation) {

        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        System.out.println("Contract net for "+diggers.size());
        this.addContract(xLocation, yLocation);

        for (AID d : diggers) {
            System.out.println("Contract net Digger Added");
            msg.addReceiver(d);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent(MessageContent.CONTRACT_PROPOSE+xLocation+","+yLocation);
        
//        this.removeBehaviour(seq_behaviour);
//        
//        seq_behaviour = new ParallelBehaviour();
//        ParallelBehaviour par = (ParallelBehaviour) seq_behaviour;
//        par.addSubBehaviour(new ContractNetInitiatorBehaviour(this, msg));
//        par.addSubBehaviour(new CyclicBehaviourDiggerCoor(this));
        
        this.addBehaviour(new ContractNetInitiatorBehaviour(this, msg));
    }
    
    public Map<MineralContract, List<MineralBid>> getContracts(){
        return contracts;
    }
}
