package camels;
import java.util.Arrays;
import java.util.Random;

/**
 * Factory, that generates instances of camels on demand
 * Singleton pattern is used
 *
 * @author Jakub Krizanovsky, Stanislav Kafara
 * @version 30.11.2022
 */
public class CamelFactory {
	
	private static final Random R = new Random();
	
	/** How close the random characteristics of ideal camels are to the max value, 1 ... max value */
	private static final double IDEAL_RATIO = 1;
	
	/** Maximum movement speed among ideal camels */
	private double maxCamelMovementSpeed = 0;
	/** Maximum travel distance among ideal camels */
	private double maxCamelDistance = 0;
	
	private CamelType[] camelTypes;
	private Camel[] idealCamels;
	
	/** The one and only instance of this class (singleton) */
	private static final CamelFactory INSTANCE = new CamelFactory();
	/** Private constructor (singleton) */
	private CamelFactory() {}
	
	
	/**
	 * Method to get the single instance (singleton)
	 * @return the single instance
	 */
	public static CamelFactory getInstance() {
		return INSTANCE;
	}
	
	/**

	 * Used to set the available camel types when they're loaded
	 * @param camelTypes camel types to set
	 */
	public void setCamelTypes(CamelType[] camelTypes) {
		this.camelTypes = camelTypes;
	}
	

	/**
	 * Returns a randomly generated camel type based on their distributions.
	 * @return Randomly generated camel type.
	 */
	private CamelType getCamelType() {
		double rng = R.nextDouble();
		int i = 0;
		while(rng > camelTypes[i].getProportionalRepresentation()) {
			rng -= camelTypes[i].getProportionalRepresentation();
			i++;
		}
		
		return camelTypes[i];
	}
	
	/**
	 * Returns a randomly generated camel based on the distributions of existing camel types
	 * and their properties.
	 * @return Randomly generated camel.
	 */
	public Camel getCamel() {
		CamelType cT = getCamelType();
		return new Camel(cT, getRandomMovementSpeed(cT), getRandomDistance(cT), false);
	}
	
	/**
	 * Returns a randomly generated anonymous (without index) camel
	 * based on the distributions of existing camel types and their properties.
	 * @return Randomly generated camel.
	 */
	public Camel getAnonymousCamel() {
		CamelType cT = getCamelType();
		return new Camel(cT, getRandomMovementSpeed(cT), getRandomDistance(cT), true);
	}
	
	/**
	 * Returns the ideal camels.
	 * @return Ideal camels.
	 */
	public Camel[] getIdealCamels() {
		if(idealCamels != null) {
			return idealCamels;
		}
		
		idealCamels = new Camel[camelTypes.length];
		for(int i = 0; i < idealCamels.length; i++) {
			CamelType cT = camelTypes[i];
			idealCamels[i] = new Camel(cT, IDEAL_RATIO * cT.getMaxMovementSpeed(), IDEAL_RATIO * cT.getMaxDistance(), true);
		}
		
		return idealCamels;
	}
	
	private double getRandomMovementSpeed(CamelType cT) {
		return cT.getMinMovementSpeed() + R.nextDouble() * (cT.getMaxMovementSpeed() - cT.getMinMovementSpeed());
	}

	private double getRandomDistance(CamelType cT) {
		return (cT.getMinDistance() + cT.getMaxDistance())/2 + R.nextGaussian()*(cT.getMaxDistance() - cT.getMinDistance())/4;
	}

	/**
	 * Returns the movement speed of the fastest ideal camel.
	 * @return Movement speed of the fastest ideal camel.
	 */
	public double getMaxCamelMovementSpeed() {
		if(maxCamelMovementSpeed > 0) {
			return maxCamelMovementSpeed;
		}
			
		maxCamelMovementSpeed = Arrays.stream(getIdealCamels())
				.mapToDouble(camel -> camel.getMovementSpeed())
				.max()
				.getAsDouble();
		
		return maxCamelMovementSpeed;
	}
	
	/**
	 * Returns the distance camel can cover without drinking of the least thirsty ideal camel.
	 * @return Distance camel can cover without drinking of the least thirsty ideal camel
	 */
	public double getMaxCamelDistance() {
		if(maxCamelDistance > 0) {
			return maxCamelDistance;
		}
		
		maxCamelDistance = Arrays.stream(getIdealCamels())
				.mapToDouble(camel -> camel.getDistance())
				.max()
				.getAsDouble();
		
		return maxCamelDistance;
	}
}
