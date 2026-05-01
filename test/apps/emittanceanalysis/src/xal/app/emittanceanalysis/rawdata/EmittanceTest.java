package xal.app.emittanceanalysis.rawdata;

import java.io.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.widgets.plot.*;
import xal.tools.xml.*;

/**
 *  The test class with main method
 *
 *@author     shishlo
 *@version    July 22, 2004
 */
public class EmittanceTest {
    /**
     *  The main program for the EmittanceTest class
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {

        System.out.println("========START==============");

        final RawDataPanel rawDataPanel = new RawDataPanel();

        javax.swing.JFrame graphFrame = new javax.swing.JFrame("Test Emittance Frame");

        graphFrame.addWindowListener(
            new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    System.exit(0);
                }
            }
            );

        JTextField messageTextLocal = new JTextField();

        rawDataPanel.setMessageTextField(messageTextLocal);

        JPanel testPnanel = new JPanel();
        testPnanel.setLayout(new BorderLayout());
        testPnanel.add(rawDataPanel.getJPanel(), BorderLayout.CENTER);
        testPnanel.add(messageTextLocal, BorderLayout.SOUTH);

        graphFrame.getContentPane().add(testPnanel, java.awt.BorderLayout.CENTER);
        graphFrame.setSize(new Dimension(400, 600));
        graphFrame.setVisible(true);

        final javax.swing.JFrame buttonFrame = new javax.swing.JFrame("Button Frame");

        buttonFrame.addWindowListener(
            new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    System.exit(0);
                }
            }
            );

        JButton dump_Button = new JButton("save data file");

        JButton read_Button = new JButton("read data file");

        buttonFrame.getContentPane().add(dump_Button, java.awt.BorderLayout.NORTH);
        buttonFrame.getContentPane().add(read_Button, java.awt.BorderLayout.SOUTH);

        dump_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser ch = new JFileChooser();
                    ch.setDialogTitle("Dump Data");
                    int returnVal = ch.showSaveDialog(buttonFrame.getContentPane());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File dataFile = ch.getSelectedFile();
                        XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
                        rawDataPanel.dumpDataToXML(da);
                        //dump data into the file
                        try {
                            da.writeTo(dataFile);
                        } catch (IOException ex) {
                            System.out.println("IOException e=" + ex);
                        }
                    }
                }
            });

        read_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser ch = new JFileChooser();
                    ch.setDialogTitle("Read Data");
                    int returnVal = ch.showOpenDialog(buttonFrame.getContentPane());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File dataFile = ch.getSelectedFile();
                        try {
                            XmlDataAdaptor da = XmlDataAdaptor.adaptorForFile(dataFile, false);
                            rawDataPanel.setDataFromXML(da);
                        } catch (MalformedURLException ex) {
                        }
                    }
                }
            });

        buttonFrame.setSize(new Dimension(100, 200));
        buttonFrame.setVisible(true);
    }
}

