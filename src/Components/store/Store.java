package Components.store;
import Components.store.item.Item;
import Components.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Store extends UnicastRemoteObject implements StoreInterface {
    String branchID;
    HashMap<String, ArrayList<Item>> inventory = new HashMap<>();

    private HashMap<String, HashMap<String, SimpleDateFormat>> userPurchaseLog = new HashMap<>();
    private HashMap<String, HashMap<String, SimpleDateFormat>> userReturnLog = new HashMap<>();
    private HashMap<String, Double> userBudgetLog = new HashMap<>();
    private HashMap<String, List<String>> waitList = new HashMap<>();

    private int quebecPurchaseItemUDPPort = 40000;
    private static int quebecListItemUDPPort = 40001;

    private int britishColumbiaPurchaseItemUDPPort = 40002;
    private static int britishColumbiaListItemUDPPort = 40003;

    private int ontarioPurchaseItemUDPPort = 40004;
    private static int ontarioListItemUDPPort = 40005;

    public Store(String branchID) throws RemoteException {
        this.branchID = branchID;
        openAllPorts(branchID);
    }


    @Override
    public boolean purchaseItem(String userID, String itemID, SimpleDateFormat dateOfPurchase) throws RemoteException{
       userID=userID.toLowerCase();
       double price = 0.00;

        switch (userID.substring(2, 3)) {
            case "u":
                if (inventory.containsKey(itemID)) {
                    for(Map.Entry<String, ArrayList<Item>> entry : inventory.entrySet())
                        if(entry.getKey().equalsIgnoreCase(itemID)) {
                            price = entry.getValue().get(0).getPrice();
                            if (userBudgetLog.containsKey(userID.toLowerCase()) && userBudgetLog.get(userID.toLowerCase()) - price >=0)
                                userBudgetLog.put(userID.toLowerCase(), userBudgetLog.get(userID.toLowerCase()) - price);
                            else if (1000 - price >=0)
                                userBudgetLog.put(userID.toLowerCase(), 1000.00 - price);
                            else{
                                System.out.print(userID + " does not have the funds to purchase Item:" + itemID );
                                Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                        + userID + " ItemID: " + itemID);
                                Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                        + userID + " ItemID: " + itemID);
                                return false;
                            }
                        }

                    inventory.get(itemID).remove(0);
                    HashMap<String,SimpleDateFormat> purchaseData = new HashMap<String,SimpleDateFormat>();
                    purchaseData.put(itemID,dateOfPurchase);
                    userPurchaseLog.put(userID,purchaseData);
                    System.out.print("Item was successfully purchased");
                    Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                            + userID + " ItemID: " + itemID);
                    Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                            + userID + " ItemID: " + itemID);
                    return true;
                } else {
                    Boolean purchaseSuccesful = false;
                    if (itemID.toLowerCase().contains("qc")) {
                        purchaseSuccesful = requestItemOverUDP(quebecPurchaseItemUDPPort, userID, itemID, dateOfPurchase);
                        System.out.print(purchaseSuccesful);
                    } else if (itemID.toLowerCase().contains("on")) {
                        purchaseSuccesful = requestItemOverUDP(ontarioPurchaseItemUDPPort, userID, itemID, dateOfPurchase);
                    } else if (itemID.toLowerCase().contains("bc")) {
                        purchaseSuccesful = requestItemOverUDP(britishColumbiaPurchaseItemUDPPort, userID, itemID, dateOfPurchase);
                    } else {
                        System.out.print("Item " + itemID + "is not availible you " + userID +
                                " will be added to the wait queue");
                        waitList(userID, itemID, dateOfPurchase);
                        Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID + "user added to waitlist");
                        Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID + "user added to waitlist");
                     }
                    if(purchaseSuccesful){
                        HashMap<String,SimpleDateFormat> purchaseData = new HashMap<>();
                        purchaseData.put(itemID,dateOfPurchase);
                        userPurchaseLog.put(userID,purchaseData);
                        System.out.print("Item was successfully purchased");
                        Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                        Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                    }
                    return purchaseSuccesful;
                }
            default:
                System.out.println(userID + " does not have permission to perform this method");
                Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID);
                Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID);
                return false;
        }
    }

    @Override
    public ArrayList<Item> findItem(String userID, String itemName) throws RemoteException {
        userID = userID.toLowerCase();
        ArrayList<Item> foundItems = new ArrayList<Item>();

        switch (userID.substring(2, 3)) {
            case "u":

                itemName = itemName.toLowerCase();

                for (Map.Entry<String, ArrayList<Item>> itemList : inventory.entrySet()) {
                    for (Item item : itemList.getValue()) {
                        if(item.getItemName().equalsIgnoreCase(itemName))
                        {
                            foundItems.add(item);
                        }
                    }
                }
                break;
            default:
                System.out.print(userID + " does not have permission to perform this method\n");
        }
        foundItems.addAll(getRemoteItemsByName(itemName, userID));

        System.out.print("Items in inventory");
        for (Map.Entry<String, ArrayList<Item>> itemList : inventory.entrySet()) {
            for (Item item : itemList.getValue()) {
                System.out.println( itemList.getKey() + " , " + itemList.getValue().get(0).getPrice());
            }
        }
        return foundItems;
    }

    @Override
    public boolean returnItem(String userID, String itemID, SimpleDateFormat dateOfReturn) throws RemoteException{
        userID = userID.toLowerCase();
        switch (userID.substring(2,3)) {
            case "u":
                if (userPurchaseLog.containsKey(userID))
                    if (userPurchaseLog.get(userID).containsKey(itemID)) {
                        if (isReturnable((userPurchaseLog.get(userID).get(itemID)), dateOfReturn)) {
                            userReturnLog.put(userID, userPurchaseLog.get(userID));
                            //TODO Put item back into inventory
                            System.out.println(userID + " has been returned");
                            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Return Item to Inventory USERID: "
                                    + userID + " ItemID: " + itemID);
                            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+ " Task SUCCESSFUL: Return Item to Inventory USERID: "
                                    + userID + " ItemID: " + itemID);
                            return true;
                        } else {
                            System.out.println(userID + " has purchased this item in the past, but item purchase date exceeds 30days");
                            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) +" Task UNSUCCESSFUL: Return Item to Inventory CustomerID: "
                                    + userID + " ItemID: " + itemID);
                            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) +" Task UNSUCCESSFUL: Return Item to Inventory CustomerID: "
                                    + userID + " ItemID: " + itemID);
                            return false;
                        }
                    } else {
                        System.out.println("User has past purchases, but NOT of this item");
                        Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+ " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                        Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                        return false;
                    }
                else {
                    System.out.println("User has no record of past purchases");
                    Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                    Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                    return false;
                }
        default:
            System.out.println(userID + " does not have permission to perform this method\n");
            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+ " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+ " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
            return false;
        }
    }

    @Override
    public boolean addItem(String userID, String itemID, String itemName, int quantity, double price) throws RemoteException{
        switch (userID.substring(2, 3).toLowerCase()) {
            case "m":
                itemID = itemID.toLowerCase();
                itemName = itemName.toLowerCase();
                Item item = new Item(itemID, itemName, price);
                int index = quantity;
                while (index != 0) {
                    if (inventory.get(itemID) == null) {
                        ArrayList<Item> itemList = new ArrayList<Item>();
                        inventory.put(itemID, itemList);
                        inventory.get(itemID).add(item);
                        index--;
                        System.out.println("Checking wait list");
                        checkWaitList(itemID);
                    }
                    else if ((inventory.get(itemID).size() > 0 && price == inventory.get(itemID).get(0).getPrice())
                            || inventory.get(itemID) == null)
                    {
                        inventory.get(itemID).add(item);
                        index--;
                        if(index==0)
                        {
                            System.out.print("\n" + itemID + " has been added to the store\n");
                            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Add Item to Inventory UserID: "
                                    +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);
                            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) +  " Task SUCCESSFUL: Add Item to Inventory UserID: "
                                    +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);
                            System.out.println("Checking wait list");
                            checkWaitList(itemID);
                        }
                    }
                    else {
                        System.out.print("\n" + itemID + " will not be added, this item does not have the " +
                                "same price as others of its kind");
                        Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                                +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);
                        Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) +  " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                                +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);

                        return false;
                    }
                }
                break;
            default:
                System.out.print(userID + "does not have permission to perform this method");
                Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                        +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);
                Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) +  " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                        +userID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price);
                return false;

        }
        return true;
    }

    @Override
    public boolean removeItem(String userID, String itemID, int quantity) throws RemoteException{
        userID = userID.toLowerCase();
        switch (userID.substring(2, 3)) {
            case "m":
                int index = quantity;
                while (index!= 0) {
                    if (inventory.containsKey(itemID) == true) {
                        inventory.get(itemID).remove(0);
                        index--;
                        if(index == 0) {
                            System.out.println("All requested " + itemID + " were removed");
                            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                                    + userID + " ItemID: " + itemID + " Quantity: " + quantity);
                            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                                    + userID + " ItemID: " + itemID + " Quantity: " + quantity);
                            return true;
                        }
                    } else if(inventory.get(itemID).size() == 0) {
                        System.out.println("Unable to remove " + quantity + " " + itemID +
                                "as there were no more in inventory");
                        Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                                +userID+" ItemID: "+itemID + " Quantity: "+quantity);
                        Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())  + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                                +userID+" ItemID: "+itemID + " Quantity: "+quantity);
                    }
                }
                if(quantity == -1 && inventory.containsKey(itemID) == true){
                    while(inventory.get(itemID).size() > 0) {
                        inventory.get(itemID).remove(0);
                    }
                    System.out.print("All " + itemID + " was removed from inventoory");
                }
                return true;
            default:
                System.out.println(userID + " does not have permission to perform this method");
                Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                        +userID+" ItemID: "+itemID + " Quantity: "+quantity);
                Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())  + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                        +userID+" ItemID: "+itemID + " Quantity: "+quantity);
        }
        return false;
    }

    @Override
    public String listItemAvailability(String userID) throws RemoteException {
        StringBuilder returnMessage = new StringBuilder("This store contains the following items: \r\n"+"\t");
       if(inventory != null) {
           for (Map.Entry<String, ArrayList<Item>> entry : inventory.entrySet()) {
               returnMessage.append(entry.getKey() + " : \r\n");
               for (Item item : entry.getValue()) {
                   returnMessage.append("\t" + item.toString() + "\n");
               }
           }
           System.out.print(returnMessage.toString());
           Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Listing Inventory ManagerID: "
                   + userID);
           Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Listing Inventory ManagerID: "
                   + userID);
           return returnMessage.toString();
       }else{
           System.out.print("no items are availible\n");
           Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Listing Inventory ManagerID: "
                   + userID);
           Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Listing Inventory ManagerID: "
                   + userID);
           return null;
       }

    }

    private void checkWaitList(String itemID) throws RemoteException {
        for(Map.Entry<String, List<String>> entry : waitList.entrySet())
            for(String waitListItemID : entry.getValue())
                if(itemID.equalsIgnoreCase(waitListItemID)) {
                    System.out.print(entry.getKey() + " is at the top of the list and will attempt to purchase" + itemID);
                        purchaseItem(entry.getKey(),itemID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ"));
                }
        }

    public Boolean waitList(String userID, String itemID, SimpleDateFormat dateOfPurchase) throws RemoteException{
        Boolean isWaitListed = false;
        if(waitList.containsKey(itemID)) {
            waitList.get(itemID).add(userID);

            Logger.writeStoreLog(this.branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" +itemID);
            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" +itemID);
            isWaitListed = true;
        }

        else {
            List<String> listOfCustomers = new ArrayList<>();
            listOfCustomers.add(userID);
            waitList.put(itemID, listOfCustomers);

            Logger.writeStoreLog(this.branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" +itemID);
            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" +itemID);
            isWaitListed = true;
        }
        return isWaitListed;
    }

    private boolean customerHasRequiredFunds(String userID, double price) {
        for(Map.Entry<String, Double> entry : userBudgetLog.entrySet())
            if(entry.getKey().equalsIgnoreCase(userID))
                return (entry.getValue().doubleValue() - price) >= 0.00 ;
            else
                return false;

        return true;
    }

    private void openAllPorts(String provinceID) {
        switch (provinceID.toLowerCase()) {
            case "on":
                openListItemUDPPort(ontarioListItemUDPPort);
                openPurchaseItemUDPPort(ontarioPurchaseItemUDPPort);
                break;
            case "qc":
                openListItemUDPPort(quebecListItemUDPPort);
                openPurchaseItemUDPPort(quebecPurchaseItemUDPPort);
                break;
            case "bc":
                openListItemUDPPort(britishColumbiaListItemUDPPort);
                openPurchaseItemUDPPort(britishColumbiaPurchaseItemUDPPort);
                break;
        }
    }

    public void openListItemUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            byte[] receiveData = new byte[8];
            String sendString = "ListItemUDPPort now open ...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            ListItemConnectionThread thread = new ListItemConnectionThread(serverSocket, receivePacket);
            thread.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Boolean requestItemOverUDP(int storePort, String userID, String itemID, SimpleDateFormat dateOfPurchase) {
        DatagramSocket socket = null;
        Boolean purchaseSuccesful = false;
        try {
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getLocalHost();
            byte[] incomingData = new byte[1024];

            StringBuilder requestMessage = new StringBuilder();
            requestMessage.append(userID + "\n");
            requestMessage.append(itemID + "\n");
            requestMessage.append(dateOfPurchase.toString() + "\n");

            byte[] b = requestMessage.toString().getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, host, storePort);
            socket.send(dp);
            Logger.writeUserLog(userID, dateOfPurchase + " Task UNCOMPLETE: Purchase Item looking at another store");
            Logger.writeStoreLog(branchID, dateOfPurchase + " Task UNCOMPLETE: Purchase Item looking at another store:");

            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length, host, dp.getPort());
            socket.receive(incomingPacket);
            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);

            purchaseSuccesful = (Boolean) is.readObject();

            System.out.println("Item object received and purchased successful:  " + purchaseSuccesful);
            System.out.print(userID + "does not have permission to perform this method");
            return purchaseSuccesful;
        } catch (Exception e) {
            System.err.println("Exception " + e);
            e.printStackTrace();
        }
        return purchaseSuccesful;
    }

    public void openPurchaseItemUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            byte[] receiveData = new byte[1024];
            String sendString = "Purchase item port opened and has received purchase request...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            PurchaseConnectionThread thread = new PurchaseConnectionThread(serverSocket, receivePacket);
            thread.start();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public List<Item> getRemoteItemsByName(String itemName,String userID) throws RemoteException{
        String currentProvinceID = this.branchID.toLowerCase();
        List<Item> remotelyReceivedItems = new ArrayList<Item>();

        switch(currentProvinceID){
            case "on":
                remotelyReceivedItems.addAll(requestRemoteItemList(quebecListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(britishColumbiaListItemUDPPort, itemName, userID));
                break;
            case "qc":
                remotelyReceivedItems.addAll(requestRemoteItemList(ontarioListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(britishColumbiaListItemUDPPort, itemName, userID));
                break;
            case "bc":
                remotelyReceivedItems.addAll(requestRemoteItemList(ontarioListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(quebecListItemUDPPort, itemName, userID));
                break;
        }
        return remotelyReceivedItems;
    }

    private List<Item> requestRemoteItemList(int storePort, String itemName,String userID) {
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getLocalHost();

            byte[] b = itemName.getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, host, storePort);
            socket.send(dp);

            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())  + " Task UNCOMPLETE: Find Item looking at another store");
            Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNCOMPLETE: Find Item looking at another store:");

            int bufferSize = 1024 * 4;
            byte[] buffer = new byte[bufferSize];
            DatagramPacket reply = new DatagramPacket(buffer, bufferSize, host, storePort);
            socket.receive(reply);

            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(in);


            ArrayList<Item> items = null;


            items = (ArrayList<Item>) is.readObject();
            System.out.println("Item List from storeport "+storePort+" has been received");
            return items;

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private boolean isReturnable (SimpleDateFormat dateOfPurchase, SimpleDateFormat dateOfReturn) {
        Calendar calendar = Calendar.getInstance();
        String dateOfPurchaseString = dateOfPurchase.format(new Date());
        String dateOfReturnString = dateOfReturn.format(new Date());

        try {
            Date dateOfPurchaseDate = dateOfPurchase.parse(dateOfPurchaseString);
            Date dateOfReturnDate = dateOfReturn.parse(dateOfReturnString);

            calendar.setTime(dateOfPurchaseDate);
            calendar.add(Calendar.HOUR, 720);

            Date acceptableLastDayForReturn = calendar.getTime();
            return !dateOfReturnDate.after(acceptableLastDayForReturn);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Item> getItemsByName(String itemName) throws RemoteException{
        ArrayList<Item> itemCollection = new ArrayList<>();
        for(Map.Entry<String, ArrayList<Item>> entry : inventory.entrySet()){
            for(Item item : entry.getValue()){
                if(item.getItemName().equalsIgnoreCase(itemName))
                    itemCollection.add(item);
            }
        }
        return itemCollection;
    }


    private class PurchaseConnectionThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;

        public PurchaseConnectionThread(DatagramSocket serverSocket, DatagramPacket receivePacket) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    serverSocket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String purchaseRequestString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                HashMap<String, String> purchaseOrder= new HashMap<>();
                String[] strParts = purchaseRequestString.split("\\r?\\n|\\r");
                purchaseOrder.put(strParts[0], strParts[1]);
                System.out.println("Requesting item: from other stores " + strParts[1]);

                String userID;
                Boolean purchaseOrderSuccess;

                for (Map.Entry<String, String> entry : purchaseOrder.entrySet()) {
                    userID = entry.getKey();
                    String itemID = entry.getValue();

                    try {
                        InetAddress host = InetAddress.getLocalHost();
                        purchaseOrderSuccess = purchaseItem(userID, itemID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ"));
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(purchaseOrderSuccess);

                        byte[] data = outputStream.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, host , receivePacket.getPort());
                        serverSocket.send(sendPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Item sent to the store that made the request ...");
            }
        }
    }

    private class ListItemConnectionThread extends Thread  {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;

        public ListItemConnectionThread(DatagramSocket serverSocket, DatagramPacket receivePacket ){
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run(){
            while (true) {
                try {
                    InetAddress host = InetAddress.getLocalHost();
                    serverSocket.receive(receivePacket);
                    String itemName = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Searching for: Item name: " + itemName);

                    ArrayList<Item> itemsFound = getItemsByName(itemName);

                    System.out.println(itemsFound);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    os.writeObject(itemsFound);

                    byte[] data = outputStream.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, host, receivePacket.getPort());
                    serverSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
