package haggisdetector;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Locale;
import java.util.Random;

import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import pamguard.GlobalArguments;

/**
 * Haggis classifier. Tries to classify sounds to sub-species types and assign confidence
 * given that Haggis don't exist, the type is unknown, apart from occasional assignments
 * to left or right. The confidence score is randomly generated, but set to be a lot higher
 * between the hours of 10pm and 1am based on local time, when most people are likely to beleive
 * that they've seen a Wild Haggis.
 * @author dg50
 *
 */
public class HaggisClassifier {


	private HaggisControl haggisControl;
	private Random random;

	public HaggisClassifier(HaggisControl haggisControl) {
		this.haggisControl = haggisControl;
		random = new Random(System.currentTimeMillis());
	}

	public HaggisClass classify(HaggisDataUnit haggisDataUnit) {

		HaggisParameters params = haggisControl.getHaggisParameters();
		
		LocalDateTime localDateTime = LocalDateTime.now();
		int hour = localDateTime.getHour();
		boolean afterPub = (hour >= 22 || hour <= 1); // between 10pm and 1 am
		/*
		 *  probability is also higher on Burns night, 25 January. Though there is a possibility that
		 *  the increased sightings of Haggis on Burns night is for the same reason that there are increased
		 *  sightings after the pubs close. Unable to find any papers investigating the correlation of these
		 *  two variables, so assuming they each cause a 10 fold increase in the probability of seeing a Wild Haggis
		 */
		boolean isBurns = localDateTime.getMonth() == Month.JANUARY && localDateTime.getDayOfMonth() == 25;
		
		/**
		 * Haggis only exist in Scotland, north of the Central belt, so do some checks that 
		 * we're actually near the highlands. 
		 */
		

		// apparently more North Americans believe the Haggis to be real, so will also increase the
		// detection probability if the computer Locale is American.
		boolean isUSA = Locale.getDefault() == Locale.US;
		
		boolean regionOK = true;
		if (params.scotlandOnly) {
			// check the global location - easiest way to see where we are
			LatLong latLong = MasterReferencePoint.getLatLong();
			if (latLong != null) {
				if (latLong.getLatitude() < 55 || latLong.getLatitude() > 60) {
					// not not Shetland, but might be Orkney
					regionOK = false;
				}
				if (latLong.getLongitude() < -9 || latLong.getLongitude() > -1.5) {
					regionOK = false;
				}
			}
		}

		double upScale = 1;
		if (afterPub) {
			upScale *= 10;
		}
		if (isBurns) {
			upScale *= 10;
		}
		if (isUSA) {
			upScale *= 10;
		}
		/*
		 * Seeing Wild Haggis is VERY random. Generate a random number from a Guassian distribution
		 * with a width of .01 and then scale for the enhanced probabilities.
		 */
		double p = random.nextDouble();
		p = Math.pow(p, 10); // raising to pow 10 skews distribution heavily towards 0. 
		p *= upScale;
		p = Math.min(p, 1.);
		
		// use the region information
		if (regionOK == false) {
			p = 0;
		}
		
		// and compare to the classification threshold.
		boolean isDet = p > params.minConfidence;
		if (!isDet && !params.keepUnclassified) {
			return null; // not a detection.
		}
		// now assign a random classifiction to the sub species.
		int id = random.nextInt(0, 3);
		HaggisTypes hagType = HaggisTypes.values()[id];

		// bound the score to 1 and return a classification.
		return new HaggisClass(hagType, Math.min(p,  1.0));
	}

}
