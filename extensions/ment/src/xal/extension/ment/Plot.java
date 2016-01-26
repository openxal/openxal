/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.extension.ment;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JFrame;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;


/**
 *
 * @author tg4
 */
public class Plot {

    private Ment m;
    private FunctionGraphsJPanel grpan;
    private FunctionGraphsJPanel prof;
    public JFrame jFrame1;
    private JFrame jFrame2;

    public Plot(Ment _m){
        
        m = _m;

            grpan = new FunctionGraphsJPanel();
            grpan.setSmartGL(false);
            grpan.setGraphBackGroundColor(Color.white);


            prof = new FunctionGraphsJPanel();
            prof.setSmartGL(false);
            prof.setGraphBackGroundColor(Color.white);

        jFrame1 = new JFrame("h-functions");
        jFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame1.setSize(1000, 500);
        jFrame1.add(grpan);

        jFrame2 = new JFrame("Profiles");
        jFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame2.setSize(1000, 500);
        jFrame2.add(prof);


    }






    public void plotHfunction(){

        if(!jFrame1.isVisible())
        jFrame1.setVisible(true);

        grpan.removeAllGraphData();

           BasicGraphData [] hdata = new BasicGraphData[m.pr_size];


            for(int i = 0; i < m.pr_size; i++)
              hdata[i] = new BasicGraphData();


            for(int i = 0; i < m.pr_size; i++)
               hdata[i].setGraphColor(IncrementalColors.getColor(i));



        Vector<double []> x = new Vector<double []>();
        Vector<double []> h = new Vector<double []>();
        

        for(int i = 0; i < m.pr_size; i++){
            x.add(new double[m.nx + 1]);
            h.add(new double[m.nx + 1]);
        }

        for(int i = 0; i < m.pr_size; i++)    {
            
            for (int j = 0; j < m.nx + 1; j++) {
                x.get(i)[j] = m.xh[i*(m.nx + 1) + j];
                h.get(i)[j] = m.hx[i*(m.nx + 1) + j];
            }

            hdata[i].addPoint(x.get(i), h.get(i));

            }


            for(int i = 0; i < m.pr_size; i++)
            grpan.addGraphData(hdata[i]);

    }


        public void plotProfiles(){
            

        if(!jFrame2.isVisible())
        jFrame2.setVisible(true);

        prof.removeAllGraphData();

       BasicGraphData [] exp_data = new BasicGraphData[m.pr_size];
       BasicGraphData [] th_data = new BasicGraphData[m.pr_size];       

       
       for(int i = 0; i < m.pr_size; i++)   {
              exp_data[i] = new BasicGraphData();
              th_data[i] = new BasicGraphData();
              exp_data[i].setDrawLinesOn(false);
              th_data[i].setDrawPointsOn(false);
            }

       for(int i = 0; i < m.pr_size; i++)   {  
               exp_data[i].setGraphColor(IncrementalColors.getColor(i));
               th_data[i].setGraphColor(IncrementalColors.getColor(i));
            }

        
        for(int i = 0; i < m.pr_size; i++)  {
            th_data[i].addPoint(m.profiles.get(i).x, m.profiles.get(i).y);  
            
            for(int j = 0; j < m.nx; j++)
            exp_data[i].addPoint(m.xh[i*(m.nx + 1) + j], m.check_h(i, m.xh[i*(m.nx + 1) + j]));
            
            }

   //    int i = 0;
        //    for(int j = 0; j < m.nx; j++)   {
      //     System.out.print(m.profiles.get(i).x[j]);System.out.print("\t");System.out.println(m.profiles.get(i).y[j]);
      //      }

            for(int i = 0; i < m.pr_size; i++)  {
            prof.addGraphData(th_data[i]);
            prof.addGraphData(exp_data[i]);
            }


    }

        public void plot(){


   try {

       FileWriter fstream = new FileWriter("out.txt");
       BufferedWriter out = new BufferedWriter(fstream);

     for (int i = 0; i < 501; i++)
     for (int j = 0; j < 501; j++)   {
         double x = m.xmax0*(2.0*i/500 - 1.0);
         double _xp =  m.xpmax0*(2.0*j/500 - 1.0);

     out.write(Double.toString(x)+"\t");
     out.write(Double.toString(_xp)+"\t");
     out.write(Double.toString(m.f(x, _xp))+"\n");

     }


      out.close();
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }


}



    public void delay(){

            System.out.println("Press Any Key to display next..!");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            try{@SuppressWarnings("unused")
            int ch = stdin.read();} catch( Exception exception ) {}
    }

      
}



class IncrementalColors{

    static private int number = 10;
    static private Color [] colors = null;

    static {
	colors = new Color[number];
	colors[0]  = Color.RED;
	colors[1]  = Color.BLUE;
	colors[2]  = Color.GREEN;
	colors[3]  = Color.MAGENTA;
	colors[4]  = Color.ORANGE;
	colors[5]  = Color.CYAN;
	colors[6]  = Color.PINK;
	colors[7]  = Color.BLACK;
	colors[8]  = Color.YELLOW;
	colors[9]  = Color.DARK_GRAY;
    }

    /* Constructor. */
    private IncrementalColors(){
    }

    /* Returns color according to an integer index. */
    static public Color getColor(int indIn){
	int ind = indIn % number;
	return colors[ind];
    }

    /** set the color of index i to your favorite color */

    static public void setColor(int index, Color color) {
	    colors[index] = color;
    }
}

