package Components.store.item;

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

    public Item(String name, String branch, Double price){
        itemName = name;
        itemID = branch + itemNumber;
        this.price = price;

    }

    public void setDateOfPurchase(){
        dateOfPurchase = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ");
    }
    public String getItemName() {
        return itemName;
    }

    public String getItemID() {
        return itemID;
    }

    public double getPrice() {
        return price;
    }

    public SimpleDateFormat getDateOfPurchase(){return dateOfPurchase;}
}
