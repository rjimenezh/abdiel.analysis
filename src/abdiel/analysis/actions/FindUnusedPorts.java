package abdiel.analysis.actions;

import org.eclipse.emf.common.util.EList;

import circuit.Circuit;
import circuit.GenericAtmelUC;
import circuit.Part;
import circuit.Port;
import circuit.PortConnection;

/**
 * The FindUnusedPorts anaylsis action
 * issues warnings for ports left unconnected
 * in the circuit.  The exception are
 * generic micro-controller ports left
 * unconnected, as by definition generic
 * micro-controllers define a superset of
 * ports that not all designs may use. 
 * 
 * @author Ramon Jimenez
 *
 */
public class FindUnusedPorts extends CircuitAnalysisAction {

	@Override
	public void analyze(Circuit circuit) {
		for(Part eachPart : circuit.getParts()) {
			if(eachPart instanceof GenericAtmelUC)
				continue;
			for(Port eachPort : eachPart.getPorts()) {
				if(isUnconnected(eachPort))
					System.err.println(eachPort.getName() + " is unused");
			}
		}
	}
	
	protected boolean isUnconnected(Port port) {
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
}
