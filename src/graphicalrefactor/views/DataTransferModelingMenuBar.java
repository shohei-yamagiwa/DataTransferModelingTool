package graphicalrefactor.views;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import graphicalrefactor.actions.CircleLayoutAction;
import graphicalrefactor.actions.DAGLayoutAction;
import graphicalrefactor.actions.DeleteAction;
import graphicalrefactor.actions.ExitAction;
import graphicalrefactor.actions.JavaPrototypeGenerateAction;
import graphicalrefactor.actions.JerseyPrototypeGenerateAction;
import graphicalrefactor.actions.NewChannelAction;
import graphicalrefactor.actions.NewFormulaChannelAction;
import graphicalrefactor.actions.NewIOChannelAction;
import graphicalrefactor.actions.NewModelAction;
import graphicalrefactor.actions.NewResourceAction;
import graphicalrefactor.actions.OpenAction;
import graphicalrefactor.actions.SaveAction;
import graphicalrefactor.actions.SaveAsAction;
import graphicalrefactor.actions.TreeLayoutAction;
import graphicalrefactor.actions.ZoomInAction;
import graphicalrefactor.actions.ZoomOutAction;
import graphicalrefactor.editor.Editor;

public class DataTransferModelingMenuBar extends JMenuBar {
	private static final long serialVersionUID = 4811536194182272888L;

	private DataTransferModelingTool graphicalModelRefactor = null;
	private NewResourceAction newResourceAction = null;
	private NewChannelAction newChannelAction = null;
	private NewIOChannelAction newIOChannelAction = null;
	private NewFormulaChannelAction newFormulaChannelAction = null;
	private DeleteAction deleteAction = null;
	private JavaPrototypeGenerateAction javaPrototypeGenerateAction = null;
	private JerseyPrototypeGenerateAction jerseyPrototypeGenerateAction = null;
	private DAGLayoutAction dagLayoutAction = null;
	private TreeLayoutAction treeLayoutAction = null;
	private CircleLayoutAction circleLayoutAction = null;

	public DataTransferModelingMenuBar(DataTransferModelingTool graphicalModelRefactor) {
		this.graphicalModelRefactor = graphicalModelRefactor;
		JMenu newMenu = new JMenu("New");
		
		newMenu.add(new NewModelAction(graphicalModelRefactor));

		newMenu.add(newResourceAction = new NewResourceAction(graphicalModelRefactor.getEditor()));
		newMenu.add(newChannelAction = new NewChannelAction(graphicalModelRefactor.getEditor()));
		newMenu.add(newIOChannelAction = new NewIOChannelAction(graphicalModelRefactor.getEditor()));
		newMenu.add(newFormulaChannelAction = new NewFormulaChannelAction(graphicalModelRefactor.getEditor()));

		JMenu menu = null;
		menu = add(new JMenu("File"));
		menu.add(newMenu);
		menu.add(new OpenAction(graphicalModelRefactor));
		menu.addSeparator();
		menu.add(new SaveAction(graphicalModelRefactor));
		menu.add(new SaveAsAction(graphicalModelRefactor));
		menu.addSeparator();
		menu.add(new ExitAction());

		menu = add(new JMenu("Edit"));
		menu.add(deleteAction = new DeleteAction(graphicalModelRefactor.getEditor()));


		menu = add(new JMenu("Layout"));
		menu.add(dagLayoutAction  = new DAGLayoutAction(graphicalModelRefactor.getEditor()));
		menu.add(treeLayoutAction  = new TreeLayoutAction(graphicalModelRefactor.getEditor()));
		menu.add(circleLayoutAction   = new CircleLayoutAction(graphicalModelRefactor.getEditor()));

		menu = add(new JMenu("View"));
		menu.add(new ZoomInAction(graphicalModelRefactor.getGraphComponent()));
		menu.add(new ZoomOutAction(graphicalModelRefactor.getGraphComponent()));

		menu = add(new JMenu("Generate"));
		menu.add(javaPrototypeGenerateAction = new JavaPrototypeGenerateAction(graphicalModelRefactor.getEditor()));
		menu.add(jerseyPrototypeGenerateAction = new JerseyPrototypeGenerateAction(graphicalModelRefactor.getEditor()));
	}

	public Editor getEditor() {
		return graphicalModelRefactor.getEditor();
	}

	public void setEditor(Editor editor) {
		newResourceAction.setEditor(editor);
		newChannelAction.setEditor(editor);
		newIOChannelAction.setEditor(editor);
		deleteAction.setEditor(editor);
		javaPrototypeGenerateAction.setEditor(editor);
		jerseyPrototypeGenerateAction.setEditor(editor);
		treeLayoutAction.setEditor(editor);
		circleLayoutAction.setEditor(editor);
	}
}
