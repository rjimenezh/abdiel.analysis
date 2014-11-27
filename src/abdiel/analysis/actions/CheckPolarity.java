package abdiel.analysis.actions;

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

	@Override
	public void analyze(Circuit circuit) {
		EList<Wire> wires = circuit.getWires();
		Joint[] joints = new Joint[2];
		for(Wire wire : wires) {
			joints[0] = wire.getSource();
			joints[1] = wire.getTarget();
			for(Joint joint : joints) {
				if(!(joint instanceof Pin))
					continue;
				Pin pin = (Pin)joint;
				if(pin.getPolarity() == Polarity.NEUTRAL)
					continue;
				Part part = (Part)pin.eContainer();
				if(isPolaritySensitive(part)) {
					Joint oppositeJoint = joint == joints[0] ? joints[1] : joints[0];
					// Case 1: oppositeJoint is a pin
					if(oppositeJoint instanceof Pin) {
						Pin oppositePin = (Pin)oppositeJoint;
						if(oppositePin.getPolarity() != Polarity.NEUTRAL) {
							if(oppositePin.getPolarity() != pin.getPolarity())
								System.err.println("Possible polarity issue: " + wire);
						}
						else {
							Part connectedPart = (Part)oppositePin.eContainer();
							if(isSerialImpedance(connectedPart)) {
								Pin transitivePin = getTransitivePin(connectedPart, oppositePin);
								EList<Pin> transitivePins = getConnectedPins(transitivePin);
								for(Pin candidateTP : transitivePins)
									if(candidateTP.getPolarity() != pin.getPolarity())
										System.err.println("Possible polarity issue: " + wire);
								// This may be needed also for non-serialZ parts
								EList<Pin> connectedPins = getConnectedPins(oppositePin);
								for(Pin candidateCP : connectedPins)
									if(candidateCP.getPolarity() != pin.getPolarity())
										System.err.println("Possible polarity issue: " + wire);
							}
						}
					}
					// Case 3: oppositeJoint is a net
					else {
						EList<Pin> transitivePins = getConnectedPins((Net)oppositeJoint);
						for(Pin candidateTP : transitivePins)
							if(candidateTP.getPolarity() != Polarity.NEUTRAL && candidateTP.getPolarity() != pin.getPolarity())
								System.err.println("Possible polarity issue: " + part.getName() + "." + pin.getName());
					}
				}
			}
		}
	}
	
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
	
	protected boolean isSerialImpedance(Part part) {
		if(part instanceof Resistor)
			return true;
		if(part instanceof Button)
			return true;
		if(part instanceof SPSTSwitch)
			return true;
		
		return false;
	}
	
	protected Pin getTransitivePin(Part part, Pin pin) {
		Pin transitivePin = null;
		for(Pin candidatePin : part.getPins())
			if(candidatePin != pin) {
				transitivePin =  candidatePin;
				break;
			}
		
		return transitivePin;
	}
	
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
