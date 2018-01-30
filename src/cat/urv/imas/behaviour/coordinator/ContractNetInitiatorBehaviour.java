/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MineralBid;
import cat.urv.imas.onthology.MineralContract;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author felipe
 */
public class ContractNetInitiatorBehaviour extends ContractNetInitiator {

    public ContractNetInitiatorBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.println("Agent " + propose.getSender().getName()
                + " proposed " + propose.getContent());
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        System.out.print("Contract Net : Responses Received");
        // Evaluate proposals.
        int bestProposal = -1;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) myAgent;
        int x = 0;
        int y = 0;

        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();

            if (msg.getPerformative() == ACLMessage.PROPOSE) {

                String[] content = msg.getContent().replace(MessageContent.CONTRACT_BID, "").split(",");

                agent.addContractBid(Integer.parseInt(content[0]), Integer.parseInt(content[1]),
                        msg.getSender(), Integer.parseInt(content[2]));
                x = Integer.parseInt(content[0]);
                y = Integer.parseInt(content[1]);
                
            }
        }
        
        MineralContract mc = new MineralContract(x,y);
        if (!agent.getContracts().containsKey(mc)){
            System.out.print("Contract not in map");
            throw new UnsupportedOperationException();
        }

        for (MineralBid contract : agent.getContracts().get(mc)) {
            ACLMessage reply = new ACLMessage(ACLMessage.CFP);
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            reply.setContent(MessageContent.CONTRACT_REJECT);
            acceptances.addElement(reply);
            int proposal = contract.getProposal();
            if (proposal > bestProposal) {
                bestProposal = proposal;
                bestProposer = contract.getDigger();
                accept = reply;
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            System.out.println("Accepting proposal " + bestProposal
                    + " from responder " + bestProposer.getName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            accept.setContent(MessageContent.CONTRACT_ASIGN+x+","+y);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent " + inform.getSender().getName()
                + " successfully performed the requested action");
    }

}
