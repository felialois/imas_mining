/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonio
 */
public class CyclicSystemBehaviour extends CyclicBehaviour{
    
    private int receivedDiggers;
    private int receivedProspectors;
    
    private int[][] prevDiggerPos;
    private int[][] nextDiggerPos;
    private int[][] prevProsPos;
    private int[][] nextProsPos;
    
    public CyclicSystemBehaviour(Agent a) {
        super(a);
        SystemAgent agent = (SystemAgent) a;
    }
    
    @Override
    public void action() {
        SystemAgent agent = (SystemAgent)this.getAgent();
        
        receivedDiggers = 0;
        receivedProspectors = 0;
        prevDiggerPos = new int[agent.getNumDiggers()][2];
        nextDiggerPos = new int[agent.getNumDiggers()][2];
        prevProsPos = new int[agent.getNumProspectors()][2];
        nextProsPos = new int[agent.getNumProspectors()][2];
        // Waits for the message of all of the workers
        while(receivedDiggers < agent.getNumDiggers() &&
                receivedProspectors < agent.getNumProspectors()) {
            ACLMessage msg = agent.receive();
            
            try {
                int[] pos = (int[])msg.getContentObject();
                String type = msg.getSender().getName().substring(0, 3);
                int numAgent = Integer.parseInt(type.substring(3, type.indexOf("@")));
                if(type.equals("dgg")) {
                    prevDiggerPos[numAgent][0] = pos[0];
                    prevDiggerPos[numAgent][1] = pos[1];
                    nextDiggerPos[numAgent][0] = pos[2];
                    nextDiggerPos[numAgent][1] = pos[3];
                    receivedDiggers++;
                } else if(type.equals("prs")) {
                    prevProsPos[numAgent][0] = pos[0];
                    prevProsPos[numAgent][1] = pos[1];
                    nextProsPos[numAgent][0] = pos[2];
                    nextProsPos[numAgent][1] = pos[3];
                    receivedProspectors++;
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
        // Actualize the map
        agent.actualize(prevDiggerPos, nextDiggerPos, prevProsPos, nextProsPos);
        agent.log("UPDATE GUI");
        agent.updateGUI();
        // Send the map to all the agents
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
            msg.setContentObject(agent.getGame());
            // Coordinator
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            msg.addReceiver(UtilsAgents.searchAgent(agent, searchCriterion));
            // Digger coordinator
            searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
            msg.addReceiver(UtilsAgents.searchAgent(agent, searchCriterion));
            // Prospector coordinator
            searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
            msg.addReceiver(UtilsAgents.searchAgent(agent, searchCriterion));
            // Diggers
            DFAgentDescription DFDescription = new DFAgentDescription();
            searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.DIGGER.toString());
            DFDescription.addServices(searchCriterion);
            DFAgentDescription[] diggers = DFService.search(agent, DFDescription);
            
            for(int i = 0; i < diggers.length; i++)
                msg.addReceiver(diggers[i].getName());
            // Prospectors
            DFDescription = new DFAgentDescription();
            searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.PROSPECTOR.toString());
            DFDescription.addServices(searchCriterion);
            DFAgentDescription[] prospectors = DFService.search(agent, DFDescription);
            
            for(int i = 0; i < prospectors.length; i++)
                msg.addReceiver(prospectors[i].getName());
            
            agent.send(msg);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (FIPAException e2) {
            e2.printStackTrace();
        }
    }
}
