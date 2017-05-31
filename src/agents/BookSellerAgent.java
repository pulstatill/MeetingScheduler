/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Hashtable;

/**
 *
 * @author philipp
 */
public class BookSellerAgent extends Agent
{

    private Hashtable catalogue;
    private BookSellerGui myGui;

    @Override
    protected void setup()
    {
        // Create the catalogue
        catalogue = new Hashtable();

        // Create and show the GUI 
        myGui = new BookSellerGui(this);
        myGui.showGui();

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        } catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());
    }
    

    @Override

    protected void takeDown()
    {
        myGui.dispose();
        System.out.println("Seller-agent " + getAID().getName() + " terminating");
    }

    public void updateCatalogue(final String title, final int price)
    {
        addBehaviour(new OneShotBehaviour()
        {
            @Override
            public void action()
            {
                System.out.println(title + " is added");
                catalogue.put(title, new Integer(price));
            }
        });
    }

    private class OfferRequestServer extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null)
            {
                System.out.println("agents.BookSellerAgent.OfferRequestServer.action()  msg!=null");;
                // CFP Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.get(title);
                if (price != null)
                {
                    
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else
                {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else
            {
                block();
            }
        }

    }

    private class PurchaseOrdersServer extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null)
            {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                System.out.println("agents.BookSellerAgent.PurchaseOrdersServer.action()   remove book");
                Integer price = (Integer) catalogue.remove(title);
                
                if (price != null)
                {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + " sold to agent " + msg.getSender().getName());
                } else
                {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else
            {
                block();
            }
        }
    }
}
