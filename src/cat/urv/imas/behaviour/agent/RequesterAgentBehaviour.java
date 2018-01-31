/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.agent.WorkerAgent;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 *
 * @author felipe
 */
public class RequesterAgentBehaviour extends AchieveREInitiator {

    public RequesterAgentBehaviour(WorkerAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        agent.log("Started behaviour to deal with AGREEs");
    }

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        WorkerAgent agent = (WorkerAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        WorkerAgent agent = (WorkerAgent) this.getAgent();
        DiggerAgent digger = null;
        if(agent instanceof DiggerAgent)
            digger = (DiggerAgent) agent;
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            GameSettings game = (GameSettings) msg.getContentObject();
            if(digger != null)
                digger.setGame(game);
            else
                agent.setGame(game);
            agent.log(game.getShortString());
            agent.setRow(agent.getGame().getAgentList().get(agent.getType()).get(Integer.valueOf(agent.getName().substring(3,agent.getName().indexOf("@")))).getRow());
            agent.setColumn(agent.getGame().getAgentList().get(agent.getType()).get(Integer.valueOf(agent.getName().substring(3,agent.getName().indexOf("@")))).getCol());
            agent.log("Position: " + agent.getRow()+","+agent.getColumn());
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
        WorkerAgent agent = (WorkerAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
        WorkerAgent agent = (WorkerAgent) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
        WorkerAgent agent = (WorkerAgent) this.getAgent();
        agent.log("Action refused.");
    }

}
