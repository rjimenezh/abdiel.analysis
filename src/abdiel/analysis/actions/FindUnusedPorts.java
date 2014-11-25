package abdiel.analysis.actions;

import abdiel.analysis.AbdielUtils;
import circuit.Circuit;
import circuit.GenericAtmelUC;
import circuit.Part;
import circuit.Port;

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

	/**
	 * Goes through the list of circuit parts
	 * and for each part that is not a 
	 * generic micro-controller, analyzes ports
	 * and finds unconnected ports.
	 * 
	 * @circuit ABDIEL circuit to analyze.
	 */
	@Override
	public void analyze(Circuit circuit) {
		for(Part eachPart : circuit.getParts()) {
			if(eachPart instanceof GenericAtmelUC)
				continue;
			for(Port eachPort : eachPart.getPorts()) {
				if(AbdielUtils.isUnconnected(eachPort))
					System.err.println(eachPort.getName() + " is unused");
			}
		}
	}
}
