/*
 * TrialVeto.java
 *
 * Created Wednesday June 9, 2004 2:39 pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver;
  
 import xal.extension.solver.constraint.Constraint;
 
 import java.util.*;
 

 /**
 * Trial veto turns down a trial based on a particular trial point.
 * @author ky6  
 * @author t6p
 */
 final public class TrialVeto {
	 final protected String _reason;
	 final protected Object _userInfo;
	 final protected Trial _trial;
	 final protected Constraint _constraint;
	 
	 
	 /**
	  * Primary Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  * @param reason describing why the veto was made
	  * @param userInfo supplying additional user inforamtion to associate with the veto
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint, final String reason, final Object userInfo ) {
		 _trial = trial;
		 _constraint = constraint;
		 _reason = reason;
		 _userInfo = userInfo;
	 }
	 
	 
	 /**
	  * Primary Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  * @param reason describing why the veto was made
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint, final String reason ) {
		 this( trial, constraint, reason, null );
	 }
	 
	
	 /**
	  * Constructor.
	  * @param trial The trial to veto.
	  * @param constraint The constraint to base the veto on.
	  */
	 public TrialVeto( final Trial trial, final Constraint constraint ) {
		 this( trial, constraint, "" );
	 }
	 
	 
	 /**
	 * Get the trial point to be tested.
	 * @return trialPoint.
	 */
	 public TrialPoint getTrialPoint() {
		 return _trial.getTrialPoint();
	 }
	 
	 
	 /**
	 * Get the trial.
	 * @return trial.
	 */
	 public Trial getTrial() {
		 return _trial;
	 }
	 
	 
	 /**
	 * Get the constaint.
	 * @return constraint.
	 */
	 public Constraint getConstraint() {
		 return _constraint;
	 }
	 
	 
	 /**
	  * Get the reason for the veto
	  * @return the reason for the veto
	  */
	 public String getReason() {
		 return _reason;
	 }
	 
	 
	 /**
	  * Get the user info
	  * @return the user info
	  */
	 public Object getUserInfo() {
		 return _userInfo;
	 }
 }
