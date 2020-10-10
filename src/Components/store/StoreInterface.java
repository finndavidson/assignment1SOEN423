package Components.store;

import Components.store.item.Item;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface StoreInterface extends Remote {
    public abstract boolean purchaseItem(String userID, String itemID, SimpleDateFormat dateOfPurchase) throws RemoteException;
    public abstract ArrayList<Item> findItem(String userID, String itemName) throws RemoteException;
    public abstract boolean returnItem(String userID, String itemID, SimpleDateFormat dateOfPurchase) throws RemoteException;
    public abstract boolean addItem (String userID,String itemID,String itemName,int quantity,double price) throws RemoteException;
    public abstract boolean removeItem (String userID,String itemID,int quantity) throws RemoteException;
    public abstract String listItemAvailability (String  userID) throws RemoteException;
}
