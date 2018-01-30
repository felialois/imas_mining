 /**
  *  IMAS base code for the practical work.
  *  Copyright (C) 2014 DEIM - URV
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.CyclicMessagingSystem;
import cat.urv.imas.behaviour.system.CyclicSystemBehaviour;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.List;
import java.util.Map;

/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {
    
    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;
    
    private int numDiggers;
    private int numProspectors;
    
    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }
    
    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }
    
    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }
    
    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
    }
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        
        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);
        
        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }
        
        // 2. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");
        
        // 3. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize agents
        this.game.createAgents();
        numDiggers = game.getNumberOfDiggers();
        numProspectors = game.getNumberOfProspectors();
        
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        
        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
        ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
        ready.setContent(MessageContent.READY);
        ready.clearAllReceiver();
        ready.addReceiver(this.coordinatorAgent);
        send(ready);
        
        SequentialBehaviour seq_behaviour = new SequentialBehaviour();
        seq_behaviour.addSubBehaviour(new CyclicSystemBehaviour(this));
        seq_behaviour.addSubBehaviour(new CyclicMessagingSystem(this));
        this.addBehaviour(seq_behaviour);
    }
    
    public void actualize(int[][] prevDiggerPos, int[][] nextDiggerPos,
            int[][] prevProsPos, int[][] nextProsPos) {
        Cell[][] map = game.getMap();
        Map<AgentType, List<Cell>> agentList = game.getAgentList();
        
        for(int i = 0; i < prevDiggerPos.length; i++) {
            PathCell prevCell = (PathCell) map[prevDiggerPos[i][0]][prevDiggerPos[i][1]];
            PathCell nextCell = (PathCell) map[nextDiggerPos[i][0]][nextDiggerPos[i][1]];
            List<InfoAgent> list = prevCell.getAgents().get(AgentType.DIGGER);
            InfoAgent agent = null;
            int j = 0;
            while(agent == null) {
                String name = list.get(j).getAID().getName();
                if(name.substring(3, name.indexOf("@")).equals(""+i)) {
                    agent = list.get(j);
                }
                j++;
            }
            try{
                prevCell.removeAgent(agent);
                nextCell.addAgent(agent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            agentList.get(AgentType.DIGGER).set(i, nextCell);
        }
        
        for(int i = 0; i < prevProsPos.length; i++) {
            PathCell prevCell = (PathCell) map[prevProsPos[i][0]][prevProsPos[i][1]];
            PathCell nextCell = (PathCell) map[nextProsPos[i][0]][nextProsPos[i][1]];
            List<InfoAgent> list = prevCell.getAgents().get(AgentType.PROSPECTOR);
            InfoAgent agent = null;
            int j = 0;
            while(agent == null) {
                String name = list.get(j).getAID().getName();
                if(name.substring(3, name.indexOf("@")).equals(""+i)) {
                    agent = list.get(j);
                }
                j++;
            }
            try{
                prevCell.removeAgent(agent);
                nextCell.addAgent(agent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            agentList.get(AgentType.PROSPECTOR).set(i, nextCell);
        }
        game.setAgentList(agentList);
    }
    
    public int getNumDiggers() {
        return numDiggers;
    }
    
    public int getNumProspectors() {
        return numProspectors;
    }
    
    public void extractMetal(int row, int col) {
        FieldCell fieldMetal = (FieldCell)game.get(row, col);
        fieldMetal.removeMetal();
    }
    
    public void metalToMC(int units, int row, int col) {
        List<Cell> mcs = game.getCellsOfType().get(CellType.MANUFACTURING_CENTER);
        int[] prices = game.getManufacturingCenterPrice();
        for(int i = 0; i < mcs.size(); i++) {
            Cell mc = mcs.get(i);
            if(mc.getRow() == row && mc.getCol() == col) {
                int points = units * prices[i];
                log("Won " + points + " points");
                return;
            }
        }
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }
    
}
