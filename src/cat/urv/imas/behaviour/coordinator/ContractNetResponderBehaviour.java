/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

/**
 *
 * @author felipe
 */
public class ContractNetResponderBehaviour extends ContractNetResponder {

    public ContractNetResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) 
            throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + myAgent.getName() + ": CFP received from " 
                + cfp.getSender().getName() 
                + ". Action is " + cfp.getContent());
        
        String[] coord = cfp.getContent()
                .replace(MessageContent.CONTRACT_PROPOSE, "")
                .split(",");
        
        DiggerAgent agent = (DiggerAgent)myAgent;
        
        
        int proposal = ((DiggerAgent)myAgent).evaluateAction(
                Integer.parseInt(coord[0]),Integer.parseInt(coord[1]));
        if (agent.getDiggerState().equals(DiggerAgent.DiggerState.MOVING)||
                agent.getDiggerState().equals(DiggerAgent.DiggerState.RETRIEVING_METAL)){
            // We provide a proposal
                System.out.println("Agent " + myAgent.getName() + ": Proposing " + proposal);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(MessageContent.CONTRACT_BID+
                        Integer.parseInt(coord[0])+","+Integer.parseInt(coord[1])
                        +","+String.valueOf(proposal));
                return propose;
        } else {
            // We refuse to provide a proposal
            System.out.println("Agent " + myAgent.getName() + ": Refuse");
            throw new RefuseException("evaluation-failed");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.println("Agent " + myAgent.getName()
                + ": Proposal accepted");
        if (((DiggerAgent)myAgent).getDiggerState().equals(DiggerAgent.DiggerState.GOING_TO_DIG)){
            System.out.println("Agent " + myAgent.getName() + 
                    ": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        } else {
            System.out.println("Agent " + myAgent.getName() + 
                    ": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent " + myAgent.getName() + ": Proposal rejected");
    }

}
