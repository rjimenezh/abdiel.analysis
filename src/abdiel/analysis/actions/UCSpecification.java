package abdiel.analysis.actions;

import circuit.GenericAtmelUC;

/**
 * The UCSpecification class defines attributes of
 * microcontrollers, such as amount of pins and
 * supported ports.  Specifications can then be
 * queried to see if they match the requirements defined
 * by a generic UC.
 * 
 * @author ramon
 *
 */
public class UCSpecification implements Comparable<UCSpecification> {
	
	/** Comparable constant to state this uc is "smaller" than other uc. */
	protected static int SMALLER_THAN_OTHER = -1;
	
	/** Comparable constant to state this uc is the same as other uc. */
	protected static int SAME_AS_OTHER = 0;
	
	/** Comparable constant to state this uc is "bigger" than other uc. */
	protected static int BIGGER_THAN_OTHER = 1;
	
	/** Uc name. */
	protected String name;
	
	/** Amount of available digital pins (not including RESET). */
	protected int digitalPins;
	
	/** Amount of available analog pins (overlaps with digital pins). */
	protected int analogPins;
	
	/** Does this uc have UART? */
	protected boolean hasUART;
	
	/** Does this uc have USART? */
	protected boolean hasUSART;
	
	/** Does this uc have USI? */
	protected boolean hasUSI;
	
	/**
	 * Default constructor.  Creates a new
	 * micro-controller specification with
	 * the provided name.
	 * 
	 * @param name Name of micro-controller to specify
	 */
	public UCSpecification(String name) {
		this.name = name;
	}
	
	public int getDigitalPins() {
		return digitalPins;
	}

	public void setDigitalPins(int digitalPins) {
		this.digitalPins = digitalPins;
	}

	public int getAnalogPins() {
		return analogPins;
	}

	public void setAnalogPins(int analogPins) {
		this.analogPins = analogPins;
	}

	public String getName() {
		return name;
	}

	public boolean hasUART() {
		return hasUART;
	}

	public void setHasUART(boolean hasUART) {
		this.hasUART = hasUART;
	}

	public boolean hasUSART() {
		return hasUSART;
	}

	public void setHasUSART(boolean hasUSART) {
		this.hasUSART = hasUSART;
	}

	public boolean hasUSI() {
		return hasUSI;
	}

	public void setHasUSI(boolean hasUSI) {
		this.hasUSI = hasUSI;
	}

	/**
	 * Validates whether this specification
	 * matches the requirements of the specified
	 * generic micro-controller.
	 * 
	 * @param requiredUC UC for which a refinement is required
	 * @return whether this specification matches the required UC's specs
	 */
	public boolean matches(GenericAtmelUC requiredUC) {
		// Count digital pin connections
		// Count analog pin connections
		// Validate external crystal connection
		return false;
	}

	/**
	 * Determines whether this specification defines a
	 * micro-controller that's "smaller", same as, or 
	 * "bigger" than the passed-in specification.  The
	 * ranking criteria is:
	 * 
	 * o Digital pins
	 * o Analog pins
	 * o Serial comms (UART > USART > USI)
	 * o Presence of SPI
	 * o Presence of TWI/I2C
	 * 
	 * @param otherSpec Specification to compare to this spec
	 * @return whether this uc is "smaller", the same, or
	 * 	"bigger" than the other uc
	 */
	@Override
	public int compareTo(UCSpecification otherSpec) {
		if(digitalPins < otherSpec.digitalPins)
			return SMALLER_THAN_OTHER;
		else if(digitalPins > otherSpec.digitalPins)
			return BIGGER_THAN_OTHER;
		
		if(analogPins < otherSpec.analogPins)
			return SMALLER_THAN_OTHER;
		else if(analogPins > otherSpec.analogPins)
			return BIGGER_THAN_OTHER;
		
		if(!hasUART && otherSpec.hasUART)
			return SMALLER_THAN_OTHER;
		else if(hasUART && !otherSpec.hasUART)
			return BIGGER_THAN_OTHER;

		if(!hasUSART && otherSpec.hasUSART)
			return SMALLER_THAN_OTHER;
		else if(hasUSART && !otherSpec.hasUSART)
			return BIGGER_THAN_OTHER;
		
		if(!hasUSI && otherSpec.hasUSI)
			return SMALLER_THAN_OTHER;
		else if(hasUSI && !otherSpec.hasUSI)
			return BIGGER_THAN_OTHER;

		// TODO SPI, TWI
		
		return SAME_AS_OTHER;
	}
}