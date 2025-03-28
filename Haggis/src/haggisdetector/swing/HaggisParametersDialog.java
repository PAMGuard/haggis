package haggisdetector.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import fftManager.FFTDataUnit;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisParameters;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class HaggisParametersDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

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
	private HaggisParameters haggisParameters;
	
	/*
	 * source panel is a handy utility for listing available data sources. 
	 */
	private SourcePanel sourcePanel;
	
	/*
	 * reference for data fields for detector part. 
	 */
	private JTextField background, lowFreq, highFreq, threshold;
	
	/*
	 * And for classifier part. 
	 */
	private JCheckBox keepUnclassified, scotlandOnly;
	private JTextField minClassScore;

	private HaggisControl haggisControl;
	
	private HaggisParametersDialog(HaggisControl haggisControl, Frame parentFrame) {
		super(parentFrame, "Haggis parameters", true);
		this.haggisControl = haggisControl;

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
		setHelpPoint("docs.haggishelp");
		
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
		
		// make another panel for the detection parameters.
		JPanel detPanel = new JPanel();
		detPanel.setBorder(new TitledBorder("Haggis Detection"));
		// use the gridbaglaoyt - it's the most flexible
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new PamGridBagContraints();
		detPanel.setLayout(layout);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.ipadx = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;
		detPanel.add(new JLabel("Background smoothing Constant"), constraints);
		constraints.gridx++;
		detPanel.add(background = new JTextField(4), constraints);
		constraints.gridx++;
		detPanel.add(new JLabel(" s"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		detPanel.add(new JLabel("low Frequency"), constraints);
		constraints.gridx++;
		detPanel.add(lowFreq = new JTextField(6), constraints);
		constraints.gridx++;
		detPanel.add(new JLabel(" Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		detPanel.add(new JLabel("high Frequency"), constraints);
		constraints.gridx++;
		detPanel.add(highFreq = new JTextField(6), constraints);
		constraints.gridx++;
		detPanel.add(new JLabel(" Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		detPanel.add(new JLabel("Detection Threshold"), constraints);
		constraints.gridx++;
		detPanel.add(threshold = new JTextField(4), constraints);
		constraints.gridx++;
		detPanel.add(new JLabel(" dB"), constraints);
		
		mainPanel.add(BorderLayout.CENTER, detPanel);
		
		// and a third panel for the classifier
		JPanel classPanel = new JPanel(new GridBagLayout());
		classPanel.setBorder(new TitledBorder("Haggis Classification"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		classPanel.add(keepUnclassified = new JCheckBox("Keep unclassified sounds"), c);
		c.gridy++;
		classPanel.add(scotlandOnly = new JCheckBox("Restrict search to Scottish highlands"), c);
		c.gridy++;
		c.gridwidth = 1;
		classPanel.add(new JLabel("Minimum classification score  ", JLabel.RIGHT), c);
		c.gridx++;
		classPanel.add(minClassScore = new JTextField(4), c);
		mainPanel.add(BorderLayout.SOUTH, classPanel);
		
		
		setDialogComponent(mainPanel);
	}
	
	public static HaggisParameters showDialog(Frame parentFrame, HaggisControl haggisControl, HaggisParameters haggisParameters) {
//		if (singleInstance == null || singleInstance.getParent() != parentFrame || singleInstance.haggisControl != haggisControl) {
			singleInstance = new HaggisParametersDialog(haggisControl, parentFrame);
//		}
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
		
		keepUnclassified.setSelected(haggisParameters.keepUnclassified);
		scotlandOnly.setSelected(haggisParameters.scotlandOnly);
		minClassScore.setText(String.format("%3.2f", haggisParameters.minConfidence));
		
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
			return showWarning("Invalid detection parameter (freuqencies must be integer values)");
		}
		
		haggisParameters.keepUnclassified = keepUnclassified.isSelected();
		haggisParameters.scotlandOnly = scotlandOnly.isSelected();
		try {
			haggisParameters.minConfidence = Double.valueOf(minClassScore.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("Invalid minimum classification score value");
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		haggisParameters = new HaggisParameters();
		setParams();
		
	}

}
