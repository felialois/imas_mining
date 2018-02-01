
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.agent.CyclicMessagingPros;
import cat.urv.imas.behaviour.agent.RequesterBehaviorProspector;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author felipe
 */
public class ProspectorAgent extends WorkerAgent{
    
    private long min_x;
    private long min_y;
    private long max_x;
    private long max_y;
    
    private AID assigned_digger;
    
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
        assigned_digger = null;
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
        
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.system = UtilsAgents.searchAgent(this, searchCriterion);
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
        seq_behaviour.addSubBehaviour(new CyclicMessagingPros(this));
        this.addBehaviour(seq_behaviour);
    }
    
    public void setAssignedDigger(AID digger) {
        this.assigned_digger = digger;
    }
    
    public boolean diggerIsAssigned() {
        return this.assigned_digger != null;
    }
    
    public AID getAssignedDigger() {
        return assigned_digger;
    }
    
    public int[] randomMovementProspector(){
        int x = getRow();
        int y = getColumn();
        Random r = new Random();
        List<Cell> possibleMovs = new ArrayList<Cell>();
        // Up
        if(x > min_x && game.get(x-1, y) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Down
        if(x < max_x && game.get(x+1, y) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Left
        if(y > min_y && game.get(x, y-1) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        // Right
        if(y < max_y && game.get(x, y+1) instanceof PathCell)
            possibleMovs.add(game.get(x, y));
        Cell c = possibleMovs.get(r.nextInt(possibleMovs.size()));
        return new int[]{c.getRow(), c.getCol()};
    }
    
    public void actualizePos() {
        String name = this.getAID().getName();
        int agentNum = Integer.parseInt(name.substring(3, name.indexOf("@")));
        Cell actPos = game.getAgentList().get(AgentType.PROSPECTOR).get(agentNum);
        this.setRow(actPos.getRow());
        this.setColumn(actPos.getCol());
    }
    
    public void actionTurn() {
        Cell[] fieldsWithMetal = GameSettings.detectFieldsWithMetal(this.game.getMap(), row, column);
        if(fieldsWithMetal.length>0){
            for(Cell mtl: fieldsWithMetal){
                informCoordinator(mtl.getCol(),mtl.getRow());
            }
        }
        int[] newPos = randomMovementProspector();
        try {
            if (this.assigned_digger != null) {
                informDigger(newPos[0], newPos[1]);
            }
            informSystem(newPos[0], newPos[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }
    
    public void informDigger(int x, int y){
        ACLMessage informPosition = new ACLMessage(ACLMessage.INFORM);
        informPosition.addReceiver(this.assigned_digger);
        informPosition.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        informPosition.setContent(MessageContent.MOVE_TO+" "+x+","+y);
        this.send(informPosition);
    }
    
    public void informCoordinator(int x, int y){
        ACLMessage metalInfo = new ACLMessage(ACLMessage.INFORM);
        metalInfo.addReceiver(this.coordinator);
        metalInfo.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        metalInfo.setContent(MessageContent.METAL+x+","+y);
        this.send(metalInfo);
        
    }
    
    public void informSystem(int x, int y) throws IOException{
        ACLMessage informPosition = new ACLMessage(ACLMessage.INFORM);
        informPosition.addReceiver(this.system);
        informPosition.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        int[] pos = new int[4];
        pos[0]=this.column;
        pos[1]=this.row;
        pos[2]=x;
        pos[3]=y;
        informPosition.setContent(pos[0]+","+pos[1]+","+pos[2]+","+pos[3]);
        this.send(informPosition);
        
    }
}
