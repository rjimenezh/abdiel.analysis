package abdiel.analysis.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import circuit.ATTiny2313;
import circuit.Button;
import circuit.Circuit;
import circuit.ElectrolyticCap;
import circuit.GenericAtmelUC;
import circuit.Joint;
import circuit.LED;
import circuit.Net;
import circuit.Part;
import circuit.Pin;
import circuit.Polarity;
import circuit.RGLED;
import circuit.Resistor;
import circuit.SPSTSwitch;
import circuit.Wire;

/**
 * The PolarityCheck class analyses a circuit and determines
 * whether polarity-sensitive parts may be incorrectly
 * connected.  This analysis entails much more semantic
 * processing than others in this proof-of-concept, and
 * several options and design decisions are possible.  This
 * implementation is fairly simplistic.
 * 
 * @author Ramon Jimenez
 *
 */
public class CheckPolarity extends CircuitAnalysisAction {

	/**
	 * Determines whether there are possible polarity violations
	 * in the circuit.  This may imply a very complex analysis
	 * of the circuit as a directed cyclic graph, catering for
	 * situations such as voltage dividers, considering Kirchoff
	 * laws, batteries and other suitable devices connected in
	 * series, etc.
	 * 
	 * It is beyond this proof-of-concept implementation having 
	 * to deal with this level of complexity.  Instead, the
	 * analysis is carried out over the following basic premises:
	 * 
	 * o All nets are "flattened", i.e. if several nets have the
	 *   same name, then all pins reaching either such net are
	 *   considered to be connected among themselves
	 * o Only certain parts are deemed to be sensitive to
	 *   polarity issues
	 * o For polarity-sensitive parts, polarized pins are checked
	 *   to see if they are connected to an opposite polarity in
	 *   three specific ways:
	 *   
	 *   (1) Direct connection to an opposite polarity pin, via
	 *       any available wire (at the moment, this entails O(n2)
	 *       performance)
	 *   (2) Indirect connection to an opposite polarity pin through
	 *       a part that can be substituted with a serial impedance,
	 *       e.g. switch, button or resistor (considering the first
	 *       level - no recursion) of wires directly connected to
	 *       the opposite/transitive pin of said part)
	 *   (3) Indirect connection to an opposite polarity pin through
	 *       a net, considering the first level (no recursion) of
	 *       wires directly connected to the same logical net (same-named
	 *       net)
	 *       
	 *   Note that in all cases above, "opposite polarity" pin entails
	 *   ANY pin of opposite polarity on ANY part, such that for instance
	 *   an LED anode, when analyzed, will be flagged if it's connected
	 *   to a power supply's negative pin, even though power supplies
	 *   themselves are excluded from analysis (mostly to prevent flagging
	 *   fairly common series connections of batteries).
	 * 
	 * @param circuit ABDIEL circuit to analyze
	 */
	@Override
	public void analyze(Circuit circuit) {
		EList<Wire> wires = circuit.getWires();
		for(Wire wire : wires) {
			Joint[] joints = new Joint[2];
			joints[0] = wire.getSource();
			joints[1] = wire.getTarget();
			for(Joint joint : joints) {
				// Minor optimizations - only polarized pins are candidates
				if(!(joint instanceof Pin))
					continue;
				Pin pin = (Pin)joint;
				if(pin.getPolarity() == Polarity.NEUTRAL)
					continue;
				Part part = (Part)pin.eContainer();
				if(isPolaritySensitive(part)) {
					Joint targetJoint = joint == joints[0] ? joints[1] : joints[0];
					if(targetJoint instanceof Pin)
						analyzePin2PinConnections(pin, wire);
					else {
						Net targetNet = (Net)targetJoint;
						analyzePin2NetConnections(pin, targetNet, wire);
					}
				}
			}
		}
	}
	
	/**
	 * Analyze connections going out from a pin to other pins (not nets).
	 * 
	 * @param pin Pin whose connections to other pins are under analysis
	 * @wire Concrete wire being tested (for error reporting)
	 */
	protected void analyzePin2PinConnections(Pin pin, Wire wire) {
		EList<Pin> connectedPins = getConnectedPins(pin);
		for(Pin candidateCP : connectedPins)
			if(candidateCP.getPolarity() == Polarity.NEUTRAL)
				analyzePin2NeutralPinConnection(pin, candidateCP, wire);
			else
				if(candidateCP.getPolarity() != pin.getPolarity())
					flagPolarityIssue(pin, wire);
	}
	
	/**
	 * Anaylze connections going out from a pin to other neutral pins,
	 * where the "other end" said neutral pins lead to must be considered.
	 * 
	 * @param pin  Pin whose connections are under analysis
	 * @param targetPin Neutral pin to which the analyzed pin is connected
	 * @wire Concrete wire being tested (for error reporting)
	 */
	protected void analyzePin2NeutralPinConnection(Pin pin, Pin targetPin, Wire wire) {
		Part targetPart = (Part)targetPin.eContainer();
		if(isSerialImpedance(targetPart)) {
			Pin transitivePin = getTransitivePin(targetPart, targetPin);
			EList<Pin> transitivePins = getConnectedPins(transitivePin);
			for(Pin candidateTP : transitivePins)
				if(candidateTP.getPolarity() != Polarity.NEUTRAL
				&& candidateTP.getPolarity() != pin.getPolarity())
					flagPolarityIssue(pin, wire);
		}
	}

