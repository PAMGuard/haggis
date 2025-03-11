package haggisdetector.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import fftManager.FFTDataUnit;
import haggisdetector.HaggisParameters;
import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class HaggisParametersDialog extends PamDialog {

	/*
	 * Make the dialog a singleton - saves time recreating it 
	 * every time it's used and will also leave the same tab showing
	 * for multi tab dialogs (doesn't really make any difference
	 * for this simple dialog)
	 */
	static private HaggisParametersDialog singleInstance;
	
	/*
	 * local copy of parameters
	 */
	HaggisParameters haggisParameters;
	
	/*
	 * source panel is a handy utility for listing available data sources. 
	 */
	SourcePanel sourcePanel;
	
	/*
	 * reference for data fields
	 *
	 */
	JTextField background, lowFreq, highFreq, threshold;
	
	private HaggisParametersDialog(Frame parentFrame) {
		super(parentFrame, "Workshop demo parameters", true);

		/*
		 * Use the Java layout manager to constructs nesting panels 
		 * of all the parameters. 
		 */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		/**
		 * For a plugin, add the relative path to the help from wherever
		 * the plugins helpset is. This can be a bit different to the path 
		 * for built in modules. The following works for this example. 
		 */
		setHelpPoint("docs.demohelp");
		
		/* 
		 * put a sourcePanel in the top of the dialog panel. 
		 * need to put it in an inner panel in order to add 
		 * a titled border (appearance is everything)
		 */
		sourcePanel = new SourcePanel(this, FFTDataUnit.class, true, true);
		JPanel sourceSubPanel = new JPanel();
		sourceSubPanel.setLayout(new BorderLayout());
		sourceSubPanel.setBorder(new TitledBorder("FFT Data source"));
		sourceSubPanel.add(BorderLayout.CENTER, sourcePanel.getPanel());
		mainPanel.add(BorderLayout.NORTH, sourceSubPanel);
		
		// make another panel for the rest of the parameters.
		JPanel detPanel = new JPanel();
		detPanel.setBorder(new TitledBorder("Detection parameters"));
		// use the gridbaglaoyt - it's the most flexible
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		detPanel.setLayout(layout);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.ipadx = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;
		addComponent(detPanel, new JLabel("Background smoothing Constant"), constraints);
		constraints.gridx++;
		addComponent(detPanel, background = new JTextField(4), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" s"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		addComponent(detPanel, new JLabel("low Frequency"), constraints);
		constraints.gridx++;
		addComponent(detPanel, lowFreq = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		addComponent(detPanel, new JLabel("high Frequency"), constraints);
		constraints.gridx++;
		addComponent(detPanel, highFreq = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		addComponent(detPanel, new JLabel("Detection Threshold"), constraints);
		constraints.gridx++;
		addComponent(detPanel, threshold = new JTextField(4), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" dB"), constraints);
		
		mainPanel.add(BorderLayout.CENTER, detPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static HaggisParameters showDialog(Frame parentFrame, HaggisParameters haggisParameters) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new HaggisParametersDialog(parentFrame);
		}
		singleInstance.haggisParameters = haggisParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.haggisParameters;
	}

	public void setParams() {
		/* 
		 * set the parameters in the source list. 
		 * including the channel list and the actual data source. 
		 */
		sourcePanel.setSource(haggisParameters.fftDataName);
		sourcePanel.setChannelList(haggisParameters.channelList);
		
		background.setText(String.format("%.1f", haggisParameters.backgroundTimeConstant));
		lowFreq.setText(String.format("%d", haggisParameters.lowFreq));
		highFreq.setText(String.format("%d", haggisParameters.highFreq));
		threshold.setText(String.format("%.1f", haggisParameters.threshold));
	}

	@Override
	public void cancelButtonPressed() {
		haggisParameters = null;		
	}

	@Override
	/**
	 * return true if all parameters are OK, otherwise, return false. 
	 */
	public boolean getParams() {
		/*
		 * get the source parameters
		 */
		PamDataBlock fftDataBlock = sourcePanel.getSource();
		if (fftDataBlock == null) {
			return showWarning("you must select a FFT data source as input");
		}
		haggisParameters.fftDataName = fftDataBlock.getLongDataName();
		haggisParameters.channelList = sourcePanel.getChannelList();
		if (haggisParameters.channelList == 0) {
			return false;
		}
		// will throw an exception if the number format of any of the parameters is invalid, 
		// so catch the exception and return false to prevent exit from the dialog. 
		try {
			haggisParameters.backgroundTimeConstant = Double.valueOf(background.getText());
			haggisParameters.lowFreq = Integer.valueOf(lowFreq.getText());
			haggisParameters.highFreq = Integer.valueOf(highFreq.getText());
			haggisParameters.threshold = Double.valueOf(threshold.getText());
		}
		catch (NumberFormatException ex) {
			return false;
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		haggisParameters = new HaggisParameters();
		setParams();
		
	}

}
