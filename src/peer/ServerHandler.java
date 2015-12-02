package peer;

import communication.Forwarder;
import communication.GlobalSnapshotMessage;
import communication.JSMessage;
import communication.Message;
import communication.OperationMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;
import java.util.Map;
import static communication.OperationMessage.getRecordForMessage;
import static peer.Peer.peersAreEqual;

/**
 * Server Handler con ACK
 * @author Ylenia Trapani, Giulia Giuffrida, Manuela Ramona Fede
 */

//Questo thread gestisce solamente Peers o il Join Server.
class ServerHandler implements Runnable
{
    private InetSocketAddress myInetSocketAddress;
    private HashSet<InetSocketAddress> myNeighbours;
    private Socket incomingSocket;
    
    private TimeStamp myTimeStamp;
    private Newsletter news; 
    private State stato;
    private ArrayList<OperationMessage> messageBuffer;
    private final TreeMap<Marker, Recorder> markerMap;
    private Logger logger;
    
    
    public ServerHandler(InetSocketAddress myInetSocketAddress,
                         Socket incomingSocket, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         TimeStamp myTimeStamp,
                         Newsletter news,
                         State stato,
                         ArrayList<OperationMessage> messageBuffer,
                         TreeMap<Marker, Recorder> markerMap,
                         Logger logger)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.incomingSocket = incomingSocket;
        this.myTimeStamp = myTimeStamp;
        this.news = news;
        this.stato = stato;
        this.messageBuffer = messageBuffer;
        this.markerMap = markerMap;
        this.logger = logger;
    }

    @Override
    public void run()
    {
        //Lasciare solo l'input stream
        //Outputstream fa saltare tutti in aria
        try (ObjectInputStream in = new ObjectInputStream(incomingSocket.getInputStream()))
        {
            Message m = (Message) in.readObject();
            processMessage(m);
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void processMessage(Message m)
    {
        if (m instanceof JSMessage)  
            updateNeighbours(((JSMessage) m).getNeighbours());
        else if (m instanceof OperationMessage) 
            processOperationMessage((OperationMessage) m);
        else if (m instanceof GlobalSnapshotMessage)
            processGlobalSnapshotMessage((GlobalSnapshotMessage) m);
        else 
            System.err.println("FORMATO DEL MESSAGGIO NON RICONOSCIUTO PER IL PROCESSAMENTO!");
    }
    
    synchronized private void updateNeighbours(HashSet<InetSocketAddress> group)
    {
        myNeighbours.addAll(group);
        System.out.println("I miei vicini sono: ");
        System.out.println(myNeighbours);
    }

    private void processOperationMessage(OperationMessage m)
    {
        switch (m.getOperationType())
        {
            //case READ:
            case WRITE:
                receiveOperationMessage(m);
                break;
            default:
                System.err.println("OPERAZIONE INVALIDA!");
        }
    }

    private void receiveOperationMessage(OperationMessage m)
    {
        if (myTimeStamp.isHappenedBefore(m.getSenderTimeStamp()))
        {
            deliverOperationMessage(m);
            checkMessageBuffer();
        }
        else
            enqueueOperationMessage(m);
    }

    private void deliverOperationMessage(OperationMessage m)
    {
        TimeStamp tmp = m.getSenderTimeStamp();
        myTimeStamp.updateTs();
        
        switch (m.getOperationType())
        {
            /*case READ:
                
                break;*/
            case WRITE:
                
                break;
            default:
                System.err.println("FORMATO DELL'OPERAZIONE NON RICONOSCIUTO!");
        }
        
        String record = getRecordForMessage(m);
        
        //Controlla se sta registrando ancora messaggi da questo canale
        for(Map.Entry<Marker, Recorder> entry: markerMap.entrySet())
        {
            Recorder r = entry.getValue();
            InetSocketAddress sender = m.getSender();
            if(r.shouldBeRegistered(sender))
            {
                r.getSnapshot().updateChannelsState(record);
            }
        }
        
        logger.log(Level.INFO, record);
        
    }
    
    synchronized private void checkMessageBuffer()
    {
        boolean end = false;
        
        while (!end)
        {
            for(OperationMessage om : messageBuffer)
            {
                if (myTimeStamp.isHappenedBefore(om.getSenderTimeStamp()))
                {
                    deliverOperationMessage(om);
                    messageBuffer.remove(om);
                    end = false;
                    break;
                }
                else
                    end = true;
            }
        }
    }

    synchronized private void enqueueOperationMessage(OperationMessage m)
    {
        messageBuffer.add(m);
    }

    private void processGlobalSnapshotMessage(GlobalSnapshotMessage m)
    {
        Marker marker = m.getMarker();
      
        synchronized(markerMap)
        {       
            if (markerMap.containsKey(marker))
            {
                Recorder recorder = markerMap.get(marker);
                
                if(isMyMarker(marker))
                {
                    //Gestiscila come INITIATOR
                    
                    if(m.getBody() != null)
                    {
                        //aggiungo il snapshot del messaggio al mio globalsnapshot
                        //incrementa il conteggio degli ack (ack del GS sono gli stati dei peer)
                        //Quando ho tutti gli ack stampo
                        //rimuovo dalla mappa il marker;
                        
                        GlobalSnapshot globalSnapshot = (GlobalSnapshot) recorder.getSnapshot();
                        globalSnapshot.addSnapshot(m.getBody());
                        recorder.incrementCounter();
                        
                        if(recorder.getCounter() == myNeighbours.size())
                        {
                            globalSnapshot.printSnapshot();
                            markerMap.remove(marker);
                        }
                    }
                }
                else
                {
                    //Gestiscila come PEER che ha sta già lavorando su questo marker
                    
                    //Mi aspetto un ack dai miei vicini tranne il committer
                    //Aumento il conteggio dei miei ack e aggiungo il sender ai banditi
                    //Quando ho tutti gli ack che mi servono SEND STATO all'initiator
                    //Mando un hack al committer. 
                    
                    recorder.excludeChannelFromRecord(m.getSender());
                    recorder.incrementCounter();
                    
                    if(recorder.getCounter() == myNeighbours.size())
                    {
                        Snapshot mySnapshot = recorder.getSnapshot();
                        InetSocketAddress initiator = marker.getInitiator();
                        GlobalSnapshotMessage gsm = new GlobalSnapshotMessage(myInetSocketAddress, 
                                                                              initiator, 
                                                                              marker, 
                                                                              mySnapshot);
                        Forwarder.sendMessage(gsm);
                        
                        InetSocketAddress committer = recorder.getCommitter();
                        GlobalSnapshotMessage ackMessage = new GlobalSnapshotMessage(myInetSocketAddress,
                                                                                     committer,
                                                                                     marker,
                                                                                     null);
                        Forwarder.sendMessage(ackMessage);
                        markerMap.remove(marker);
                    }
                    
                    
                }
            }
            else //Qui non sarò mai INITIATOR
            {
                //Potrebbero arrivare ACK Spuri quindi:
                //Se l'ACK (o messaggio) si riferisce a me come inititiator
                //  lo scarto
                
                if(peersAreEqual(myInetSocketAddress, marker.getInitiator()))
                    System.out.println("Ricevuto ACK SPURIO.");
                
                  //Altrimenti
                //  Mi hanno COLORATO
                
                //inserisco marker nella mappa
                //inserisco indirizzo di chi mi colora tra i committer
                //Faccio il mio snapshot
                //inibisco il canale
                //inoltro il messaggio ai miei vicini
                
                else
                {
                    State myStato = stato.getCopy();
                    Snapshot mySnapshot = new LocalSnapshot(myInetSocketAddress, myStato);
                    
                    //Se ho solo un vicino SEND STATO all'initiator
                    //Mando l'ack al committer
                    //Rimuovo la marker map
                    
                    if(myNeighbours.size() == 1)
                    {
                        InetSocketAddress initiator = marker.getInitiator();
                        GlobalSnapshotMessage gsMessage = new GlobalSnapshotMessage(myInetSocketAddress,
                                                                                    initiator, 
                                                                                    marker, 
                                                                                    mySnapshot);
                        Forwarder.sendMessage(gsMessage);
                        
                        InetSocketAddress committer = m.getSender();
                        GlobalSnapshotMessage ackMessage = new GlobalSnapshotMessage(myInetSocketAddress,
                                                                                     committer,
                                                                                     marker,
                                                                                     null);
                        Forwarder.sendMessage(ackMessage);
                    }
                    else
                    {
                        Recorder myRecorder = new Recorder(mySnapshot);
                        myRecorder.incrementCounter();
                        myRecorder.excludeChannelFromRecord(m.getSender());
                        markerMap.put(marker, myRecorder);

                        inoltraMarker(marker, m.getSender());
                    }  
                } 
            }
        }
    }

    
    private boolean isMyMarker(Marker marker)
    {
        return peersAreEqual(marker.getInitiator(), myInetSocketAddress);
    }

    private void inoltraMarker(Marker marker, InetSocketAddress excludedPeer)
    {
        for(InetSocketAddress neighbour: myNeighbours)
        {
            if(!peersAreEqual(neighbour, excludedPeer))
            {
                System.out.println(">>> INOLTRO MARKER A " + neighbour);
                GlobalSnapshotMessage gsMessage = new GlobalSnapshotMessage(myInetSocketAddress, neighbour, marker, null);
                Forwarder.sendMessage(gsMessage);
            }
        }
    }
    
}
