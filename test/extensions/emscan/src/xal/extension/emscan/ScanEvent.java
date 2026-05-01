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
public class ScanEvent<V> {

    private final ScanStatus status;
    private final V arg;
    
    public ScanEvent(ScanStatus s){
        this(s,null);
    }
    public ScanEvent(ScanStatus status, V arg){
        this.status=status;
        this.arg = arg;
        
    }
    public V getArg(){
        return arg;
    }
    public ScanStatus getStatus(){
        return status;
    }
    
}
