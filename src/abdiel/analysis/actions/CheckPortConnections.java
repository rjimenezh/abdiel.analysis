package abdiel.analysis.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

import abdiel.analysis.AbdielUtils;
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
		String sourceProto = source.getProtocol();
		String targetProto = target.getProtocol();
		if(sourceProto != null && targetProto != null) {
			if(!(sourceProto.equals(targetProto)))
				addMarker(source.getName() + "->" + target.getName(),
					"Protocol mismatch in port connection", IMarker.SEVERITY_ERROR);
		}
		else {
			Port offendingPort = (sourceProto == null) ? source : target;
			Part part = (Part)offendingPort.eContainer();
			addMarker(part.getName() + "." + offendingPort.getName(),
				offendingPort.getName() + "'s protocol cannot be null", IMarker.SEVERITY_ERROR);
		}
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
		List<PortWiring> sourceWires = AbdielUtils.getWiresFor(source);
		List<PortWiring> targetWires = AbdielUtils.getWiresFor(target);
		Map<String, PortWiring> rolesWires = new HashMap<String, PortWiring>();
		for(PortWiring srcWire : sourceWires) {
			if(srcWire.getPin() == null)
				addDisconectedRoleMarker(srcWire);
			rolesWires.put(srcWire.getRole(), srcWire);
		}
		
		for(PortWiring tgtWire : targetWires) {
			if(tgtWire.getPin() == null)
				addDisconectedRoleMarker(tgtWire);
			rolesWires.remove(tgtWire.getRole());
		}
		
		if(!rolesWires.isEmpty())
			for(String role : rolesWires.keySet()) {
				PortWiring wire = rolesWires.get(role);
				Port port = wire.getPort();
				Part part = (Part)port.eContainer();
				addMarker(part.getName() + "." + port.getName() + "::" + role,
					"Role " + role + " must also be declared on opposite port",
					IMarker.SEVERITY_ERROR);
			}
	}

	/**
	 * Adds a user warning to signal that a wire
	 * with no proper role/pin connection was found.
	 * 
	 * @param wire Wire to report error for
	 */
	protected void addDisconectedRoleMarker(PortWiring wire) {
		String role =  wire.getRole();
		Port port = wire.getPort();
		Part part = (Part)port.eContainer();
		addMarker(part.getName() + "." + port.getName() + "::" + role,
			"Role " + role + " in " + port.getName() + " must be connected to a pin",
			IMarker.SEVERITY_ERROR);
	}
}
