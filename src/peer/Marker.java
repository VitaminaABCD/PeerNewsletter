package peer;

import java.io.Serializable;
import java.net.InetSocketAddress;
import static peer.Peer.peersAreEqual;

/**
 *
 * @author Ylenia Trapani, Giulia Giuffrida, Manuela Ramona Fede
 */
public class Marker implements Serializable, Comparable<Marker>
{
    private InetSocketAddress initiator;
    private int globalSnapshotNumber;

    public Marker(InetSocketAddress initiator, int globalSnapshotNumber)
    {
        this.initiator = initiator;
        this.globalSnapshotNumber = globalSnapshotNumber;
    }

    public InetSocketAddress getInitiator()
    {
        return initiator;
    }

    public void setInitiator(InetSocketAddress initiator)
    {
        this.initiator = initiator;
    }

    @Override
    public int compareTo(Marker o)
    {
        /*if (initiator.getHostString().equalsIgnoreCase(o.initiator.getHostString()) &&
             Integer.compare(initiator.getPort(), o.initiator.getPort()) == 0)
            return Integer.compare(globalSnapshotNumber, o.globalSnapshotNumber);
        else if(!initiator.getHostString().equalsIgnoreCase(o.initiator.getHostString()))
        {
            return initiator.getHostString().compareToIgnoreCase(o.initiator.getHostString());
        }
        else
        {
            return Integer.compare(initiator.getPort(), o.initiator.getPort());
        }        
    }*/
        
        if(peersAreEqual(initiator, o.getInitiator())
           && globalSnapshotNumber == o.globalSnapshotNumber)
            return 0;
        else 
            return -1;
    }
    
    @Override
    public String toString()
    {
        return "[MARKER] Initiator: " + initiator + " GlobalShotNumber: " + globalSnapshotNumber;
    }
}
