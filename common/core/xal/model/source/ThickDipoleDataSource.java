/*
 * Created on Mar 9, 2004
 */
package xal.model.source;

import xal.model.IModelDataSource;


/**
 * Specifies interface for sources used to construct ThickDipole elements.
 * 
 * @author Craig McChesney
 */
public interface ThickDipoleDataSource extends IModelDataSource {
	
	public double dsGetLength();
	public double dsGetMagField();
	public double dsGetKQuad();
	public double dsGetEntranceAngleRadians();
	public double dsGetExitAngleRadians();
	public int dsGetOrientationEnum();
	
}
