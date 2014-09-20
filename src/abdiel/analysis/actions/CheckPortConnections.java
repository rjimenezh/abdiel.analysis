package abdiel.analysis.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import circuit.Circuit;
import circuit.Part;
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
	 * 
	 * @param circuit ABDIEL circuit to analyze.
	 */
	@Override
	public void analyze(Circuit circuit) {
		for(PortConnection conn : circuit.getPortConns()) {			
			checkProtocols(conn);
			checkRoleConsistency(conn);
		}
	}
	
	/**
	 * Validates that the protocols of the ports in a port
	 * connection match each other.
	 * 
	 * @param conn Port connection to analyze for protocol consistency
	 */
	protected void checkProtocols(PortConnection conn) {
		Port source = conn.getSource();
		Port target = conn.getTarget();
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
	
	/**
	 * Checks the port wiring of each port in a connection and
	 * determines whether the wiring is consistent, i.e. whether
	 * both ports export the same set of roles, and whether all
	 * roles in both ports are properly wired to a concrete pin.
	 * 
	 * @param conn Port connection to analyze for protocol consistency
	 */
	protected void checkRoleConsistency(PortConnection conn) {
		Port source = conn.getSource();
		Port target = conn.getTarget();
		System.err.println("Checking " + source.getName() + "->" + target.getName()
			+ " roles...");
		List<PortWiring> sourceWires = getWiresFor(source);
		List<PortWiring> targetWires = getWiresFor(target);
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
	
	/**
	 * Finds and returns the list of port wirings attached
	 * to a particular port.
	 * 
	 * @param port Port whose port wirings are desired
	 * @return List of port wirings connected to the specified port
	 */
	protected List<PortWiring> getWiresFor(Port port) {
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
