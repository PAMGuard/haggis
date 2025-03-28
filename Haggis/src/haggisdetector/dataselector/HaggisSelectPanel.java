package haggisdetector.dataselector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import haggisdetector.HaggisTypes;

public class HaggisSelectPanel implements PamDialogPanel {
	
	private JPanel mainPanel;
	
	private JCheckBox[] speciesBoxes;
	
	private JTextField minScore;

	private HaggisSelector haggisSelector;

	public HaggisSelectPanel(HaggisSelector haggisSelector) {
		this.haggisSelector = haggisSelector;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Haggis Class"));
		GridBagConstraints c = new PamGridBagContraints();
		HaggisTypes[] types = HaggisTypes.values();
		speciesBoxes = new JCheckBox[types.length];
		for (int i = 0; i < types.length; i++) {
			speciesBoxes[i] = new JCheckBox(types[i].toLowerString());
			mainPanel.add(speciesBoxes[i], c);
			c.gridy++;
		}
		minScore = new JTextField(4);
		mainPanel.add(new JLabel("Minimum classification score ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minScore, c);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		HaggisSelectorParams params = haggisSelector.getParams(); 
		HaggisTypes[] types = HaggisTypes.values();
		for (int i = 0; i < types.length; i++) {
			speciesBoxes[i].setSelected(params.isType(types[i]));
		}
		minScore.setText(String.format("%3.2f", params.getMinScore()));
	}

	@Override
	public boolean getParams() {
		HaggisSelectorParams params = haggisSelector.getParams(); 
		HaggisTypes[] types = HaggisTypes.values();
		for (int i = 0; i < types.length; i++) {
			params.setType(types[i], speciesBoxes[i].isSelected());
		}
		try {
			params.setMinScore(Double.valueOf(minScore.getText()));
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Invalid Haggis selection parameter", "Invalid entry for minimum score. Must be a number");
		}
		return true;
	}

}
