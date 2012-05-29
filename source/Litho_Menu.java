// introduces the GUI and the thread handler for whole reading/writing/painting

package LithoTransform;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class Litho_Menu extends JFrame implements Runnable {
    
    public static boolean handler_active = false; // if Litho_Run (thread handler) ist active or not

    String name;
    public Toolkit tk = Toolkit.getDefaultToolkit();
    public Dimension dim = tk.getScreenSize();
    public Image dbi;
    public Graphics dbg;
    
    public static boolean change = false;
    
    Color BG_toDo = new Color (255, 66, 66);
    Color working = new Color (66, 66, 255);
    Color finished = new Color (66, 255, 66);
    Color progColor = working;
    int progWidth = 0;
    static double progFactor = 0;
    int progHeight = 0;
    
    Litho_Menu(String iname){
        name = iname;
        this.setSize(dim.width/3, dim.height/10);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);  
        this.setVisible(true);
        progHeight = this.getHeight(); // get height of progression bar after creating JFrame and before loop
        run();
    }
    
    @Override
    public void run() {
        progHeight = this.getHeight();
        while(true){
//           System.out.println("PING");
           if(handler_active == false){
               // starts thread handler
               Runnable r_handler = new Litho_Run("Litho_Run001");
               Thread t_handler = new Thread(r_handler);
               t_handler.start();
           }
           updateIt(); 
           threadDelay(10);
        }
    }
    
    public void updateIt(){
        if(change == false){
        }
        else{
            progWidth = (int) ((int) Math.round(this.getWidth()*(progFactor)));
            if(progWidth >= this.getWidth()){
                progColor = finished;
            }
            repaint();
            change = false;
        }
    }
    
    public void paintComponent(Graphics g){
        g.setColor(BG_toDo);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(progColor);
        g.fillRect(0, 0, progWidth, progHeight);
    }
    
    @Override
    public void paint(Graphics g){
        dbi = createImage(getWidth(), getHeight());
        dbg = dbi.getGraphics();
        paintComponent(dbg);
        g.drawImage(dbi, 0, 0, this);
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
