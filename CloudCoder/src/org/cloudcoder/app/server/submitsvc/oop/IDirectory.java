package org.cloudcoder.app.server.submitsvc.oop;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDirectory extends Remote
{
    IRemoteSubmitService getRemoteSubmitService() throws RemoteException;
}
