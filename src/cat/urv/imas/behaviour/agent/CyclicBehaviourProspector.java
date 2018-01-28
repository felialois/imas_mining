/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;
import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


/**
 *
 * @author Antonio
 */
public class CyclicBehaviourProspector extends CyclicBehaviour{
        
    public CyclicBehaviourProspector(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ProspectorAgent agent = (ProspectorAgent)this.getAgent();
        int result[] = new int[2];
        
        result = agent.randomMovement();
        
        if(result != null)
        {
            agent.log(result[0]+","+result[1]);
            agent.setRow(result[0]);
            agent.setColumn(result[1]);
            agent.getGame().getAgentList().get(agent.getType()).get(Integer.valueOf(agent.getName().substring(3,4))).setRow(result[0]);
            agent.getGame().getAgentList().get(agent.getType()).get(Integer.valueOf(agent.getName().substring(3,4))).setCol(result[1]);
        }
   
    }

}
