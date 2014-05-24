package abdiel.analysis.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import circuit.Circuit;
import circuit.Port;
import circuit.PortConnection;
import circuit.PortWiring;

/**
 * The CheckPortConnections analysis action
 * verifies that for all port connections in 
 * a circuit:
 * 
 * * The connected ports' protocol matches
 * * The connected ports' role list matches
 * * All roles have a port wire back to a concrete pin
 *   for both connected parts.
 * 
 * @author Ramon Jimenez
 *
 */
public class CheckPortConnections extends CircuitAnalysisAction {

	/**
	 * Analyze a circuit by validating
	 * its port connections' protocols,
	 * role consistency (identical role
	 * sets on both connected ports),
	 * and wiring.
	 */
	@Override
	public void analyze(Circuit circuit) {
		EList<PortWiring> wires = circuit.getPortWires();
		for(PortConnection conn : circuit.getPortConns()) {
			Port source = conn.getSource();
			Port target = conn.getTarget();
			checkProtocols(source, target);
			checkRoleConsistency(source, target, wires);
		}
	}
	
	protected void checkProtocols(Port source, Port target) {
		System.err.println("Checking " + source.getName() + "->" + target.getName()
			+ " protocols...");
		String sourceProto = source.getProtocol();
		String targetProto = target.getProtocol();
		if(sourceProto != null && targetProto != null) {
			if(!(sourceProto.equals(targetProto)))
				System.err.println("Protocol mismatch");  // flag error
		}
		else
			System.err.println("Null protocol"); // flag error
	}
	
	protected void checkRoleConsistency(Port source, Port target,
	EList<PortWiring> wires) {
		System.err.println("Checking " + source.getName() + "->" + target.getName()
			+ " roles...");
		List<PortWiring> sourceWires = getWiresFor(wires, source);
		List<PortWiring> targetWires = getWiresFor(wires, target);
		List<String> roles = new ArrayList<String>();
		for(PortWiring srcWire : sourceWires) {
			if(srcWire.getPin() == null)
				System.err.println("Unconnected src role"); // flag error
			roles.add(srcWire.getRole());
		}
		for(PortWiring tgtWire : targetWires) {
			if(tgtWire.getPin() == null)
				System.err.println("Unconnected tgt role"); // flag error
			roles.remove(tgtWire.getRole());
		}
		if(!(roles.isEmpty()))
			System.err.println("Role mismatch"); // flag error
	}
	
	protected List<PortWiring> getWiresFor(EList<PortWiring> wires, Port port) {
		List<PortWiring> wiresFor = new ArrayList<PortWiring>();
		for(PortWiring eachWire : wires)
			if(eachWire.getPort() == port)
				wiresFor.add(eachWire);
		
		return wiresFor;
	}
}
