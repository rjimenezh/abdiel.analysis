package abdiel.analysis.actions;


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
	
	/** Does this uc support SPI? */
	protected boolean hasSPI;
	
	/** Does this uc support TWI (I2C)? */
	protected boolean hasTWI;
	
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
	
	public void setHasSPI(boolean hasSPI) {
		this.hasSPI = hasSPI;
	}
	
	public boolean hasSPI() {
		return hasSPI;
	}
	
	public void setHasTWI(boolean hasTWI) {
		this.hasTWI = hasTWI;
	}
	
	public boolean hasTWI() {
		return hasTWI;
	}

	/**
	 * Determines whether this specification defines a
	 * micro-controller that's "smaller", same as, or 
	 * "bigger" than the passed-in specification.  The
	 * comparison criteria is:
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
		boolean meetsDigitalPins = (digitalPins >= otherSpec.digitalPins);
		boolean meetsAnalogPins = (analogPins >= otherSpec.analogPins);
		boolean meetsUART = hasUART || !(otherSpec.hasUART);
		boolean meetsUSART = hasUSART || !(otherSpec.hasUSART);
		boolean meetsUSI = hasUSI || !(otherSpec.hasUSI);
		boolean meetsSPI = hasSPI || !(otherSpec.hasSPI);
		boolean meetsTWI = hasTWI || !(otherSpec.hasTWI); 

		if(meetsDigitalPins && meetsAnalogPins && meetsUART
			&& meetsUSART && meetsUSI && meetsSPI && meetsTWI)
				return SAME_AS_OTHER;
		
		return SMALLER_THAN_OTHER;
	}
}