	/**
	 * Analyze connections going out from a pin to nets.
	 * 
	 * @param pin  Pin whose connections are under analysis
	 * @param targetNet  LOGICAL net to which the pin is connected
	 * @wire Concrete wire being tested (for error reporting)
	 */
	protected void analyzePin2NetConnections(Pin pin, Net targetNet, Wire wire) {
			EList<Pin> transitivePins = getConnectedPins(targetNet);
			for(Pin candidateTP : transitivePins)
				if(candidateTP.getPolarity() != Polarity.NEUTRAL
				&& candidateTP.getPolarity() != pin.getPolarity())
					flagPolarityIssue(pin, wire);
	}
	
	/**
	 * Determines whether a given part is sensitive
	 * to a wrong polarity connection, within the
	 * scope of this analysis plugin.  Power supplies
	 * are not of interest.
	 * 
	 * @param part Part to determine if polarity is a concern
	 * @return Whether polarity is a concern for the part
	 */
	protected boolean isPolaritySensitive(Part part) {
		if(part instanceof LED)
			return true;
		if(part instanceof RGLED)
			return true;
		if(part instanceof ElectrolyticCap)
			return true;
		if(part instanceof ATTiny2313)
			return true;
		if(part instanceof GenericAtmelUC)
			return true;
		
		return false;
	}
	
	/**
	 * Determines whether a part is a serial impedance,
	 * i.e. if it could be replaced by a suitable resistor
	 * placed in series.  Such parts don't have a polarity
	 * per se, but define nets or connections that may end
	 * up violating polarity constraints (e.g. a switch may
	 * connect an LED's anode to a battery's negative).
	 * 
	 * @param part  Part to analyze for serial impedance equivalence
	 * @return Whether the part can be substituted by a serial impedance
	 */
	protected boolean isSerialImpedance(Part part) {
		if(part instanceof Resistor)
			return true;
		if(part instanceof Button)
			return true;
		if(part instanceof SPSTSwitch)
			return true;
		
		return false;
	}
	
	/**
	 * Flags a potential polarity issue.
	 * 
	 * @param pin Pin for which a potentially incorrect connection is detected
	 * @param wire Concrete wire to potentially incorrect polarity connection
	 */
	protected void flagPolarityIssue(Pin pin, Wire wire) {
		Part part = (Part)pin.eContainer();
		Joint oppositeJoint = (pin == wire.getSource())
			? wire.getTarget() : wire.getSource();
		String target = null;
		// TODO joints should have names...
		if(oppositeJoint instanceof Net)
			target = ((Net)oppositeJoint).getName();
		else {
			Pin oppositePin = (Pin)oppositeJoint;
			Part oppositePart = (Part)oppositePin.eContainer();
			target = oppositePart.getName() + "." + oppositePin.getName();
		}
		String fqn = part.getName() + "." + pin.getName() +
			"->" + target;
		addMarker(fqn, 
			"Possible polarity issue with " + pin.getName(),
			IMarker.SEVERITY_WARNING);
	}
	
	/**
	 * Gets the "transitive pin" of a part, i.e.,
	 * given a part and a specific pin within it,
	 * returns the first pin of the part not the same
	 * as the specified pin.  In the case of a two-pin
	 * serial impedance, this returns the pin opposite
	 * to the provided pin, e.g. for a resistor r1,
	 * getTransitivePin(r1, t1) returns t2.
	 * 
	 * @param part Part to get transitive pin for
	 * @param pin  Pin whose opposite or transitive must be found;
	 * 	must belong to part
	 * @return Opposite or transitive pin relative to specified pin,
	 * 	<code>null</code> if not found
	 */
	protected Pin getTransitivePin(Part part, Pin pin) {
		Pin transitivePin = null;
		for(Pin candidatePin : part.getPins())
			if(candidatePin != pin) {
				transitivePin =  candidatePin;
				break;
			}
		
		return transitivePin;
	}
	
	/**
	 * Returns a list of all pins that have direct
	 * connections to this pin, i.e. there is some
	 * wire that connects both pins.
	 * 
	 * @param pin Pin whose connected pins are sought
	 * @return List of pins connected through wires to the specified pin
	 */
	protected EList<Pin> getConnectedPins(Pin pin) {
		EList<Pin> connectedPins = new BasicEList<Pin>();
		Part containingPart = (Part)pin.eContainer();
		Circuit ckt = (Circuit)containingPart.eContainer();
		EList<Wire> allWires = ckt.getWires();
		for(Wire wire : allWires)
			if(wire.getSource() == pin || wire.getTarget() == pin) {
				Joint connectedJoint = wire.getSource() == pin ? wire.getTarget() : wire.getSource();
				if(connectedJoint instanceof Pin)
					connectedPins.add((Pin)connectedJoint);
			}
		
		return connectedPins;
	}
	
	/**
	 * Returns a list of all pins that have direct
	 * LOGICAL connections to this net.  This means
	 * all pins that are connected to all nets in the
	 * diagram that share the same name.
	 * 
	 * @param net  Nets whose connected pins are sought
	 * @return List of pins logically connected to the specified net
	 */
	protected EList<Pin> getConnectedPins(Net net) {
		EList<Pin> connectedPins = new BasicEList<Pin>();
		Circuit ckt = (Circuit)net.eContainer();
		EList<Wire> wires = ckt.getWires();
		Joint[] joints = new Joint[2];
		for(Wire wire : wires) {
			joints[0] = wire.getSource();
			joints[1] = wire.getTarget();
			for(Joint joint : joints)
				if(joint instanceof Net) {
					Net candidateNet = (Net)joint;
					if(candidateNet.getName().equals(net.getName())) {
						Joint connectedJoint = joint == joints[0] ? joints[1] : joints[0];
						if(connectedJoint instanceof Pin)
							connectedPins.add((Pin)connectedJoint);
					}
				}
		}
		
		return connectedPins;
	}
}