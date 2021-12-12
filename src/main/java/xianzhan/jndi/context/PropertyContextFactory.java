package xianzhan.jndi.context;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

/**
 * 上下文工厂
 *
 * @author xianzhan
 */
public class PropertyContextFactory implements InitialContextFactory {

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return new PropertyContext(environment).load();
    }
}
