package abdiel.analysis.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import circuit.Circuit;
import circuit.diagram.edit.parts.CircuitEditPart;

/**
 * The CircuitAnalysisAction class model an
 * abstract base clase to add circuit analysis
 * actions to ABDIEL.  Analysis actions are
 * visually represented as pop-up menu entries
 * under an "Analyze" sub-menu when right-clicking
 * circuits.
 * 
 * This class encapsulates all details of implementing
 * an Eclipse pop-up menu action.  Concrete subclasses
 * need only implement the actual circuit analysis logic
 * by virtue of implementing the @{link analyze} method.
 * 
 * @author Ramon Jimenez 
 *
 */
public abstract class CircuitAnalysisAction implements IObjectActionDelegate {

	/** Shell reference to provide concrete subclasses access to the UI. */
	protected Shell shell;

	/** Selected GMF CircuitEditPart of circuit being edited. */
	protected CircuitEditPart circuitEditPart;
	
	/** Private reference to model file (to attach markers). */
	private IFile modelFile;
	
	/** Action-specific feedback markers. */
	private List<IMarker> markers;
	
	/**
	 * Retrieves the shell reference from the workbench part.
	 *  
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public final void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * Carries out the action.  Derives the circuit from 
	 * the previously selected circuit edit part and delegates
	 * the analysis action to a concrete subclass via its
	 * {@link analyze(Circuit)} method.
	 * 
	 * @see IActionDelegate#run(IAction)
	 */
	public final void run(IAction action) {
		Diagram model = (Diagram)circuitEditPart.getModel();
		Circuit circuit = (Circuit)model.getElement();
		setUpMarkerSupportElements(model);
		analyze(circuit);
		/*
		MessageDialog.openInformation(
			shell,
			"ABDIEL Analysis",
			"Run Analysis was executed."); */
	}
	
	/**
	 * Extracts the GMF circuit part from the mouse
	 * selection (the circuit editor that was right-clicked
	 * by the user to trigger this pop-up menu).
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public final void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof CircuitEditPart)
				circuitEditPart = (CircuitEditPart)structuredSelection.getFirstElement();
		}		
	}

	/**
	 * Carry out actual circuit analysis.
	 */
	public abstract void analyze(Circuit circuit);
	
	/**
	 * Sets up marker support elements.  This:
	 * (1) obtains a reference to the model file in
	 * order to enable adding markers to it; (2) clears
	 * any previous markers a concrete analysis action
	 * may have emitted.
	 * 
	 * @param model Model element to resolve file for
	 */
	private void setUpMarkerSupportElements(Diagram model) {
		modelFile = WorkspaceSynchronizer.getFile(model.eResource());
		if(markers == null)
			markers = new ArrayList<IMarker>();
		try {
			for(IMarker marker : markers)
				marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		markers.clear();
	}
	
	/**
	 * Allows concrete analysis actions to mark model elements with
	 * their findings, enabling user feedback via platform-standard
	 * elements.
	 * 
	 * @param location Model-specific context to which marker applies
	 * @param message Message describing the analysis finding to report
	 * @param severity Report's severity level
	 */
	protected void addMarker(String location, String message, int severity) {
		try {
			IMarker marker = modelFile.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.LOCATION, location);
			marker.setAttribute(IMarker.SEVERITY, severity);
			markers.add(marker);
		}
		catch(CoreException ce) {
			System.err.println(ce);
		}
	}
}