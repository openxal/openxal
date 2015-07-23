/**
 * ElementIdClass.java
 *
 * @author Christopher K. Allen
 * @since  Jun 12, 2013
 */
package xal.tools.text;


import java.util.NoSuchElementException;

/**
 * <p>
 * Represents an equivalence class of element identifiers.  An element ID is a member of the
 * equivalence class if it is a proper substring of the current class representative or
 * the current class representative is a proper substring of the given element ID.
 * The class representative identifier is continually truncated whenever a
 * member element ID is encounter that is shorter, that is, the encountered ID becomes the 
 * representative ID. 
 * </p>
 * <p>
 * The class of identifiers is intended to
 * be associated with one hardware element.
 * </p>
 * <p>
 * Due to a peculiarity of the XAL lattice generator, modeling elements
 * are created with identifiers that are suffixed versions of their
 * hardware counterpart.  Thus, we equate two <code>ElementIdClass</code>
 * objects if they have the same prefix (ergo, they must belong to the
 * same hardware object).
 * </p>  
 *
 * @author Christopher K. Allen
 * @since  Jun 4, 2013
 *
 */
public class IdentifierEquivClass implements Comparable<IdentifierEquivClass> {

    /*
     * Local Attributes
     */

    /** The representative of the element ID class */
    private String      strClassId;


    /*
     * Initialization 
     */

    /**
     * Creates a new instance of <code>ElementIdClass</code>.
     *
     * @param strElemId
     *
     * @author Christopher K. Allen
     * @since  Jun 5, 2013
     */
    public IdentifierEquivClass(String strElemId) {
        this.strClassId = strElemId;
    }


    /*
     * Operations
     */

    /**
     * Checks whether or not the given ID is an member of this 
     * equivalence class of element IDs.
     * 
     * @param strElemId     element identifier under comparison
     * 
     * @return              <code>true</code> if argument is equivalent to the representative element
     *                      of this equivalence class, <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Jun 10, 2013
     */
    public boolean  isMember(String strElemId) {

        try {
            int iResult = this.compareTo(strElemId);

            // Truncate the class representative if there is a smaller representative
            //  out there
            if (iResult > 0)
                this.strClassId = strElemId;

            return true;

            // The argument is not comparable, therefore, not in the equivalence class
        } catch (NoSuchElementException e) {
            return false;

        }
    }

    /**
     * Tests whether or not the given equivalence class object is the same as
     * this, w.r.t. equivalence of the element ID class.  Returns the result
     * <br>
     * <br>
     * &nbsp; &nbsp;    ElementIdClass.isMember(<var>idCmp.strClassId)</var>
     * 
     * @param idCmp     element ID class object to compare
     * 
     * @return          returns <code>true</code> if the argument and this object represent 
     *                  the same equivalence class, or <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Jun 10, 2013
     */
    public boolean equivalentTo(IdentifierEquivClass idCmp) {
        return idCmp.isMember(this.strClassId);
    }


    /*
     * Comparable Interface
     */

    /** 
     * Implements a comparison of equivalences classes for the element IDs.
     * We first check whether or not the argument is equivalent to this object,
     * if so a value of 0 is returned.  If not, then the representative strings 
     * of the equivalence class are compared lexicographically, the result of that
     * comparison is returned.  Note that is value will not be 0 since the objects
     * under comparison are not in the same equivalence class of element IDs.
     * 
     * @param   idcCmp  equivalence class object to compare w.r.t. the element ID class
     * 
     * @return          a value 0 if the argument is the same equivalence class, or 
     *                  the lexicographical comparison result otherwise
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     *
     * @author Christopher K. Allen
     * @since  Jun 6, 2013
     */
    @Override
    public int compareTo(IdentifierEquivClass idcCmp) {

        if (this.equivalentTo(idcCmp))
            return 0;

        // The two classes are not equivalent.
        //  Use a lexicographical string comparison since neither is a substring of the other
        int iResult = this.strClassId.compareTo(idcCmp.strClassId);

//        System.out.println("lexicographic compareTo(" + this.strClassId + ", " + idcCmp.strClassId + ") = " + iResult);

        return iResult;
    }


    /*
     * Object Overrides
     */

    /** 
     * Checks whether or not the given object is equivalent to this object
     * and also sets the base ID string if necessary.
     * Two <code>ElementIdentifiers</code> are equivalent if one is a substring
     * of the other.  The base ID string then becomes the smallest common 
     * string between them.
     * 
     * @param   obj     obj to be compared (must be of type <code>ElementIdentifier</code>
     *
     * @return          returns <code>true</code> if the given element identifier object is
     *                  equivalent to this (see above), <code>false</code> otherwise. 
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     *
     * @author Christopher K. Allen
     * @since  Jun 4, 2013
     */
    @Override
    public boolean equals(Object obj) {

        // Make sure the argument is one of us then obviate it if so
        if (! (obj instanceof IdentifierEquivClass) )
            return false;

        IdentifierEquivClass   idcCmp = (IdentifierEquivClass)obj;

//        System.out.println("equals(x) with this.strClassId=" + this.strClassId + " and x.strClassId=" + idcCmp.strClassId);

        return this.equivalentTo(idcCmp);
    }

