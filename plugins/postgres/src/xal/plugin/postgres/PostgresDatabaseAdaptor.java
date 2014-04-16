/*
 * PostgresDatabaseAdaptor.java
 *
 * Created on Sat Feb 09
 *
 */

package xal.plugin.postgres;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DatabaseAdaptor;
import xal.tools.database.DatabaseException;

public class PostgresDatabaseAdaptor extends DatabaseAdaptor {	
	/**
	 * Public Constructor
	 */
	public PostgresDatabaseAdaptor() {	
	}	

	/**
	 * Get an SQL Array given an SQL array type, connection and a primitive array
	 * @param type An SQL array type identifying the type of array
	 * @param connection An SQL connection
	 * @param array The primitive Java array
	 * @return the SQL array which wraps the primitive array
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */	
	public Array getArray( final String type, final Connection connection, final Object array ) throws DatabaseException {
        Object[] newArray=null;
        
        if (array instanceof byte[]){            
            byte[] a=(byte[])array;
            newArray=new Byte[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof short[]){            
            short[] a=(short[])array;
            newArray=new Short[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof int[]){            
            int[] a=(int[])array;
            newArray=new Integer[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof long[]){            
            long[] a=(long[])array;
            newArray=new Long[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof float[]){            
            float[] a=(float[])array;
            newArray=new Float[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof double[]){            
            double[] a=(double[])array;
            newArray=new Double[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof boolean[]){           
            boolean[] a=(boolean[])array;
            newArray=new Boolean[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else if (array instanceof char[]){            
            char[] a=(char[])array;
            newArray=new Character[a.length];
            for (int j=0;j<a.length;j++)
                newArray[j]=a[j];                
        } else 
        	newArray = (Object[])array;

		try {
			return connection.createArrayOf(type, newArray);
		} catch (SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception );			
			throw new DatabaseException( "Exception generating an SQL array.", this, exception );			
		}		
	}

	@Override
	public Connection getConnection(String urlSpec, String user, String password)
			throws DatabaseException {
		Connection conn = super.getConnection(urlSpec, user, password);
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to disable autocommit", this, e);
		}
		return conn;
	}

	@Override
	public Connection getConnection(ConnectionDictionary dictionary)
			throws DatabaseException {
		Connection conn = super.getConnection(dictionary);
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to disable autocommit", this, e);
		}
		return conn;
	}
}

