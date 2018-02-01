/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package cat.urv.imas.behaviour.agent;

import Movement.BreadthFirstSearch;
import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author santi
 */
public class CyclicMessagingDigger extends CyclicBehaviour{
    
    private final String FOLLOW_PROS = "Follow prospector";
    
    public CyclicMessagingDigger(Agent a) {
        super(a);
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        
        // Sends a message to the digger coordinator to get the movement
        ACLMessage movementRequest = new ACLMessage(ACLMessage.REQUEST);
        movementRequest.addReceiver(agent.getCoordinator());
        movementRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        movementRequest.setContent(MessageContent.GET_MOVEMENT);
        agent.send(movementRequest);
        agent.log("Request movement");
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if(msg == null)
            return;
        
        String content = msg.getContent();
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        if(content.startsWith(MessageContent.CONTRACT_ASIGN)) {
            String[] location = content.replace(MessageContent.CONTRACT_ASIGN, "").split(",");
            agent.log("Bid Won Location "+location[0]+" ,"+location[1]);
            agent.setState(DiggerAgent.DiggerState.GOING_TO_DIG);
            agent.setMovingToPos(Integer.parseInt(location[0]), Integer.parseInt(location[1]));
            agent.restartContractNetBehaviour();
        } else if(msg.getContent().equals(MessageContent.RANDOM)) {
            agent.setMovement(MessageContent.RANDOM);
            agent.log("Set movement random");
            agent.actionTurn();
        } else if(content.contains(MessageContent.CONTRACT_REJECT)) {
            agent.log("Bid Lost");
            agent.restartContractNetBehaviour();
        } else if(content.startsWith(MessageContent.MOVE_TO)) {
            if(agent.getDiggerState().equals(DiggerAgent.DiggerState.MOVING) && 
                    agent.getMovement().equals(FOLLOW_PROS)) {
                String[] pos = content.substring(MessageContent.MOVE_TO.length() + 1).split(",");
                GameSettings game = agent.getGame();
                Cell nextMov = BreadthFirstSearch.shortestPath(game.get(agent.getRow(), agent.getColumn()),
                    game.get(Integer.parseInt(pos[0]), Integer.parseInt(pos[1])), 
                    game.getMap());
                agent.sendMovToSys(nextMov.getRow(), nextMov.getCol());
            }
        } else try {
            if(msg.getContentObject() instanceof AID) {
                agent.setMovement(FOLLOW_PROS);
                agent.setAssignedProspector((AID)msg.getContentObject());
                agent.log("Following prospector");
                agent.actionTurn();
            } else if(msg.getContentObject() instanceof GameSettings){
                agent.setGame((GameSettings)msg.getContentObject());
                agent.actionTurn();
            } else{
                agent.errorLog("Error: " + content);
            }
        } catch (Exception e) {
            System.out.print("ERROR :"+msg.getContent());
            e.printStackTrace();
        }
        
        if(agent.getMovement().equals(MessageContent.RANDOM))
        {
            int result[] = agent.randomMovementDigger();
            
            if(result!=null)
            {
                agent.setRow(result[0]);
                agent.setColumn(result[1]);
            }

            
        }
        
    }
    
}
