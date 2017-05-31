/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 *
 * @author philipp
 */
public class MyAgent extends Agent
{
    @Override
    protected void setup()
    {
        System.out.println("Adding waker behaviour");
        addBehaviour(new WakerBehaviour(this, 10000)
        {
            //perform operation X
        });
    }
}
