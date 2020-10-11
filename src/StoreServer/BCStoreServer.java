package src.StoreServer;

import src.Components.store.Store;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BCStoreServer {

    private static Store bcStore;

    public static void main(String args[]) throws RemoteException {

        try {
            startRegistry(40007);
            String url = "rmi://localhost:40007/bcStore";
            Store bcStore = new Store("BC");

            Naming.rebind(url, (Remote) bcStore);

            System.out.print("BC store waiting requests client's request\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startRegistry(int RMIPortNum) throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list( );

        }
        catch (RemoteException e) {
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }
}
