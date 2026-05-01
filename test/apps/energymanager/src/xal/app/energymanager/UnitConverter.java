//
//  Converter.java
//  xal
//
//  Created by Thomas Pelaia on 5/26/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** convert raw values to physical ones */
public interface UnitConverter {
	static final public UnitConverter NO_OPERATION = new UnitConverter() {
		public double toPhysical( final double caValue )  {
			return caValue;
		}
		
		public double toCA( final double physicalValue ) {
			return physicalValue;
		}
	};
	
	public double toPhysical( final double caValue );
	public double toCA( final double physicalValue );
}
