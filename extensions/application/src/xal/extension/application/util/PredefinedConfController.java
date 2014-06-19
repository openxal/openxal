/*
 *  PredefinedConfController.java
 *
 *  Created on June 17, 2004, 01:18 PM
 */
package xal.extension.application.util;

import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import xal.extension.application.Application;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.*;


/**
 *  PredefinedConfController provides a panel with a configuration tree and can
 *  register and event of choosing the configuration. 
 *  Loads the configuration files from the application's resources.
 *
 *@author    shishlo
 */
public class PredefinedConfController {
	private JTextField messageText = new JTextField(10);

	private String resourcePath = null;
	private String resourceName = null;

	private URL configFileURL = null;

	private ConfigNode rootNode = null;

	//-------------------------------------------------
	//GUI elements
	//-------------------------------------------------
	private JPanel predefConf_Panel = new JPanel();

	private JButton setConfigButton = new JButton("SET CONFIGURATION");

	private JTextArea descriptionText = new JTextArea("Description: ");

	private JTree tree = null;

	private JLabel titleLabel = new JLabel("==Predefined Configurations Selection Panel==", JLabel.CENTER);

	//external action listener
	private ActionListener extSelectionListener = null;



	/**
	 *  PredefinedConfController constructor. resourcePathIn -
	 *  directory name for configuration files for this particular subclass of
	 *  XalDocument. It is usually "config". resourceNameIn - the name of XML file
	 *  with a configuration structure.
	 *
	 *@param  resourcePathIn  Description of the Parameter
	 *@param  resourceNameIn  Description of the Parameter
	 */
	public PredefinedConfController( final String resourcePathIn, final String resourceNameIn ) {
		URL predefConfURL = Application.getAdaptor().getResourceURL( resourcePathIn + "/" + resourceNameIn );

		resourcePath = resourcePathIn;
		resourceName = resourceNameIn;

		//set JTextArea parameters
		descriptionText.setEditable(false);
		descriptionText.setRows(3);
		descriptionText.setColumns(40);
		descriptionText.setLineWrap(true);
		JScrollPane scrollPaneForText = new JScrollPane(descriptionText);
		descriptionText.setForeground(Color.blue);

		//descriptionText.setLineWrap(true);
		//descriptionText.setWrapStyleWord(true);


		//prepare Data Adaptor and Nodes Tree
		if ( predefConfURL != null ) {	// TODO: cleanup this code to better handle the case of no predefined configurations
			XmlDataAdaptor readAdp = XmlDataAdaptor.adaptorForUrl(predefConfURL, false);

			rootNode = new ConfigNode((XmlDataAdaptor) readAdp.childAdaptor("CONFIGURATIONS"));

			tree = new JTree((TreeNode) rootNode);
			tree.setRootVisible(false);
			tree.setShowsRootHandles(true);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			ConfigNodeCellRenderer render = new ConfigNodeCellRenderer();
			tree.setCellRenderer(render);

			//Listen for when the selection changes.
			tree.addTreeSelectionListener(
				new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent e) {
						System.out.println( "Configuration item selected..." );
						ConfigNode node = (ConfigNode)tree.getLastSelectedPathComponent();
						if (node == null) {
							return;
						}
						configFileURL = null;
						if (node.isConfig()) {
							String fileName = node.getURL_String();
							configFileURL = Application.getAdaptor().getResourceURL( resourcePath + "/" + fileName );
						}
						descriptionText.setText(null);
						descriptionText.setText("Description: " + node.getDescription());
					}
				});

