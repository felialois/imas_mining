/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Antonio
 */
public class CyclicSystemBehaviour extends CyclicBehaviour{
    
        public CyclicSystemBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        SystemAgent agent = (SystemAgent)this.getAgent();
        agent.log("UPDATE GUI");
        agent.updateGUI();
    }    
}
