/*
 * LaunchModel.java
 *
 * Created on Fri Mar 05 09:40:44 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;

import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;

import java.util.*;
import java.io.File;


/**
 * LaunchModel is the main model.  It manages the collections of groups and applications.
 * @author  tap
 */
public class LaunchModel implements DataListener {
	/** DataAdaptor label used in reading and writing */
	static public final String DATA_LABEL = "LaunchModel";
	
	/** message center used to post events from this instance */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting events */
	final private LaunchModelListener EVENT_PROXY;
	
	/** the launcher which executes the selection */
	final private Launcher LAUNCHER;
	
	/** file watcher */
	final private FileWatcher FILE_WATCHER;
	
	/** list of applications that can be run */
	final private List<App> APPLICATIONS;
	
	/** rules for running applications */
	final private List<Rule> RULES;
	
	
	/** Constructor */
	public LaunchModel() {
		MESSAGE_CENTER = new MessageCenter( "LaunchModel" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, LaunchModelListener.class );
		
		APPLICATIONS = new ArrayList<App>();
		RULES = new ArrayList<Rule>();
		LAUNCHER = new Launcher();
		FILE_WATCHER = new FileWatcher( this );
		
		refreshApplications();
	}
	
	
	/** post modification notice */
	public void postModifications() {
		EVENT_PROXY.modified( this );
	}
	
	
	/** get the file watcher */
	FileWatcher getFileWatcher() {
		return FILE_WATCHER;
	}
    
	
	/** get the applications */
	public List<App> getApplications() {
		return APPLICATIONS;
	}
	
	
	/** get rules */
	public List<Rule> getRules() {
		return RULES;
	}
	
	
	/** add a new rule at the specified index or at the end if the index is invalid */
	public void addNewRuleAt( final int index ) {
		final Rule rule = new Rule();
		
		if ( index >= 0 && index < RULES.size() ) {
			RULES.add( index, rule );
		}
		else {
			RULES.add( rule );
		}
		
		postModifications();
	}
	
	
	/** update the rule pattern at the specified location */
	public void updateRulePatternAt( final int index, final String pattern ) {
		if ( index >= 0 && index < RULES.size() ) {
			final Rule rule = RULES.get( index );
			rule.setPattern( pattern );
			postModifications();
		}		
	}
	
	
	/** update the rule kind at the specified location */
	public void updateRuleExclusionAt( final int index, final boolean exclude ) {
		if ( index >= 0 && index < RULES.size() ) {
			final Rule rule = RULES.get( index );
			rule.setExcludes( exclude );
			postModifications();
		}		
	}
	
	
	/** update the rule kind at the specified location */
	public void updateRuleKindAt( final int index, final String kind ) {
		if ( index >= 0 && index < RULES.size() ) {
			final Rule rule = RULES.get( index );
			rule.setKind( kind );
			postModifications();
		}		
	}

	
	/** delete the rule at the specified index */
	public void deleteRuleAt( final int index ) {
		if ( index >= 0 && index < RULES.size() ) {
			RULES.remove( index );
			postModifications();
		}		
	}
	
	
	/** refresh the list of applications */
	public void refreshApplications() {
		final List<File> files = FILE_WATCHER.listFiles();
		final List<App> applications = new ArrayList<App>( files.size() );
		for ( final File file : files ) {
			final Rule rule = getRule( file );
			if ( rule != null ) {
				applications.add( new App( file, rule ) );
			}
		}
		Collections.sort( applications );
		APPLICATIONS.clear();
		APPLICATIONS.addAll( applications );
	}
	
	
	/** get the first rule that matches the specified file */
	private Rule getRule( final File file ) {
		final List<Rule> rules = new ArrayList<Rule>( RULES );
		for ( final Rule rule : rules ) {
			if ( rule.matches( file ) ) {
				return rule;
			}
		}
		return null;
	}
	
	
	/** get the first rule that matches the specified application */
	private Rule getRule( final App application ) {
		final List<Rule> rules = new ArrayList<Rule>( RULES );
		for ( final Rule rule : rules ) {
			if ( rule.matches( application ) ) {
				return rule;
			}
		}
		return null;
	}
	
	
	/** launch the specified application using the first matching rule */
	public void launchApplication( final App application ) throws Exception {
		LAUNCHER.launch( application );			
	}
	
	
	/** preconfigure the model when initializing without a document file */
	public void preConfigure() {
		RULES.clear();
		RULES.add( new Rule( "*.jar", "Application", "java", "-DuseDefaultAccelerator=true", "-jar", "%f" ) );
		RULES.add( new Rule( "*.rb", "JRuby", "jruby", "%f" ) );
		RULES.add( new Rule( "*.py", "Jython", "jython", "%f" ) );
				
		LAUNCHER.preConfigure();
		
		refreshApplications();
	}
	
    
    /** 
     * provides the name used to identify the class in an external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		RULES.clear();
		final List<DataAdaptor> ruleAdaptors = adaptor.childAdaptors( Rule.DATA_LABEL );
		for ( final DataAdaptor ruleAdaptor : ruleAdaptors ) {
			RULES.add( Rule.getInstance( ruleAdaptor ) );
		}
		
		final DataAdaptor fileWatcherAdaptor = adaptor.childAdaptor( FileWatcher.DATA_LABEL );
		if ( fileWatcherAdaptor != null ) {
			FILE_WATCHER.update( fileWatcherAdaptor );
		}
		
		final DataAdaptor launchAdaptor = adaptor.childAdaptor( Launcher.DATA_LABEL );
		if ( launchAdaptor != null ) {
			LAUNCHER.update( launchAdaptor );
		}
		
		refreshApplications();
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.writeNodes( RULES );
		adaptor.writeNode( FILE_WATCHER );
		adaptor.writeNode( LAUNCHER );
    }
	
	
	/**
	 * Add a listener of LaunchModel events from this instance
	 * @param listener The listener to add
	 */
	public void addLaunchModelListener( final LaunchModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, LaunchModelListener.class );
	}
	
	
	/**
	 * Remove a listener of LaunchModel events from this instance
	 * @param listener The listener to remove
	 */
	public void removeLaunchModelListener( final LaunchModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, LaunchModelListener.class );
	}
	
	
	/**
	 * Get the launcher
	 * @return launcher
	 */
	public Launcher getLauncher() {
		return LAUNCHER;
	}	
}
