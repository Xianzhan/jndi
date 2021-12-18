package xianzhan.jndi.rmi.server;


import xianzhan.jndi.base.service.IUserService;
import xianzhan.jndi.rmi.service.UserServiceImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;

/**
 * @author xianzhan
 * @since 2021-12-18
 */
public class Main {

    public static void main(String[] args) throws Exception {
        LocateRegistry.createRegistry(2021);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://127.0.0.1:2021");

        Context ctx = new InitialContext();
        IUserService userService = new UserServiceImpl();
        ctx.bind("userService", userService);
    }
}
