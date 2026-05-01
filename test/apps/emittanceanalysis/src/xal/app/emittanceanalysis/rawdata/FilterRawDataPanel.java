package xal.app.emittanceanalysis.rawdata;

import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.swing.*;
import xal.extension.widgets.plot.*;
import xal.tools.xml.*;

/**
 *  This is the sub-panel of the RawDataPanel with filtering parameters of the
 *  raw data.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class FilterRawDataPanel {

    private String xmlName = "FILTER_RAW_DATA_PANEL";

    //panel by inself
    private JPanel panel = new JPanel();
    private TitledBorder border = null;

    //graph panel
    private FunctionGraphsJPanel GP = null;

    //format for numbers of rows and channels
    private DecimalFormat int_Format = new DecimalFormat( "##0" );

    //filter level 0-1.0
    private double filterLevel = 0.;

    //-----------------------------------
    //GUI elements
    //-----------------------------------
    private JButton filter_Button = new JButton( "APPLY FILTER TO GRAPHS" );

    private JLabel par_0_Label = new JLabel( "Param.[%]=" );
    private JLabel par_1_Label = new JLabel( "  N =" );

    private JRadioButton avg_Button = new JRadioButton( "Use Avg." );

    private DoubleInputTextField par_0_Text = new DoubleInputTextField( 5 );

    private JSpinner nAvg_Spinner = new JSpinner( new SpinnerNumberModel( 1, 1, 20, 1 ) );

    //temporary array
    private double[] tmp_vals = new double[300];


    /**
     *  Constructor for the FilterRawDataPanel object
     *
     *@param  GP_in  The FunctionGraphsJPanel with raw wire waveforms
     */
    public FilterRawDataPanel( FunctionGraphsJPanel GP_in ) {
        GP = GP_in;

        par_0_Text.setNumberFormat( int_Format );

        par_0_Text.setHorizontalAlignment( JTextField.CENTER );

        par_0_Text.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    double level = par_0_Text.getValue() / 100.0;
                    if ( level > 1.0 ) {
                        level = 1.0;
                    }
                    level = 1.0 - level;
                    if ( level < 0. ) {
                        level = 0.;
                    }
                    filterLevel = level;
                }
            } );

        par_0_Text.setBackground( Color.white );

        par_0_Text.setValue( 0. );

        par_0_Label.setHorizontalAlignment( SwingConstants.RIGHT );
        par_1_Label.setHorizontalAlignment( SwingConstants.RIGHT );

        nAvg_Spinner.setAlignmentX( JSpinner.CENTER_ALIGNMENT );

        filter_Button.setForeground( Color.blue.darker() );
        //filter_Button.setBackground( Color.cyan );
        filter_Button.setBorder( BorderFactory.createRaisedBevelBorder() );

        filter_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    filterGraphRawData();
                }
            } );

        //panel border
        Border etchedBorder = BorderFactory.createEtchedBorder();
        border = BorderFactory.createTitledBorder( etchedBorder, "filter graph data" );
        panel.setBorder( border );
        panel.setLayout( new BorderLayout() );
        panel.setBackground( panel.getBackground().darker() );

        JPanel tmp_panel_0 = new JPanel();
        tmp_panel_0.setLayout( new GridLayout( 1, 5, 1, 1 ) );
        //tmp_panel_0.setLayout(new FlowLayout(FlowLayout.CENTER,1,1));
        tmp_panel_0.setBorder( etchedBorder );
        tmp_panel_0.add( par_1_Label );
        tmp_panel_0.add( nAvg_Spinner );
        tmp_panel_0.add( par_0_Label );
        tmp_panel_0.add( par_0_Text );
        tmp_panel_0.add( avg_Button );

        JPanel tmp_panel_1 = new JPanel();
        tmp_panel_1.setLayout( new FlowLayout( FlowLayout.CENTER, 1, 1 ) );
        tmp_panel_1.add( filter_Button );

        panel.add( tmp_panel_0, BorderLayout.NORTH );
        panel.add( tmp_panel_1, BorderLayout.SOUTH );
    }


    /**
     *  Sets the font for all GUI elements
     *
     *@param  fnt  The new font
     */
    public void setFontForAll( Font fnt ) {
        border.setTitleFont( fnt );

        filter_Button.setFont( fnt );

        par_0_Label.setFont( fnt );
        par_1_Label.setFont( fnt );
        par_0_Text.setFont( fnt );
        avg_Button.setFont( fnt );

        nAvg_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) nAvg_Spinner.getEditor() ).getTextField().setFont( fnt );

    }


    /**
     *  Returns the JPanel of this class
     *
     *@return    The JPanel
     */
    public JPanel getJPanel() {
        return panel;
    }


    /**
     *  Executes filtering data that currently displayed on the graph panel
     */
    public void filterGraphRawData() {
        Vector<CurveData> cdV = GP.getAllCurveData();
        if ( cdV.size() == 0 ) {
            return;
        }
        for ( int i = 0; i < cdV.size(); i++ ) {
            filterCurveData( cdV.get( i ) );
        }
        GP.refreshGraphJPanel();
    }


    /**
     *  Does filtering data in the one CurveData instance
     *
     *@param  cd  The CurveData instance with data for filtering
     */
    public void filterCurveData( CurveData cd ) {

        double level = filterLevel * Math.max( Math.abs( cd.getMaxY() ), Math.abs( cd.getMinY() ) );

        int nP = cd.getSize();
        int nP1 = nP - 1;

        if ( nP < 4 ) {
            return;
        }

        double y0 = 0.;
        double y1 = 0.;

        //first and last points
        y0 = cd.getY( 0 );
        if ( Math.abs( y0 ) > level ) {
            cd.setPoint( 0, 0., 0. );
        }

        y0 = cd.getY( nP1 );
        if ( Math.abs( y0 ) > level ) {
            cd.setPoint( nP1, (double) nP1, 0. );
        }

        int ind0 = 0;
        int ind1 = 0;

        double d1 = 0.;
        double d2 = 0.;

        double y = 0;

        int n_filter = ( (Integer) nAvg_Spinner.getValue() ).intValue();

        for ( int ifl = 1; ifl <= n_filter; ifl++ ) {
            ind0 = 0;
            if ( ( ind0 + ifl + 1 ) > nP1 ) {
                break;
            }
            int nP_last = nP1 - ifl - 1;

            while ( ind0 <= nP_last ) {
                ind1 = ind0 + ifl + 1;
                y0 = cd.getY( ind0 );
                y1 = cd.getY( ind1 );
                d1 = cd.getY( ind0 + 1 ) - y0;
                d2 = y1 - cd.getY( ind1 - 1 );
                if ( d1 * d2 < 0. && Math.abs( d1 ) > level && Math.abs( d2 ) > level ) {
                    for ( int j = ( ind0 + 1 ); j < ind1; j++ ) {
                        y = y0 + ( ( j - ind0 ) * ( y1 - y0 ) ) / ( ind1 - ind0 );
                        cd.setPoint( j, (double) j, y );
                    }
                    ind0 = ind1;
                }
                else {
                    ind0++;
                }
            }
        }

        boolean doAvg = avg_Button.isSelected();
        if ( doAvg == false || n_filter <= 1 ) {
            return;
        }

        if ( tmp_vals.length < nP ) {
            tmp_vals = new double[nP];
        }
        double[] coeff_arr = new double[n_filter + 1];

        for ( int i = 0; i <= n_filter; i++ ) {
            coeff_arr[i] = ( (double) ( n_filter - i ) );
        }

        for ( int i = 0; i < nP; i++ ) {
            tmp_vals[i] = cd.getY( i );
        }

        double weight = 0.;

        for ( int ip0 = 0; ip0 < nP; ip0++ ) {
            ind0 = Math.max( 0, ip0 - n_filter );
            ind1 = Math.min( nP1, ip0 + n_filter );

            weight = 0.;
            y = 0.0;
            for ( int i = ind0; i <= ind1; i++ ) {
                y += coeff_arr[Math.abs( ip0 - i )] * tmp_vals[i];
                weight += coeff_arr[Math.abs( ip0 - i )];
            }
            y /= weight;
            cd.setPoint( ip0, (double) ip0, y );
        }
    }


    /**
     *  Returns the filter parameter
     *
     *@return    The filter parameter
     */
    public int getFilterParam() {
        return (int) par_0_Text.getValue();
    }


    /**
     *  Sets the filter parameter
     *
     *@param  level  The new filter parameter value
     */
    public void setFilterParam( int level ) {
        par_0_Text.setValue( (double) level );
    }


    /**
     *  Returns the number of averaging during filtering
     *
     *@return    The number of averaging
     */
    public int getNAvg() {
        return ( (Integer) nAvg_Spinner.getValue() ).intValue();
    }


    /**
     *  Sets the number of averaging during filtering
     *
     *@param  nAvg  The new number of averaging during filtering
     */
    public void setNAvg( int nAvg ) {
        nAvg_Spinner.setValue( new Integer( nAvg ) );
    }


    /**
     *  Returns the avgState variable (true - use filtering)
     *
     *@return    Use filtering or do not
     */
    public boolean getAvgState() {
        return avg_Button.isSelected();
    }


    /**
     *  Sets the boolean valriable - use filtering or do not
     *
     *@param  isSel  Use filtering or do not
     */
    public void setAvgState( boolean isSel ) {
        avg_Button.setSelected( isSel );
    }


    /**
     *  Returns the string identifier in the XML structure.
     *
     *@return    The name in the XAL adapter structure
     */
    public String getNameXMLData() {
        return xmlName;
    }


    /**
     *  Defines the XML data file.
     *
     *@param  rawDataPanelData  The XML data adapter to store configuration data
     */
    public void dumpDataToXML( XmlDataAdaptor rawDataPanelData ) {
        XmlDataAdaptor filterRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.createChild( getNameXMLData() );
        XmlDataAdaptor params = (XmlDataAdaptor) filterRawDataPanelData.createChild( "PARAMS" );

        params.setValue( "useAvg", avg_Button.isSelected() );
        params.setValue( "nAvg", ( (Integer) nAvg_Spinner.getValue() ).intValue() );
        params.setValue( "filterLevel", par_0_Text.getValue() );
    }


    /**
     *  Sets the configuration by using data from XML adapter
     *
     *@param  rawDataPanelData  The XML data adapter to store configuration data
     */
    public void setDataFromXML( XmlDataAdaptor rawDataPanelData ) {
        XmlDataAdaptor filterRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.childAdaptor( getNameXMLData() );
        XmlDataAdaptor params = (XmlDataAdaptor) filterRawDataPanelData.childAdaptor( "PARAMS" );

        avg_Button.setSelected( params.booleanValue( "useAvg" ) );
        nAvg_Spinner.setValue( new Integer( params.intValue( "nAvg" ) ) );
        par_0_Text.setValue( params.doubleValue( "filterLevel" ) );
    }

}

