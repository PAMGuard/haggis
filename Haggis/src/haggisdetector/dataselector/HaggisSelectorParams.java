package haggisdetector.dataselector;

import java.util.HashMap;

import PamguardMVC.dataSelector.DataSelectParams;
import haggisdetector.HaggisTypes;

public class HaggisSelectorParams extends DataSelectParams {

	public static final long serialVersionUID = 1L;
	
	private HashMap<HaggisTypes, Boolean> selectedTypes = new HashMap<>();
	
	private double minScore = 0;

	public HaggisSelectorParams() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Set whether a given type is selected
	 * @param type
	 * @param select
	 */
	public void setType(HaggisTypes type, boolean select) {
		selectedTypes.put(type, select);
	}
	
	/**
	 * Get the type. Use default if not in map
	 * @param type
	 * @return
	 */
	public boolean isType(HaggisTypes type) {
		Boolean isSel = selectedTypes.get(type);
		if (isSel != null) {
			return isSel;
		}
		else {
			return type.defaultSelection();
		}
	}
	
	/**
	 * @return the minScore
	 */
	public double getMinScore() {
		return minScore;
	}

	/**
	 * @param minScore the minScore to set
	 */
	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}

}
