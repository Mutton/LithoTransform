// to switch between normal script and script for further use with QGIS 
// make changes in createImages()- and saveWorld()-methods

package LithoTransform;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Litho_Run_Images implements Runnable {

    RenderedImage rendImage;
    
    // variables to get the right color for lithologies at the given position
    int lithoSearch;
    String lithoHex;
    Color lithoColor;
    
    int width_max = 0; // user information about px-width of all created pictures
    int height_max = 0; // user information about px-height of biggest lithostratigraphy
    double ratio_PixelMeter = 0.0; // how much pixel are 1 meter?
    double depth_MaxMeter = 0.0; // maximum depth of lithostratigraphy in whole dataset dataValues
    
    // important variables from dataValues-2D-Array for creating pictures and world files
    String IRE = null;
    String X = null;
    String Y = null;
    String Z = null;
    double Depth_max = 0;
    double From = 0;
    double To = 0;
    String Lithology = null;
    String Nr = null;
    String Quarter = null;
    
    BufferedImage buffImage;
       
    Litho_Run_Images(){
        
    }
    
    @Override
    public void run() {
        Litho_Run.Run_Images_active = true;
        System.out.println("Litho_Run_Images: starts");
        
        // delete all former pictures before creating new ones (prevents graphical errors)
        loopDelete();
        
        // create new pictures and (over-)write world files
        loopCreate();
        
        System.out.println("Litho_Run_Images: finished");
        Litho_Run.Run_Images_finished = true;
        Litho_Run.Run_Images_active = false;
    }
    
    public void loopDelete(){
        System.out.println("Litho_Run_Images: start deleting preexisting pictures and world files");
        int j = 0;
        
        while(j < Litho_Run_RW.dataValues.length){
            IRE = Litho_Run_RW.dataValues[j][0];
            Nr = Litho_Run_RW.dataValues[j][9];
            deleteFile(new File(Litho_Run_RW.path_main+Nr+".png"));
            deleteFile(new File(Litho_Run_RW.path_main+Nr+".pgw"));
            j++;
        }
        System.out.println("Litho_Run_Images: finished deleting preexisting pictures and world files");
    }
    
    public void loopCreate(){
        System.out.println("Litho_Run_Images: start creating new pictures and world files");
        int j = 0;
        width_max = (int) Math.round(Litho_Run_RW.configValues[1]);
        height_max = (int) Math.round(Litho_Run_RW.configValues[2]);
        depth_MaxMeter = findMax_2DArr(Litho_Run_RW.dataValues, 4); // get deepest lithostratigraphy
        ratio_PixelMeter = (double) height_max/depth_MaxMeter; // [px/m]
        System.out.println("Litho_Run_Images: setting "+ratio_PixelMeter+" [px/m]");

        while(j < Litho_Run_RW.dataValues.length){
            
            IRE = Litho_Run_RW.dataValues[j][0];
            X = Litho_Run_RW.dataValues[j][1];
            Y = Litho_Run_RW.dataValues[j][2];
            Z = Litho_Run_RW.dataValues[j][3];
            Depth_max = Double.parseDouble(Litho_Run_RW.dataValues[j][4]);
            From = Double.parseDouble(Litho_Run_RW.dataValues[j][5]);
            To = Double.parseDouble(Litho_Run_RW.dataValues[j][6]);
            Lithology = Litho_Run_RW.dataValues[j][8];
            Nr = Litho_Run_RW.dataValues[j][9];
            Quarter = Litho_Run_RW.dataValues[j][10];
            lithoSearch = searchArray(Litho_Run_RW.lithoNameArr, Lithology); // search Lithology in possible ones
            lithoHex = Litho_Run_RW.lithoColorArr[lithoSearch]; // find equivalent colorcode (Hex) for Lithology
            lithoColor = Color.decode("#"+lithoHex); // finally set color of Lithology
//            ratio_PixelMeter = (double) height_max/Depth_max;
            
            rendImage = createImage();
            saveImage();
            
            saveWorld(Nr);
            
            Litho_Menu.progFactor = (double) ((double)j/(((double)Litho_Run_RW.dataValues.length)-1));
            Litho_Menu.change = true;
            
            j++;
        }
        System.out.println("Litho_Run_Images: finished creating new pictures and world files");
    }
    
    public void deleteFile(File inputFile){
        File searchFile = inputFile;
        if(searchFile.exists()){
            boolean deletionSuccess = searchFile.delete();
            if(deletionSuccess == false){
                System.out.println("Litho_Run_Images: wasn't able to delete existing file "+Litho_Run_RW.path_main+Nr+".png");
            }
            else{
                System.out.println("Litho_Run_Images: deleted existing file "+Litho_Run_RW.path_main+Nr+".png");
            }
        }
    }
    
    public RenderedImage createImage(){
        System.out.println("Litho_Run_Images: adding lithological strata to Nr."+Nr+" from "+From+" to "+To+" m");
        File searchFile = new File(Litho_Run_RW.path_main+Nr+".png");
        if(searchFile.exists()){
            try {
                buffImage = ImageIO.read(searchFile);
            } catch (IOException ex) {
                Logger.getLogger(Litho_Run_Images.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            buffImage = new BufferedImage(width_max, (int) Math.round(Depth_max*ratio_PixelMeter), BufferedImage.TYPE_INT_RGB);
        }
         
        Graphics2D g2D = buffImage.createGraphics();
        // draw everything hereafter
        g2D.setColor(lithoColor);
        // draw litho-rectangles in normal order
        if(Litho_Run_RW.configValues[3] == 0){
            g2D.fillRect(0, (int)Math.round(From*ratio_PixelMeter), width_max, (int)Math.round((To-From)*ratio_PixelMeter));
        }
        // draw litho rectangles in inverse order for QGIS
        if(Litho_Run_RW.configValues[3] == 1){
            g2D.fillRect(0, (int)Math.round((Depth_max-To)*ratio_PixelMeter), width_max, (int)Math.round((To-From)*ratio_PixelMeter));
        }
        // graphics2D no longer needed and return the Image
        g2D.dispose();
        
        return buffImage;
    }
    
    public void saveImage(){
        try {
            File file = new File(Litho_Run_RW.getPathMain()+Nr+".png");
            ImageIO.write(rendImage, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(Litho_Run_Images.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveWorld(String Nr){
        if(new File(Litho_Run_RW.getPathMain()+Nr+".pgw").exists()){
            
        }
        else{
            System.out.println("Litho_Run_Images: creating and saving world file for Nr. "+Nr);
            // for QGIS only: set Y-coordinate to new position for the column to appear under it's location on map
            double Y_QGIS = Double.parseDouble(Y);
            // correction for QGIS if user gives "QGIS-fix   1" in config file
            if(Litho_Run_RW.configValues[4] == 1){
                Y_QGIS = Double.parseDouble(Y)-((Depth_max)*(Litho_Run_RW.configValues[0])*ratio_PixelMeter);
            }
            Y = String.valueOf(Y_QGIS);
            // filling content
            String content = "";
            content += Double.toString(Litho_Run_RW.configValues[0])+"\n";
            content += "0"+"\n";
            content += "0"+"\n";
            content += Double.toString(Litho_Run_RW.configValues[0])+"\n";
            content += X+"\n";
            content += Y;
            // save content to world file
            saveContent(content, new File(Litho_Run_RW.path_main+Nr+".pgw"));
        }
    }
    
    public void saveContent(String contents, File file){
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file));
            out.print(contents);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Litho_Run_Images.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }
    
    public double findMax_2DArr(String[][] array, int where){
        double max = 0;
        int it1 = 0;
        while(it1 < array.length){
            if(Double.parseDouble(array[it1][where]) > max){
                max = Double.parseDouble(array[it1][where]);
            }
            else{}
            it1++;
        }
        return max;
    }
    
    public int searchArray(String[] inputArray, String searchValue){
//        System.out.println("Litho_Run_Images: search color for lithology "+searchValue);
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
}