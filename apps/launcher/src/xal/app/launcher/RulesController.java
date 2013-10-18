//
//  RulesController.java
//  xal
//
//  Created by Tom Pelaia on 5/6/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.swing.KeyValueTableModel;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/** Controller for managing and editing rules */
public class RulesController {
	/** The main model of this document */
	final private LaunchModel MODEL;
	
	/** table of rules */
	final private JTable RULES_TABLE;
	
	/** table model for displaying the rules */
	final private KeyValueTableModel<Rule> RULES_TABLE_MODEL;
	
	/** field for specifying the selected rule's pattern */
	final private JTextField RULE_PATTERN_FIELD;
	
	/** field for specifying the selected rule's kind */
	final private JTextField RULE_KIND_FIELD;
	
	/** editor for specifying the rule's command */
	final private JTextField RULE_COMMAND_EDITOR;
	
	/** checkbox for excluding matching files */
	final private JCheckBox RULE_EXCLUSION_CHECKBOX;
	
	
	/** Constructor */
	public RulesController( final LaunchModel model, final WindowReference windowReference ) {
		MODEL = model;

		RULE_PATTERN_FIELD = (JTextField)windowReference.getView( "RulePatternField" );
		RULE_PATTERN_FIELD.addActionListener( rulePatternAction() );
		RULE_PATTERN_FIELD.addFocusListener( rulePatternFocusHandler() );
		
		RULE_EXCLUSION_CHECKBOX = (JCheckBox)windowReference.getView( "ExclusionCheckbox" );
		RULE_EXCLUSION_CHECKBOX.addActionListener( ruleExcludeFilesAction() );
		
		RULE_KIND_FIELD = (JTextField)windowReference.getView( "RuleKindField" );
		RULE_KIND_FIELD.addActionListener( ruleKindAction() );
		RULE_KIND_FIELD.addFocusListener( ruleKindFocusHandler() );
		
		RULE_COMMAND_EDITOR = (JTextField)windowReference.getView( "RuleCommandField" );
		RULE_COMMAND_EDITOR.addActionListener( ruleCommandAction() );
		RULE_COMMAND_EDITOR.addFocusListener( ruleCommandFocusHandler() );
		
		final JButton deleteRuleButton = (JButton)windowReference.getView( "DeleteRuleButton" );
		deleteRuleButton.addActionListener( deleteRuleAction() );
		
		final JButton addRuleButton = (JButton)windowReference.getView( "AddRuleButton" );
		addRuleButton.addActionListener( addRuleAction() );
		
		RULES_TABLE = (JTable)windowReference.getView( "RulesTable" );
		RULES_TABLE.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		RULES_TABLE_MODEL = new KeyValueTableModel<Rule>( new ArrayList<Rule>(), "pattern", "kind", "command" );
		RULES_TABLE.setModel( RULES_TABLE_MODEL );
		
		RULES_TABLE.getSelectionModel().addListSelectionListener( rulesTableSelectionHandler() );
		
		refreshView();
	}
	
	
	/** refresh the view with the model data */
	private void refreshView() {
		RULES_TABLE_MODEL.setRecords( MODEL.getRules() );
	}
	
	
	/** refresh the view with the model data */
	private void refreshRuleRow( final int row ) {
		RULES_TABLE_MODEL.fireTableRowsUpdated( row, row );
	}
	
	
	/** action to delete a rule */
	private AbstractAction deleteRuleAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				final int selectedRow = RULES_TABLE.getSelectedRow();
				MODEL.deleteRuleAt( selectedRow );
				refreshView();
			}
		};
	}	
	
	
	/** action to add a new rule */
	private AbstractAction addRuleAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				final int selectedRow = RULES_TABLE.getSelectedRow();
				MODEL.addNewRuleAt( selectedRow );
				refreshView();
			}
		};
	}
	
	
	/** action to apply the rule's pattern edits */
	private AbstractAction rulePatternAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				applyRulePatternEdit();
			}
		};
	}
	
	
	/** action for handling rule pattern events */
	private FocusListener rulePatternFocusHandler() {
		return new FocusListener() {
			private String _originalPattern = null;
			private int _selectedRow = -1;
			
			public void focusGained( final FocusEvent event ) {
				_selectedRow = RULES_TABLE.getSelectedRow();
				_originalPattern = RULE_PATTERN_FIELD.getText();
			}
			
			public void focusLost( final FocusEvent event ) {
				final String text = RULE_PATTERN_FIELD.getText();
				if ( text != null && _originalPattern != null ) {
					if ( !text.equals( _originalPattern ) ) {	// make sure the text actually changed
						applyRulePatternEdit( _selectedRow );
					}
				}
				_originalPattern = null;
				_selectedRow = -1;
			}
		};
	}
	
	
	/** action handling the toggle of excluding matching files */
	private AbstractAction ruleExcludeFilesAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				applyRuleExclusionEdit();
			}
		};
	}
	
	
	/** action to apply the rule's kind edits */
	private AbstractAction ruleKindAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				applyRuleKindEdit();
			}
		};
	}
	
	
	/** action for handling rule kind events */
	private FocusListener ruleKindFocusHandler() {
		return new FocusListener() {
			private String _originalKind = null;
			private int _selectedRow = -1;
			
			public void focusGained( final FocusEvent event ) {
				_selectedRow = RULES_TABLE.getSelectedRow();
				_originalKind = RULE_KIND_FIELD.getText();
			}
			
			public void focusLost( final FocusEvent event ) {
				final String text = RULE_KIND_FIELD.getText();
				if ( text != null && _originalKind != null ) {
					if ( !text.equals( _originalKind ) ) {	// make sure the text actually changed
						applyRuleKindEdit( _selectedRow );
					}
				}
				_originalKind = null;
				_selectedRow = -1;
			}
		};
	}
	
	
	/** action to apply the rule's edits */
	private AbstractAction ruleCommandAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				applyRuleCommandEdit();
			}
		};
	}
	
	
	/** action for handling rule command events */
	private FocusListener ruleCommandFocusHandler() {
		return new FocusAdapter() {
			private String _originalCommand = null;
			private int _selectedRow = -1;
			
			public void focusGained( final FocusEvent event ) {
				_selectedRow = RULES_TABLE.getSelectedRow();
				_originalCommand = RULE_COMMAND_EDITOR.getText();
			}
			
			public void focusLost( final FocusEvent event ) {
				final String text = RULE_COMMAND_EDITOR.getText();
				if ( text != null && _originalCommand != null ) {
					if ( !text.equals( _originalCommand ) ) {	// make sure the text actually changed
						applyRuleCommandEdit( _selectedRow );
					}
				}
				_originalCommand = null;
				_selectedRow = -1;
			}
		};
	}
	
	
	/** apply the rule pattern */
	private void applyRuleExclusionEdit() {
		final int selectedRow = RULES_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			applyRuleExclusionEdit( selectedRow );
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRuleExclusionEdit( final int row ) {
		if ( row >= 0 ) {
			final boolean exclude = RULE_EXCLUSION_CHECKBOX.isSelected();
			RULE_KIND_FIELD.setEnabled( !exclude );
			RULE_COMMAND_EDITOR.setEnabled( !exclude );
			MODEL.updateRuleExclusionAt( row, exclude );
			if ( RULE_EXCLUSION_CHECKBOX.isSelected() ) {
				RULE_KIND_FIELD.setText( "Excluded" );
				MODEL.updateRuleKindAt( row, RULE_KIND_FIELD.getText() );				
				RULE_COMMAND_EDITOR.setText( "" );
				MODEL.updateRuleCommandAt( row, RULE_COMMAND_EDITOR.getText() );
			}
			refreshRuleRow( row );		
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRulePatternEdit() {
		final int selectedRow = RULES_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			applyRulePatternEdit( selectedRow );
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRulePatternEdit( final int row ) {
		if ( row >= 0 ) {
			final String pattern = RULE_PATTERN_FIELD.getText();
			MODEL.updateRulePatternAt( row, pattern );
			refreshRuleRow( row );		
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRuleKindEdit() {
		final int selectedRow = RULES_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			applyRuleKindEdit( selectedRow );
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRuleKindEdit( final int row ) {
		if ( row >= 0 ) {
			final String kind = RULE_KIND_FIELD.getText();
			MODEL.updateRuleKindAt( row, kind );
			refreshRuleRow( row );		
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRuleCommandEdit() {
		final int selectedRow = RULES_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			applyRuleCommandEdit( selectedRow );
		}
	}
	
	
	/** apply the rule pattern */
	private void applyRuleCommandEdit( final int row ) {
		if ( row >= 0 ) {
			final String command = RULE_COMMAND_EDITOR.getText();
			MODEL.updateRuleCommandAt( row, command );
			refreshRuleRow( row );		
		}
	}
	
	
	/** handler of rules table selection events */
	private ListSelectionListener rulesTableSelectionHandler() {
		return new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					final int selectedRow = RULES_TABLE.getSelectedRow();
					final List<Rule> rules = MODEL.getRules();
					if ( selectedRow >= 0 && selectedRow < rules.size() ) {
						final Rule rule = rules.get( selectedRow );
						RULE_PATTERN_FIELD.setText( rule.getPattern() );
						RULE_KIND_FIELD.setText( rule.getKind() );
						RULE_COMMAND_EDITOR.setText( rule.getCommand() );
						final boolean excludes = rule.excludes();
						RULE_EXCLUSION_CHECKBOX.setSelected( excludes );
						RULE_KIND_FIELD.setEnabled( !excludes );
						RULE_COMMAND_EDITOR.setEnabled( !excludes );
					}
					else {
						RULE_PATTERN_FIELD.setText( "" );
						RULE_KIND_FIELD.setText( "" );
						RULE_COMMAND_EDITOR.setText( "" );
						RULE_EXCLUSION_CHECKBOX.setSelected( false );
					}
				}
			}
		};
	}
}
