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
import xal.tools.data.KeyValueRecordListener;

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

	/** table displaying the list of commands for the selected rule */
	final private JTable RULE_COMMAND_TABLE;

	/** table model for displaying the list of commands for the selected rule */
	final private KeyValueTableModel<StringArgument> RULE_COMMAND_TABLE_MODEL;

	/** list of rule commands */
	private List<StringArgument> _ruleCommands;

	/** field for specifying the selected rule's pattern */
	final private JTextField RULE_PATTERN_FIELD;
	
	/** field for specifying the selected rule's kind */
	final private JTextField RULE_KIND_FIELD;

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

		final JButton deleteRuleButton = (JButton)windowReference.getView( "DeleteRuleButton" );
		deleteRuleButton.addActionListener( deleteRuleAction() );
		
		final JButton addRuleButton = (JButton)windowReference.getView( "AddRuleButton" );
		addRuleButton.addActionListener( addRuleAction() );
		
		RULES_TABLE = (JTable)windowReference.getView( "RulesTable" );
		RULES_TABLE.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		RULES_TABLE_MODEL = new KeyValueTableModel<>( new ArrayList<Rule>(), "pattern", "kind", "commands" );
		RULES_TABLE.setModel( RULES_TABLE_MODEL );
		
		RULES_TABLE.getSelectionModel().addListSelectionListener( rulesTableSelectionHandler() );

		_ruleCommands = new ArrayList<StringArgument>();

		RULE_COMMAND_TABLE = (JTable)windowReference.getView( "RuleCommandTable" );
		RULE_COMMAND_TABLE_MODEL = new KeyValueTableModel<>( _ruleCommands, "value" );
		RULE_COMMAND_TABLE_MODEL.setColumnName( "value", "Command" );
		RULE_COMMAND_TABLE_MODEL.setColumnEditable( "value", true );
		RULE_COMMAND_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<StringArgument>,StringArgument>() {
			public void recordModified( final KeyValueTableModel<StringArgument> tableModel, final StringArgument argument, final String keyPath, final Object value ) {
				pushRuleCommandsToModel();
			}
		});
		RULE_COMMAND_TABLE.setModel( RULE_COMMAND_TABLE_MODEL );

		final JButton deleteRuleCommandButton = (JButton)windowReference.getView( "DeleteRuleCommandButton" );
		deleteRuleCommandButton.addActionListener( new ActionListener() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = RULE_COMMAND_TABLE.getSelectedRows();
				final Set<StringArgument> argumentsToRemove = new HashSet<StringArgument>( selectedRows.length );
				for ( final int selectedRow : selectedRows ) {
					final int commandRow = RULE_COMMAND_TABLE.convertRowIndexToModel( selectedRow );
					argumentsToRemove.add( RULE_COMMAND_TABLE_MODEL.getRecordAtRow( commandRow ) );
				}
				_ruleCommands.removeAll( argumentsToRemove );
				pushRuleCommandsToModel();
				RULE_COMMAND_TABLE_MODEL.fireTableDataChanged();
			}
		});

		final JButton addRuleCommandButton = (JButton)windowReference.getView( "AddRuleCommandButton" );
		addRuleCommandButton.addActionListener( new ActionListener() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				final int selectedRow = RULE_COMMAND_TABLE.getSelectedRow();
				if ( selectedRow >= 0 ) {
					final int commandRow = RULE_COMMAND_TABLE.convertRowIndexToModel( selectedRow );
					_ruleCommands.add( commandRow, new StringArgument( "" ) );
					pushRuleCommandsToModel();
					RULE_COMMAND_TABLE_MODEL.fireTableDataChanged();
				}
			}
		});

		refreshView();
	}


	/** push the selected rule's commands to the model */
	private void pushRuleCommandsToModel() {
		final Rule rule = getSelectedRule();
		if ( rule != null ) {
			final List<String> commands = StringArgument.toStrings( _ruleCommands );
			rule.setCommands( commands );
		}
	}


	/** refresh the rule command view */
	private void refreshRuleCommandsView() {
		final Rule rule = getSelectedRule();
		if ( rule != null ) {
			_ruleCommands = StringArgument.toArguments( rule.getCommands() );
		}
		else {
			_ruleCommands = new ArrayList<>();

		}
		RULE_COMMAND_TABLE_MODEL.setRecords( _ruleCommands );
	}
	
	
	/** refresh the view with the model data */
	private void refreshView() {
		RULES_TABLE_MODEL.setRecords( MODEL.getRules() );
		refreshRuleCommandsView();
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
			RULE_COMMAND_TABLE.setEnabled( !exclude );
			MODEL.updateRuleExclusionAt( row, exclude );
			if ( RULE_EXCLUSION_CHECKBOX.isSelected() ) {
				RULE_KIND_FIELD.setText( "Excluded" );
				MODEL.updateRuleKindAt( row, RULE_KIND_FIELD.getText() );				
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
	

	/** Get the currently selected rule */
	private Rule getSelectedRule() {
		final List<Rule> rules = MODEL.getRules();
		final int ruleCount = rules.size();
		if ( rules == null || ruleCount == 0 )  return null;

		final int selectedRow = RULES_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			final int ruleIndex = RULES_TABLE.convertRowIndexToModel( selectedRow );
			if ( ruleIndex < ruleCount ) {
				return rules.get( ruleIndex );
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	
	/** handler of rules table selection events */
	private ListSelectionListener rulesTableSelectionHandler() {
		return new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					final Rule rule = getSelectedRule();
					if ( rule != null ) {
						RULE_PATTERN_FIELD.setText( rule.getPattern() );
						RULE_KIND_FIELD.setText( rule.getKind() );
						final boolean excludes = rule.excludes();
						RULE_EXCLUSION_CHECKBOX.setSelected( excludes );
						RULE_KIND_FIELD.setEnabled( !excludes );
						RULE_COMMAND_TABLE.setEnabled( !excludes );
					}
					else {
						RULE_PATTERN_FIELD.setText( "" );
						RULE_KIND_FIELD.setText( "" );
						RULE_EXCLUSION_CHECKBOX.setSelected( false );
					}
					refreshRuleCommandsView();
				}
			}
		};
	}
}
