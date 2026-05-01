/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;


import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.PutException;

/**
 *
 * @author az9
 */
public class Corrector {

    private Channel channel = null;
    private double xCoeff = 0;
    private double xPCoeff = 0;
    private double minV = 0;
    private double maxV = 0;
    private double initialV =0;
    
    private boolean DEBUG=false;
    private double xC;
    private double xPC;
    private double prevX;
    private double prevXp;

    Corrector(String id, ChannelFactory cf, boolean debug) {
        boolean valid = false;
        DEBUG=debug;
        try {
            String[] tokens = id.split(",");
            channel = cf.getChannel(tokens[0].trim());
            xCoeff = Double.parseDouble(tokens[1]);
            xPCoeff = Double.parseDouble(tokens[2]);
            minV = Double.parseDouble(tokens[3]);
            maxV = Double.parseDouble(tokens[4]);
            xC = Double.parseDouble(tokens[5]);
            xPC = Double.parseDouble(tokens[6]);
            
            initialV = channel.getValDbl();
            valid = true;
            System.out.println("Created corrector "+channel.channelName()+", saved setpoint "+initialV);
        } catch (Exception e) {
            System.out.println("Failed to create corrector");
            e.printStackTrace();

        }

    }
    private double forwardFactor=0.5;
    
    public boolean adjust(double x, double xp){
        return adjust(x,xp,true);
    }
    public boolean adjust(double x, double xp, boolean adjust){
        double newSetting=0.0;
        if(adjust){
            newSetting=(x-xC+forwardFactor*(x-prevX))*xCoeff+(xp-xPC+forwardFactor*(xp-prevXp))*xPCoeff;
        }
        else {
            newSetting=(x-xC)*xCoeff+(xp-xPC)*xPCoeff;
        }
            
        prevX=x;
        prevXp=xp;    
            
        newSetting = newSetting<minV?minV:newSetting;
        newSetting = newSetting>maxV?maxV:newSetting;

        
        System.out.println("Will set corrector "+channel.channelName()+" to "+newSetting+ " for ("+x+","+xp+")");
        if(!DEBUG){
            try {
                channel.putVal(newSetting);
                return true;
            } catch (ConnectionException ex) {
                Logger.getLogger(Corrector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PutException ex) {
                Logger.getLogger(Corrector.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        return true;
        
        
    }
    
    public boolean set(double x, double xp){
        return adjust(x, xp,false);
    }

    void restoreInitialValue() {
        try {   
                System.out.println("Will restore "+channel.channelName()+" to "+initialV);
                if(!DEBUG) {
                    channel.putVal(initialV);  
                }
            } catch (ConnectionException ex) {
                Logger.getLogger(Corrector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PutException ex) {
                Logger.getLogger(Corrector.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    

}


