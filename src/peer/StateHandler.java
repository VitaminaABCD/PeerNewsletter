package peer;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Ylenia Trapani, Giulia Giuffrida, Manuela Ramona Fede
 */
public class StateHandler extends Handler
{
    private final State stato;

    public StateHandler(State stato)
    {
        this.stato = stato;
    }

    @Override
    public void publish(LogRecord record)
    {
        stato.recordOperation(record.getMessage());    
    }

    @Override
    public void flush()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SecurityException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
