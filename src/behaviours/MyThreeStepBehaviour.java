/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.core.behaviours.Behaviour;

/**
 *
 * @author philipp
 */
public class MyThreeStepBehaviour extends Behaviour
{
    private int step = 0;
    @Override
    public void action()
    {
        switch (step)
        {
            case 0: step++; break;
            case 1: step++; break;
            case 2: step++; break;
        }
    }

    @Override
    public boolean done()
    {
        return step == 3;
    }
    
}
