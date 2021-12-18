package xianzhan.jndi.rmi.service;


import xianzhan.jndi.base.model.User;
import xianzhan.jndi.base.service.IUserService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author xianzhan
 * @since 2021-12-18
 */
public class UserServiceImpl extends UnicastRemoteObject implements IUserService {
    public UserServiceImpl() throws RemoteException {
    }

    @Override
    public User get() throws RemoteException {
        return new User("lxz", 2021);
    }
}
