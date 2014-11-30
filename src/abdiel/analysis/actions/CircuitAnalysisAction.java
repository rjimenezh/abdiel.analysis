package abdiel.analysis.actions;

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
		IFile modelFile = WorkspaceSynchronizer.getFile(model.eResource());
		try {
			IMarker marker = modelFile.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, "testing");
			marker.setAttribute(IMarker.LOCATION, "1");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		}
		catch(CoreException ce) {
			System.err.println(ce);
		}
		Circuit circuit = (Circuit)model.getElement();
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

}