    /** 
     * Returns the hash code of the base ID. This is the prefix of the
     * identifier, which has intersected all others compared to so far.
     * 
     * @return      hash code of the ID prefix (makes the hash class larger)
     *
     * @see java.lang.Object#hashCode()
     *
     * @author Christopher K. Allen
     * @since  Jun 4, 2013
     */
    @Override
    public int hashCode() {
        return this.strClassId.hashCode();
    }

    /** 
     * Returns the representative identifier string.
     * 
     * @return  returns the value of the class ID
     *
     * @see java.lang.Object#toString()
     *
     * @author Christopher K. Allen
     * @since  Jun 4, 2013
     */
    @Override
    public String toString() {
        return this.strClassId;
    }


    /*
     * Support Methods
     */

    /**
     * <p>
     * This is essentially a utility method for the other methods of the class, as such I have
     * removed the visibility.
     * </p>
     * <p>
     * Makes a comparison of the equivalence class representative string ID with the given
     * string ID.  If the two strings are not equivalent, then
     * a <code>NoSuchElementException</code> is thrown.  If the two strings are equivalent 
     * (w.r.t. to the equivalence class rules), then an integer value is returned (described below).
     * The empty identifier <b>""</b> is handled as a special case: no string of finite length is
     * equivalent to the empty string, both strings must be empty to be equivalent. Otherwise we have
     * the trivial case where all IDs would be equivalent to the empty ID since all IDs contain 
     * the empty string.    
     * </p>
     * <p>
     * Now consider when the given string is found to be an element of this equivalence class. Denote by 
     * <tt>s</tt> the given argument string and by <tt>r</tt> the representative string of this
     * equivalence class (so that the equivalence class is represented as [<tt>r</tt>]).  Then the follow
     * results are returned:
     * <br>
     * <br>
     * &nbsp; &nbsp;  -1 &#8658; <tt>s</tt> &sub; <tt>r</tt> ,
     * <br>
     * &nbsp; &nbsp; &nbsp;  0 &#8658; <tt>s</tt> = <tt>r</tt> ,
     * <br>
     * &nbsp; &nbsp; +1 &#8658; <tt>s</tt> &sup; <tt>r</tt> ,
     * <br>
     * <br>
     * where all strings are compared starting from the head, i.e., <tt>s</tt>[0] == <tt>r</tt>[0] must
     * hold in every case since neither is the empty string.
     * </p>
     * 
     * @param strElemId     string to be compared to the representative string of this equivalence class
     * 
     * @return              if the two strings are equivalent, then the result of the comparison as
     *                      described above, otherwise an exception is thrown.
     * 
     * @throws NoSuchElementException   the given string ID is not equivalent to this object
     *
     * @author Christopher K. Allen
     * @since  Jun 10, 2013
     */
    public int  compareTo(String strElemId) throws NoSuchElementException {

        // Check if this is the zero class and argument is the empty ID
        if (this.strClassId.length()==0 && strElemId.length()==0 ) {

//            System.out.println("len(" + this.strClassId + ") = 0");

            return 0;
        }

        // Check if either string is empty, then we cannot be equivalent strings
        if (this.strClassId.length() == 0  ||  strElemId.length() == 0)
            throw new NoSuchElementException("Argument " + strElemId + " is not an element of the empty ID ''");

        // Check if the argument is contained within this ID
        //  (Then this is "larger" then the argument)
        if (strElemId.length() < this.strClassId.length()) 
            if (this.strClassId.startsWith(strElemId)) {

//                System.out.println(this.strClassId + " > " + strElemId);

                return +1;
            }

        // Check if this ID is contained within the argument
        //  (Then this is "smaller" then the argument)
        if (this.strClassId.length() < strElemId.length())
            if (strElemId.startsWith(this.strClassId)) {

//                System.out.println(this.strClassId + " < " + strElemId);

                return -1;
            }

        // The two identifier strings are the same length
        //  They must be equal as strings to be equal
        if ( this.strClassId.equals(strElemId) )
            return 0;

        // The argument is not a member of this equivalence class  
        throw new NoSuchElementException("Argument " + strElemId + 
                " is not an element of ID class [" + this.strClassId + "]");        
    }
}