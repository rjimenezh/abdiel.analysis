package abdiel.analysis.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;

import abdiel.analysis.AbdielUtils;
import circuit.Circuit;
import circuit.GenericAtmelUC;
import circuit.Part;

/**
 * The RefineGenericUC analysis action finds instances
 * of the GenericAtmelUC part and suggests concrete
 * Atmel microcontrollers that can satisfy the requirements
 * of the circuit design as evidenced by port and pin
 * connections of the generic part.
 * 
 * @author ramon
 *
 */
public class RefineGenericUC extends CircuitAnalysisAction {
	
	/** List of candidate concrete micro-controllers to refine generic ones into. */
	protected List<UCSpecification> candidateUCs;
	
	/** List of proposed micro-controller refinements, grouped by applicable generic UC. */
	protected Map<GenericAtmelUC, List<UCSpecification>> refinements;
	
	/**
	 * Default constructor.  GMF seems
	 * to call this exactly once and reuse
	 * the instance repeatedly over the
	 * editing instance's lifetime.
	 */
	public RefineGenericUC() {
		initializeCandidateUCs();
		refinements = new HashMap<GenericAtmelUC, List<UCSpecification>>();
	}
	
	/**
	 * Initialize the list of candidate micro-controllers.
	 * Since this is expected to change seldom, it is hard-coded,
	 * culled from Atmel's datasheets for popular 8-bit micros.
	 */
	protected void initializeCandidateUCs() {
		candidateUCs = new ArrayList<UCSpecification>();
		UCSpecification attiny85 = new UCSpecification("ATTiny85");
		attiny85.setDigitalPins(5);
		attiny85.setAnalogPins(3);
		attiny85.setHasUART(false);
		attiny85.setHasUSART(false);
		attiny85.setHasUSI(true);
		attiny85.setHasSPI(true);
		attiny85.setHasTWI(false);
		candidateUCs.add(attiny85);
		//
		UCSpecification attiny2313 = new UCSpecification("ATTiny2313");
		attiny2313.setDigitalPins(17);
		attiny2313.setAnalogPins(0);
		attiny2313.setHasUART(true);
		attiny2313.setHasUSART(true);
		attiny2313.setHasUSI(true);
		attiny2313.setHasSPI(true);
		attiny2313.setHasTWI(false);
		candidateUCs.add(attiny2313);
		//
		UCSpecification atmega328 = new UCSpecification("ATMega328P");
		atmega328.setDigitalPins(20);
		atmega328.setAnalogPins(6);
		atmega328.setHasUART(true);
		atmega328.setHasUSART(true);
		atmega328.setHasUSI(true);
		atmega328.setHasSPI(true);
		atmega328.setHasTWI(true);
		candidateUCs.add(atmega328);
		// Important - sort the collection!
		Collections.sort(candidateUCs);
	}
	
	/**
	 * Goes through the list of circuit parts
	 * and for each generic micro-controller,
	 * attempts to find and suggest a suitable
	 * concrete micro-controller.
	 * 
	 * @circuit ABDIEL circuit to analyze.
	 */
	@Override
	public void analyze(Circuit circuit) {
		EList<Part> parts = circuit.getParts();
		for(Part eachPart : parts)
			if(eachPart instanceof GenericAtmelUC)
				analyzeUC((GenericAtmelUC)eachPart);
		//
		MessageDialog.openInformation(
			shell,
			"Generic Microcontroller Refinement Results",
			getRefinementResults());
		
	}

	/**
	 * Determines required features of a generic micro-controller.
	 * Builds a {@link UCSpecification} from said features and attempts
	 * to find a candidate specification that meets the requirements.
	 * 
	 * @param uc Generic micro-controller model to define requirements from
	 */
	protected void analyzeUC(GenericAtmelUC uc) {
		UCSpecification req = new UCSpecification(uc.getName());
		req.setAnalogPins(AbdielUtils.countPinConns(uc, "analogPin"));
		req.setDigitalPins(AbdielUtils.countPinConns(uc, "digitalPin"));
		req.setHasUART(AbdielUtils.isConnected(uc, "UART"));
		req.setHasUSART(AbdielUtils.isConnected(uc, "USART"));
		req.setHasUSI(AbdielUtils.isConnected(uc, "USI"));
		req.setHasSPI(AbdielUtils.isConnected(uc, "SPI"));
		req.setHasTWI(AbdielUtils.isConnected(uc, "TWI"));
		// 
		for(UCSpecification eachCandidate : candidateUCs) {
			if(eachCandidate.compareTo(req) != UCSpecification.SMALLER_THAN_OTHER)
				logRefinement(uc, eachCandidate);
		}
	}
	
	/**
	 * Logs the specified concrete micro-controller as a suitable
	 * refinement for the specified generic micro-controller.
	 * 
	 * @param uc Generic UC for which a concrete refinement has been found
	 * @param ucSpec Concrete refinement found for the specified generic UC
	 */
	protected void logRefinement(GenericAtmelUC uc, UCSpecification ucSpec) {
		List<UCSpecification> concreteUCs = refinements.get(uc);
		if(concreteUCs == null) {
			concreteUCs = new ArrayList<UCSpecification>();
			refinements.put(uc, concreteUCs);
		}
		concreteUCs.add(ucSpec);
	}
	
	/**
	 * Prepares the refinement results report.
	 * 
	 * @return A user-friendly string detailing the refinement results
	 */
	protected String getRefinementResults() {
		// Scenario 1: no UCs to refine
		Set<GenericAtmelUC> genericUCs = refinements.keySet();
		if(genericUCs.isEmpty())
			return "The circuit has no generic micro-controllers to refine";
		// Scenario 2: no refinements found
		int nRefinements = 0;
		for(GenericAtmelUC genUC : genericUCs) {
			List<UCSpecification> concreteUCs = refinements.get(genUC);
			if(concreteUCs != null)
				nRefinements += concreteUCs.size();
		}
		if(nRefinements == 0)
			return "No suitable refinements were found for the "
				+ "generic-microcontrollers in the circuit";
		// Scenario 3: refinements found for generic UCs
		StringBuilder results = new StringBuilder();
		results.append("The following refinements were found for the specified ")
			.append("generic micro-controllers in this circuit:\n\n");
		for(GenericAtmelUC genUC : genericUCs) {
			List<UCSpecification> concreteUCs = refinements.get(genUC);
			if(concreteUCs != null) {
				results.append("* ").append(genUC.getName()).append(":\n");
				for(UCSpecification concreteUC : concreteUCs)
					results.append("\t-")
						.append(concreteUC.getName())
						.append(" is a suitable refinement\n");
			}
		}
		return results.toString();
	}
}