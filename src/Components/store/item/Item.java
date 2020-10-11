package src.Components.store.item;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Item implements ItemInterface, Serializable {
    private static final long serialVersionUID = 1L;
    String itemID;
    String itemName;
    double price;
    SimpleDateFormat dateOfPurchase;

    public Item(String name, String itemID, Double price){
        itemName = name;
        this.itemID = itemID ;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public String getItemID(){return itemID;}

}
