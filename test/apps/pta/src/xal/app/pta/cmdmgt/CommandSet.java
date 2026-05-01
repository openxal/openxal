/**
 * CommandSet.java
 *
 *  Created	: Jul 23, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.cmdmgt;

import xal.extension.application.Commander;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * Maintains a set of <code>{@link Command}</code> objects
 * which are generally provided by a derived class.  The derived class
 * must provide an array of <code>{@link CommandSet.ICmdDescriptor}</code> 
 * objects at
 * construction time, since it is assumed that these commands are constant
 * and immutable.
 * </p>  
 * <p>
 * Derived classes <em>must</em> pass the array of command descriptions
 * to the constructor of this base class.  Thus, the typical method 
 * for base classes to define commands is through enumerations that define
 * the <code>{@link CommandSet.ICmdDescriptor}</code> interface.  The 
 * enumeration then points to an (static) internal class which performs the
 * actual command.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  Jul 23, 2009
 * @author Christopher K. Allen
 * 
 * @see CommandSet.ICmdDescriptor
 */
public abstract class CommandSet {



    /**
     * <p>
     * Interface that should be exposed by enumeration
     * classes representing application menu
     * commands.  
     * Such objects <em>must</em> describe their represented
     * command by returning
     * the unique identifier string of the command and
     * the class type of the command.
     * </p>
     * <p>
     * To elaborate,
     * the command identifier string is the same identifier 
     * as that for a menu action, defined
     * in the <tt>menudef.properties</tt> resource file.
     * </p>      
     * <p>
     * The identifier should be unique, although there 
     * can be multiple components using it.  The 
     * command identifier should be bound to <em>one</em> 
     * <code>Command</code> object representing
     * the command response.
     * </p> 
     *
     * 
     * @since  Jul 20, 2009
     * @author Christopher K. Allen
     */
    public interface ICmdDescriptor {


        /**
         * <p>
         * Returns the command associated with the enumeration
         * constant.  
         * Note this ID is the same as that defined for a menu command
         * in the <tt>menudef.properties</tt> resource file.
         * </p> 
         *
         * @return      unique identifier string of the command
         * 
         * @since  Jul 20, 2009
         * @author Christopher K. Allen
         */
        public String getCommandId();

        /**
         * Returns the class of the command (i.e., a child class
         * of <code>Command</code>) which this interface
         * describes.
         *
         * @return      class type of the command
         * 
         * @since  Jul 28, 2009
         * @author Christopher K. Allen
         */
        public Class<? extends Command> getCommandClass();
    }



    

    /*
     * Instance Attributes
     */

    /** The set of commands belonging to this command set */
    private final Set<Command>    setCmds; 



    /*
     * Operations
     */


    /**
     * Returns all the commands contained in this
     * command set.
     *
     * @return set of command objects
     *
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    public Set<Command> getCommandSet() {
        return setCmds;
    }


    /**
     * Adds the command set into the set of managed
     * application commands. 
     *
     * @param cmdr   command manager
     * 
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    public void register(Commander cmdr) {
        
        for (Command cmd : this.getCommandSet()) 
            cmd.register(cmdr);
    }

    
    
    /*
     * Initialization
     */

//    /**
//     * Create a new <code>CommandSet</code> object and 
//     * set the collection of command actions.
//     *
//     * @throws  InstantiationException  failure to create a command in the command set 
//     *                                  (see message)
//     * 
//     * @since     Jul 23, 2009
//     * @author    Christopher K. Allen
//     */
//    protected CommandSet(ICmdDescriptor[] arrCmdDescr) throws InstantiationException {
//        this.setCmds = this.buildCommandSet(arrCmdDescr);
//    }
//
    /**
     * Create a new <code>CommandSet</code> object and 
     * set the collection of command actions.  All the commands are automatically
     * supported by passing the array of command descriptors.
     *
     * @param arrCmdDescr  array of command descriptors for the supported command set
     *  
     * @throws  InstantiationException  failure to create a command in the command set 
     *                                  (see message)
     * 
     * @since     Jul 23, 2009
     * @author    Christopher K. Allen
     */
    protected CommandSet(ICmdDescriptor[] arrCmdDescr) throws InstantiationException {
        this.setCmds = this.buildCommandSet(arrCmdDescr);
    }


    /*
     * Support Methods
     */

    /**
     * Builds the set of commands contained in this
     * <code>CommandSet</code> instance.   Given an array
     * of <code>ICmdDescriptor</code> objects describing
     * the commands, the constructed for the 
     * <code>CommandSet</code>-derived class is call with 
     * the command identifier provided by the interface.
     *
     * @param arrCmdDescr array of command description objects
     * 
     * @return the set of commands contained in this command set
     * 
     * @throws InstantiationException   a command failed to instantiate
     *                                  (see exception message)
     * 
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    protected Set<Command> buildCommandSet(ICmdDescriptor[] arrCmdDescr) 
        throws InstantiationException 
    {

        Set<Command> setCmds = new HashSet<Command>( arrCmdDescr.length );

        for (ICmdDescriptor enmCmd : arrCmdDescr)  {
            Class<? extends Command> clsCmd   = enmCmd.getCommandClass();

            try {
                String strCmdId = enmCmd.getCommandId();
                Constructor<? extends Command> ctorCmd = clsCmd.getConstructor(this.getClass(), String.class);
                Command cmd = ctorCmd.newInstance(this, strCmdId);

                setCmds.add(cmd);

            } catch (IllegalArgumentException e) {
                this.buildCommandSetFailure(clsCmd);

            } catch (InstantiationException e) {
                this.buildCommandSetFailure(clsCmd);

            } catch (IllegalAccessException e) {
                this.buildCommandSetFailure(clsCmd);

            } catch (InvocationTargetException e) {
                this.buildCommandSetFailure(clsCmd);

            } catch (SecurityException e) {
                this.buildCommandSetFailure(clsCmd);

            } catch (NoSuchMethodException e) {
                this.buildCommandSetFailure(clsCmd);

            }
        }

        return setCmds;
    }


    /**
     * Method that creates and throws an
     * <code>Instantiation</code> exception in the
     * event of a command instantiation failure.
     * This method is meant to consolidate the plethora
     * of exception potentially thrown in the process
     * of command instantiation.
     *
     * @param clsCmd    class type of command that failed instantiation
     * 
     * @throws InstantiationException exception containing failure message
     * 
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    private void      buildCommandSetFailure(Class<? extends Command> clsCmd)
        throws InstantiationException
    {
        String strMsg = "CommandSet#buildCommandSet(); " + //$NON-NLS-1$
        "unable to create command of type " + clsCmd.getName(); //$NON-NLS-1$

        throw new InstantiationException(strMsg);
    }
}
