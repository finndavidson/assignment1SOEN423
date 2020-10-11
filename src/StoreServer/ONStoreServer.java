package src.StoreServer;

import src.Components.store.Store;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;

public class ONStoreServer {

    private static Store ontarioStore;

    public static void main(String args[]) throws RemoteException {
        try {
            startRegistry(40008);
            String url = "rmi://localhost:40008/ontarioStore";
            ontarioStore = new Store("ON");

            Naming.rebind(url, (Remote) ontarioStore);

            System.out.print("Ontario store waiting for the client's requests\n");
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
