/*
 * QueryModel.java
 *
 * Created on Tue Mar 30 11:37:48 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.tools.database.*;

import java.sql.*;
import java.util.*;


/**
 * QueryModel
 * @author  tap
 */
public class QueryModel {
	/**
	 * Execute a SQL query with the user's default database connection
	 * @param query the SQL to execute
	 * @return the result of executing the query
	 */
	static public String executeQuery( final Connection connection, final String query ) throws RuntimeException {
		try {
			final Statement statement = connection.createStatement();	
			statement.execute( query );
			final ResultSet resultSet = statement.getResultSet();
			final ResultSetMetaData metaData = resultSet.getMetaData();
			final int columnCount = metaData.getColumnCount();
			
			final int[] columnTypes = new int[columnCount];
			for ( int column = 0 ; column < columnCount ; column++ ) {
				columnTypes[column] = metaData.getColumnType( column + 1 );
			}
			
			final StringBuffer results = new StringBuffer();
			while ( resultSet.next() ) {
				for ( int column = 1 ; column <= columnCount ; column++ ) {
					final Object data = getValue( resultSet, column, columnTypes[column-1] );
					results.append( data ).append( '\t' );
				}
				results.append( '\n' );
			}
			resultSet.close();
			statement.close();
			return results.toString();
		}
		catch( SQLException exception ) {
			throw new RuntimeException( "Database exception", exception );
		}
	}
	
	
	/** get the column data as the appropriate type */
	static public Object getValue( final ResultSet resultSet, final int column, final int type ) throws SQLException {
		switch( type ) {
			case Types.TIMESTAMP:
				return resultSet.getTimestamp( column );
			case Types.TIME:
				return resultSet.getTime( column );
			case Types.DATE:
				return resultSet.getDate( column );
			default:
				return resultSet.getObject( column );
		}
	}
}

