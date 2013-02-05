package xal.tools.database;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySQLDatabaseAdaptor extends DatabaseAdaptor {

	protected Map arrayDescriptorMap;

	/**
	 * Public Constructor
	 */
	public MySQLDatabaseAdaptor() {
		arrayDescriptorMap = new HashMap();
	}
	
	/**
	 * Static initializer
	 */
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch(ClassNotFoundException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error loading Oracle driver.", exception );
			exception.printStackTrace();
		}
	}
	
	@Override
	public Array getArray(String type, Connection connection, Object array)
			throws DatabaseException {
		// TODO Auto-generated method stub
//		try {
//			final ArrayDescriptor descriptor = getArrayDescriptor(type, connection);
//			return new ARRAY(descriptor, connection, array);
//		}
//		catch(SQLException exception) {
//			Logger.getLogger("global").log( Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception );
//			throw new DatabaseException("Exception generating an SQL array.", this, exception);
//		}
		
		return null;
	}

	@Override
	public Connection getConnection(String urlSpec, String user, String password)
			throws DatabaseException {
		try {
			return DriverManager.getConnection(urlSpec, user, password);
		}
		catch(SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error connecting to the database at URL: \"" + urlSpec + "\" as user: " + user , exception );
			throw new DatabaseException("Exception connecting to the database.", this, exception);
		}
	}

//	/**
//	 * Get the array descriptor for the specified array type
//	 * @param type An SQL array type
//	 * @param connection A database connection
//	 * @return the array descriptor for the array type
//	 * @throws java.sql.SQLException if a database exception is thrown
//	 */
//	private ArrayDescriptor getArrayDescriptor(final String type, final Connection connection) throws SQLException {
//		if ( arrayDescriptorMap.containsKey(type) ) {
//			return (ArrayDescriptor)arrayDescriptorMap.get(type);
//		}
//		else {
//			ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(type, connection);
//			arrayDescriptorMap.put(type, descriptor);
//			return descriptor;
//		}		
//	}
}
