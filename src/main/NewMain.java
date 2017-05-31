/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author philipp
 */
public class NewMain
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // TODO code application logic here
        Runtime rt = Runtime.instance();
        System.out.println("Runtime created");
        
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.println("profile created");
        AgentContainer mainContainer = rt.createMainContainer(profile);
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        System.out.println("Launching the agent container ..."+pContainer);
        AgentContainer cont = rt.createAgentContainer(pContainer);
        System.out.println("Launching the agent container after ..." +pContainer);
        System.out.println("containers created");
        System.out.println("Launching the rma agent on the main container ...");
        try
        {
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
            rma.start();
            Object [] a = {"Harry Potter"};
            AgentController createNewAgent = cont.createNewAgent("BookBuyerAgent", "agents.BookBuyerAgent", a);
            createNewAgent.start();
            AgentController bksa = cont.createNewAgent("Book Seller Agent","agents.BookSellerAgent", new Object[0]);
            bksa.start();
        } catch (StaleProxyException ex)
        {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
