//
//  ControllerListener.java
//  xal
//
//  Created by Pelaia II, Tom on 9/26/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

/** interface for controller events */
public interface ControllerListener {
	/** indicates that the document has been modified */
	public void documentModified( final AbstractController source );
}
