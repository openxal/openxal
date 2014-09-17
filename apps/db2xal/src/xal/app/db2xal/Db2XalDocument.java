/*
 * @(#)Db2XalDocument.java          2.5 02/13/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.db2xal;

//import java.sql.*;
import java.io.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.net.URL;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.JToggleButton.ToggleButtonModel;

import xal.extension.application.smf.*;
import xal.tools.xml.*;
import xal.tools.data.*;

import xal.extension.application.*;
import xal.tools.apputils.NonConsecutiveSeqSelector; 

import xal.tools.database.ConnectionDictionary;
//import gov.sns.tools.database.*;


/**
 * Application for generating XAL XML file from SNS global database
 * 
 * @version 2.5 13 Feb 2004
 * @author Paul C. Chu
 * @author K.Danilova
 */

public class Db2XalDocument extends AcceleratorDocument implements DataListener {
	/** name of the production server in the configruation file */
	final private static String PRODUCTION_SERVER = "production";
	
	/** name of the development server in the configruation file */
	final private static String DEVELOPMENT_SERVER = "development";
	
	private static final String OUTPUT_FILENAME = "sns.xdxf";
	
	protected JFileChooser _exportFileChooser;

	/**
	 * The document for the text pane in the main window.
	 */
	protected PlainDocument textDocument;

	protected String databaseServer = PRODUCTION_SERVER;

	protected ArrayList<Object> seqList;

	// private Db2XalDocument myDoc;

