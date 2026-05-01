/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author sashazhukov
 */
public class EmittanceGenerator {

    public static void main(String[] args) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("test.csv"));


        int slitSteps = 20;
        int harpSteps = 20;
        int wfLength = 100;
        
        double slitStep=0.1;
        double harpStep = 0.1;
        double initialSlit =19.0;
        double initialHarp=12;
                

        for (int i = 0; i < slitSteps; i++) {
            for (int j = 0; j < harpSteps; j++) {
                double[] wf = new double[wfLength];
                for (int k = 0; k < wfLength; k++) {
                    if (k < wfLength / 3 || k > wfLength / 3 * 2) {
                        wf[k] = 0;
                    }
                    else {
                        wf[k] = (i>slitSteps/3&&i<slitSteps/3*2)&&(j>harpSteps/3&&j<harpSteps/3*2)?1.0:0.1;
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append(i*harpSteps+j+",").append(initialSlit+i*slitStep+",").append(initialHarp+j*harpStep+",");
                for(double d : wf){
                    sb.append(d+",");
                }
                sb.append("\n");
                bw.write(sb.toString());
                
            }

        }
        bw.flush();
        bw.close();

    }
}
