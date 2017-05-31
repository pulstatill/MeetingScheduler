/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author philipp
 */
public class BookBuyerAgent extends Agent
{

    private String targetBookTitle;

    private AID[] sellerAgents =
    {
        new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)
    };

    @Override
    protected void setup()
    {
        /**
         * * Printout a welcome Message **
         */
        System.out.println("Helllo! Buyer-agent " + getAID().getName() + " is ready.");
        // Ge the title of the book to buy as a start-up argument

        Object[] args = getArguments();
        if (args != null && args.length > 0)
        {
            targetBookTitle = (String) args[0];
            System.out.println("Target book is " + targetBookTitle);

            // Add a TickerBehaviour that schedules a request to seller agents every minute
            addBehaviour(new TickerBehaviour(this, 10000)
            {
                protected void onTick()
                {
                    System.out.println("Trying to buy " + targetBookTitle);
                    // Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setName("JADE-book-trading");
                    sd.setType("book-selling");
                    template.addServices(sd);
                    try
                    {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; i++)
                        {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException fe)
                    {
                        fe.printStackTrace();
                    }

                    // Perform the request
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else
        {
            // Make the agent terminate
            System.out.println("No target book title specified");
            doDelete();
        }
    }

    @Override
    protected void takeDown()
    {
        //Printout a dismissal message
        System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
    }

    private class RequestPerformer extends Behaviour
    {

        private AID bestSeller;
        private int bestPrice;
        private int repliesCnt = 0;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action()
        {
            switch (step)
            {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; i++)
                    {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null)
                    {
                        if (reply.getPerformative() == ACLMessage.PROPOSE)
                        {
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice)
                            {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length)
                        {
                            step = 2;
                        }
                    } else
                    {
                        block();
                    }
                    break;
                case 2:

                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"), MessageTemplate.MatchInReplyTo(order.getReplyWith()));

                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null)
                    {
                        if (reply.getPerformative() == ACLMessage.INFORM)
                        {
                            System.out.println(targetBookTitle + " successfully purchased");
                            System.out.println("Price = " + bestPrice);
                            myAgent.doDelete();
                        }
                        step = 4;
                    } else
                    {
                        block();
                    }
                    break;

            }
        }

        @Override
        public boolean done()
        {
            return ((step == 2 && bestSeller == null) || step == 4);
        }

    }
}