	/** Create a new empty document */
	public Db2XalDocument() {
		this(null);
		// myDoc = this;
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public Db2XalDocument(java.net.URL url) {
		setSource(url);
		makeTextDocument();

		if (url == null)
			return;
	}

	public void customizeCommands(Commander commander) {

		// action for popping up sequence chooser
		Action showSeqsAction = new AbstractAction() {
			static final long serialVersionUID = 0;

			public void actionPerformed(ActionEvent event) {
				// NonConsecutiveSequenceSelector seqSel = new
				// SequenceSelector(myDoc);
				NonConsecutiveSeqSelector seqSel = new NonConsecutiveSeqSelector();
				seqSel.selectSequence();
				seqList = seqSel.getSeqList();
				if (seqList.size() != 0)
					setHasChanges(true);
			}
		};
		showSeqsAction.putValue(Action.NAME, "showSeqs");
		commander.registerAction(showSeqsAction);

		ToggleButtonModel useProdModel;
		ToggleButtonModel useDevlModel;

		// action for query production database
		useProdModel = new ToggleButtonModel();
		useProdModel.setSelected(true);
		useProdModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				databaseServer = PRODUCTION_SERVER;
			}
		});
		commander.registerModel("use-prod", useProdModel);

		// action for query development database
		useDevlModel = new ToggleButtonModel();
		useDevlModel.setSelected(true);
		useDevlModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				databaseServer = DEVELOPMENT_SERVER;
			}
		});
		commander.registerModel("use-devl", useDevlModel);

		// action for generating XAL XML file
		Action export1Action = new AbstractAction() {
			static final long serialVersionUID = 0;

			public void actionPerformed(ActionEvent event) {
				if (seqList == null) {
					JOptionPane.showMessageDialog( getMainWindow(), "You have not selected any sequence yet!  Please select sequence(s) first.", "Warning!", JOptionPane.WARNING_MESSAGE);
				} 
				else {
					try {
						writeXal();
					} 
					catch (IOException exception) {
						exception.printStackTrace();
					}

					JOptionPane.showMessageDialog( getMainWindow(), "The output XDXF file name is sns.xdxf.", "Message!", JOptionPane.PLAIN_MESSAGE );
				}
			}

		};
		export1Action.putValue(Action.NAME, "export-xal");
		commander.registerAction(export1Action);

	}
	
	
	/** generate a channel entry for a given signal */
	static private String channelEntry( final String handle, final String signalName, final Map<String,String> signalTable, final boolean settable ) {
		return "          <channel handle=\"" + handle + "\"" + " signal=\"" + signalTable.get( signalName ) + "\"" + " settable=\"" + settable + "\"/>\n";
	}
	
	
	/** fetch the signals for the specified device */
	static private Map<String,String> fetchSignals( final PreparedStatement fetchStatement, final String deviceID ) throws java.sql.SQLException {
		fetchStatement.setString( 1, deviceID );
		final ResultSet signalSet = fetchStatement.executeQuery();
		final Map<String,String> signalTable = new HashMap<String,String>();
		while ( signalSet.next() ) {
			final String signalID = signalSet.getString( 1 );
			final String signalName = signalSet.getString( 2 );
			signalTable.put( signalName, signalID );
			//System.out.println( "name: " + signalName + ", signal: " + signalID );
		}
		return signalTable;
	}
	
	
	/** generate and write the optics to a file */
	public void writeXal() throws IOException {
		if ( _exportFileChooser == null ) {
			_exportFileChooser = new JFileChooser();
			_exportFileChooser.setSelectedFile( new File( OUTPUT_FILENAME ) );
		}
		
		final int status = _exportFileChooser.showSaveDialog( getMainWindow() );
		switch( status ) {
			case JFileChooser.APPROVE_OPTION:
				break;
			case JFileChooser.CANCEL_OPTION:
				return;
			case JFileChooser.ERROR_OPTION:
				return;
			default:
				return;
		}
		
		OutputStream fout;
		fout = new FileOutputStream( _exportFileChooser.getSelectedFile() );
		Connection conn;
		ResultSet rset, rsetDTLs;
		String str;

		// get the current time and date and insert in the XML file
		java.util.Date today = new java.util.Date();

		str = "<?xml version = '1.0' encoding = 'UTF-8'?>\n"
				+ "<!DOCTYPE xdxf SYSTEM \"xdxf.dtd\">\n"
				+ "<xdxf system=\"sns\" ver=\"2.0.0\" date=\""
				+ today.toString() + "\">\n";

		int dtlCounter = 1;
		int cclCounter = 1;

		byte buf0[] = str.getBytes();
		fout.write(buf0);

		try {
			final ConnectionDictionary connectionDictionary = ConnectionDictionary.getInstance( "reports", databaseServer );
			conn = connectionDictionary.getDatabaseAdaptor().getConnection( connectionDictionary );

			// Create a Statement
			Statement stmt = conn.createStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
			Statement stmt1 = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );

			PreparedStatement stmt0;

			String[] sequences = new String[seqList.size()];
			for (int i = 0; i < seqList.size(); i++) {
				sequences[i] = (String) seqList.get(i);
			}

			if (sequences[0].substring(0, 4).equals("Ring")) {
				str = "  <comboseq id=\"Ring\">\n"
						+ "	  <sequence id=\"Ring1\"/>\n"
						+ "	  <sequence id=\"Ring2\"/>\n"
						+ "	  <sequence id=\"Ring3\"/>\n"
						+ "	  <sequence id=\"Ring4\"/>\n"
						+ "	  <sequence id=\"Ring5\"/>\n" + "  </comboseq>\n";
			} else {
				str = "  <comboseq id=\"MEBT-DTL\">\n"
						+ "	  <sequence id=\"MEBT\"/>\n"
						+ "	  <sequence id=\"DTL1\"/>\n"
						+ "	  <sequence id=\"DTL2\"/>\n"
						+ "	  <sequence id=\"DTL3\"/>\n"
						+ "	  <sequence id=\"DTL4\"/>\n"
						+ "	  <sequence id=\"DTL5\"/>\n"
						+ "	  <sequence id=\"DTL6\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"MEBT-SCL\">\n"
						+ "	  <sequence id=\"MEBT\"/>\n"
						+ "	  <sequence id=\"DTL1\"/>\n"
						+ "	  <sequence id=\"DTL2\"/>\n"
						+ "	  <sequence id=\"DTL3\"/>\n"
						+ "	  <sequence id=\"DTL4\"/>\n"
						+ "	  <sequence id=\"DTL5\"/>\n"
						+ "	  <sequence id=\"DTL6\"/>\n"
						+ "	  <sequence id=\"CCL1\"/>\n"
						+ "	  <sequence id=\"CCL2\"/>\n"
						+ "	  <sequence id=\"CCL3\"/>\n"
						+ "	  <sequence id=\"CCL4\"/>\n"
						+ "	  <sequence id=\"SCLMed\"/>\n"
						+ "	  <sequence id=\"SCLHigh\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"MEBT-HEBT\">\n"
						+ "	  <sequence id=\"MEBT\"/>\n"
						+ "	  <sequence id=\"DTL1\"/>\n"
						+ "	  <sequence id=\"DTL2\"/>\n"
						+ "	  <sequence id=\"DTL3\"/>\n"
						+ "	  <sequence id=\"DTL4\"/>\n"
						+ "	  <sequence id=\"DTL5\"/>\n"
						+ "	  <sequence id=\"DTL6\"/>\n"
						+ "	  <sequence id=\"CCL1\"/>\n"
						+ "	  <sequence id=\"CCL2\"/>\n"
						+ "	  <sequence id=\"CCL3\"/>\n"
						+ "	  <sequence id=\"CCL4\"/>\n"
						+ "	  <sequence id=\"SCLMed\"/>\n"
						+ "	  <sequence id=\"SCLHigh\"/>\n"
						+ "	  <sequence id=\"HEBT1\"/>\n"
						+ "	  <sequence id=\"HEBT2\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"MEBT-LDmp\">\n"
						+ "	  <sequence id=\"MEBT\"/>\n"
						+ "	  <sequence id=\"DTL1\"/>\n"
						+ "	  <sequence id=\"DTL2\"/>\n"
						+ "	  <sequence id=\"DTL3\"/>\n"
						+ "	  <sequence id=\"DTL4\"/>\n"
						+ "	  <sequence id=\"DTL5\"/>\n"
						+ "	  <sequence id=\"DTL6\"/>\n"
						+ "	  <sequence id=\"CCL1\"/>\n"
						+ "	  <sequence id=\"CCL2\"/>\n"
						+ "	  <sequence id=\"CCL3\"/>\n"
						+ "	  <sequence id=\"CCL4\"/>\n"
						+ "	  <sequence id=\"SCLMed\"/>\n"
						+ "	  <sequence id=\"SCLHigh\"/>\n"
						+ "	  <sequence id=\"HEBT1\"/>\n"
						+ "	  <sequence id=\"LDmp\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"DTL\">\n"
						+ "	  <sequence id=\"DTL1\"/>\n"
						+ "	  <sequence id=\"DTL2\"/>\n"
						+ "	  <sequence id=\"DTL3\"/>\n"
						+ "	  <sequence id=\"DTL4\"/>\n"
						+ "	  <sequence id=\"DTL5\"/>\n"
						+ "	  <sequence id=\"DTL6\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"CCL\">\n"
						+ "	  <sequence id=\"CCL1\"/>\n"
						+ "	  <sequence id=\"CCL2\"/>\n"
						+ "	  <sequence id=\"CCL3\"/>\n"
						+ "	  <sequence id=\"CCL4\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"SCL\">\n"
						+ "	  <sequence id=\"SCLMed\"/>\n"
						+ "	  <sequence id=\"SCLHigh\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"HEBT\">\n"
						+ "	  <sequence id=\"HEBT1\"/>\n"
						+ "	  <sequence id=\"HEBT2\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"Ring\">\n"
						+ "	  <sequence id=\"Ring1\"/>\n"
						+ "	  <sequence id=\"Ring2\"/>\n"
						+ "	  <sequence id=\"Ring3\"/>\n"
						+ "	  <sequence id=\"Ring4\"/>\n"
						+ "	  <sequence id=\"Ring5\"/>\n" + "  </comboseq>\n"
						+ "  <comboseq id=\"RTBT\">\n"
						+ "	  <sequence id=\"RTBT1\"/>\n"
						+ "	  <sequence id=\"RTBT2\"/>\n" + "  </comboseq>\n";
			}
			buf0 = str.getBytes();
			fout.write(buf0);
			
			final PreparedStatement SEQUENCE_FETCH = conn.prepareStatement( "SELECT * FROM EPICS.DVC_SEQ_NM where SEQ_NM = ?" );

			// loop through all the sequences
			for (int k = 0; k < sequences.length; k++) {

				String tmpID = null;
				boolean seqAttTag = true;
				
				final String theSequence = sequences[k];
				SEQUENCE_FETCH.setString( 1, theSequence );
				final ResultSet rsetSeq = SEQUENCE_FETCH.executeQuery();

				// produce <sequence> and its attributes
				while (rsetSeq.next()) {
					str = "  <sequence id=\"" + sequences[k] + "\"" + " len=\"" + rsetSeq.getString("TOTAL_SEQ_LNGTH") + "\"";
					if (sequences[k].equals("DTL1")
							|| sequences[k].equals("DTL2")
							|| sequences[k].equals("DTL3")
							|| sequences[k].equals("DTL4")
							|| sequences[k].equals("DTL5")
							|| sequences[k].equals("DTL6"))
						// add DTL type to the <sequence>
						str = str.concat(" type=\"DTLTank\"");
					if (sequences[k].equals("CCL1")
							|| sequences[k].equals("CCL2")
							|| sequences[k].equals("CCL3")
							|| sequences[k].equals("CCL4"))
						// add DTL type to the <sequence>
						str = str.concat(" type=\"CCL\"");

					str = str.concat(">\n");

					// start sequence attributes
					str = str.concat("   <attributes>\n");
					seqAttTag = true;

					if (rsetSeq.getString("PREV_SEQ_NM") != "null") {
						if (rsetSeq.getString("ALT_SEQ_NM") == null)
							str = str.concat("      <sequence predecessors=\""
									+ rsetSeq.getString("PREV_SEQ_NM")
									+ "\"/>\n");
						else
							str = str.concat("      <sequence predecessors=\""
									+ rsetSeq.getString("PREV_SEQ_NM") + ","
									+ rsetSeq.getString("ALT_SEQ_NM")
									+ "\"/>\n");
					} else {
						str = str.concat("      <sequence predecessors=\"\"/>\n");
					}
				}

				System.out.println(sequences[k]);

				str = queryDTL_CCL_cavs(str, stmt, sequences, k);

				// insert a begin of sequence flag (as a virtual node)
				str = str + "    <node type=\"marker" + "\""
						+ " id=\"Begin_Of_" + sequences[k] + "\""
						+ " pos=\"0\" len=\"0\"/>\n";

				// get all the magnet nodes within this sequence
				/*
				 * stmt0 = conn .prepareStatement("SELECT EPICS.DVC.DVC_ID, " +
				 * "EPICS.DVC.SUBSYS_ID, " + "EPICS.DVC.DVC_TYPE_ID, " +
				 * "EPICS.DVC.PARENT_DVC_ID, " +
				 * "EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC, " +
				 * "EPICS.MAG_DVC.POLARITY, " + "EPICS.MAG_DVC.PM_QUAD_STRENGTH, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_X, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Y, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Z, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PHI, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PSI, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_THETA, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DIST_FROM_STRT, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.PHYS_LNGTH, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND, " +
				 * "EPICS.BEAM_LINE_DVC_LOC.SEQ_NM, " + "MAGMDL.MAG_LNGTH, " +
				 * "MAGMDL.GAP, " + "MAGMDL.DIPOLE_BEND_ANGLE, " +
				 * "MAGMDL.DIPOLE_ENTR_ROTATION_ANGLE, " +
				 * "MAGMDL.DIPOLE_EXIT_ROTATION_ANGLE, " + "MAGMDL.PATH_LNGTH, " +
				 * "MAGMDL.DIPOLE_QUAD_TERM, " + "EPICS.DVC_SETTING.SETTING_ID, " +
				 * "EPICS.DVC_SETTING.SETTING_VALUE " + "FROM EPICS.DVC, " +
				 * "EPICS.BEAM_LINE_DVC_LOC, " + "EPICS.FUNC_DVC_GRP_ITEM, " +
				 * "EPICS.MAG_DVC, " + "EPICS.DSGN_DVC_MNFCTR_MDL_ASGN, " +
				 * "EPICS.DVC_SETTING, " + "EQUIP.MAG_MNFCTR_MDL MAGMDL " +
				 * "where (EPICS.DVC.DVC_TYPE_ID IN " + "( 'QH', 'QV', 'PMQH',
				 * 'PMQV', 'DCH', 'DCV', 'DH', 'DV', " + "'QTH', 'QTV',
				 * 'InjSptm', 'SH', 'SV', 'SSH', 'SSV' )) " + "and
				 * EPICS.BEAM_LINE_DVC_LOC.SEQ_NM = '" + sequences[k] + "' " +
				 * "and EPICS.DVC.Dvc_id = EPICS.BEAM_LINE_DVC_LOC.Dvc_id " +
				 * "and EPICS.DVC.Dvc_id = EPICS.MAG_DVC.Dvc_id " + "and
				 * EPICS.DVC.Dvc_id = EPICS.DVC_SETTING.Dvc_id(+) " + "and
				 * EPICS.DVC.Dvc_id = EPICS.DSGN_DVC_MNFCTR_MDL_ASGN.Dvc_id " +
				 * "and EPICS.DVC.Dvc_id = EPICS.FUNC_DVC_GRP_ITEM.Dvc_id(+) " +
				 * "and MAGMDL.MDL_NBR = EPICS.DSGN_DVC_MNFCTR_MDL_ASGN.MDL_NBR " +
				 * "and MAGMDL.MNFCTR_ID =
				 * EPICS.DSGN_DVC_MNFCTR_MDL_ASGN.MNFCTR_ID " + "order by " +
				 * "EPICS.BEAM_LINE_DVC_LOC.Dist_From_Strt, " + " DECODE(
				 * EPICS.DVC.dvc_type_id," + " 'QH',1," + " 'QV',1," + " 2)");
				 */
				stmt0 = conn.prepareStatement("SELECT EPICS.DVC.DVC_ID, "
								+ "EPICS.DVC.SUBSYS_ID, "
								+ "EPICS.DVC.DVC_TYPE_ID, "
								+ "EPICS.DVC.PARENT_DVC_ID, "
								+ "EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC, "
								+ "EPICS.FUNC_DVC_GRP.FUNC_DVC_GRP_NM, "
								+ "EPICS.MAG_DVC.POLARITY, "
								+ "EPICS.MAG_DVC.PM_QUAD_STRENGTH, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_X, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Y, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Z, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PHI, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PSI, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_THETA, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DIST_FROM_STRT, "
								+ "EPICS.BEAM_LINE_DVC_LOC.PHYS_LNGTH, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND, "
								+ "EPICS.BEAM_LINE_DVC_LOC.SEQ_NM, "
								+ "MAGMDL.MAG_LNGTH, "
								+ "MAGMDL.GAP, "
								+ "MAGMDL.DIPOLE_BEND_ANGLE, "
								+ "MAGMDL.DIPOLE_ENTR_ROTATION_ANGLE, "
								+ "MAGMDL.DIPOLE_EXIT_ROTATION_ANGLE, "
								+ "MAGMDL.PATH_LNGTH, "
								+ "EPICS.MAG_DVC.DIPOLE_QUAD_TERM, "
								+ "EPICS.DVC_SETTING.SETTING_ID, "
								+ "EPICS.DVC_SETTING.SETTING_VALUE "
								+ "FROM EPICS.DVC, "
								+ "EPICS.BEAM_LINE_DVC_LOC, "
								+ "EPICS.FUNC_DVC_GRP_ITEM, "
								+ "EPICS.FUNC_DVC_GRP, "
								+ "EPICS.MAG_DVC, "
								+ "EQUIP.EQUIP, "
								+ "EQUIP.EQUIP_DVC_ASGN, "
								+ "EPICS.DVC_SETTING, "
								+ "EQUIP.MAG_MNFCTR_MDL MAGMDL "
								+ "where (EPICS.DVC.DVC_TYPE_ID IN "
								+ "( 'QH', 'QV', 'PMQH', 'PMQV', 'DCH', 'DCV', 'DH', 'DV', "
								+ "'QTH', 'QTV', 'QSC', 'EKick', 'InjSptm', 'ExSptm', 'Sptm', 'SH', 'SV', 'SSH', 'SSV' )) "
								+ "and EPICS.BEAM_LINE_DVC_LOC.SEQ_NM = '"
								+ sequences[k]
								+ "' "
								+ "and EPICS.DVC.Dvc_id = EPICS.BEAM_LINE_DVC_LOC.Dvc_id "
								+ "and EPICS.DVC.Dvc_id = EPICS.MAG_DVC.Dvc_id "
								+ "and EPICS.DVC.Dvc_id = EPICS.DVC_SETTING.Dvc_id(+) "
								+ "and EPICS.DVC.Dvc_id = EPICS.FUNC_DVC_GRP_ITEM.Dvc_id(+) "
								+ "and EPICS.FUNC_DVC_GRP_ITEM.FUNC_DVC_GRP_ID = EPICS.FUNC_DVC_GRP.FUNC_DVC_GRP_ID(+) "
								+ "and EPICS.DVC.Dvc_id = EQUIP.EQUIP_DVC_ASGN.Dvc_id(+) "
								+ "and EQUIP.EQUIP.Equip_id = EQUIP.EQUIP_DVC_ASGN.Equip_id "
								+ "and MAGMDL.MDL_NBR = EQUIP.EQUIP.MDL_NBR "
								+ "order by "
								+ "EPICS.BEAM_LINE_DVC_LOC.Dist_From_Strt, "
								+ "DECODE( EPICS.DVC.dvc_type_id, 'QH',1, 'QV',1, 'QTH',1, 'QTV',1, 2), EPICS.DVC.DVC_ID");

				rset = stmt0.executeQuery();

				// generating the nodes
				int tmpCounter = 0;
				boolean chSuiteTag = true;

				// Iterate through the result and print the specified tag name
				while (rset.next()) {
					final String deviceID = rset.getString( "DVC_ID" );
					final String deviceType = rset.getString( "DVC_TYPE_ID" );
					System.out.println(deviceID);
					// check if the previous device has a closed </node>
					if ( !(deviceID.equals(tmpID)) && !(tmpID == null) ) {
						if (chSuiteTag) {
							str = str.concat("       </channelsuite>\n");
						}
						str = str.concat("    </node>\n");
					}

					if (!(deviceID.equals(tmpID))) {
						final boolean ringLike = sequences[k].startsWith("Ring") || sequences[k].startsWith("RTBT") || sequences[k].equals("IDmp+") || sequences[k].equals("EDmp");
						if ( deviceType.equals("InjSptm") || deviceType.equals( "ExSptm" ) || deviceType.equals("Sptm") ) {
							str = str.concat("    <node type=\"DH" + "\" id=\"" + deviceID + "\"" + " pos=\"" + rset.getString("DIST_FROM_STRT") + "\""
									+ " len=\"" + getNumericString( rset.getString("PHYS_LNGTH") )
									+ "\"");
							if (rset.getString("DSGN_USAGE_IND").equals("Y"))
								str = str.concat(" status=\"true\">\n");
							else
								str = str.concat(" status=\"false\">\n");
						}
						// for Ring QTH and QTV, we replace the device type with
						// "QH" and "QV"
						else if ( deviceType.equals("QTH") && ringLike ) {
							str = str.concat("    <node type=\"QH" + "\" id=\"" + deviceID + "\" pos=\""
									+ rset.getString("DIST_FROM_STRT") + "\" len=\"" + getNumericString( rset.getString("PHYS_LNGTH") ) + "\"");
							if (rset.getString("DSGN_USAGE_IND").equals("Y"))
								str = str.concat(" status=\"true\">\n");
							else
								str = str.concat(" status=\"" + "false\">\n");

						} else if (rset.getString("DVC_TYPE_ID").equals("QTV")
								&& (sequences[k].equals("Ring1") || sequences[k].equals("Ring2") || sequences[k].equals("Ring3")
										|| sequences[k].equals("Ring4") || sequences[k].equals("Ring5") || sequences[k].equals("RTBT1") || sequences[k].equals("RTBT2"))) {
							str = str.concat("    <node type=\"QV" + "\""
									+ " id=\"" + rset.getString("DVC_ID")
									+ "\"" + " pos=\""
									+ rset.getString("DIST_FROM_STRT") + "\""
									+ " len=\"" + getNumericString( rset.getString("PHYS_LNGTH") )
									+ "\"");
							if (rset.getString("DSGN_USAGE_IND").equals("Y"))
								str = str.concat(" status=\"true\">\n");
							else
								str = str.concat(" status=\"false\">\n");
						}

						else {
							str = str.concat("    <node type=\"" + deviceType + "\"" + " id=\"" + deviceID + "\" pos=\"" + rset.getString("DIST_FROM_STRT") + "\""
									+ " len=\"" + getNumericString( rset.getString("PHYS_LNGTH") ) + "\"");
							if (rset.getString("DSGN_USAGE_IND").equals("Y"))
								str = str.concat(" status=\"true\">\n");
							else
								str = str.concat(" status=\"false\">\n");
						}
						str = str.concat("       <attributes>\n");

						// converting the polarity 'A', 'B' to '1', '-1' we need to handle the proton part of the machine and the H- part of the machine differently
						if (rset.getString("POLARITY").equals("A")) {
							if ( ringLike && ( deviceType.equals("DCH") || deviceType.equals("DCV") || deviceType.equals("EKick") ) )
								str = str.concat("          <magnet len=\"" + getNumericString( rset.getString("MAG_LNGTH") ) + "\" polarity=\"+1\"");
							else
								str = str.concat("          <magnet len=\"" + getNumericString( rset.getString("MAG_LNGTH") ) + "\" polarity=\"-1\"");
						} 
						else if (rset.getString("POLARITY").equals("B")) {
							if ( ringLike && ( deviceType.equals("DCH") || deviceType.equals("DCV") || deviceType.equals("EKick") ) )
								str = str.concat("          <magnet len=\"" + getNumericString( rset.getString("MAG_LNGTH") ) + "\" polarity=\"-1\"");
							else
								str = str.concat("          <magnet len=\"" + getNumericString( rset.getString("MAG_LNGTH") ) + "\" polarity=\"+1\"");
						} 
						else
							str = str.concat("          <magnet len=\"" + getNumericString( rset.getString("MAG_LNGTH") ) + "\" polarity=\"0\"");

						if ( sequences[k].startsWith("DTL") ) {
							if ( !deviceType.equals("DCH") && !deviceType.equals("DCV") ) {
								// add field sign for PMQ, will be removed once the polarity key is populated
								if ( deviceType.equals("PMQH") ) {
									str = str.concat(" dfltMagFld=\"" + getNumericString( rset.getString("PM_QUAD_STRENGTH") ) + "\"/>\n");
								} 
								else if ( deviceType.equals("PMQV") ) {
									str = str.concat(" dfltMagFld=\"" + getNumericString( rset.getString("PM_QUAD_STRENGTH") ) + "\"/>\n");
								}
							} else {
								str = str.concat(" dfltMagFld=\"" + getNumericString( rset.getString("SETTING_VALUE") ) + "\"/>\n");
							}
						} else {
							// for bending dipoles, we need to add extra attributes
							if (rset.getString("DVC_TYPE_ID").equals("DH")
									|| rset.getString("DVC_TYPE_ID").equals("DV")
									|| rset.getString("DVC_TYPE_ID").equals("InjSptm")
									|| rset.getString("DVC_TYPE_ID").equals("Sptm")
									|| rset.getString("DVC_TYPE_ID").equals("ExSptm")) {
								str = str.concat(" pathLength=\"" + rset.getString("PATH_LNGTH") + "\" dipoleQuadComponent=\""+ getNumericString( rset.getString("DIPOLE_QUAD_TERM") ) + "\"");
								// we need to reverse bend angle, etc. for Chicane in HEBT2 and IDmp-
								if ( (sequences[k].equals("HEBT2") && deviceID.equals( "Ring_Mag:DH_A11" ) ) || (sequences[k].equals("IDmp-") && deviceID.equals("Ring_Mag:DH_A12")) )
									str = str.concat(" bendAngle=\""
										+ -1.0 * Double.parseDouble(rset.getString("DIPOLE_BEND_ANGLE"))
										+ "\""
										+ " dipoleEntrRotAngle=\""
										+ -1.0 * Double.parseDouble(rset.getString("DIPOLE_ENTR_ROTATION_ANGLE"))
										+ "\""
										+ " dipoleExitRotAngle=\""
										+ -1.0 * Double.parseDouble(rset.getString("DIPOLE_EXIT_ROTATION_ANGLE"))
										+ "\"");
								// other than the 2 middle Chicane dipoles
								else
									str = str.concat(" bendAngle=\"" + rset.getString("DIPOLE_BEND_ANGLE")
										+ "\""
										+ " dipoleEntrRotAngle=\""
										+ getNumericString( rset.getString("DIPOLE_ENTR_ROTATION_ANGLE") )
										+ "\""
										+ " dipoleExitRotAngle=\""
										+ getNumericString( rset.getString("DIPOLE_EXIT_ROTATION_ANGLE") )
										+ "\"");
							}
							str = str.concat(" dfltMagFld=\"" + getNumericString( rset.getString("SETTING_VALUE") ) + "\"/>\n");
						}

						// we use "zero" error for alignment for now
						str = str
								.concat("          <align x=\""
										+ ( Double.parseDouble( rset.getString("DSGN_GLBL_COORD_X") ) - Double.parseDouble( rset.getString("DSGN_GLBL_COORD_X") ) )
										+ "\" y=\""
										+ ( Double.parseDouble( rset.getString("DSGN_GLBL_COORD_Y") ) - Double.parseDouble( rset.getString("DSGN_GLBL_COORD_Y") ) )
										+ "\" z=\""
										+ (Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Z")) - Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Z")))
										+ "\""
										+ " pitch=\"" + rset.getString("DSGN_GLBL_COORD_PHI")
										+ "\""
										+ " yaw=\"" + rset.getString("DSGN_GLBL_COORD_PSI")
										+ "\""
										+ " roll=\"" + rset.getString("DSGN_GLBL_COORD_THETA")
										+ "\"/>\n");
						// for magnet aperture
						str = str.concat("          <aperture shape=\"0\" x=\"" + getNumericString( rset.getString("GAP") ) + "\"/>\n       </attributes>\n");
						// dealing with the <channelsuite> tag for PMQs (no channelsuite)
						if ( sequences[k].startsWith("DTL") ) {
							// for non-PMQs
							if (!(rset.getString("DVC_TYPE_ID").equals("PMQH")) && !(rset.getString("DVC_TYPE_ID").equals("PMQV"))) {
								// for power supply
								str = str.concat("       <ps main=\"" + rset.getString("PARENT_DVC_ID") + "\"");
								// has trim winding or shunt
								final String relatedDevice = rset.getString( "RELATED_DVC" );
								final String relatedDeviceRole = rset.getString( "FUNC_DVC_GRP_NM" );
								if ( relatedDevice != null && relatedDeviceRole != null && ( relatedDeviceRole.equals("shunt") || relatedDeviceRole.equals("trim") ) ) {
									str = str.concat(" trim=\"" + relatedDevice + "\"");
								}
								str = str.concat("/>\n");
								// for read back PV
								str += "       <channelsuite name=\"magnetsuite\">\n          <channel handle=\"fieldRB\"" + " signal=\"" + deviceID + ":B\" settable=\"false\"/>\n";
								chSuiteTag = true;
							} else {
								chSuiteTag = false;
							}
							// for non-DTL sequences
						} 
						else {
							// for non-PMQ power supply
							str = str.concat("       <ps main=\"" + rset.getString("PARENT_DVC_ID") + "\"");
							// has trim winding
							final String relatedDevice = rset.getString( "RELATED_DVC" );
							final String relatedDeviceRole = rset.getString( "FUNC_DVC_GRP_NM" );
							if ( relatedDevice != null && relatedDeviceRole != null && ( relatedDeviceRole.equals("shunt") || relatedDeviceRole.equals("trim") ) ) {
								str = str.concat(" trim=\"" + relatedDevice + "\"");
							}

							str = str.concat("/>\n");
							// for read back PV
							str = str.concat("       <channelsuite name=\"" + "magnetsuite\">\n");
							str = str.concat("          <channel handle=\"" + "fieldRB" + "\"");
							// inside the following "if" bracket is for
							// temporary ring B readback
							if ( ringLike ) {
								if ( deviceID.indexOf("QTH") > 0) {
									str = str.concat(" signal=\"" + deviceID.replaceAll("QTH", "QH") + ":B" + "\"" + " settable=\"false\"/>\n");
								} 
								else if ( deviceID.indexOf( "QTV" ) > 0 ) {
									str = str.concat(" signal=\"" + deviceID.replaceAll("QTV", "QV") + ":B" + "\"" + " settable=\"false\"/>\n");
								} 
								else
									str = str.concat(" signal=\"" + deviceID + ":B" + "\"" + " settable=\"false\"/>\n");
							} 
							else
								str = str.concat(" signal=\"" + rset.getString("DVC_ID") + ":B" + "\"" + " settable=\"false\"/>\n");
						}
						tmpID = deviceID;
					}

					tmpCounter++;
				}

				rset.close();
				stmt0.close();

				str = queryRFQ(str, stmt, sequences, k);

				final PreparedStatement SIGNAL_FETCH = conn.prepareStatement( "select sgnl_id, sgnl_nm from epics.sgnl_rec where dvc_id = ? order by sgnl_nm" );
				
				// put diagnostic devices, vacuum windows, laser strippers and foil (they all act like markers) here
				final String diagDeviceQuery = "SELECT EPICS.DVC.DVC_ID, "
								+ "EPICS.DVC.SUBSYS_ID, "
								+ "EPICS.DVC.ACT_DVC_IND, "
								+ "EPICS.DVC.DVC_TYPE_ID, "
								+ "EPICS.DVC_DVC_TYPE_SFTW_ASSC.SFTW_ID, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_X, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Y, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Z, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PHI, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PSI, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_THETA, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DIST_FROM_STRT, "
								+ "EPICS.BEAM_LINE_DVC_LOC.PHYS_LNGTH, "
								+ "EPICS.BEAM_LINE_DVC_LOC.SEQ_NM, "
								+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND, "
								+ "EPICS.BPM_DVC.FREQ, "
								+ "EPICS.BPM_DVC.ELCTD_LNGTH, "
								+ "EPICS.BPM_DVC.ORIENT_IND "
								+ "FROM EPICS.DVC, "
								+ "EPICS.DVC_DVC_TYPE_SFTW_ASSC, "
								+ "EPICS.BEAM_LINE_DVC_LOC, "
								+ "EPICS.BPM_DVC "
								+ "where (EPICS.DVC.DVC_TYPE_ID IN "
								+ "( 'BPM','BCM', 'BLM', 'BSM', 'ChMPS', 'EMS', 'LW', 'WS', 'ND', 'Harp', 'Foil', 'LStrp', 'VIW', 'Tgt' )) "
								+ "and EPICS.DVC.ACT_DVC_IND = 'Y' "
								+ "and EPICS.BEAM_LINE_DVC_LOC.SEQ_NM = '" + sequences[k] + "' "
								+ "and EPICS.DVC.Dvc_id = EPICS.BEAM_LINE_DVC_LOC.Dvc_id(+) "
								+ "and EPICS.DVC.Dvc_id = EPICS.BPM_DVC.Dvc_id(+) "
								+ "and EPICS.DVC.Dvc_id = EPICS.DVC_DVC_TYPE_SFTW_ASSC.DVC_ID(+) "
								+ "order by "
								+ "EPICS.BEAM_LINE_DVC_LOC.Dist_From_Strt, EPICS.DVC.DVC_ID";
				
				System.out.println( "Diag Device Query: " + diagDeviceQuery );
				rset = stmt.executeQuery( diagDeviceQuery );
				while ( rset.next() ) {
					final String deviceID = rset.getString( "DVC_ID" );
					final String softType = rset.getString( "SFTW_ID" );
					final String deviceType = rset.getString( "DVC_TYPE_ID" );
					System.out.println( deviceID );
					if ( softType != null ) {
						System.out.println( "************************ Soft Type:  >>>" + softType + "<<< ****************************" );
					}

					// check if the previous device has a closed </node>
					if (!(deviceID.equals(tmpID)) && !(tmpID == null)) {
						if (chSuiteTag) {
							str = str.concat("       </channelsuite>\n");
						}
						str = str.concat("    </node>\n");
					}

					if ( !deviceID.equals( tmpID ) ) {
						final String devicePosition = rset.getString( "DIST_FROM_STRT" );
						final String deviceLength = getNumericString( rset.getString("PHYS_LNGTH") );
						final boolean deviceStatus = rset.getString("DSGN_USAGE_IND").equals("Y");
						final boolean ringLike = sequences[k].substring(0, 4).equals("Ring") || sequences[k].substring(0, 4).equals("RTBT") || sequences[k].equals("IDmp+") || sequences[k].equals("EDmp");
						
						if ( ringLike && deviceType.equals("BPM") ) {
							str += "    <node type=\"R" + deviceType + "\" id=\"" + deviceID + "\"" + " pos=\"" + devicePosition + "\"" + " len=\"" + deviceLength + "\"";
							str += " status=\"" + deviceStatus + "\">\n";
						} 
						else {
							str += "    <node type=\"" + deviceType + "\"";
							if ( softType != null ) {
								str += " softType=\"" + softType + "\"";
							}
							str += " id=\"" + deviceID + "\" pos=\"" + devicePosition + "\"";
							str += " len=\"" + deviceLength + "\" status=\"" + deviceStatus + "\">\n";
						}
						str = str.concat("       <attributes>\n");
						// we use "zero" error for alignment for now
						str = str.concat("          <align x=\"" + (Double.parseDouble(rset.getString("DSGN_GLBL_COORD_X")) - Double.parseDouble(rset.getString("DSGN_GLBL_COORD_X"))) + "\""
							+ " y=\"" + (Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Y")) - Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Y"))) + "\""
							+ " z=\"" + (Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Z")) - Double.parseDouble(rset.getString("DSGN_GLBL_COORD_Z"))) + "\""
							+ " pitch=\"" + rset.getString("DSGN_GLBL_COORD_PHI") + "\""
							+ " yaw=\"" + rset.getString("DSGN_GLBL_COORD_PSI") + "\""
							+ " roll=\"" + rset.getString("DSGN_GLBL_COORD_THETA") + "\"/>\n");
						if ( deviceType.equals("BPM") ) {
							str = str.concat("          <bpm frequency=\"" + Double.parseDouble(rset.getString("FREQ")) + "\"\n"
											+ "               length=\"" + Double.parseDouble(rset.getString("ELCTD_LNGTH")) + "\"\n");
							if (rset.getString("ORIENT_IND").equals("U"))
								str = str.concat("               orientation=\"" + "-1\"/>\n");
							else if (rset.getString("ORIENT_IND").equals("D"))
								str = str.concat("               orientation=\"" + "1\"/>\n");
							else
								str = str.concat("/>\n");
						}
						str = str + "       </attributes>\n";

						if ( deviceType.equals("BPM") ) {
							str = str.concat("       <channelsuite name=\"bpmsuite\">\n"
									+ "          <channel handle=\"" + "xAvg" + "\"" + " signal=\"" + deviceID + ":xAvg" + "\" settable=\"false\"/>\n"
									+ "          <channel handle=\"" + "yAvg" + "\"" + " signal=\"" + deviceID + ":yAvg" + "\" settable=\"false\"/>\n"
									+ "          <channel handle=\"xTBT\"" + " signal=\"");
							str += deviceID + ( ringLike ? ":xTBT" : ":hposA" );
							str = str.concat("\" settable=\"false\"/>\n          <channel handle=\"yTBT\" signal=\"");
							str += deviceID + ( ringLike ? ":yTBT" : ":vposA" );
							str = str.concat("\"" + " settable=\"false\"/>\n");

							if ( ringLike ) {
								str = str.concat("          <channel handle=\"Stage1Len\" signal=\"" + deviceID + ":Stage1Turns\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage1Gain\" signal=\"" + deviceID + ":Stage1Gain\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage1Method\" signal=\"" + deviceID + ":Analysis_Type1\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage2Len\" signal=\"" + deviceID + ":Stage2Turns\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage2Gain\" signal=\"" + deviceID + ":Stage2Gain\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage2Method\" signal=\"" + deviceID + ":Analysis_Type2\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage3Len\" signal=\"" + deviceID + ":Stage3Turns\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage3Gain\" signal=\"" + deviceID + ":Stage3Gain\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage3Method\" signal=\"" + deviceID + ":Analysis_Type3\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage4Len\" signal=\"" + deviceID + ":Stage4Turns\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage4Gain\" signal=\"" + deviceID + ":Stage4Gain\" settable=\"true\"/>\n"
										+ "          <channel handle=\"Stage4Method\" signal=\"" + deviceID + ":Analysis_Type4\" settable=\"true\"/>\n"
										+ "          <channel handle=\"ampTBT\" signal=\"" + deviceID + ":ampTBT\" settable=\"false\"/>\n");
								str = str.concat("          <channel handle=\"Stage1LenRB\" signal=\""+ deviceID + ":Stage1Turns_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage1GainRB\" signal=\"" + deviceID + ":Stage1Gain_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage1MethodRB\" signal=\"" + deviceID + ":Analysis_Type1_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage2LenRB\" signal=\"" + deviceID + ":Stage2Turns_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage2GainRB\" signal=\"" + deviceID + ":Stage2Gain_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage2MethodRB\" signal=\"" + deviceID + ":Analysis_Type2_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage3LenRB\" signal=\"" + deviceID + ":Stage3Turns_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage3GainRB\" signal=\"" + deviceID + ":Stage3Gain_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage3MethodRB\" signal=\"" + deviceID + ":Analysis_Type3_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage4LenRB\" signal=\"" + deviceID + ":Stage4Turns_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage4GainRB\" signal=\"" + deviceID + ":Stage4Gain_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"Stage4MethodRB\" signal=\"" + deviceID + ":Analysis_Type4_RB\" settable=\"false\"/>\n"
										+ "          <channel handle=\"amplitudeAvg\" signal=\"" + deviceID + ":ampAvg\" settable=\"false\"/>\n");
							} 
							else
								str = str.concat("          <channel handle=\"phaseAvg\" signal=\"" + deviceID + ":phaseAvg\" settable=\"false\"/>\n"
										+ "          <channel handle=\"amplitudeAvg\" signal=\"" + deviceID + ":amplitudeAvg\" settable=\"false\"/>\n"
										+ "          <channel handle=\"ampTBT\" signal=\"" + deviceID + ":beamIA\" settable=\"false\"/>\n"
										+ "          <channel handle=\"phaseTBT\" signal=\"" + deviceID + ":beamPA\" settable=\"false\"/>\n");

							chSuiteTag = true;
						} 
						else if ( deviceType.equals("BCM") ) {
							str = str.concat("       <channelsuite name=\"bcmsuite\">\n" 
									+ "          <channel handle=\"Particles\" signal=\"" + deviceID + ":Particles\" settable=\"false\"/>\n"
									+ "          <channel handle=\"currentTBT\" signal=\"" + deviceID + ":currentTBT\" settable=\"false\"/>\n"
									+ "          <channel handle=\"tDelay\" signal=\"" + deviceID + ":tAvgDelay\" settable=\"false\"/>\n"
									+ "          <channel handle=\"DisplayLength\" signal=\"" + deviceID + ":tLength\" settable=\"false\"/>\n"
									+ "          <channel handle=\"currentAvg\" signal=\"" + deviceID + ":currentAvg\" settable=\"false\"/>\n"
									+ "          <channel handle=\"currentMax\" signal=\"" + deviceID + ":currentMax\" settable=\"false\"/>\n");

							chSuiteTag = true;
						} 
						else if ( deviceType.equals( "BLM" ) ) {
							final Map<String,String> signalTable = fetchSignals( SIGNAL_FETCH, deviceID );
							str = str + "       <channelsuite name=\"blmsuite\">\n";
							str += channelEntry( "lossAvg", "Slow1PulseBeamOnTotalLoss", signalTable, false );
							str += channelEntry( "lossTBT", "Fast1PulseBeamOnLoss", signalTable, false );
							str += channelEntry( "lossInt", "Slow60PulsesTotalLoss", signalTable, false );							
							chSuiteTag = true;
						} 
						else if ( deviceType.equals( "ChMPS" ) ) {
							final Map<String,String> signalTable = fetchSignals( SIGNAL_FETCH, deviceID );
							str = str + "       <channelsuite name=\"chumpssuite\">\n";
							str += channelEntry( "BeamOn", "BeamOn", signalTable, false );
							str += channelEntry( "BeamInGap", "BeamInGap", signalTable, false );
							str += channelEntry( "waveform", "wf", signalTable, false );
							chSuiteTag = true;
						} 
						else if ( rset.getString( "DVC_TYPE_ID" ).equals( "LW" ) ) {
							chSuiteTag = false;
						} 
						else if ( deviceType.equals( "WS" ) ) {
							final Map<String,String> signalTable = fetchSignals( SIGNAL_FETCH, deviceID );
							str += "       <channelsuite name=\"wssuite\">\n";
							
							if ( softType != null && softType.toLowerCase().contains( "version 2.0.0" ) ) {
								str += channelEntry( "Command", "Command", signalTable, true );
								str += channelEntry( "CommandResult", "CommandResult", signalTable, false );
								str += channelEntry( "CommandStatus", "Status", signalTable, false );
								
								str += channelEntry( "StatCollisionRb", "Collision", signalTable, false );
								str += channelEntry( "StatFwdLimitRb", "Forward", signalTable, false );
								str += channelEntry( "StatRevLimitRb", "Reverse", signalTable, false );
								str += channelEntry( "StatScanOutOfRngRb", "Scan_OOR", signalTable, false );
								str += channelEntry( "StatHorWireDmgRb", "Hor_Cont", signalTable, false );
								str += channelEntry( "StatVerWireDmgRb", "Ver_Cont", signalTable, false );
								str += channelEntry( "StatDiaWireDmgRb", "Diag_Cont", signalTable, false );
								str += channelEntry( "StatAlarmSgnlRb", "SignalAlarm", signalTable, false);
								str += channelEntry( "StatAlarmTmgRb", "TimingAlarm", signalTable, false);
								str += channelEntry( "StatMps0Rb", "MPS0", signalTable, false );
								str += channelEntry( "StatMps1Rb", "MPS1", signalTable, false );
								str += channelEntry( "StatWirePosRb", "Position", signalTable, false );
								str += channelEntry( "StatPowerSupplyRb", "Power", signalTable, false );
								str += channelEntry( "StatWireSpeedRb", "Speed", signalTable, false );
								str += channelEntry( "StatScanErrorRb", "ScanErr", signalTable, false );
								str += channelEntry( "StatMotionRb", "MotionStat", signalTable, false );
								str += channelEntry( "StatScanSeqIdRb", "Sequence", signalTable, false );
								str += channelEntry( "StatScanStrokeRb", "Stroke", signalTable, false);
								
								str += channelEntry( "ScanMotionRb", "MotionStat", signalTable, false );
								str += channelEntry( "ScanPositionRb", "Position", signalTable, false );
								str += channelEntry( "ScanSpeedRb", "Speed", signalTable, false );
								str += channelEntry( "ScanRevLimitRb", "Reverse", signalTable, false );
								str += channelEntry( "ScanForLimitRb", "Forward", signalTable, false );
								str += channelEntry( "ScanSeqIdRb", "Sequence", signalTable, false );
								str += channelEntry( "ScanErrorRb", "ScanErr", signalTable, false );
								str += channelEntry( "ScanCfgInitPosRb", "Scan_InitialMove_rb", signalTable, false );
								str += channelEntry( "ScanCfgInitPosSet", "Scan_InitialMove_set", signalTable, true );
								str += channelEntry( "ScanCfgStepCntRb", "Scan_Steps_rb", signalTable, false );
								str += channelEntry( "ScanCfgStepCntSet", "Scan_Steps_set", signalTable, true );
								str += channelEntry( "ScanCfgStepLngRb", "Scan_StepSize_rb", signalTable, false );
								str += channelEntry( "ScanCfgStepLngSet", "Scan_StepSize_set", signalTable, true );
								str += channelEntry( "ScanCfgStepPulsesRb", "Scan_Traces/step_rb", signalTable, false );
								str += channelEntry( "ScanCfgStepPulsesSet", "Scan_Traces/step_set", signalTable, true );
                                str += channelEntry( "ScanCfgStrokeLngRb", "Stroke", signalTable, false);
                                str += channelEntry( "ScanCfgScanLngRb",   "Scan_Length", signalTable, false);
                                str += channelEntry( "ScanCfgScanOutOfRngRb", "Scan_OOR", signalTable, false);
								
								str += channelEntry( "ActrCfgInitSpeedRb", "Motion_Speed_Init_rb", signalTable, false );
								str += channelEntry( "ActrCfgInitSpeedSet", "Motion_Speed_Init_set", signalTable, true );
								str += channelEntry( "ActrCfgInitAccelRb", "Motion_Accel_Init_rb", signalTable, false );
								str += channelEntry( "ActrCfgInitAccelSet", "Motion_Accel_Init_set", signalTable, true );
								str += channelEntry( "ActrCfgStepSpeedRb", "Motion_Speed_Step_rb", signalTable, false );
								str += channelEntry( "ActrCfgStepSpeedSet", "Motion_Speed_Step_set", signalTable, true );
								str += channelEntry( "ActrCfgStepAccelRb", "Motion_Accel_Step_rb", signalTable, false );
								str += channelEntry( "ActrCfgStepAccelSet", "Motion_Accel_Step_set", signalTable, true );
								str += channelEntry( "ActrCfgSearchSpeedRb", "Motion_Speed_Search_rb", signalTable, false );
								str += channelEntry( "ActrCfgSearchSpeedSet", "Motion_Speed_Search_set", signalTable, true );
								str += channelEntry( "ActrCfgSearchAccelRb", "Motion_Accel_Search_rb", signalTable, false );
								str += channelEntry( "ActrCfgSearchAccelSet", "Motion_Accel_Search_set", signalTable, true );
								str += channelEntry( "ActrCfgReturnSpeedRb", "Motion_Speed_Return_rb", signalTable, false );
								str += channelEntry( "ActrCfgReturnSpeedSet", "Motion_Speed_Return_set", signalTable, true );
								str += channelEntry( "ActrCfgReturnAccelRb", "Motion_Accel_Return_rb", signalTable, false );
								str += channelEntry( "ActrCfgReturnAccelSet", "Motion_Accel_Return_set", signalTable, true );
								str += channelEntry( "ActrCfgSearchTimeoutRb", "Motion_TMO_Search_rb", signalTable, false );
								str += channelEntry( "ActrCfgSearchTimeoutSet", "Motion_TMO_Search_set", signalTable, true );
								str += channelEntry( "ActrCfgStepTimeoutRb", "Motion_TMO_Step_rb", signalTable, false );
								str += channelEntry( "ActrCfgStepTimeoutSet", "Motion_TMO_Step_set", signalTable, true );
								
								str += channelEntry( "DaqCfgScanRateRb", "Acq_Scanrate_rb", signalTable, false );
								str += channelEntry( "DaqCfgScanRateSet", "Acq_Scanrate_set", signalTable, true );
								str += channelEntry( "DaqCfgGainRb", "Acq_Gain_rb", signalTable, false );
								str += channelEntry( "DaqCfgGainSet", "Acq_Gain_set", signalTable, true );
								str += channelEntry( "DaqCfgWindowRb", "Acq_Length_rb", signalTable, false );
								str += channelEntry( "DaqCfgWindowSet", "Acq_Length_set", signalTable, true );
								str += channelEntry( "DaqCfgTimeoutRb", "Acq_Time-out_rb", signalTable, false );
								str += channelEntry( "DaqCfgTimeoutSet", "Acq_Time-out_set", signalTable, true );
								
								str += channelEntry( "PrcgCfgInvertRb", "Analysis_Gain_rb", signalTable, false );
								str += channelEntry( "PrcgCfgInvertSet", "Analysis_Gain_set", signalTable, true );
								str += channelEntry( "PrcgCfgAvgBeginRb", "Analysis_Avg_Start_rb", signalTable, false );
								str += channelEntry( "PrcgCfgAvgBeginSet", "Analysis_Avg_Start_set", signalTable, true );
								str += channelEntry( "PrcgCfgAvgLengthRb", "Analysis_Avg_Len_rb", signalTable, false );
								str += channelEntry( "PrcgCfgAvgLengthSet", "Analysis_Avg_Len_set", signalTable, true );
								
								str += channelEntry( "TrgCfgDelayRb", "Delay00_Rb", signalTable, true );
								str += channelEntry( "TrgCfgDelaySet", "Delay00", signalTable, true );
								str += channelEntry( "TrgCfgTrigEventRb", "Event00_Rb", signalTable, true );
								str += channelEntry( "TrgCfgTrigEventSet", "Event00", signalTable, true );
								
								str += channelEntry( "DatDiaLivePtPositions", "Diag_point_pos", signalTable, false );
								str += channelEntry( "DatHorLivePtPositions", "Hor_point_pos", signalTable, false );
								str += channelEntry( "DatVerLivePtPositions", "Ver_point_pos", signalTable, false );
								str += channelEntry( "DatDiaLivePtSignal", "Diag_point_sig", signalTable, false );
								str += channelEntry( "DatHorLivePtSignal", "Hor_point_sig", signalTable, false );
								str += channelEntry( "DatVerLivePtSignal", "Ver_point_sig", signalTable, false );
								
                                str += channelEntry( "DatDiaLiveArrPositions", "Diag_live_pos", signalTable, false );
                                str += channelEntry( "DatHorLiveArrPositions", "Hor_live_pos", signalTable, false );
                                str += channelEntry( "DatVerLiveArrPositions", "Ver_live_pos", signalTable, false );
                                str += channelEntry( "DatDiaLiveArrSignal", "Diag_live_sig", signalTable, false );
                                str += channelEntry( "DatHorLiveArrSignal", "Hor_live_sig", signalTable, false );
                                str += channelEntry( "DatVerLiveArrSignal", "Ver_live_sig", signalTable, false );
                                
								str += channelEntry( "DatDiaRawPositions", "Diag_prof_pos", signalTable, false );
								str += channelEntry( "DatHorRawPositions", "Hor_prof_pos", signalTable, false );
								str += channelEntry( "DatVerRawPositions", "Ver_prof_pos", signalTable, false );
								str += channelEntry( "DatDiaRawSignal", "Diag_prof_sig", signalTable, false );
								str += channelEntry( "DatHorRawSignal", "Hor_prof_sig", signalTable, false );
								str += channelEntry( "DatVerRawSignal", "Ver_prof_sig", signalTable, false );
								
								str += channelEntry( "DatDiaFitPositions", "Diag_prof_pos", signalTable, false );
								str += channelEntry( "DatHorFitPositions", "Hor_prof_pos", signalTable, false );
								str += channelEntry( "DatVerFitPositions", "Ver_prof_pos", signalTable, false );
								str += channelEntry( "DatDiaFitSignal", "Diag_prof_fit", signalTable, false );
								str += channelEntry( "DatHorFitSignal", "Hor_prof_fit", signalTable, false );
								str += channelEntry( "DatVerFitSignal", "Ver_prof_fit", signalTable, false );

								str += channelEntry( "DatTraceTimeStep", "trace_dt", signalTable, false );
                                str += channelEntry( "DatDiaTracePositions", "trace_x", signalTable, false );
                                str += channelEntry( "DatHorTracePositions", "trace_x", signalTable, false );
                                str += channelEntry( "DatVerTracePositions", "trace_x", signalTable, false );
                                str += channelEntry( "DatDiaTraceSignal", "Diag_trace_raw", signalTable, false );
                                str += channelEntry( "DatHorTraceSignal", "Hor_trace_raw", signalTable, false );
                                str += channelEntry( "DatVerTraceSignal", "Ver_trace_raw", signalTable, false );
								
								str += channelEntry( "DatHorNoiseAvg", "Hor_noise_mean", signalTable, false);
                                str += channelEntry( "DatHorNoiseStd", "Hor_noise_std", signalTable, false);
                                str += channelEntry( "DatVerNoiseAvg", "Ver_noise_mean", signalTable, false);
                                str += channelEntry( "DatVerNoiseStd", "Ver_noise_std", signalTable, false);
                                str += channelEntry( "DatDiaNoiseAvg", "Diag_noise_mean", signalTable, false);
                                str += channelEntry( "DatDiaNoiseStd", "Diag_noise_std", signalTable, false);
								
								str += channelEntry( "SigHorStatAmp", "Hor_Amp_rms", signalTable, false );
								str += channelEntry( "SigVerStatAmp", "Ver_Amp_rms", signalTable, false );
								str += channelEntry( "SigDiaStatAmp", "Diag_Amp_rms", signalTable, false );
								str += channelEntry( "SigHorStatOffset", "Hor_Offset_rms", signalTable, false );
								str += channelEntry( "SigVerStatOffset", "Ver_Offset_rms", signalTable, false );
								str += channelEntry( "SigDiaStatOffset", "Diag_Offset_rms", signalTable, false );
								str += channelEntry( "SigHorStatArea", "Hor_Area_rms", signalTable, false );
								str += channelEntry( "SigVerStatArea", "Ver_Area_rms", signalTable, false );
								str += channelEntry( "SigDiaStatArea", "Diag_Area_rms", signalTable, false );
								str += channelEntry( "SigHorStatMean", "Hor_Mean_rms", signalTable, false );
								str += channelEntry( "SigVerStatMean", "Ver_Mean_rms", signalTable, false );
								str += channelEntry( "SigDiaStatMean", "Diag_Mean_rms", signalTable, false );
								str += channelEntry( "SigHorStatStd", "Hor_Sigma_rms", signalTable, false );
								str += channelEntry( "SigVerStatStd", "Ver_Sigma_rms", signalTable, false );
								str += channelEntry( "SigDiaStatStd", "Diag_Sigma_rms", signalTable, false );
								str += channelEntry( "SigHorGaussAmp", "Hor_Amp_gs", signalTable, false );
								str += channelEntry( "SigVerGaussAmp", "Ver_Amp_gs", signalTable, false );
								str += channelEntry( "SigDiaGaussAmp", "Diag_Amp_gs", signalTable, false );
								str += channelEntry( "SigHorGaussOffset", "Hor_Offset_gs", signalTable, false );
								str += channelEntry( "SigVerGaussOffset", "Ver_Offset_gs", signalTable, false );
								str += channelEntry( "SigDiaGaussOffset", "Diag_Offset_gs", signalTable, false );
								str += channelEntry( "SigHorGaussArea", "Hor_Area_gs", signalTable, false );
								str += channelEntry( "SigVerGaussArea", "Ver_Area_gs", signalTable, false );
								str += channelEntry( "SigDiaGaussArea", "Diag_Area_gs", signalTable, false );
								str += channelEntry( "SigHorGaussMean", "Hor_Mean_gs", signalTable, false );
								str += channelEntry( "SigVerGaussMean", "Ver_Mean_gs", signalTable, false );
								str += channelEntry( "SigDiaGaussMean", "Diag_Mean_gs", signalTable, false );
								str += channelEntry( "SigHorGaussStd", "Hor_Sigma_gs", signalTable, false );
								str += channelEntry( "SigVerGaussStd", "Ver_Sigma_gs", signalTable, false );
								str += channelEntry( "SigDiaGaussStd", "Diag_Sigma_gs", signalTable, false );
								str += channelEntry( "SigHorDblGaussAmp", "Hor_Amp_dgs", signalTable, false );
								str += channelEntry( "SigVerDblGaussAmp", "Ver_Amp_dgs", signalTable, false );
								str += channelEntry( "SigDiaDblGaussAmp", "Diag_Amp_dgs", signalTable, false );
								str += channelEntry( "SigHorDblGaussOffset", "Hor_Offset_dgs", signalTable, false );
								str += channelEntry( "SigVerDblGaussOffset", "Ver_Offset_dgs", signalTable, false );
								str += channelEntry( "SigDiaDblGaussOffset", "Diag_Offset_dgs", signalTable, false );
								str += channelEntry( "SigHorDblGaussArea", "Hor_Area_dgs", signalTable, false );
								str += channelEntry( "SigVerDblGaussArea", "Ver_Area_dgs", signalTable, false );
								str += channelEntry( "SigDiaDblGaussArea", "Diag_Area_dgs", signalTable, false );
								str += channelEntry( "SigHorDblGaussMean", "Hor_Mean_dgs", signalTable, false );
								str += channelEntry( "SigVerDblGaussMean", "Ver_Mean_dgs", signalTable, false );
								str += channelEntry( "SigDiaDblGaussMean", "Diag_Mean_dgs", signalTable, false );
								str += channelEntry( "SigHorDblGaussStd", "Hor_Sigma_dgs", signalTable, false );
								str += channelEntry( "SigVerDblGaussStd", "Ver_Sigma_dgs", signalTable, false );
								str += channelEntry( "SigDiaDblGaussStd", "Diag_Sigma_dgs", signalTable, false );
							}
							else {
								str += channelEntry( "position", "Pos", signalTable, false );
								str += channelEntry( "statusArray", "StatArray", signalTable, false );
								str += channelEntry( "abortScan", "AbortScan", signalTable, true );
								str += channelEntry( "vDataArray", "XRaw", signalTable, false );
								str += channelEntry( "dDataArray", "YRaw", signalTable, false );
								str += channelEntry( "hDataArray", "ZRaw", signalTable, false );
								str += channelEntry( "positionArray", "PosArray", signalTable, false );
								str += channelEntry( "beginScan", "BeginScan", signalTable, true );
								str += channelEntry( "scanLength", "ScanLen", signalTable, false );
								str += channelEntry(  "vSigmaF", "XSigmaF", signalTable, false );
								str += channelEntry(  "dSigmaF", "YSigmaF", signalTable, false );
								str += channelEntry(  "hSigmaF", "ZSigmaF", signalTable, false );
								str += channelEntry(  "vFit", "XFit", signalTable, false );
								str += channelEntry(  "dFit", "YFit", signalTable, false );
								str += channelEntry(  "hFit", "ZFit", signalTable, false );
								str += channelEntry(  "vPos", "XPos", signalTable, false );
								str += channelEntry(  "dPos", "YPos", signalTable, false );
								str += channelEntry(  "hPos", "ZPos", signalTable, false );
								str += channelEntry(  "vSigmaM", "XSigmaM", signalTable, false );
								str += channelEntry(  "dSigmaM", "YSigmaM", signalTable, false );
								str += channelEntry(  "hSigmaM", "ZSigmaM", signalTable, false );
								str += channelEntry(  "vAreaF", "XAreaF", signalTable, false );
								str += channelEntry(  "dAreaF", "YAreaF", signalTable, false );
								str += channelEntry(  "hAreaF", "ZAreaF", signalTable, false );
								str += channelEntry(  "vAreaM", "XAreaM", signalTable, false );
								str += channelEntry(  "dAreaM", "YAreaM", signalTable, false );
								str += channelEntry(  "hAreaM", "ZAreaM", signalTable, false );
								str += channelEntry(  "vAmpF", "XAmplF", signalTable, false );
								str += channelEntry(  "dAmpF", "YAmplF", signalTable, false );
								str += channelEntry(  "hAmpF", "ZAmplF", signalTable, false );
								str += channelEntry(  "vAmpM", "XAmplM", signalTable, false );
								str += channelEntry(  "dAmpM", "YAmplM", signalTable, false );
								str += channelEntry(  "hAmpM", "ZAmplM", signalTable, false );
								str += channelEntry(  "vMeanF", "XMeanF", signalTable, false );
								str += channelEntry(  "dMeanF", "YMeanF", signalTable, false );
								str += channelEntry(  "hMeanF", "ZMeanF", signalTable, false );
								str += channelEntry(  "vMeanM", "XMeanM", signalTable, false );
								str += channelEntry(  "dMeanM", "YMeanM", signalTable, false );
								str += channelEntry(  "hMeanM", "ZMeanM", signalTable, false );
								str += channelEntry(  "vOffstF", "XOffstF", signalTable, false );
								str += channelEntry(  "dOffstF", "YOffstF", signalTable, false );
								str += channelEntry(  "hOffstF", "ZOffstF", signalTable, false );
								str += channelEntry(  "vOffstM", "XOffstM", signalTable, false );
								str += channelEntry(  "dOffstM", "YOffstM", signalTable, false );
								str += channelEntry(  "hOffstM", "ZOffstM", signalTable, false );
								str += channelEntry(  "vSlopeF", "XSlopeF", signalTable, false );
								str += channelEntry(  "dSlopeF", "YSlopeF", signalTable, false );
								str += channelEntry(  "hSlopeF", "ZSlopeF", signalTable, false );
								str += channelEntry(  "vSlopeM", "XSlopeM", signalTable, false );
								str += channelEntry(  "dSlopeM", "YSlopeM", signalTable, false );
								str += channelEntry(  "hSlopeM", "ZSlopeM", signalTable, false );
								str += channelEntry(  "nSteps", "Steps", signalTable, true );
								str += channelEntry(  "hRealData", "HorzSamp", signalTable, false );
								str += channelEntry(  "vRealData", "VertSamp", signalTable, false );
								str += channelEntry(  "dRealData", "DiagSamp", signalTable, false );
							}
							chSuiteTag = true;
						} 
						else if (rset.getString("DVC_TYPE_ID").equals("ND")) {
							final Map<String,String> signalTable = fetchSignals( SIGNAL_FETCH, deviceID );
							str = str + "       <channelsuite name=\"blmsuite\">\n";
							str += channelEntry( "lossAvg", "Slow1PulseBeamOnTotalLoss", signalTable, false );
							str += channelEntry( "lossTBT", "Fast1PulseBeamOnLoss", signalTable, false );
							chSuiteTag = true;
						} 
						else if (rset.getString("DVC_TYPE_ID").equals("Foil")) {
							chSuiteTag = false;
						} 
						else if (rset.getString("DVC_TYPE_ID").equals("LStrp")) {
							chSuiteTag = false;
						} 
						else if (rset.getString("DVC_TYPE_ID").equals("VIW")) {
							chSuiteTag = false;
						} 
						else if (rset.getString("DVC_TYPE_ID").equals("Harp")) {
							str = str.concat( "\t\t<channelsuite name=\"harpsuite\">\n"
							+ "\t\t\t<channel handle=\"xAmp\" signal=\"" + deviceID + ":AmpX_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"yAmp\" signal=\"" + deviceID + ":AmpY_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"diagAmp\" signal=\"" + deviceID + ":AmpZ_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"xSigma\" signal=\"" + deviceID + ":SigmaX_Rb\" settable=\"false\"/>\n"
						    + "\t\t\t<channel handle=\"ySigma\" signal=\"" + deviceID + ":SigmaY_Rb\" settable=\"false\"/>\n"
						    + "\t\t\t<channel handle=\"diagSigma\" signal=\"" + deviceID + ":SigmaZ_Rb\" settable=\"false\"/>\n"
						    + "\t\t\t<channel handle=\"xOffset\" signal=\"" + deviceID + ":OffsetX_Rb\" settable=\"false\"/>\n"
						    + "\t\t\t<channel handle=\"yOffset\" signal=\"" + deviceID + ":OffsetY_Rb\" settable=\"false\"/>\n"
						    + "\t\t\t<channel handle=\"diagOffset\" signal=\"" + deviceID + ":OffsetZ_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"xRMS\" signal=\"" + deviceID + ":RMSX_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"yRMS\" signal=\"" + deviceID + ":RMSY_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"diagRMS\" signal=\"" + deviceID + ":RMSZ_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"xRMS1\" signal=\"" + deviceID + ":RMS1X_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"yRMS1\" signal=\"" + deviceID + ":RMS1Y_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"diagRMS1\" signal=\"" + deviceID + ":RMS1Z_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"xPosAvg\" signal=\"" + deviceID + ":RMSPX_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"yPosAvg\" signal=\"" + deviceID + ":RMSPY_Rb\" settable=\"false\"/>\n"
							+ "\t\t\t<channel handle=\"diagPosAvg\" signal=\"" + deviceID + ":RMSPZ_Rb\" settable=\"false\"/>\n"
							);
							chSuiteTag = true;
						}
						else {
							chSuiteTag = false;
						}
						tmpCounter++;
					}

					tmpID = deviceID;
				}

				// add closing tags for the last device
				// System.out.println(tmpCounter);
				if (tmpCounter != 0) {
					if (chSuiteTag) {
						str = str.concat("       </channelsuite>\n");
					}
					str = str.concat("    </node>\n");
				}

				// put Rf gaps here
				String dtlCounter_str = String.valueOf(dtlCounter);
				String cclCounter_str = String.valueOf(cclCounter);

				if (sequences[k].equals("DTL1") || sequences[k].equals("DTL2")
						|| sequences[k].equals("DTL3")
						|| sequences[k].equals("DTL4")
						|| sequences[k].equals("DTL5")
						|| sequences[k].equals("DTL6")
						|| sequences[k].equals("CCL1")
						|| sequences[k].equals("CCL2")
						|| sequences[k].equals("CCL3")
						|| sequences[k].equals("CCL4")) {

					String queryString = "";
					// get all the rf gaps within this sequence
					if (sequences[k].substring(0, 3).equals("DTL"))
						queryString = "SELECT * " + "FROM EPICS.DVC a, "
								+ "EPICS.RF_GAP d " + "where a.sys_id = 'DTL' "
								+ "and a.Dvc_id = d.Dvc_id "
								+ "and d.Dvc_id like '"
								+ sequences[k].substring(0, 3) + "_RF:Cav0"
								+ dtlCounter_str + "%'"
								+ "order by d.Dist_From_Strt";
					else
						queryString = "SELECT * " + "FROM EPICS.DVC a, "
								+ "EPICS.RF_GAP d " + "where a.sys_id = 'CCL' "
								+ "and a.Dvc_id = d.Dvc_id "
								+ "and d.Dvc_id like '"
								+ sequences[k].substring(0, 3) + "_RF:Cav0"
								+ cclCounter_str + "%'"
								+ "order by d.Dist_From_Strt";

					ResultSet rsetRfGaps = stmt.executeQuery(queryString);

					while (rsetRfGaps.next()) {
						System.out.println(rsetRfGaps.getString("RF_GAP_ID"));
						str = str.concat("    <node type=\"RG\" id=\""
								+ rsetRfGaps.getString("RF_GAP_ID") + "\""
								+ " pos=\""
								+ rsetRfGaps.getString("DIST_FROM_STRT")
								+ "\">\n" + "       <attributes>\n"
								+ "          <rfgap length=\""
								+ rsetRfGaps.getString("GAP_LNGTH") + "\""
								+ " phaseFactor=\""
								+ rsetRfGaps.getString("PHASE_OFFSET") + "\""
								+ " ampFactor=\""
								+ rsetRfGaps.getString("AMPL_TILT") + "\""
								+ " TTF=\"" + rsetRfGaps.getString("TTF")
								+ "\"");
						if (rsetRfGaps.getString("END_CELL_IND").equals("Y"))
							str = str.concat(" endCell=\"1\"");
						else if (rsetRfGaps.getString("END_CELL_IND").equals(
								"N"))
							str = str.concat(" endCell=\"0\"");
						str = str.concat(" gapOffset=\""
								+ getNumericString( rsetRfGaps.getString("ELEC_CNTR_OFF") )
								+ "\"/>\n" + "       </attributes>\n"
								+ "    </node>\n");
					}
					rsetRfGaps.close();
					if (sequences[k].substring(0, 3).equals("DTL"))
						dtlCounter++;
					else if (sequences[k].substring(0, 3).equals("CCL"))
						cclCounter++;
					// for all other sequences with rf cavities in them.
				} 
				else if (sequences[k].equals("MEBT") || sequences[k].equals("SCLMed") || sequences[k].equals("SCLHigh") || sequences[k].equals("Ring1") || sequences[k].equals("Ring2") || sequences[k].equals("Ring3") || sequences[k].equals("Ring4") || sequences[k].equals("Ring5")) {

					// get all the rf rebunchers
					ResultSet rsetRb;
					rsetRb = stmt.executeQuery("SELECT EPICS.DVC.DVC_ID, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_X, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Y, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Z, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PHI, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PSI, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_THETA, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DIST_FROM_STRT, "
									+ "EPICS.BEAM_LINE_DVC_LOC.PHYS_LNGTH, "
									+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND, "
									+ "EPICS.DVC_SETTING.SETTING_ID, "
									+ "EPICS.DVC_SETTING.SETTING_VALUE, "
									+ "EPICS.RF_CAV_DVC.* "
									+ "FROM EPICS.DVC, "
									+ "EPICS.RF_CAV_DVC, "
									+ "EPICS.BEAM_LINE_DVC_LOC, "
									+ "EPICS.DVC_SETTING "
									+ "where EPICS.DVC.Dvc_id = EPICS.RF_CAV_DVC.Dvc_id "
									+ "and EPICS.DVC.Dvc_id = EPICS.DVC_SETTING.Dvc_id "
									+ "and EPICS.BEAM_LINE_DVC_LOC.DVC_id like '"
									+ sequences[k].substring(0, 3)
									+ "%' "
									+ "and EPICS.BEAM_LINE_DVC_LOC.SEQ_NM='"
									+ sequences[k]
									+ "' "
									+ "and EPICS.DVC.Dvc_id = EPICS.BEAM_LINE_DVC_LOC.Dvc_id "
									+ "order by EPICS.BEAM_LINE_DVC_LOC.Dist_From_Strt, EPICS.DVC.DVC_ID");

					// get all the rf gaps within this sequence
					ResultSet rsetRfGaps = stmt1.executeQuery("SELECT * FROM EPICS.RF_GAP ");

					String rbID = null;
					boolean rbAttTag;

					while (rsetRb.next()) {
						final String rbName = rsetRb.getString("DVC_ID");

						if ( !rbName.equals( rbID ) ) {
							rbAttTag = false;
							System.out.println( rbName );
							if ( sequences[k].startsWith( "SCL" ) )
								str = str.concat("    <sequence type=\"SCLCavity\" id=\"" + rbName + "\"");
							else
								str = str.concat("    <sequence type=\"Bnch\" id=\""+ rbName + "\"");
							str = str.concat(" pos=\"" + rsetRb.getString("DIST_FROM_STRT") + "\" len=\"" + getNumericString( rsetRb.getString("PHYS_LNGTH") ) + "\"");
							if ( rsetRb.getString("DSGN_USAGE_IND").equals("Y") )
								str = str.concat(" status=\"true\">\n");
							else
								str = str.concat(" status=\"false\">\n");
							str = str.concat("       <attributes>\n");

							// we use "zero" error for alignment for now
							str = str.concat("          <align" 
								+ " x=\"0.0\" y=\"0.0\" z=\"0.0\""
								+ " pitch=\"" + rsetRb.getString("DSGN_GLBL_COORD_PHI") + "\""
								+ " yaw=\""+ rsetRb.getString("DSGN_GLBL_COORD_PSI") + "\""
								+ " roll=\"" + rsetRb.getString("DSGN_GLBL_COORD_THETA") + "\"/>\n");
							str = str.concat("          <rfcavity");
							if (rsetRb.getString("SETTING_ID").equals("MV/m"))
								str = str.concat("                 amp=\"" + rsetRb.getString("SETTING_VALUE") + "\"\n");
							if (rsetRb.getString("SETTING_ID").equals("deg"))
								str = str.concat("                 phase=\"" + rsetRb.getString("SETTING_VALUE") + "\"\n");
							str = str.concat("                 TTFCoefs=\"" + rsetRb.getString("T0_COEF") + ", " + rsetRb.getString("T1_COEF") + ", " + rsetRb.getString("T2_COEF") + "\"\n"
								+ "                 TTFPrimeCoefs=\"" + rsetRb.getString("TP0_COEF") + ", " + rsetRb.getString("TP1_COEF") + ", " + rsetRb.getString("TP2_COEF") + "\"\n"
								+ "                 STFCoefs=\"" + rsetRb.getString("S0_COEF") + ", " + rsetRb.getString("S1_COEF") + ", " + rsetRb.getString("S2_COEF") + "\"\n"
								+ "                 STFPrimeCoefs=\"" + rsetRb.getString("SP0_COEF") + ", " + rsetRb.getString("SP1_COEF") + ", " + rsetRb.getString("SP2_COEF") + "\"\n"
								+ "                 TTF_endCoefs=\"" + rsetRb.getString("T0_END_COEF") + ", " + rsetRb.getString("T1_END_COEF") + ", " + rsetRb.getString("T2_END_COEF") + "\"\n"
								+ "                 TTFPrime_EndCoefs=\"" + rsetRb.getString("TP0_END_COEF") + ", " + rsetRb.getString("TP1_END_COEF") + ", " + rsetRb.getString("TP2_END_COEF") + "\"\n"
								+ "                 STF_endCoefs=\"" + rsetRb.getString("S0_END_COEF") + ", " + rsetRb.getString("S1_END_COEF") + ", " + rsetRb.getString("S2_END_COEF") + "\"\n"
								+ "                 STFPrime_endCoefs=\"" + rsetRb.getString("SP0_END_COEF") + ", " + rsetRb.getString("SP1_END_COEF") + ", " + rsetRb.getString("SP2_END_COEF") + "\"\n"
								+ "                 structureMode=\"" + rsetRb.getString("STRUCT_TYPE_IND") + "\"\n");
							if (sequences[k].substring(0, 3).equals("SCL"))
								str = str.concat("                 structureTTF=\"" + rsetRb.getString("TTF") + "\"\n                 qLoaded=\"" + getNumericString( rsetRb.getString("Q_LD") ) + "\"\n");

							rbID = rbName;
						} 
						else {
							if ( rsetRb.getString("SETTING_ID").equals("MV/m") ) {
								str = str.concat("                 amp=\"" + rsetRb.getString("SETTING_VALUE") + "\"");
							}
							else if ( rsetRb.getString("SETTING_ID").equals("deg") )
								str = str.concat("                 phase=\"" + rsetRb.getString("SETTING_VALUE") + "\"");
							rbAttTag = true;
						}

						if (rbAttTag) {
							str = str.concat(" freq=\"" + rsetRb.getString("DSGN_FREQ") + "\"/>\n       </attributes>\n       <channelsuite name=\"rfsuite\">\n");

							if ( sequences[k].equals("MEBT") ) {
								final String buncher = rbName.substring( rbName.indexOf("Bnch") + 5);
								final String buncherFCM = sequences[k] + "_LLRF:FCM" + buncher;
								str = str.concat("        <channel handle=\"cavAmpSet\" signal=\"" + buncherFCM + ":CtlAmpSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavPhaseSet\" signal=\"" + buncherFCM + ":CtlPhaseSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavAmpAvg\" signal=\"" + buncherFCM + ":cavV\" settable=\"false\"/>\n"
									+ "        <channel handle=\"peakErr\" signal=\"" + buncherFCM + ":PeakErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"regErr\" signal=\"" + buncherFCM + ":RegErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"resErrAvg\" signal=\"MEBT_LLRF:ResCtrl" + buncher + ":ResErr_Avg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"cavPhaseAvg\" signal=\"" + buncherFCM + ":cavPhaseAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"deltaTRFStart\" signal=\"" + buncherFCM + ":deltaTRFStart\" settable=\"true\"/>\n"
									+ "        <channel handle=\"deltaTRFEnd\" signal=\"" + buncherFCM + ":deltaTRFEnd\" settable=\"true\"/>\n"
									+ "        <channel handle=\"tDelay\" signal=\"" + buncherFCM + ":tDelay\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"blankBeam\" signal=\"" + buncherFCM + ":BlnkBeam\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"RF_ON\" signal=\"" + sequences[k] + "_LLRF:Cav" + buncher + ":RF_ON\" settable=\"false\"/>\n"
                                    );
							}
							// for ring
							else if ( sequences[k].startsWith("Ring") ) {
								final String ringCav = rbName.substring( rbName.indexOf("Cav") + 4 );
								final String ringFCM = "Ring_LLRF:FCM" + ringCav;
								str = str.concat("        <channel handle=\"cavAmpSet\" signal=\"" + ringFCM + ":CtlAmpSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavPhaseSet\" signal=\"" + ringFCM + ":CtlPhaseSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavAmpAvg\" signal=\"" + ringFCM + ":cavV\" settable=\"false\"/>\n"
									+ "        <channel handle=\"peakErr\" signal=\"" + ringFCM + ":PeakErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"regErr\" signal=\"" + ringFCM + ":RegErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"resErrAvg\" signal=\"Ring_LLRF:ResCtrl" + ringCav + ":ResErr_Avg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"cavPhaseAvg\" signal=\"" + ringFCM + ":cavPhaseAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"deltaTRFStart\" signal=\"" + ringFCM + ":deltaTRFStart\" settable=\"true\"/>\n"
									+ "        <channel handle=\"deltaTRFEnd\" signal=\"" + ringFCM + ":deltaTRFEnd\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"tDelay\" signal=\"" + ringFCM + ":tDelay\" settable=\"true\"/>\n"
                                    );
							}
							// for SCL
							else if ( sequences[k].startsWith("SCL") ) {
								final String sclCav = rbName.substring( rbName.indexOf("Cav") + 3 );
								final String sclFCM = "SCL_LLRF:FCM" + sclCav;
								str = str.concat("        <channel handle=\"cavAmpSet\" signal=\"" + sclFCM + ":CtlAmpSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavPhaseSet\" signal=\"" + sclFCM + ":CtlPhaseSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavAmpAvg\" signal=\"" + sclFCM + ":cavV\" settable=\"false\"/>\n"
									+ "        <channel handle=\"peakErr\" signal=\"" + sclFCM + ":PeakErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"regErr\" signal=\"" + sclFCM + ":RegErr\" settable=\"false\"/>\n"
									+ "        <channel handle=\"resErrAvg\" signal=\"SCL_LLRF:ResCtrl" + sclCav + ":ResErr_Avg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"cavPhaseAvg\" signal=\"" + sclFCM + ":cavPhaseAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"deltaTRFStart\" signal=\"" + sclFCM + ":deltaTRFStart\" settable=\"true\"/>\n"
									+ "        <channel handle=\"deltaTRFEnd\" signal=\"" + sclFCM + ":deltaTRFEnd\" settable=\"true\"/>\n"
									+ "        <channel handle=\"tDelay\" signal=\"" + sclFCM + ":tDelay\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"blankBeam\" signal=\"" + sclFCM + ":BlnkBeam\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"RF_ON\" signal=\"" + "SCL_LLRF:Cav" + sclCav + ":RF_ON\" settable=\"false\"/>\n"
                                    );
							}

							str = str + "       </channelsuite>\n";

							str = queryRFGaps(str, rsetRfGaps, rbName);

							str = str.concat("    </sequence>\n");

						}
					}

					rsetRfGaps.close();
					rsetRb.close();

				}

				// close the <sequence>
				str = str + "  </sequence>\n";

				byte buf[] = str.getBytes();
				fout.write(buf);
			}

			str = queryPowerSupplies(fout, conn, stmt, sequences);
		} catch ( Exception exception ) {
			System.err.println("JDBC Error:" + exception.getMessage());
			exception.printStackTrace();
			displayError( "Error", "Exception generating device.", exception );
		}

		byte buf1[] = str.getBytes();
		fout.write(buf1);
		fout.close();
	}

	private String queryRFGaps(String str, ResultSet rsetRfGaps, String rbName)
			throws SQLException {
		// // get all the rf gaps within this sequence
		// ResultSet rsetRfGaps = stmt1.executeQuery("SELECT
		// * "
		// +"FROM EPICS.RF_GAP "
		// +"where RF_GAP_id like '" + rbName +"%'"
		// );
		//
		rsetRfGaps.first();
		while (rsetRfGaps.next()) {
			if (rsetRfGaps.getString("DVC_ID").equals(rbName)) {
				System.out.println(rsetRfGaps.getString("RF_GAP_ID"));
				str = str.concat("       <node type=\"RG\" id=\""
								+ rsetRfGaps.getString("RF_GAP_ID") + "\""
								+ " pos=\""
								+ rsetRfGaps.getString("DIST_FROM_STRT")
								+ "\">\n" + "         <attributes>\n"
								+ "          <rfgap length=\""
								+ rsetRfGaps.getString("GAP_LNGTH") + "\""
								+ " phaseFactor=\""
								+ rsetRfGaps.getString("PHASE_OFFSET") + "\""
								+ " ampFactor=\""
								+ rsetRfGaps.getString("AMPL_TILT") + "\""
								+ " TTF=\"" + rsetRfGaps.getString("TTF")
								+ "\"");
				if (rsetRfGaps.getString("END_CELL_IND").equals("Y"))
					str = str.concat(" endCell=\"1\"");
				else if (rsetRfGaps.getString("END_CELL_IND").equals("N"))
					str = str.concat(" endCell=\"0\"");
				str = str.concat(" gapOffset=\""
						+ getNumericString( rsetRfGaps.getString("ELEC_CNTR_OFF") ) + "\"/>\n");
				str = str.concat("         </attributes>\n");
				str = str.concat("       </node>\n");
			}
		}
		return str;
	}

	private String queryPowerSupplies( OutputStream fout, Connection connection, Statement stmt, String[] sequences ) throws IOException, SQLException {
		String str;
		str = "  <powersupplies>\n";

		byte buf[] = str.getBytes();
		fout.write(buf);
		
		// query for all systems related to a specified sequence
		final PreparedStatement systemQuery = connection.prepareStatement( "select epics.dvc.sys_id from epics.dvc, epics.beam_line_dvc_loc where epics.dvc.dvc_id = epics.beam_line_dvc_loc.dvc_id and epics.beam_line_dvc_loc.seq_nm = ? group by epics.dvc.sys_id" );
		
		// collect all systems covering all the specified sequences
		final Set<String> systemSet = new HashSet<String>();
		for ( final String sequence : sequences ) {
			systemQuery.setString( 1, sequence );
			final ResultSet resultSet = systemQuery.executeQuery();
			while ( resultSet.next() ) {
				systemSet.add( resultSet.getString( "sys_id" ) );
			}
		}
		// order the systems by name
		final List<String> systems = new ArrayList<String>( systemSet );
		Collections.sort( systems );
		
		// for power supplies
		String tmpSystem = "";
		for ( final String system : systems ) {
			/*
			 * if (sequences[k].equals("HEBT1") || sequences[k].equals("HEBT2")) {
			 * queryString = "SELECT EPICS.DVC.DVC_ID, " +
			 * "EPICS.DVC.ACT_DVC_IND, " + "EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC " +
			 * "FROM EPICS.DVC, " + "EPICS.FUNC_DVC_GRP_ITEM " + "where
			 * ((EPICS.DVC.DVC_TYPE_ID='PS' " + "and EPICS.DVC.SUBSYS_ID='Mag') " +
			 * "or EPICS.DVC.DVC_TYPE_ID='ShntC' ) " + "and (EPICS.DVC.DVC_id
			 * like '" + sequences[k].substring(0, 2) + "%' " + "or
			 * EPICS.DVC.DVC_id='Ring_Mag:PS_DH_A11') " + "and EPICS.DVC.Dvc_id =
			 * EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC(+) " + "and
			 * EPICS.DVC.ACT_DVC_IND = 'Y' "; } else {
			 */
			 String queryString = "SELECT DISTINCT EPICS.DVC.DVC_ID, "
					+ "EPICS.DVC.ACT_DVC_IND, "
					+ "EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC "
					+ "FROM EPICS.DVC, "
					+ "EPICS.FUNC_DVC_GRP_ITEM "
					+ "where ((EPICS.DVC.DVC_TYPE_ID='PS'  "
					+ "and EPICS.DVC.SUBSYS_ID='Mag') "
					+ "or EPICS.DVC.DVC_TYPE_ID='ShntC' ) "
					+ "and EPICS.DVC.sys_id = '" + system + "' "
					+ "and EPICS.DVC.Dvc_id = EPICS.FUNC_DVC_GRP_ITEM.RELATED_DVC(+) "
					+ "and EPICS.DVC.ACT_DVC_IND = 'Y' "
					+ "order by EPICS.DVC.DVC_ID";
					
			if ( !tmpSystem.equals( system ) ) {
				System.out.println( "---" + system );
				tmpSystem = system;
				ResultSet rsetPS;

				rsetPS = stmt.executeQuery( queryString );

				while (rsetPS.next()) {
                    final String deviceID = rsetPS.getString( "DVC_ID" );
					System.out.println( deviceID );
					if (rsetPS.getObject("RELATED_DVC") != null) {
						str = "    <ps type=\"trim\" id=\"" + rsetPS.getString("DVC_ID") + "\">\n";
						str = str + "       <channelsuite name=\"pssuite\">\n"
								+ "          <channel handle=\"trimSet\" signal=\"" + deviceID + ":B_Set\"/>\n"
								+ "          <channel handle=\"trimRB\" signal=\"" + deviceID + ":B\"/>\n"
								+ "          <channel handle=\"trimI_Set\" signal=\"" + deviceID + ":I_Set\"/>\n"
								+ "          <channel handle=\"trimI\" signal=\"" + deviceID + ":I\"/>\n"
								+ "          <channel handle=\"cycleState\" signal=\"" + deviceID + ":cycleState\"/>\n";

						str = str + "       </channelsuite>\n" + "    </ps>\n";
					} 
					else {
						str = "    <ps type=\"main\" id=\"" + rsetPS.getString("DVC_ID") + "\">\n";
						str = str + "       <channelsuite name=\"pssuite\">\n"
								+ "          <channel handle=\"" + "I\" signal=\"" + deviceID + ":I\"/>\n" 
                                + "          <channel handle=\"" + "I_Set\" signal=\"" + deviceID + ":I_Set\"/>\n"
								+ "          <channel handle=\"" + "fieldSet\" signal=\"" + deviceID + ":B_Set\"/>\n"
								+ "          <channel handle=\"" + "psFieldRB\" signal=\"" + deviceID + ":B\"/>\n";
                        
                        // only extraction kicker power supplies have voltage PVs (todo: need to fetch signals directly from DB instead of hardcoding exceptions)
                        if ( deviceID.matches( ".+:PS_EKick[^_]*" ) ) {     // match extraction kickers but not the associated waveform power supplies
                            str +=  "          <channel handle=\"" + "voltageSet\" signal=\"" + deviceID + ":V_Set\"/>\n"
                                +   "          <channel handle=\"" + "voltageRB\" signal=\"" + deviceID + ":V\"/>\n";
                        }
								
                        str +=  "          <channel handle=\"" + "cycleState\" signal=\"" + deviceID + ":cycleState\"/>\n"
                            +   "          <channel handle=\"" + "cycleEnable\" signal=\"" + deviceID + ":cycleEnable\"/>\n";
                        
						final int qIndex = deviceID.indexOf( "Q" );			// quadrupole
						final int dhIndex = deviceID.indexOf( "DH" );		// bend
						final int sptmIndex = deviceID.indexOf( "Sptm" );	// septum
						if ( ( qIndex > 7 && qIndex < 13 ) || ( dhIndex > 7 && dhIndex < 13 ) || ( sptmIndex > 7 && sptmIndex < 20 ) ) {
							str = str + "          <channel handle=\"B_Book\" signal=\"" + deviceID + ":B_Book\"/>\n";
						}

						str = str + "       </channelsuite>\n    </ps>\n";
					}
					byte bufPS[] = str.getBytes();
					fout.write(bufPS);
				}
			}

		}

		str = " </powersupplies>\n";
		str = str + "</xdxf>";
		return str;
	}

	private String queryRFQ(String str, Statement stmt, String[] sequences,
			int k) throws SQLException {
		// RFQ
		if (sequences[k].equals("RFQ")) {
			ResultSet rsetRfq;
			rsetRfq = stmt.executeQuery("SELECT EPICS.DVC.DVC_ID, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_X, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Y, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_Z, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PHI, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_PSI, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DSGN_GLBL_COORD_THETA, "
					+ "EPICS.BEAM_LINE_DVC_LOC.DIST_FROM_STRT, "
					+ "EPICS.BEAM_LINE_DVC_LOC.PHYS_LNGTH "
					+ "FROM EPICS.DVC, " + "EPICS.BEAM_LINE_DVC_LOC "
					+ "where (EPICS.DVC.DVC_TYPE_ID IN ('RF')) "
					+ "and EPICS.DVC.Dvc_id = EPICS.BEAM_LINE_DVC_LOC.Dvc_id "
					+ "and EPICS.BEAM_LINE_DVC_LOC.DSGN_USAGE_IND = 'Y' "
					+ "order by EPICS.BEAM_LINE_DVC_LOC.Dist_From_Strt");
			while (rsetRfq.next()) {
				System.out.println(rsetRfq.getString("DVC_ID"));
				str = str.concat("    <node type=\"RF\" id=\""
						+ rsetRfq.getString("DVC_ID") + "\"" + " pos=\""
						+ rsetRfq.getString("DIST_FROM_STRT") + "\">\n"
						+ "    </node>\n");
			}
			rsetRfq.close();
		}
		return str;
	}

	private String queryDTL_CCL_cavs(String str, Statement stmt, String[] sequences, int k) throws SQLException {
		ResultSet rsetDTLs;
		boolean seqAttTag;
		// Select from tables... We have to deal with DTL in a different
		// way because of the PMQs
		if (sequences[k].equals("DTL1") || sequences[k].equals("DTL2") || sequences[k].equals("DTL3") || sequences[k].equals("DTL4") || sequences[k].equals("DTL5") || sequences[k].equals("DTL6") || sequences[k].equals("CCL1") || sequences[k].equals("CCL2") || sequences[k].equals("CCL3") || sequences[k].equals("CCL4")) {
			// add DTL sequence attributes
			rsetDTLs = stmt.executeQuery( "SELECT * FROM EPICS.DVC a, EPICS.RF_CAV_DVC b, EPICS.BEAM_LINE_DVC_LOC c, EPICS.DVC_SETTING d, EPICS.FUNC_DVC_GRP_ITEM e where a.Dvc_id = b.Dvc_id and a.Dvc_id = d.Dvc_id and c.SEQ_NM='" + sequences[k] + "' and a.Dvc_id = c.Dvc_id and a.Dvc_id = e.Dvc_id(+) and c.DSGN_USAGE_IND = 'Y' " );

			String cavID = null;
			while (rsetDTLs.next()) {
                final String deviceID = rsetDTLs.getString( "DVC_ID" );
                final String cavityNumber = deviceID.substring( deviceID.indexOf("Cav") + 3 );

				if (!deviceID.equals(cavID)) {
					str = str.concat("      <rfcavity ");
					if (rsetDTLs.getString("SETTING_ID").equals("MV/m"))
						str = str.concat(" amp=\"" + rsetDTLs.getString("SETTING_VALUE") + "\"");
					if (rsetDTLs.getString("SETTING_ID").equals("deg"))
						str = str.concat(" phase=\"" + rsetDTLs.getString("SETTING_VALUE") + "\"");
					seqAttTag = false;
				} 
                else {
					if (rsetDTLs.getString("SETTING_ID").equals("MV/m"))
						str = str.concat(" amp=\"" + rsetDTLs.getString("SETTING_VALUE") + "\"");
					if (rsetDTLs.getString("SETTING_ID").equals("deg"))
						str = str.concat(" phase=\"" + rsetDTLs.getString("SETTING_VALUE") + "\"");
					str = str.concat(" freq=\""
							+ rsetDTLs.getString("DSGN_FREQ") + "\"\n"
							+ "                 TTFCoefs=\""
							+ rsetDTLs.getString("T0_COEF") + ", "
							+ rsetDTLs.getString("T1_COEF") + ", "
							+ rsetDTLs.getString("T2_COEF") + "\"\n"
							+ "                 TTFPrimeCoefs=\""
							+ rsetDTLs.getString("TP0_COEF") + ", "
							+ rsetDTLs.getString("TP1_COEF") + ", "
							+ rsetDTLs.getString("TP2_COEF") + "\"\n"
							+ "                 STFCoefs=\""
							+ rsetDTLs.getString("S0_COEF") + ", "
							+ rsetDTLs.getString("S1_COEF") + ", "
							+ rsetDTLs.getString("S2_COEF") + "\"\n"
							+ "                 STFPrimeCoefs=\""
							+ rsetDTLs.getString("SP0_COEF") + ", "
							+ rsetDTLs.getString("SP1_COEF") + ", "
							+ rsetDTLs.getString("SP2_COEF") + "\"\n"
							+ "                 TTF_endCoefs=\""
							+ rsetDTLs.getString("T0_END_COEF") + ", "
							+ rsetDTLs.getString("T1_END_COEF") + ", "
							+ rsetDTLs.getString("T2_END_COEF") + "\"\n"
							+ "                 TTFPrime_EndCoefs=\""
							+ rsetDTLs.getString("TP0_END_COEF") + ", "
							+ rsetDTLs.getString("TP1_END_COEF") + ", "
							+ rsetDTLs.getString("TP2_END_COEF") + "\"\n"
							+ "                 STF_endCoefs=\""
							+ rsetDTLs.getString("S0_END_COEF") + ", "
							+ rsetDTLs.getString("S1_END_COEF") + ", "
							+ rsetDTLs.getString("S2_END_COEF") + "\"\n"
							+ "                 STFPrime_endCoefs=\""
							+ rsetDTLs.getString("SP0_END_COEF") + ", "
							+ rsetDTLs.getString("SP1_END_COEF") + ", "
							+ rsetDTLs.getString("SP2_END_COEF") + "\"\n"
							+ "                 structureMode=\""
							+ rsetDTLs.getString("STRUCT_TYPE_IND") + "\"/>\n"
							+ "   </attributes>\n");

					seqAttTag = false;
                    final String fcm = rsetDTLs.getString("RELATED_DVC");
                

					str = str.concat("   <channelsuite name=\"rfsuite\">\n"
									+ "        <channel handle=\"cavAmpSet\" signal=\"" + fcm + ":CtlAmpSet\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"cavPhaseSet\" signal=\"" + fcm + ":CtlPhaseSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavAmpAvg\" signal=\"" + fcm + ":cavAmpAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"cavPhaseAvg\" signal=\"" + fcm + ":cavPhaseAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"deltaTRFStart\" signal=\"" + fcm + ":deltaTRFStart\" settable=\"true\"/>\n"
									+ "        <channel handle=\"deltaTRFEnd\" signal=\"" + fcm + ":deltaTRFEnd\" settable=\"true\"/>\n"
									+ "        <channel handle=\"tDelay\" signal=\"" + fcm + ":tDelay\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"blankBeam\" signal=\"" + fcm + ":BlnkBeam\" settable=\"true\"/>\n"
									+ "   </channelsuite>\n");

				}

				cavID = rsetDTLs.getString("DVC_ID");

				if (seqAttTag) {
                    final String fcm = rsetDTLs.getString("RELATED_DVC");
					str = str.concat("      <rfcavity "
									+ " freq=\""
									+ rsetDTLs.getString("DSGN_FREQ")
									+ "\"/>\n"
									+ "   </attributes>\n"
									+ "   <channelsuite name=\"rfsuite\">\n"
									+ "        <channel handle=\"cavAmpSet\" signal=\"" + fcm + ":CtlAmpSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavPhaseSet\" signal=\"" + fcm + ":CtlPhaseSet\" settable=\"true\"/>\n"
									+ "        <channel handle=\"cavAmpAvg\" signal=\"" + fcm + ":cavAmpAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"cavPhaseAvg\" signal=\"" + fcm + ":cavPhaseAvg\" settable=\"false\"/>\n"
									+ "        <channel handle=\"deltaTRFStart\" signal=\"" + fcm + ":deltaTRFStart\" settable=\"true\"/>\n"
									+ "        <channel handle=\"deltaTRFEnd\" signal=\"" + fcm + ":deltaTRFEnd\" settable=\"true\"/>\n"
									+ "        <channel handle=\"tDelay\" signal=\"" + fcm + ":tDelay\" settable=\"true\"/>\n"
                                    + "        <channel handle=\"blankBeam\" signal=\"" + fcm + ":BlnkBeam\" settable=\"true\"/>\n"
									+ "   </channelsuite>\n");

				}

			}

		}

		else {
			str = str + "   </attributes>\n";

		}
		return str;
	}
	
	
	/** convert a raw value to a valid numeric value by converting "null" to "0.0" */
	static private String getNumericString( final String rawValue ) {
		return rawValue != null && !rawValue.trim().equalsIgnoreCase( "null" ) ? rawValue : "0.0";
	}
	

	/*    *//**
			 * Convenience method for getting the main window cast to the proper
			 * subclass of XalWindow. This allows me to avoid casting the window
			 * every time I reference it.
			 * 
			 * @return The main window cast to its dynamic runtime class
			 */
	/*
	 * private Db2XalWindow myWindow() { return (Db2XalWindow)mainWindow; }
	 * 
	 */
	/**
	 * Instantiate a new PlainDocument that servers as the document for the text
	 * pane. Create a handler of text actions so we can determine if the
	 * document has changes that should be saved.
	 */
	private void makeTextDocument() {
		textDocument = new PlainDocument();
		textDocument.addDocumentListener(new DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				setHasChanges(true);
			}

			public void removeUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}

			public void insertUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}
		});
	}

	/**
	 * Make a main window by instantiating the my custom window. Set the text
	 * pane to use the textDocument variable as its document.
	 */
	public void makeMainWindow() {
		mainWindow = new Db2XalWindow(this);

		if (getSource() != null) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			update(xda.childAdaptor("db2xal"));
			setHasChanges(false);
		}
		setHasChanges(false);
	}

	/**
	 * Save the document to the specified URL.
	 * 
	 * @param url
	 *            The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {
		try {
			XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
			xda.writeNode(this);
			xda.writeToUrl(url);
			setHasChanges(false);
		} catch (XmlDataAdaptor.WriteException exception) {
			exception.printStackTrace();
			displayError("Save Failed!",
					"Save failed due to an internal write exception!",
					exception);
		} catch (Exception exception) {
			exception.printStackTrace();
			displayError("Save Failed!",
					"Save failed due to an internal exception!", exception);
		}
	}

	public String dataLabel() {
		return "db2xal";
	}

	public void update(DataAdaptor adaptor) {
		if (getSource() != null) {
			// set up the right sequence combo from selected primaries:
			List<DataAdaptor> temp = adaptor.childAdaptors("sequences");
			if (temp.isEmpty())
				return; // bail out, nothing left to do

			seqList = new ArrayList<Object>();
			DataAdaptor da2a = adaptor.childAdaptor("sequences");
			// String seqName = da2a.stringValue("name");

			temp = da2a.childAdaptors("seq");
			Iterator<DataAdaptor> itr = temp.iterator();
			while (itr.hasNext()) {
				DataAdaptor da = itr.next();
				seqList.add((da.stringValue("name")));
			}
			// setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
		}
	}

	public void write(DataAdaptor adaptor) {
		// save date
		adaptor.setValue("date", new java.util.Date().toString());
		// save selected sequences
		DataAdaptor daSeq = adaptor.createChild("sequences");

		Iterator<Object> itr = seqList.iterator();

		while (itr.hasNext()) {
			DataAdaptor daSeqComponents = daSeq.createChild("seq");
			daSeqComponents.setValue("name", itr.next());
		}

	}

}
