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
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;

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
        
        receivedDiggers = 0;
        receivedProspectors = 0;
        prevDiggerPos = new int[agent.getNumDiggers()][2];
        nextDiggerPos = new int[agent.getNumDiggers()][2];
        prevProsPos = new int[agent.getNumProspectors()][2];
        nextProsPos = new int[agent.getNumProspectors()][2];
    }
    
    @Override
    public void action() {
        SystemAgent agent = (SystemAgent)this.getAgent();
        
        ACLMessage msg = agent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        agent.log("Received message from " + msg.getSender());
        if(content != null && content.equals(MessageContent.GET_MAP)) {
            // Agents wants a map
            ACLMessage reply = msg.createReply();
            try {
                if (content.equals(MessageContent.GET_MAP)) {
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                }
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.errorLog(e.getMessage());
                e.printStackTrace();
            }
            agent.log("Response being prepared");
            agent.send(reply);
            
            reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            
            try {
                agent.addElementsForThisSimulationStep();
                reply.setContentObject(agent.getGame());
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                agent.errorLog(e.toString());
                e.printStackTrace();
            }
            agent.log("Game settings sent");
            agent.send(reply);
        } else if(content != null && content.startsWith(MessageContent.EXTRACT_METAL)) {
            String coord_string = content.substring(MessageContent.EXTRACT_METAL.length() + 1);
            String[] coord = coord_string.split(",");
            agent.log("Metal extraction");
            agent.extractMetal(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
        } else if(content != null && content.startsWith(MessageContent.METAL_TO_MC)) {
            String coord_string = content.substring(MessageContent.METAL_TO_MC.length() + 1);
            String[] coord = coord_string.split(",");
            agent.log("Metal to MC");
            agent.metalToMC(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]));
        } else{
            // Waits for the message of all of the workers
            try {
                String[] pos = content.split(",");
                String type = msg.getSender().getName().substring(0, 3);
                int numAgent = Integer.parseInt(msg.getSender().getName().substring(3, msg.getSender().getName().indexOf("@")));
                if(type.equals("dgg")) {
                    prevDiggerPos[numAgent][0] = Integer.parseInt(pos[0]);
                    prevDiggerPos[numAgent][1] = Integer.parseInt(pos[1]);
                    nextDiggerPos[numAgent][0] = Integer.parseInt(pos[2]);
                    nextDiggerPos[numAgent][1] = Integer.parseInt(pos[3]);
                    receivedDiggers++;
                } else if(type.equals("prs")) {
                    prevProsPos[numAgent][0] = Integer.parseInt(pos[0]);
                    prevProsPos[numAgent][1] = Integer.parseInt(pos[1]);
                    nextProsPos[numAgent][0] = Integer.parseInt(pos[2]);
                    nextProsPos[numAgent][1] = Integer.parseInt(pos[3]);
                    receivedProspectors++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if(agent.getNumDiggers() == receivedDiggers &&
                agent.getNumProspectors() == receivedProspectors) {
            // Actualize the map
            agent.actualize(prevDiggerPos, nextDiggerPos, prevProsPos, nextProsPos);
            agent.log("UPDATE GUI");
            agent.updateGUI();
            
            // Send the map to all the agents
            try {
                msg = new ACLMessage(ACLMessage.UNKNOWN);
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
                
                
                receivedDiggers = 0;
                receivedProspectors = 0;
                prevDiggerPos = new int[agent.getNumDiggers()][2];
                nextDiggerPos = new int[agent.getNumDiggers()][2];
                prevProsPos = new int[agent.getNumProspectors()][2];
                nextProsPos = new int[agent.getNumProspectors()][2];
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (FIPAException e2) {
                e2.printStackTrace();
            }
        }
    }
}
