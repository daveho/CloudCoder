package org.cloudcoder.app.server.submitsvc.oop;

import java.rmi.RemoteException;

public class Directory implements IDirectory
{

    /* (non-Javadoc)
     * @see org.cloudcoder.app.server.submitsvc.oop.IDirectory#getRemoteSubmitService()
     */
    @Override
    public IRemoteSubmitService getRemoteSubmitService() throws RemoteException
    {
        // Find and return the remote submit service (there should be only the one)
        return null;
    }

}
