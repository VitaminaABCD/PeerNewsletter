package peer1hw;

import communication.Forwarder;
import communication.GlobalSnapshotMessage;
import communication.Message;
import communication.OperationMessage;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class ClientHandler implements Runnable
{
    private HashSet<InetSocketAddress> myNeighbours;
    private VectorClock myVectorClock;
    private Newsletter news; 
    private InetSocketAddress myInetSocketAddress;
    private State stato;
    private int globalSnapshotCounter;
    final private TreeMap<Marker, Recorder> markerMap;
    private Logger logger;

    public ClientHandler(InetSocketAddress myInetSocketAddress, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         VectorClock myVectorClock, 
                         Newsletter news,
                         State stato,
                         TreeMap<Marker, Recorder> markMap,
                         Logger logger)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.myVectorClock = myVectorClock;
        this.news = news;
        this.stato = stato;
        this.globalSnapshotCounter = 0;
        this.markerMap = markMap;
        this.logger = logger;
    }
    
    @Override
    public void run()
    {
        Scanner scanner = new Scanner(System.in);
        int choice;
        
        while (true)
        {
            printMenu();
            choice = scanner.nextInt();
            switch(choice)
            {
                case 1:
                    //withdraw();
                    break;
                case 2:
                    //deposit();
                    break;
                case 3:
                    printTotal();
                    break;
                case 4:
                    takeGlobalSnapshot();
                    break;
                case 5:
                    printLog();
                    break;
                case 6:
                    printNeighbours();
                    break;
                default:
                    System.out.println("Comando non contemplato!");
            }
        }
    }
    
    public void printMenu()
    {
        System.out.println("--------------MENU--------------");
        System.out.println("\n\t1) ");
        System.out.println("\t2) ");
        System.out.println("\t3) Stampa il saldo attuale");
        System.out.println("\t4) Global Snapshot");
        System.out.println("\t5) Stampa Log");
        System.out.println("\t6) Stampa Peer Vicini\n");
        System.out.println("--------------------------------");
        System.out.println("\nInserisci il comando:");
    }

    
    //Si deve sincronizzare sull'Hashset dei vicini
    /*private void causalOrderMulticast(OperationMessage.OperationType operationType,
                                      double amount)
    {
        
        myVectorClock.updateVectorClock();
        for(InetSocketAddress receiver: myNeighbours)
        {
            Message m = new OperationMessage(myInetSocketAddress, 
                                             receiver, 
                                             myVectorClock, 
                                             operationType, 
                                             amount);
            Forwarder.sendMessage(m);
        }
        
    }*/
    
    /*private void deposit()
    {
        double amount = getAmount();
        //causalOrderMulticast(OperationMessage.OperationType.DEPOSIT, amount);
        news.deposit(myInetSocketAddress, amount);
        String record = "[PEER: " + myInetSocketAddress +
                         " deposita " + amount + "]";
        //Logger.getLogger(Peer.class.getName()).log(Level.INFO, record);
        logger.log(Level.INFO, record);
    }*/
    
    /*private void withdraw()
    {
        //Possiamo avere saldo negativo.
        //Non c'è controllo sul quantitativo che possiamo prelevare;
        double amount = getAmount();
        causalOrderMulticast(OperationMessage.OperationType.WITHDRAW, amount);
        news.withdraw(myInetSocketAddress, amount);
        String record = "[PEER: " + myInetSocketAddress +
                         " preleva " + amount + "]";
        Logger.getLogger(Peer.class.getName()).log(Level.INFO, record);
    }*/
    
    /*private double getAmount()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("> Amount: ");
        return scanner.nextDouble();
    }*/

    private void printTotal()
    {
        System.out.println("> SALDO DISPONIBILE : " + news.getTotal());
    }

    private void takeGlobalSnapshot()
    {
        State frozenState = stato.getCopy();
        LocalSnapshot mySnapshot = new LocalSnapshot(myInetSocketAddress, frozenState);
        GlobalSnapshot globalSnapshot = new GlobalSnapshot(myInetSocketAddress);
        globalSnapshot.addSnapshot(mySnapshot);
        
        Recorder recorder = new Recorder(globalSnapshot);
        Marker marker = new Marker(myInetSocketAddress, globalSnapshotCounter++);
        
        synchronized(markerMap)
        {
            markerMap.put(marker, recorder);
        }
        
        floodMarker(marker);
        
    }

    private void printLog()
    {
        stato.printInfrastacstrur();
    }
    
    private void printNeighbours()
    { 
        System.out.println("{");
        for (InetSocketAddress isa : myNeighbours)
            System.out.println("Indirizzo: " + isa.getAddress() + " - Porta: " + isa.getPort());
        System.out.println("}");
    }
    
    private void floodMarker(Marker marker)
    {
        for (InetSocketAddress isa : myNeighbours)
        {
            GlobalSnapshotMessage gsm = new GlobalSnapshotMessage(myInetSocketAddress, 
                                                                  isa, 
                                                                  marker, 
                                                                  null);
            Forwarder.sendMessage(gsm);
        }
    }
}
