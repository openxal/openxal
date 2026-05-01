//Example channel access
package xal.app.rocs;
import xal.ca.*;

public class TuneChannelAccess{


    public static void setValue(Channel ch, double val){
	try {
	    ch.putVal(val);
	}
	catch (ConnectionException e){}
	catch (PutException e){}
	return;
    }

    public static double getValue(Channel ch){
	double val = 0.0;
	try {
	    val  = ch.getValDbl();
	}
	catch (ConnectionException e){}
	catch (GetException e){}

	return val;
    }
    
    public void channelAccess() {

	Channel ch_1 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_1_mag:Field");
	Channel ch_2 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_2_mag:Field");
	Channel ch_3 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_3_mag:Field");
	Channel ch_4 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_4_mag:Field");
	Channel ch_5 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_5_mag:Field");
	Channel ch_6 = ChannelFactory.defaultFactory()
	    .getChannel("QUAD_6_mag:Field");
   
       double q1 = 1.0, q2 = 2.0, q3 = 3.0, q4 = 4.0, q5 = 5.0, q6 = 6.0;
       
       setValue(ch_1, q1);
       setValue(ch_2, q2);
       setValue(ch_3, q3);
       setValue(ch_4, q4);
       setValue(ch_5, q5);
       setValue(ch_6, q6);

       q1 = getValue(ch_1);
       q2 = getValue(ch_2);
       q3 = getValue(ch_3);
       q4 = getValue(ch_4);
       q5 = getValue(ch_5);
       q6 = getValue(ch_6);

       System.out.println("q1 = " + q1 + "; q2 = " + q2 + "; q3 = " + q3
			  + "; q4 = " + q4 + "; q5 = " + q5 + "; q6 = " + 
			  q6);
	
    }
    
}






