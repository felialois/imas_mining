/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package cat.urv.imas.agent;

import AStar.AStar;
import AStar.Node;
import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.CyclicMessagingDigger;
import cat.urv.imas.behaviour.agent.RequesterBehaviorDigger;
import cat.urv.imas.behaviour.coordinator.ContractNetResponderBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MetalType;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

/**
 *
 * @author felipe
 */
public class DiggerAgent extends WorkerAgent {
    
    public enum DiggerState{MOVING, GOING_TO_DIG,
    DIGGING, RETRIEVING_METAL};
    
    private DiggerState actState;
    
    private String movement;
    private AID assigned_pros;
    private int[] movingToPos;
    private MessageTemplate template;
    
    private int maxCapacity;
    
    private Map<MetalType, Integer> metalCarried;
    
    public DiggerAgent() {
        super(AgentType.DIGGER);
    }
    
    @Override
    public void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        
        this.actState = DiggerState.MOVING;
        
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
        
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.system = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // Waits for the ready message from the coor
        ACLMessage ready;
        do {
            ready = receive();
        } while (ready == null
                || !MessageContent.READY.equals(ready.getContent()));
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
        } while (ready_pros == null
                || !MessageContent.PROS_ASSIGNED.equals(ready_pros.getContent()));
        log("Pros coor ready");
        
        
        template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new RequesterBehaviorDigger(this, mapRequest));
        parallelBehaviour.addSubBehaviour(new CyclicMessagingDigger(this));
        parallelBehaviour.addSubBehaviour(new ContractNetResponderBehaviour(this, template));
        this.addBehaviour(parallelBehaviour);
        
    }
    
    public AID getCoordinator() {
        return coordinator;
    }
    
    public AID getSystem() {
        return system;
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
    
    public int[] randomMovementDigger() {
        int x = getRow();
        int y = getColumn();
        Random r = new Random();
        ArrayList<Cell> possibleMovs = new ArrayList<Cell>();
        // Up
        if(x > 0 && game.get(x-1, y) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Down
        if(x < game.getMap().length && game.get(x+1, y) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Left
        if(y > 0 && game.get(x, y-1) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Right
        if(y < game.getMap()[0].length && game.get(x, y+1) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        Cell c = possibleMovs.get(r.nextInt(possibleMovs.size()));
        return new int[]{c.getRow(), c.getCol()};
    }
    
    public int evaluateAction(int x, int y) {
        int totalMetal = 0;
        for(int val: metalCarried.values())
            totalMetal += val;
        return (maxCapacity-totalMetal) / (Math.abs(x-this.getRow()) + Math.abs(y-this.getColumn()));
    }
    
    @Override
    public void setGame(GameSettings game) {
        if(metalCarried == null) {
            maxCapacity = game.getDiggersCapacity();
            metalCarried = new HashMap();
            MetalType[] types = game.getManufacturingCenterMetalType();
            for(MetalType type: types)
                metalCarried.put(type, 0);
        }
        
        actualizePos();
        
        super.setGame(game);
    }
    
    public void actualizePos() {
        String name = this.getAID().getName();
        int agentNum = Integer.parseInt(name.substring(3, name.indexOf("@")));
        Cell actPos = game.getAgentList().get(AgentType.DIGGER).get(agentNum);
        this.setRow(actPos.getRow());
        this.setColumn(actPos.getCol());
    }
    
    public void extractMetal(MetalType type) {
        metalCarried.put(type, metalCarried.get(type) + 1);
    }
    
    public boolean hasSpaceAvailable() {
        int totalMetal = 0;
        for(int val: metalCarried.values())
            totalMetal += val;
        return totalMetal < maxCapacity;
    }
    
    public DiggerState getDiggerState() {
        return actState;
    }
    
    public void setState(DiggerState state) {
        this.actState = state;
    }
    
    public void setMovingToPos(int row, int col) {
        this.movingToPos = new int[]{row, col};
    }
    
    public MessageTemplate getTemplate(){
        return template;
    }
    
    public void restartContractNetBehaviour(){
        this.addBehaviour(new ContractNetResponderBehaviour(this, template));
    }
    
    public void chooseMC() {
        List<Cell> mcs = game.getCellsOfType().get(CellType.MANUFACTURING_CENTER);
        ManufacturingCenterCell bestMc = null;
        double bestValue = 0.0;
        for(Cell mc: mcs) {
            double price = ((ManufacturingCenterCell)mc).getPrice();
            MetalType type = ((ManufacturingCenterCell)mc).getMetal();
            double value = price * metalCarried.get(type)
                    / ((Math.abs(mc.getRow()-this.getRow())
                    + Math.abs(mc.getCol()-this.getColumn())));
            if(value > bestValue) {
                bestValue = value;
                bestMc = (ManufacturingCenterCell)mc;
            }
        }
        setMovingToPos(bestMc.getRow(), bestMc.getCol());
    }
    
    public void sendMovToSys(int newRow, int newCol) {
        ACLMessage msgSys = new ACLMessage(ACLMessage.INFORM);
        msgSys.addReceiver(system);
        int[] mov = new int[4];
        mov[0] = row;
        mov[1] = column;
        mov[2] = newRow;
        mov[3] = newCol;
        try {
            msgSys.setContentObject(mov);
            send(msgSys);
            log("Movement sent to system");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void actionTurn() {
        ACLMessage move = new ACLMessage(ACLMessage.INFORM);
        move.addReceiver(system);
        switch (actState){
            case MOVING:
                if(movement.equals(MessageContent.RANDOM)) {
                    // Decide a random movement
                    int[] nextMov = randomMovementDigger();
                    // Send the movement to the system
                    sendMovToSys(nextMov[0], nextMov[1]);
                } else{
                    // Wait for the prospector message and move to the position given
                    return;
                }
                break;
            case DIGGING:
                // Dig one metal and add it to the list
                Map<MetalType, Integer> metal= ((FieldCell)game.get(movingToPos[0], movingToPos[1])).getMetal();
                for(MetalType type: metal.keySet()) {
                    if(metal.get(type) > 0)
                        extractMetal(type);
                }
                // Send a message to the system with the dig
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(system);
                msg.setContent(MessageContent.EXTRACT_METAL + " " +
                        movingToPos[0] + "," + movingToPos[1]);
                send(msg);
                log("Dig sent");
                // Check if the digger can load more or there are more metal
                // If the digger cannot dig more change the state and decide MC
                int metalLeft = 0;
                for(MetalType type: metal.keySet())
                    metalLeft += metal.get(type);
                if(metalLeft < 0 || hasSpaceAvailable()) {
                    setState(DiggerState.GOING_TO_DIG);
                    chooseMC();
                }
                // Send a message to the system with the movement
                sendMovToSys(row, column);
                break;
            case GOING_TO_DIG:
                // Apply AStar
                Cell nextMovDig = AStar.shortestPath((Node)game.get(row, column),
                    (Node)game.get(movingToPos[0], movingToPos[1]), 
                    game.getMap());
                // Send the movement to the system, going to the digging point
                sendMovToSys(nextMovDig.getRow(), nextMovDig.getCol());
                // Check if next movement is close to the digging point
                if(Math.abs(nextMovDig.getRow() - movingToPos[0]) <= 1 &&
                        Math.abs(nextMovDig.getCol() - movingToPos[1]) <= 1)
                    setState(DiggerState.DIGGING);
                break;
            case RETRIEVING_METAL:
                // Check if it is close to the MC
                if(Math.abs(row - movingToPos[0]) <= 1 &&
                        Math.abs(column - movingToPos[1]) <= 1) {
                    ManufacturingCenterCell mc = (ManufacturingCenterCell)game.get(movingToPos[0], movingToPos[1]);
                    ACLMessage msgMetalMc = new ACLMessage(ACLMessage.INFORM);
                    msgMetalMc.addReceiver(system);
                    msgMetalMc.setContent(MessageContent.METAL_TO_MC + " " + 
                            metalCarried.get(mc.getMetal()) + "," + movingToPos[0] +
                            movingToPos[1]);
                    send(msgMetalMc);
                    log("Left metal to MC");
                    setState(DiggerState.MOVING);
                    sendMovToSys(row, column);
                } else{
                    // Apply AStar
                    Cell nextMovMc = AStar.shortestPath((Node)game.get(row, column),
                        (Node)game.get(movingToPos[0], movingToPos[1]), 
                        game.getMap());
                    // Send the movement to the system, going to the best MC
                    sendMovToSys(nextMovMc.getRow(), nextMovMc.getCol());
                }
            default:
                break;
        }
        send(move);
    }
}
