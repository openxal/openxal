/*
 * Fft.java
 *
 * Created on May 17, 2006, 12:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class Fft {   
    
    public static void transfm(int s, int len, Signal[] sgl) { 
        
        double rl;
        double ig;        
        double d, q, r, g;        
        
        double fq = Math.sqrt(1.0/(float)len);        
        int i, j, m, l;        
        for (i=j=0; i<len; ++i) {            
            if (j>=i) {
	         rl = sgl[j].getreal()*fq;
	         ig = sgl[j].getimage()*fq;
	         sgl[j].reconstruct(sgl[i].getreal()*fq, sgl[i].getimage()*fq);
	         sgl[i].reconstruct(rl, ig);
            }
            int k = len/2;
            while (k>=1 && j>=k) {
	            j -= k;
	            k /= 2;
            }
            j += k;
        }
    
        for (m=1,l=2*m; m<len; m=l,l=2*m) {
            d = (float) s * Math.PI/(float)m;
            for (int k=0; k<m; ++k) {
	        q = (float) k*d;
	        rl = Math.cos(q);
	        ig = Math.sin(q);
	        for (i=k; i<len; i+=l) {
	            j = i+m;
	            r = rl*sgl[j].getreal()  - ig*sgl[j].getimage();
	            g = rl*sgl[j].getimage() + ig*sgl[j].getreal();
	            sgl[j].reconstruct(sgl[i].getreal()-r, sgl[i].getimage()-g);
	            sgl[i].reconstruct(sgl[i].getreal()+r, sgl[i].getimage()+g);
	        }
            }
            m = l;
        }
    }    
}
