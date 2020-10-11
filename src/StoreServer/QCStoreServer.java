package src.StoreServer;

import src.Components.store.Store;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;

/* This class represents the object server for a distributed object of class
    Hello, which implements the remote interface HelloInterface. */
public class QCStoreServer {

    private static Store quebecStore;

    public static void main(String args[]) throws RemoteException {

        try {
            startRegistry(40006);
            String url = "rmi://localhost:40006/quebecStore";
            quebecStore = new Store("QC");

            Naming.rebind(url, (Remote) quebecStore);

            System.out.print("Quebec store waiting for the client's requests\n");

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