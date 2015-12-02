/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ylenia Trapani, Giulia Giuffrida, Manuela Ramona Fede
 */
public class UIPeer
{
    private static final int JS_PORT = 9999;
  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        File file = new File("/Users/giuliagiuffrida/NetBeansProjects/PeerNewsletter/src/peer/port");
        int myPort = 0;
        
        if (args.length < 1)
        {
            System.out.println("ATTENZIONE: INSERIRE L'INDIRIZZO IP DEL JOIN SERVER");
            System.exit(1);
        }
        
        String js_addr = args[0];
        
        try 
        {
            Scanner s = new Scanner(file);
            myPort = s.nextInt();
            
            assert (myPort < 10006) && (myPort > 9999);
            
            PrintWriter p = new PrintWriter(file);
            p.print(myPort+1);
            p.flush();
            
            System.out.println("La mia porta è: " + myPort);
            
            Peer peer = new Peer(myPort);
            peer.connect(js_addr, JS_PORT);
            peer.start();
            
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(UIPeer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
