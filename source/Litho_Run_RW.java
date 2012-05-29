// whole reading and wirting before image creation process
// getting the user input

package LithoTransform;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Litho_Run_RW implements Runnable {

    public static String path_main = "TRANSFORM/";
    public static String path_config = path_main+"config.csv";
    public static String path_litho = path_main+"litho.csv";
    public static String path_data = path_main+"data.csv";
    FileInputStream fStream;
    DataInputStream dStream;
    BufferedReader bReader;
    String temp;    // String storage while reading
    
    // for reading config file
    static int configAmount = 5;
    String[] configOptions = {"MeterPerMapUnit", "width_max", "height_max", "inverse", "QGIS-fix"};
//    static int[] configValues = new int[configAmount];
    static double[] configValues = new double[configAmount];
    String[] tempArr = null;
    
    // for reading data file
    String[] dataOptions = {"IRE", "X", "Y", "Z", "Depth_max", "From", "To", "Thickness", "Lithology", "Nr.", "Quarter"};
    
    static String[][] dataValues; // Array which holds every read data from user
    int lineCount = 0;
    
    static String[] lithoColorArr; // Array for Colors of ...
    static String[] lithoNameArr; // ... these equivalent lithologies by name
       
    Litho_Run_RW(){
        
    }
    
    @Override
    public void run() {
        Litho_Run.Run_RW_active = true;
        System.out.println("Litho_Run_RW: starts");
        
        // loading user configuraitons
        open(path_config);
        read("config");
        close();
        System.out.println("Litho_Run_RW: MeterPerMapUnit = "+configValues[0]);
        System.out.println("Litho_Run_RW: width_max = "+configValues[1]);
        System.out.println("Litho_Run_RW: height_max = "+configValues[2]);
        System.out.println("Litho_Run_RW: inverse = "+configValues[3]);
        System.out.println("Litho_Run_RW: QGIS-fix = "+configValues[4]);

        // loading lithological information (colors of lithos)
        open(path_litho);
        read("count");
        close();
        System.out.println("Litho_Run_RW: counted "+(lineCount-1)+" rows in litho file");
        lithoColorArr = new String[lineCount];
        lithoNameArr = new String[lineCount];
        open(path_litho);
        read("litho");
        close();
        
        
        // loading data-csv
        open(path_data);
        read("count");
        close();
        System.out.println("Litho_Run_RW: counted "+(lineCount-1)+" rows in data file");
        dataValues = new String[lineCount-1][dataOptions.length];
        open(path_data);
        read("data");
        close();
        
//        show2DArray_String(dataValues);
        
        System.out.println("Litho_Run_RW: finished");
        Litho_Run.Run_RW_finished = true;
        Litho_Run.Run_RW_active = false;
    }
    
    // for opening files
    public void open(String filepath){
        try {
            bReader = new BufferedReader(
                     new InputStreamReader(
                        new FileInputStream(filepath)));
//            System.out.println("Litho_Run_RW: FileInputStream and BufferedReader active");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // contains all functions concerning the reading and saving into arrays
    public void read(String operation){
        if(operation.equals("count")){
            try {
                if(bReader.ready() == true){
                    lineCount = 0;
                    while((temp = bReader.readLine()) != null){
                        lineCount++;
                    }   
                }
                else{
                    System.out.println("Litho_Run_RW: cannot count lines because BufferedReader is not ready");
                }
            } catch (IOException ex) {
                Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(operation.equals("config")){
            try {
                if(bReader.ready() == true){
                    while((temp = bReader.readLine()) != null){
                        tempArr = temp.split("\\t");
                        int k = searchArray(configOptions, tempArr[0]);
                        configValues[k] = new Double(tempArr[1]);
                    }   
                }
                else{
                    System.out.println("Litho_Run_RW: cannot read config-file because BufferedReader is not ready");
                }
            } catch (IOException ex) {
                Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(operation.equals("data")){
            int col = 0; // col, row for actual reference in dataValues
            int row = 0;
            int i = 0; //i, j for navigating tempArr
            int j = 0;
            int k = 0; // stores searches indices of dataOptions
            ArrayList<Integer> orderAL = new ArrayList<Integer>(); // stores indices of tempArr (where to find values for given table headers)
            try {
                if(bReader.ready() == true){
                    while((temp = bReader.readLine()) != null){
                        tempArr = temp.split("\\t");
                        if(j == 0){
                            i = 0;
                            while(i < tempArr.length){
                                k = searchArray(dataOptions, tempArr[i]);
                                System.out.println("Litho_Run_RW: header: ["+i+"-->"+k+"] "+tempArr[i]);
                                orderAL.add(i, k);
                                i++;
                            }
                        }
                        else{
                            i = 0;
                            while(i < tempArr.length){
                                if((orderAL.get(i)) != 999){
//                                    System.out.println("Litho_Run_RW: ["+(j-1)+"] "+"["+i+"] "+tempArr[i]);
                                    dataValues[j-1][orderAL.get(i)] = tempArr[i];
//                                    System.out.println("Litho_Run_RW: saving: "+dataValues[j-1][orderAL.get(i)]);
                                }
                                i++;
                            }
                        }
                        j++;
                    }   
                }
                else{
                    System.out.println("Litho_Run_RW: cannot read data-file because BufferedReader is not ready");
                }
            } catch (IOException ex) {
                Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(operation.equals("litho")){
            int row = 0;
            try {
                if(bReader.ready() == true){
                    while((temp = bReader.readLine()) != null){
                        tempArr = temp.split("\\t");
                        lithoColorArr[row] = tempArr[0];
                        lithoNameArr[row] = tempArr[1];
                        row++;
                    }   
                }
                else{
                    System.out.println("Litho_Run_RW: cannot read ,litho-file because BufferedReader is not ready");
                }
            } catch (IOException ex) {
                Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // closing reader
    public void close(){
        try {
            bReader.close();
        } catch (IOException ex) {
            Logger.getLogger(Litho_Run_RW.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("Litho_Run_RW: FileInputStream and BufferedReader closed");
    }
    
    // pauses threads for a while
    public static void threadDelay(int d){
        int delay = d;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Logger.getLogger(LithoTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // search 1D-Array for String
    public int searchArray(String[] inputArray, String searchValue){
        int iterate = 0;
        int notfound = 0;
        
        for(iterate=0; iterate<inputArray.length; iterate++){
            if(inputArray[iterate].equals(searchValue)){
                break;
            }
            else{
                notfound++;
            }
        }
        
        if(notfound == inputArray.length){
            return 999;
        }
        else{
            return iterate;
        }
    }
    
    // search 2D-Array for String
    public void show2DArray_String(String[][] inputArr){
        int iterator1 = 0;
        int iterator2 = 0;
        while(iterator1 < inputArr.length){
            System.out.print(iterator1+"{");
            while(iterator2 < inputArr[iterator1].length){
                System.out.print(" ["+iterator1+"]"+"["+iterator2+"]-> "+inputArr[iterator1][iterator2]+" ");
                iterator2++;
            }
            System.out.print("}\n");
            iterator2 = 0;
            iterator1++;
        }
    }
    
    public static String getPathMain(){
        return path_main;
    }
    
    public static String[][] getdataValues(){
        return dataValues;
    }
}