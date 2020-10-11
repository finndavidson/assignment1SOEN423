package src.Components.client.user;

import src.Components.store.StoreInterface;
import src.Components.store.item.Item;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

public class User extends UserInterface {

    public static String userID = null;
    public static double budget = 1000.00;

    private static StoreInterface QCStore = null;
    private static StoreInterface ONStore = null;
    private static StoreInterface BCStore = null;

    private static HashMap<String, HashMap<String, String>> commandInterface = new HashMap<>();

    public static void main(String[] args){
        prepareCommandConsoleInterfaces(commandInterface);

        Scanner scanner = new Scanner(System.in);
        System.out.print("\n>>>>>>>>>>> Welcome to your local DSMS <<<<<<<<<<<<\r\n");
        System.out.print("Please Specify what type of user you are. Type 'U' for Customer or 'M' for Manager.\n");

        userID = generateUserType(scanner);
        userID = generateUserStore(scanner)+userID;
        userID = userID + generateUserID(scanner);
        userID = userID.toLowerCase();

        try {
            QCStore = (StoreInterface) Naming.lookup("rmi://localhost:40006/quebecStore");
            BCStore = (StoreInterface) Naming.lookup("rmi://localhost:40007/bcStore");
            ONStore = (StoreInterface) Naming.lookup("rmi://localhost:40008/ontarioStore");

            while(true) {
                switch (userID.substring(0, 2)) {
                    case "qc":
                        generateUserAction(scanner, QCStore);
                        break;
                    case "on":
                        generateUserAction(scanner, ONStore);
                        break;
                    case "bc":
                        generateUserAction(scanner, BCStore);
                        break;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void prepareCommandConsoleInterfaces(HashMap<String, HashMap<String, String>> commandInterface) {
        HashMap<String, String> managerCommandInterfaces = new HashMap<>();
        HashMap<String, String> userCommandInterfaces = new HashMap<>();

        String addCommandInterface = "\tAdd Item:  User ID  , Item ID, Item name, Quantity, Price\n";
        String removeCommandInterface = "\tRemove Item:  ( User ID  ,  Item ID, Quantity )\n";
        String listCommandInterface = "\tList Items:  ( User ID )\n";


        String purchaseCommandInterface = "\tPurchase Item:  User ID  , Item ID \n";
        String findCommandInterface =  "\tFind Item:  ( User ID  ,  Item Name )\n";
        String returnCommandInterface = "\tReturn Item:  ( User ID  ,  Item ID )\n";

        managerCommandInterfaces.put("add", addCommandInterface);
        managerCommandInterfaces.put("remove", removeCommandInterface);
        managerCommandInterfaces.put("list", listCommandInterface);
        User.commandInterface.put("M", managerCommandInterfaces);

        userCommandInterfaces.put("purchase", purchaseCommandInterface);
        userCommandInterfaces.put("find", findCommandInterface);
        userCommandInterfaces.put("return", returnCommandInterface);
        User.commandInterface.put("U", userCommandInterfaces);
    }

    private static String generateUserType(Scanner scanner) {
        String userType = scanner.nextLine();
        userType = userType.toLowerCase();

        switch(userType){
            case "u" :
                System.out.print("A User can execute the following actions in their own store: \n");
                userTaskList();
                return userType;
            case "m" :
                managerTaskList();
                System.out.print("A Manager can execute the following actions in their own store: \n");

                return userType;
            default:
                System.out.println("Error: Invalid user type entry. Please try again entering M/U");
                    return generateUserType(scanner);
        }
    }

    private static String generateUserStore(Scanner scanner) {
        System.out.print("Enter the branch that is local to you (BC/ON/QC): ");
        String branchID = scanner.nextLine();
        branchID = branchID.toLowerCase();
        switch (branchID){
            case "on":
            case "qc":
            case "bc":
                return branchID;
            default:
            System.out.print("Error: Invalid branch ID please try again using (BC/ON/QC):\n\n");
            return generateUserStore(scanner);
        }
    }

    private static int generateUserID(Scanner scanner) {
        System.out.print("Enter a 4 digit number that will be used to identify you as a user:");
        int userNumber = scanner.nextInt();
        if(userNumber>=1000 && userNumber<10000)
            return userNumber;
        else
            return generateUserID(scanner);
    }

    private static void generateUserAction(Scanner scanner, StoreInterface store) {
        if(userID.substring(2,3).equalsIgnoreCase("M")){

            System.out.print("A Manager can enter the following commands for their own store: \n\n");
            managerTaskList();

            System.out.println("Enter a command: ");

            String command = scanner.nextLine();

            handleManagerRequest(command, scanner, store);
        }
        else if(userID.substring(2,3).equalsIgnoreCase("U")){
            System.out.print("A Customer can enter the following commands for their own store: \n\n");
            userTaskList();
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();

            handleUserRequest(command, scanner, store);
        }
    }
    private static void managerTaskList(){
        for(Map.Entry<String, HashMap<String, String>> entry : commandInterface.entrySet())
            if(entry.getKey().equalsIgnoreCase("M"))
                for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                    System.out.println(command.getValue());
                }
    }
    private static void userTaskList(){
        for(Map.Entry<String, HashMap<String, String>> entry : commandInterface.entrySet())
            if(entry.getKey().equalsIgnoreCase("U"))
                for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                    System.out.println(command.getValue());
                }
    }

    private static void handleUserRequest(String command, Scanner scanner, StoreInterface store) {
        String itemID = null;
        String itemName = null;
        switch(command.toLowerCase()){
            case "purchase":
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                purchaseItem(store, userID, itemID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ"));
                break;
            case "find":
                System.out.println("Enter the required item name : ");
                itemName = scanner.nextLine();
                findItem(store, userID,itemName);
                break;
            case "return" :
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                returnItem(store, userID, itemID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ"));
                break;
        }
    }

    public static boolean purchaseItem(StoreInterface store, String userID, String itemID, SimpleDateFormat dateOfPurchase) {
        boolean isPurchaseSuccessful = false;
        try {
            isPurchaseSuccessful=store.purchaseItem(userID, itemID, dateOfPurchase);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isPurchaseSuccessful;
    }

    public static ArrayList<Item> findItem(StoreInterface store, String userID, String itemID) {
        ArrayList<Item> items = null;
        try {
            items = store.findItem(userID, itemID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static Boolean returnItem(StoreInterface store, String customerID, String itemID, SimpleDateFormat dateOfReturn){
        boolean isReturnSuccessful = false;
        try {
            isReturnSuccessful = store.returnItem(customerID,itemID, dateOfReturn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isReturnSuccessful;
    }

    private static void handleManagerRequest(String command, Scanner scanner, StoreInterface store) {
        String itemID = null;
        String itemName = null;
        int quantity = 0;
        double price = 0.00;
        switch(command.toLowerCase()){
            case "add":
                System.out.println("Enter the required itemID : ");
            itemID = scanner.nextLine();
            System.out.println("Enter the required itemName : ");
            itemName = scanner.nextLine();
            System.out.println("Enter the required quantity : ");
            quantity = scanner.nextInt();
            System.out.println("Enter the required price : ");
            price = scanner.nextDouble();
            addItem(store, userID, itemID, itemName, quantity, price);
            break;
            case "remove":
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                removeItem(store, userID, itemID, quantity);
                break;
            case "list" :
                listItemAvailability(store,userID);
                break;
        }
    }
    public static void addItem(StoreInterface store, String userID, String itemID, String itemName, int quantity, double price) {
        try {
            store.addItem(userID , itemID, itemName, quantity, price);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void removeItem(StoreInterface store, String userID, String itemID, int quantity) {
        try {
            store.removeItem(userID, itemID, quantity);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String listItemAvailability(StoreInterface store, String userID) {
        String listOfItems = "";
        try {
            listOfItems = store.listItemAvailability(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return listOfItems;
    }
}
