package xianzhan.jndi.base.service;


import xianzhan.jndi.base.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author xianzhan
 * @since 2021-12-18
 */
public interface IUserService extends Remote {

    /**
     * 获取用户
     *
     * @return 用户
     * @throws RemoteException rmi
     */
    User get() throws RemoteException;
}
