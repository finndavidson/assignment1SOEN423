package Components;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger{
    private static File file;
    private static FileWriter fileWriter;

    private static String currentBranch = "";
    private static String userBaseRelativeFilePath = "./src/logs/users";
    private static String storeRelativeFilePath ;

    private static final String QC = "QC";
    private static final String BC = "BC";
    private static final String ON = "ON";


    public static void writeStoreLog(String storeID, String output) {
        if(storeID.toLowerCase().contains("on"))
            currentBranch = "on";
        else if(storeID.toLowerCase().contains("qc"))
            currentBranch = "qc";
        else if(storeID.toLowerCase().contains("bc"))
            currentBranch = "bc";

        storeRelativeFilePath = "./src/logs/server";
        System.out.println("\nLog stored in" + storeRelativeFilePath);
        createFile(storeRelativeFilePath,storeID);
        createFileWriter(storeRelativeFilePath,storeID);
        if(file != null && fileWriter != null) {
            try {
                fileWriter.write(output+"\n\n\n");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void writeUserLog(String userID, String output) {
        if(userID == null || output == null)
            return;

        currentBranch = userID.substring(0, 2).toLowerCase();
        String userType = userID.substring(2, 3);
        String numberSuffix = userID.substring(3, userID.length() - 1);

        if(userType.equalsIgnoreCase("M")) {
            createFile(userBaseRelativeFilePath, userID);
            createFileWriter(userBaseRelativeFilePath,userID);

            if(file != null && fileWriter != null) {
                try {
                    fileWriter.write(output+"\n\n\n");
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        else if(userType.equalsIgnoreCase("U")) {
            createFile(userBaseRelativeFilePath,userID);
            createFileWriter(userBaseRelativeFilePath,userID);

            if(file != null && fileWriter != null) {
                try {
                    fileWriter.write(output+"\n\n\n");
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void createFileWriter(String relativeFilePath, String userID) {
        try {
            fileWriter = new FileWriter(relativeFilePath + userID + ".txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFile(String relativeFilePath, String userID) {
        file = new File(relativeFilePath + userID + ".txt");
        try {
            if (file.createNewFile()) {
             } else {
                 System.out.println("/n");
             }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