			//Mouse listener for tree
			MouseListener ml =
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() != 2) {
							return;
						}
						int selRow = tree.getRowForLocation(e.getX(), e.getY());
						TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
						if (selRow != -1) {
							Object value = selPath.getLastPathComponent();
							ConfigNode node = (ConfigNode) value;
							configFileURL = null;
							if (node.isConfig()) {
								String fileName = node.getURL_String();
								configFileURL = Application.getAdaptor().getResourceURL( resourcePath + "/" + fileName );
								URL url = getSelectedConfigFileURL();
								if (extSelectionListener != null && url != null) {
									ActionEvent actEvnt = new ActionEvent(url, 0, "selected");
									extSelectionListener.actionPerformed(actEvnt);
								}
								else {
									System.out.println( "url: " + url + ", external selection listener: " + extSelectionListener );
									messageText.setText(null);
									messageText.setText("Please, select a configuration from the tree.");
									Toolkit.getDefaultToolkit().beep();
								}
							}
							else {
								messageText.setText(null);
								messageText.setText( "The selected node is not a config. Please, select a configuration from the tree." );
								Toolkit.getDefaultToolkit().beep();
							}
						}
					}
				};

			tree.addMouseListener(ml);

			JScrollPane scrollTreePane = new JScrollPane();
			scrollTreePane.setViewportView(tree);

			titleLabel.setForeground(Color.blue);

			//set button
			setConfigButton.setForeground(Color.red);
			ActionListener internalListener =
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						URL url = getSelectedConfigFileURL();
						if (extSelectionListener != null && url != null) {
							ActionEvent actEvnt = new ActionEvent(url, 0, "selected");
							extSelectionListener.actionPerformed(actEvnt);
						}
						else {
							System.out.println( "url: " + url + ", external selection listener: " + extSelectionListener );
							messageText.setText(null);
							messageText.setText("Please, select a configuration from the tree.");
							Toolkit.getDefaultToolkit().beep();
						}

					}
				};

			setConfigButton.addActionListener(internalListener);

			//make the panels
			JPanel treePanel = new JPanel();
			tree.setBackground(treePanel.getBackground());
			treePanel.setLayout(new BorderLayout());
			treePanel.add(titleLabel, BorderLayout.NORTH);
			treePanel.add(scrollTreePane, BorderLayout.CENTER);

			JPanel buttonAndTextPanel = new JPanel();
			buttonAndTextPanel.setLayout(new BorderLayout());

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
			buttonPanel.add(setConfigButton);

			buttonAndTextPanel.add(buttonPanel, BorderLayout.NORTH);
			buttonAndTextPanel.add(scrollPaneForText, BorderLayout.CENTER);

			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					treePanel,
					buttonAndTextPanel);

			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(0.5);

			predefConf_Panel.setLayout(new BorderLayout());
			splitPane.setPreferredSize(new Dimension(0, 0));
			predefConf_Panel.add(splitPane, BorderLayout.CENTER);
		}
	}


	/**
	 *  Gets the selectedConfigFileURL attribute of the PredefinedConfController
	 *  object
	 *
	 *@return    The selectedConfigFileURL value
	 */
	private URL getSelectedConfigFileURL() {
		return configFileURL;
	}


	/**
	 *  Register the selection event listener.
	 *
	 *@param  selectListener  The new selectorListener value
	 */
	public void setSelectorListener(ActionListener selectListener) {
		extSelectionListener = selectListener;
	}


	/**
	 *  Sets the message text field. All messages will be shown in this text field.
	 *
	 *@param  messageText  The new messageTextField value
	 */
	public void setMessageTextField(JTextField messageText) {
		this.messageText = messageText;
	}


	/**
	 *  Sets font for all GUI elements.
	 *
	 *@param  fnt  The new fontsForAll value
	 */
	public void setFontsForAll(Font fnt) {
		messageText.setFont(fnt);
		setConfigButton.setFont(fnt);
		descriptionText.setFont(fnt);
		if ( tree != null )  tree.setFont(fnt);
		titleLabel.setFont(fnt);
	}


	/**
	 *  Returns the configuration panel.
	 *
	 *@return    The jPanel value
	 */
	public JPanel getJPanel() {
		return predefConf_Panel;
	}


	//------------------------------------------------
	//tree node class to keep info about configuration
	//------------------------------------------------

	/**
	 *  Description of the Class
	 *
	 *@author    shishlo
	 */
	private class ConfigNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 0L;

		private boolean isConfigutation = false;

		private String descriptionText = "null";

		private String treeName = "null";

		private String urlString = "null";


		/**
		 *  Constructor for the ConfigNode object
		 *
		 *@param  DA  Description of the Parameter
		 */
		public ConfigNode( XmlDataAdaptor DA ) {
			if (DA != null) {
				java.util.List<DataAdaptor> childList = DA.childAdaptors();
				descriptionText = DA.stringValue("text");
				treeName = DA.stringValue("tree_name");
				if (DA.hasAttribute("url")) {
					urlString = DA.stringValue("url");
					isConfigutation = true;
				}
				for (int i = 0; i < childList.size(); i++) {
					XmlDataAdaptor childDA = (XmlDataAdaptor) childList.get(i);
					ConfigNode confNode = new ConfigNode(childDA);
					add(confNode);
				}
			}
		}


		/**
		 *  Gets the config attribute of the ConfigNode object
		 *
		 *@return    The config value
		 */
		public boolean isConfig() {
			return isConfigutation;
		}


		/**
		 *  Gets the description attribute of the ConfigNode object
		 *
		 *@return    The description value
		 */
		public String getDescription() {
			return descriptionText;
		}


		/**
		 *  Gets the treeName attribute of the ConfigNode object
		 *
		 *@return    The treeName value
		 */
		public String getTreeName() {
			return treeName;
		}


		/**
		 *  Gets the uRL_String attribute of the ConfigNode object
		 *
		 *@return    The uRL_String value
		 */
		public String getURL_String() {
			return urlString;
		}

	}


	//------------------------------------------
	//cell renderer for configuration nodes
	//------------------------------------------

	/**
	 *  Description of the Class
	 *
	 *@author    shishlo
	 */
	private class ConfigNodeCellRenderer implements TreeCellRenderer {

		/**
		 *  Constructor for the ConfigNodeCellRenderer object
		 */
		public ConfigNodeCellRenderer() { }


		/**
		 *  Gets the treeCellRendererComponent attribute of the ConfigNodeCellRenderer
		 *  object
		 *
		 *@param  tree      Description of the Parameter
		 *@param  value     Description of the Parameter
		 *@param  selected  Description of the Parameter
		 *@param  expanded  Description of the Parameter
		 *@param  leaf      Description of the Parameter
		 *@param  row       Description of the Parameter
		 *@param  hasFocus  Description of the Parameter
		 *@return           The treeCellRendererComponent value
		 */
		public Component getTreeCellRendererComponent(JTree tree,
				Object value,
				boolean selected,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {
			Font fnt = tree.getFont();
			JPanel treecell = new JPanel();
			treecell.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			treecell.setFont(tree.getFont());

			ConfigNode confNode = (ConfigNode) value;

			if (confNode.isConfig()) {
				JLabel confLabel = new JLabel("Config.:", JLabel.LEFT);
				confLabel.setFont(fnt);
				confLabel.setForeground(Color.blue);
				JLabel nameLabel = new JLabel(confNode.getTreeName(), JLabel.LEFT);
				nameLabel.setFont(fnt);
				treecell.add(confLabel);
				treecell.add(nameLabel);
			} else {
				JLabel nameLabel = new JLabel(confNode.getTreeName(), JLabel.LEFT);
				nameLabel.setFont(fnt);
				treecell.add(nameLabel);

			}

			if (selected) {
				treecell.setBackground(treecell.getBackground().brighter());
			}

			return treecell;
		}
	}

}

