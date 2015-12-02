package peer;

import java.net.InetSocketAddress;
import java.util.LinkedList;

/**
 *
 * @author Ylenia Trapani, Giulia Giuffrida, Manuela Ramona Fede
 */
public class LocalSnapshot extends Snapshot 
{
    State stato;
    LinkedList<String> channelsState;

    public LocalSnapshot(InetSocketAddress ownerPeer, State stato)
    {
        super(ownerPeer);
   
        this.stato = stato;
        this.channelsState = new LinkedList<>();
    }
    
    @Override
    public void updateChannelsState(String str)
    {
        channelsState.add(str);
    }
    
    private void printChannelStates()
    {
        for(String s : channelsState)
            System.out.println(s);
    }

    @Override
    public void printSnapshot()
    {
        System.out.println("*************** " + ownerPeer + " ***************\n");
        stato.printInfrastacstrur();
        System.out.println("--------------- Stato dei Canali ---------------\n");
        printChannelStates();
    }
    
}
