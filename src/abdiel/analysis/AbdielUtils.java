package abdiel.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import circuit.Circuit;
import circuit.Part;
import circuit.Pin;
import circuit.Port;
import circuit.PortConnection;
import circuit.PortWiring;
import circuit.Wire;

/**
 * The AbdielUtils class wraps convenience methods to traverse
 * an ABDIEL model, allowing analysis plug-ins to focus on
 * analysis itself as opposed to model traversal.  Ideally,
 * these methods should be part of model classes themselves
 * (assigned as per the Information Expert pattern), but more
 * reading is required to understand how to achieve this from
 * a model artifact as opposed to writing the methods with
 * "@Generated NOT" annotations, which can be lost if wiping
 * the code and regenerating anew from the models.
 * 
 * @author Ramon Jimenez
 *
 */
public class AbdielUtils {
	
	/**
	 * Determines whether there are no connections
	 * to a port.  Traverses the model to get the
	 * list of port connections on the port's containing
	 * circuit.
	 * 
	 * @param port Port to check for connectedness
	 * @return True if the port has no connections, false otherwise
	 */
	public static boolean isUnconnected(Port port) {
		boolean unConnected = true;
		Part part = (Part)port.eContainer();
		Circuit ckt = (Circuit)part.eContainer();
		EList<PortConnection> portConns = ckt.getPortConns();
		//
		for(PortConnection eachConn : portConns) {
			if(eachConn.getSource() == port || eachConn.getTarget() == port) {
				unConnected = false;
				break;
			}
		}
		return unConnected;
	}
	
	/**
	 * Finds a part's pin given its name.
	 * 
	 * @param part Part to search pin within
	 * @param pinName Pin name within part
	 * @return Pin with specified name within part, or <code>null</code>
	 * 	if no such pin is found.
	 */
	public static Pin findPinByName(Part part, String pinName) {
		for(Pin eachPin : part.getPins())
			if(eachPin.getName().equals(pinName))
				return eachPin;
		
		return null;
	}

	/**
	 * Counts how many wires are connected to a given part's pin.
	 * 
	 * @param part Part to determine number of incoming connections
	 * @param pinName Name of specific pin whose connections will be counted
	 * @param wires List of wires (joint connections) in circuit
	 * @return Number of wires connecting to the specified pin
	 */
	public static int countPinConns(Part part, String pinName) {
		Circuit ckt = (Circuit)part.eContainer();
		EList<Wire> wires = ckt.getWires();
		int pinConns = 0;
		Pin pin = findPinByName(part, pinName);
		for(Wire eachWire : wires)
			if(eachWire.getSource() == pin || eachWire.getTarget() == pin)
				pinConns++;
				
		return pinConns;
	}
	
	/**
	 * Finds a part's port given its name.
	 * 
	 * @param part Part to search port within
	 * @param portName Port name within part
	 * @return Port with specified name within part, or <code>null</code>
	 * 	if no such port is found.
	 */
	public static Port findPortByName(Part part, String portName) {
		for(Port eachPort : part.getPorts())
			if(eachPort.getName().equals(portName))
				return eachPort;
		
		return null;
	}
	
	/**
	 * Determines whether there are connections to the
	 * port with specified name within the specified part.
	 * 
	 * @param part Part to search port within
	 * @param portName Port name within part
	 * @return Whether the specified port has any conenctions;
	 * 	<code>false</code> if the port doesn't exist within the part.
	 */
	public static boolean isConnected(Part part, String portName) {
		Port port = findPortByName(part, portName);
		if(port == null)
			return false;
		return !isUnconnected(port);
	}
	
	/**
	 * Finds and returns the list of port wirings attached
	 * to a particular port.
	 * 
	 * @param port Port whose port wirings are desired
	 * @return List of port wirings connected to the specified port
	 */
	public static List<PortWiring> getWiresFor(Port port) {
		Part part = (Part)port.eContainer();
		Circuit ckt = (Circuit)part.eContainer();
		EList<PortWiring> wires = ckt.getPortWires();
		List<PortWiring> wiresFor = new ArrayList<PortWiring>();
		for(PortWiring eachWire : wires)
			if(eachWire.getPort() == port)
				wiresFor.add(eachWire);
		
		return wiresFor;
	}
}
