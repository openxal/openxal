/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.extension.ment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import xal.extension.widgets.plot.BasicGraphData;



/**
 *
 * @author T. Gorlov
 * 
 * @version Jan 26, 2016 - ported to Open XAL by Christopher K. Allen
 */
public class ScanData {

    private Vector<singleData> datav;
    private Boolean raw;
    private int count_data;



    public ScanData() {

        datav = new Vector<singleData>();
        raw = false;

    }



    public BasicGraphData getScan_x(int i) {return datav.get(i).zx;}

    public BasicGraphData getScan_y(int i) {return datav.get(i).zy;}

    public String getWScanId(int i) {return datav.get(i).wsId;}

    public int getPvlogId(int i) {return datav.get(i).pvlogId;}

    public void clean() {datav.removeAllElements();}

    public int getSize() {return datav.size();}




    public void addDataFromFile(File file) {

        count_data = 0;

           try {
               BufferedReader in = new BufferedReader(new FileReader(file));
                String str;


                while ((str = in.readLine()) != null) {process_string(str);}

           in.close();

                } catch (IOException e) {}

    }




    private void process_string(String str) {



             if (str.contains("WS")) {
                 datav.add(new singleData());
                 datav.get(datav.size() - 1).wsId = str.trim();
                 count_data ++;
             }

             
             if(str.contains("Raw")) raw = true;
             if(str.contains("Fit")) raw = false;


             if (str.contains("PVLoggerID")) {
                 for (int i = 0; i < count_data; i++)
                 datav.get(datav.size() - 1 - i).pvlogId = getNo(str);
             }


             if (raw && str.split("\\s+").length == 7 && !str.contains("Position")) {

                 datav.get(datav.size() - 1).zx.addPoint(Double.parseDouble(str.trim().split("\\s+")[4]), Double.parseDouble(str.trim().split("\\s+")[3]));
                 datav.get(datav.size() - 1).zy.addPoint(Double.parseDouble(str.trim().split("\\s+")[4]), Double.parseDouble(str.trim().split("\\s+")[1]));

    }




}


    private int getNo(String stringNo) {

    String  parsedNo = "";
    for(int n = 0; n < stringNo.length(); n++)
    {String i = stringNo.substring(n,n+1);

    if(      i.equals("0") ||
             i.equals("1") ||
             i.equals("2") ||
             i.equals("3") ||
             i.equals("4") ||
             i.equals("5") ||
             i.equals("6") ||
             i.equals("7") ||
             i.equals("8") ||
             i.equals("9")) {
    parsedNo += i;
        }
    }
    return Integer.parseInt(parsedNo);

    }
    }






class singleData {

    public BasicGraphData zx;
    public BasicGraphData zy;
    public String wsId;
    public int pvlogId;


   public singleData()  {

       zx = new BasicGraphData();
       zy = new BasicGraphData();

    }





}

