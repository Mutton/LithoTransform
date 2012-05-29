// organizes important threads

package LithoTransform;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Litho_Run implements Runnable {
    
    public static boolean Run_RW_active = false;
    public static boolean Run_RW_finished = false;
    
    public static boolean Run_Images_active = false;
    public static boolean Run_Images_finished = false;
    
    Litho_Run(String inputname){
        Litho_Menu.handler_active = true;
        String name = inputname;
    }
 
    @Override
    public void run(){
        while(true){
            if(Run_RW_active == false && Run_RW_finished == false){
                Runnable r_RW = new Litho_Run_RW();
                Thread t_RW = new Thread(r_RW);
                t_RW.start();
            }
            if(Run_Images_active == false && Run_Images_finished == false && Run_RW_finished == true){
                Runnable r_Images = new Litho_Run_Images();
                Thread t_Images = new Thread(r_Images);
                t_Images.start();
            }
            threadDelay(100);
        }
    }
    
    public static void threadDelay(int d){
        int delay = d;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Logger.getLogger(LithoTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
