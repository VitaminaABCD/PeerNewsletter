package communication;

import java.net.InetSocketAddress;
import peer.TimeStamp;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */

public class OperationMessage extends Message<Double>
{   
    private TimeStamp senderTimeStamp;
    private OperationType operationType;
    
    public OperationMessage(InetSocketAddress sender, 
                            InetSocketAddress receiver, 
                            TimeStamp senderTimeStamp,
                            OperationType operationType,
                            Double body)
    {
        super(sender, receiver, body);
        this.senderTimeStamp = senderTimeStamp;
        this.operationType = operationType;
    }
    
    static public String getRecordForMessage(OperationMessage message)
    {
        OperationMessage.OperationType type = message.getOperationType();
        String record = null;
        
        switch (type)
        {
            case READ:
                record = "[MESSAGGIO -> PEER: " + message.getSender() +
                         " deposita " + message.getBody() + "]";
                break;
            case WRITE:
                record = "[PEER: " + message.getSender() +
                         " preleva " + message.getBody() + "]";
                break;
            default:
                record = "FORMATO MESSAGGIO NON RICONOSCIUTO";
        }
        
        return record;
    }

    public TimeStamp getSenderTimeStamp()
    {
        return senderTimeStamp;
    } 
    
    public enum OperationType
    {
        READ,
        WRITE;
    }

    public OperationType getOperationType()
    {
        return operationType;
    }
    
    
}


