package xianzhan.jndi.rmi.client;

import xianzhan.jndi.base.service.IUserService;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @author xianzhan
 * @since 2021-12-18
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://127.0.0.1:2021");

        Context ctx = new InitialContext();
        Object jndi = ctx.lookup("userService");
        System.out.println(jndi);

        IUserService userService = (IUserService) jndi;
        System.out.println(userService.get());
    }
}
