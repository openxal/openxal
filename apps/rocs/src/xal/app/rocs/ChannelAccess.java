//Example channel access
package xal.app.rocs;
import xal.ca.*;

public class ChannelAccess{

    public double[] q_get = new double[6];

    //Routines used internally
    
    private static void setValue(Channel ch, double val){
	try {
	    ch.putVal(val);
	}
	catch (ConnectionException e){
	    System.err.println("Unable to connect to channel access.");
	}
	catch (PutException e){
	    System.err.println("Unable to set process variables.");
	}
	return;
    }

    private static double getValue(Channel ch){
	double val = 0.0;
	try {
	    val  = ch.getValDbl();
	}
	catch (ConnectionException e){
	    System.err.println("Unable to connect to channel access.");
	}
	catch (GetException e){
	    System.err.println("Unable to get process variables.");
	}

	return val;
    }

    private double getMagLowLimits(Channel ch){
        double limit = 0.0;
        try{
            limit = ch.lowerControlLimit().doubleValue();
        }
        catch (ConnectionException e){
            System.err.println("Unable to connect to channel access.");
        }   
        catch (GetException e){
            System.err.println("Unable to get process variables.");
        }
        return limit;
    }

    private double getMagUpLimits(Channel ch){
        double limit = 0.0;
        try{
            limit = ch.upperControlLimit().doubleValue();
        }
        catch (ConnectionException e){
            System.err.println("Unable to connect to channel access.");
        }   
        catch (GetException e){
            System.err.println("Unable to get process variables.");
        }
        return limit;
    }


    //Routines accessed from external classes

    public void setQuadPVs(double q_set[]){
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

	setValue(ch_1, q_set[0]); 
	setValue(ch_2, q_set[1]); 
	setValue(ch_3, q_set[2]);
	setValue(ch_4, q_set[3]); 
	setValue(ch_5, q_set[4]); 
	setValue(ch_6, q_set[5]);
    }

    public double[] getQuadPVs(){
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

	q_get[0] = getValue(ch_1); 
	q_get[1] = getValue(ch_2);
	q_get[2] = getValue(ch_3); 
	q_get[3] = getValue(ch_4);
	q_get[4] = getValue(ch_5); 
	q_get[5] = getValue(ch_6);

	return q_get;
    }

    public double[] getQuadLLimitPVs(){
	
	double limit[] = new double[6];

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

	limit[0] = getMagLowLimits(ch_1); 
	limit[1] = getMagLowLimits(ch_2);
	limit[2] = getMagLowLimits(ch_3); 
	limit[3] = getMagLowLimits(ch_4);
	limit[4] = getMagLowLimits(ch_5); 
	limit[5] = getMagLowLimits(ch_6);

	return limit;
    }
    
    public double[] getQuadULimitPVs(){
	
	double limit[] = new double[6];
	
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
	
	limit[0] = getMagUpLimits(ch_1); 
	limit[1] = getMagUpLimits(ch_2);
	limit[2] = getMagUpLimits(ch_3); 
	limit[3] = getMagUpLimits(ch_4);
	limit[4] = getMagUpLimits(ch_5); 
	limit[5] = getMagUpLimits(ch_6);

	return limit;
    }

    
    public void setSextPVs(double q_set[]){
	Channel ch_1 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_1_mag:Field");
	Channel ch_2 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_2_mag:Field");
	Channel ch_3 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_3_mag:Field");
	Channel ch_4 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_4_mag:Field");

	setValue(ch_1, q_set[0]); 
	setValue(ch_2, q_set[1]); 
	setValue(ch_3, q_set[2]);
	setValue(ch_4, q_set[3]); 
    }

    public double[] getSextPVs(){
	Channel ch_1 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_1_mag:Field");
	Channel ch_2 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_2_mag:Field");
	Channel ch_3 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_3_mag:Field");
	Channel ch_4 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_4_mag:Field");
	
	q_get[0] = getValue(ch_1); 
	q_get[1] = getValue(ch_2);
	q_get[2] = getValue(ch_3); 
	q_get[3] = getValue(ch_4);
	return q_get;
    }

    public double[] getSextLLimitPVs(){

	double limit[] = new double[4];

	Channel ch_1 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_1_mag:Field");
	Channel ch_2 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_2_mag:Field");
	Channel ch_3 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_3_mag:Field");
	Channel ch_4 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_4_mag:Field");
	
	limit[0] = getMagLowLimits(ch_1); 
	limit[1] = getMagLowLimits(ch_2);
	limit[2] = getMagLowLimits(ch_3); 
	limit[3] = getMagLowLimits(ch_4);

	return limit;
    }

        public double[] getSextULimitPVs(){

	    double limit[] = new double[4];
	Channel ch_1 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_1_mag:Field");
	Channel ch_2 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_2_mag:Field");
	Channel ch_3 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_3_mag:Field");
	Channel ch_4 = ChannelFactory.defaultFactory()
	    .getChannel("SEXT_4_mag:Field");
		
	limit[0] = getMagUpLimits(ch_1); 
	limit[1] = getMagUpLimits(ch_2);
	limit[2] = getMagUpLimits(ch_3); 
	limit[3] = getMagUpLimits(ch_4);

	return limit;
    }
    
    
}









