package camels;

/**
 * Represents a type of a camel.
 * @author Jakub Krizanovsky
 */
public class CamelType {
	
	private final String name;
	private final double minMovementSpeed;
	private final double maxMovementSpeed;
	private final double minDistance;
	private final double maxDistance;
	private final double drinkTime;
	private final int maxLoad;
	private final double proportionalRepresentation;
	
	/**
	 * Constructs a type of a camel.
	 * @param name Name.
	 * @param minMovementSpeed Minimal movement speed.
	 * @param maxMovementSpeed Maximal movement speed.
	 * @param minDistance Minimal distance the camel type can cover without drinking.
	 * @param maxDistance Maximal distance the camel type can cover without drinking.
	 * @param drinkTime Time it takes the camel type to drink.
	 * @param maxLoad Maximal basket count he camel type can carry.
	 * @param proportionalRepresentation Proportional representation of the camel type.
	 */
	public CamelType(String name, double minMovementSpeed, double maxMovementSpeed, double minDistance,
			double maxDistance, double drinkTime, int maxLoad, double proportionalRepresentation) {
		this.name = name;
		this.minMovementSpeed = minMovementSpeed;
		this.maxMovementSpeed = maxMovementSpeed;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.drinkTime = drinkTime;
		this.maxLoad = maxLoad;
		this.proportionalRepresentation = proportionalRepresentation;
	}

	/**
	 * Returns the name of the camel type.
	 * @return Name of the camel type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the minimal movement speed of the camel type.
	 * @return Minimal movement speed of the camel type.
	 */
	public double getMinMovementSpeed() {
		return minMovementSpeed;
	}

	/**
	 * Returns the maximal movement speed of the camel type.
	 * @return Maximal movement speed of the camel type.
	 */
	public double getMaxMovementSpeed() {
		return maxMovementSpeed;
	}

	/**
	 * Returns the minimal distance the camel type can cover without drinking.
	 * @return Minimal distance the camel type can cover without drinking.
	 */
	public double getMinDistance() {
		return minDistance;
	}
	
	/**
	 * Returns the maximal distance the camel type can cover without drinking.
	 * @return Minimal maximal the camel type can cover without drinking.
	 */
	public double getMaxDistance() {
		return maxDistance;
	}

	/**
	 * Returns the time it takes the camel type to drink.
	 * @return Time it takes the camel type to drink.
	 */
	public double getDrinkTime() {
		return drinkTime;
	}

	/**
	 * Returns the maximal basket count the camel type can carry.
	 * @return Maximal basket count the camel type can carry.
	 */
	public int getMaxLoad() {
		return maxLoad;
	}

	/**
	 * Returns the proportional representation of the camel type.
	 * @return Proportional representaion of the camel type.
	 */
	public double getProportionalRepresentation() {
		return proportionalRepresentation;
	}

}
