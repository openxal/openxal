/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author az9
 */
public enum ScanStatus {
    IDLE("Idle"),
    PAUSED("Scan paused"),
    ABORT("Waiting to abort"),
    PROGRESS("Scan in Progress"),
    START("Starting scan");
        
    private final String msg;

    ScanStatus(String msg) {
        this.msg = msg;
    }
    public String toString(){
        return msg;
    }
}
