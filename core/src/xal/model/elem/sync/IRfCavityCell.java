/**
 * IRfCavityCell.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jan 8, 2015
 */
package xal.model.elem.sync;

/**
 * <p>
 * Class exposing this interface will express parameters for describing a cell
 * within an RF cavity.  
 * </p>
 * <p>
 * Currently is it intended to be used in tandem with the
 * <code>IRfGap</code> interface to get a complete description.  It might be 
 * more natural to inherit from the <code>IRfGap</code> interface since most
 * cavity cells are used for their gap actions (but not all).  I don't know
 * if we would ever use a cavity cell modeling element for a cell that does not
 * accelerator (i.e., a side-coupled cell), but the current architecture supports
 * it.
 * </p>
 * <p>
 * This design also allows us to assign RF cavity cell properties to the RF gap
 * object (e.g., IdealRfGap).  This isn't exactly sound practice since the gap
 * is not a cell, however, it's a quick, klugy way into the current architecture.
 * </p>
 * <p>
 * It is quite possible that the architecture will be refactored here if we find
 * a better way.  This is essential an alpha design.
 * </p>    
 *
 * @author Christopher K. Allen
 * @since  Jan 8, 2015
 */
public interface IRfCavityCell {

    /**
     * <p>
     * Set the index <i>n</i> of this cell within the enclosing RF cavity.  The 
     * index origin begins at 0, specifically, the first cell in the cavity will 
     * have a cell index of O.  Since cell phase &phi; seen by the probe is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; = <i>nq</i>&pi; + &phi;<sub>0</sub>
     * <br/>
     * <br/>
     * where <i>q</i> is the cavity structure constant and &phi;<sub>0</sub>
     * is the klystron driving phase, the first cell always has the phase of
     * the klystron.
     * </p>
     * <p>
     * When considered with an RF gap, it can be convenient to consider the 
     * phase rather as a spatial component of the field and combine it with
     * the field amplitude.  We simply get a signum function effect where
     * the new field <i>E><sub>n</sub></i> at cell <i>n</i> is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>E<sub>n</sub></i> = <i>E</i><sub>0</sub> cos(<i>nq</i>&pi;)
     * <br/>
     * <br/>
     * where <i>E</i><sub>0</sub> is the usual gap field strength.  
     * </p>
     * <p>
     * See the discussion below on cavity mode constants.
     * </p> 
     * 
     * @param indCell   index of the cavity cell within the cavity, starting at 0
     *
     * @since  Jan 8, 2015   by Christopher K. Allen
     */
    public void setCavityCellIndex(int indCell);
    
    /**
     * <p>
     * Sets the structure mode <b>number</b> <i>q</i> for the cavity in which this 
     * cell belongs.  Here the structure mode number is defined in terms of
     * the fractional phase advance between cells, with respect to &pi;.  
     * To make this explicit
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 0  &nbsp; &nbsp; &rAarr;  0 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1/2 &rArr; &pi;/2 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1  &nbsp; &nbsp; &rAarr;  &pi; mode
     * <br/>
     * <br/>
     * Thus, a cavity mode constant of <i>q</i> = 1/2 indicates a &pi;/2
     * phase advance between adjacent cells and a corresponding cell amplitude
     * function <i>A<sub>n</sub></i> of
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>A<sub>n</sub></i> = cos(<i>nq</i>&pi;)
     * <br/>
     * <br/>
     * where <i>n</i> is the index of the cell within the coupled cavity.
     * </p>
     * 
     * @param   dblCavModeConst the cavity mode structure constant for the cavity containing this cell 
     *
     * @see <i>RF Linear Accelerators</i>, Thomas P. Wangler (Wiley, 2008).
     * 
     * @since  Jan 8, 2015   by Christopher K. Allen
     */
    public void setCavityModeConstant(double dblCavModeConst);
    
    
    /**
     * Returns the index of this cell within the parent RF cavity.  The index
     * origin starts at zero.
     * 
     * @return  the cell number within the parent cavity, starting at zero
     *
     * @since  Jan 8, 2015   by Christopher K. Allen
     * 
     * @see #setCavityCellIndex(int)
     */
    public int  getCavityCellIndex();
    
    /**
     * <p>
     * Returns the structure mode <b>number</b> <i>q</i> for the cavity in which this 
     * gap belongs.  This is the 
     * fractional phase advance between cells, with respect to &pi;.  It can also
     * be interpreted as describing the spatial advance of the axial electric
     * field from cell to cell.  
     * </p>
     * 
     * @return  the cavity mode constant for the cell containing this gap
     *
     * @see <i>RF Linear Accelerators</i>, Thomas P. Wangler (Wiley, 2008).
     * 
     * @since  Jan 8, 2015   by Christopher K. Allen
     * 
     * @see #setCavityModeConstant(double)
     */
    public double getCavityModeConstant();
    
    /**
     * Returns whether or not the cell is the first or last in a string of cells within an
     * RF cavity.  This is particularly important in structures operating outside
     * 0 mode where the cell phasing may change.
     * 
     * @return  <code>true</code> if this cell is at either end in a bank of cells,
     *          <code>false</code> otherwise
     *
     * @since  Jan 23, 2015   by Christopher K. Allen
     */
    public boolean  isEndCell();
    
    /**
     * Indicates whether or not this cell is the first cell of an RF cavity.
     * 
     * @return  <code>true</code> if this is the initial cell in an RF cavity,
     *          <code>false</code> otherwise
     *
     * @since  Jan 23, 2015   by Christopher K. Allen
     */
    public boolean isFirstCell();


}
