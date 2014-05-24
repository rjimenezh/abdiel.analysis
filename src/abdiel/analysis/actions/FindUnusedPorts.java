package abdiel.analysis.actions;

import org.eclipse.emf.common.util.EList;

import circuit.Circuit;
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
		EList<PortConnection> portConns = circuit.getPortConns();
		//
		for(Part eachPart : circuit.getParts()) {
			// TODO skip generic ucs
			for(Port eachPort : eachPart.getPorts()) {
				if(isUnconnected(eachPort, portConns))
					System.err.println(eachPort.getName() + " is unused");
			}
		}
	}
	
	protected boolean isUnconnected(Port port, EList<PortConnection> portConns) {
		boolean unConnected = true;
		for(PortConnection eachConn : portConns) {
			if(eachConn.getSource() == port || eachConn.getTarget() == port) {
				unConnected = false;
				break;
			}
		}
		return unConnected;
	}
}
