package peer;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class Newsletter
{
    private TreeMap<InetSocketAddress, String> operationsMap;

    public Newsletter()
    {
        operationsMap = new TreeMap<>(new Comparator<InetSocketAddress>()
        {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2)
            {                
                if(!o1.getHostString().equalsIgnoreCase(o2.getHostString()))
                {
                    return o1.getHostString().compareToIgnoreCase(o2.getHostString());
                }
                else
                {
                    return Integer.compare(o1.getPort(), o2.getPort());
                }
            }
        });
    }
    
    synchronized String getNews()
    {
        String tot = null;
        
        for(Map.Entry<InetSocketAddress, String> e : operationsMap.entrySet())
            tot += e.getValue();
        
        return tot;
    }
    
}
