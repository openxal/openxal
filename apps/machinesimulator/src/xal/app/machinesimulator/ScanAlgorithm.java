package xal.app.machinesimulator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/** 
 * @author luxiaohan
 *generate all the scanning spots from scan source
 */
public class ScanAlgorithm<T> {
	
	/**the maximum capacity*/
	public static final int MAXIMUM_CAPACITY = 100;
	/**scan source*/
	private List<T[]> scanSource;
	/**scan spots*/
	private List<T[]> scanSpots;
	/**class type*/
	private Class<T> classType;
	/**total scan steps*/
	private int scanSteps;
	/**the index list consist of step of each node*/
	private List<int[]> scanSpotsIndex;
	
	
	/**Constructor*/
	public ScanAlgorithm () {		
	}
	
	/**Constructor with source data*/
	public ScanAlgorithm ( final List<T[]> source, final Class<T> type ) {
		scanSource = source;
		classType = type;
	}
	
	/**set the scan source*/
	public void setSource( final List<T[]> source, final Class<T> type ) {
		scanSource = source;
		classType = type;
	}
	
	/**get scanning spots*/
	public List<T[]> getScanSpots() {
		return scanSpots;
	}
	
	/**get scan spots index*/
	public List<int[]> getScanSpotsIndex() {
		return scanSpotsIndex;
	}
	
	/**get scan steps*/
	public int getScanSteps() {
		return scanSteps;
	}
	
	/**
	 * generate scanning spots. 
	 * @return all the scanning spots
	 */
	public boolean generateScanSpots () {
		boolean state = true;
		scanSteps = calculSteps( scanSource );
		if ( scanSteps >= MAXIMUM_CAPACITY ) state = false;
		else {
			scanSpots = new ArrayList<T[]>( scanSteps );
			scanSpotsIndex = new ArrayList<int[]>( scanSteps );
			for ( int index = 0; index < scanSteps; index++ ) {
				@SuppressWarnings("unchecked")
				T[] nodeArray = (T[]) Array.newInstance( classType, scanSource.size() );
				int[] spotIndex = new int[scanSource.size()];
			scanSpots.add( nodeArray );
			scanSpotsIndex.add( spotIndex );
			}			
			iterate( 0, 0, scanSteps, scanSource );
		}
		
		return state;
	}
	/**
	 * calculate all the steps
	 * @param source the scan source
	 * @return the steps
	 */
	private int calculSteps ( final List<T[]> source ) {
		int totalSteps = 1;
		for ( final T[] node : source ) {
			totalSteps = totalSteps*node.length;
		}
		return totalSteps;
	}
	
	/**
	 * the iteration to generate scanning spots
	 * @param layer the layer in iterating
	 * @param upIndex the position index of the upper layer 
	 * @param count the count corresponding to the current layer
	 * @param source the scan source
	 */
	private void iterate ( final int layer, final int upIndex, final int count, final List<T[]> source ) {
		if ( layer >= source.size() ) return;
		T[] node = source.get( layer );
		for ( int i = 0; i < node.length; i++ ) {
			int pos = upIndex + i*( count/node.length );
			for ( int j = 0; j< count/node.length; j++ ) {
				scanSpots.get( j + pos )[layer] = node[i];
				scanSpotsIndex.get( j + pos )[layer] = i + 1;
			}
			iterate( layer + 1, pos, count/node.length, source );
		}
	}
	
	
}
