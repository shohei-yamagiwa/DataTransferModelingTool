package graphicalrefactor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import graphicalrefactor.editor.Editor;
import graphicalrefactor.views.DataTransferModelingTool;

public abstract class AbstractSystemAction extends AbstractAction {

	protected DataTransferModelingTool frame;

	public AbstractSystemAction(String name, DataTransferModelingTool frame) {
		super(name);
		this.frame = frame;
	}

	public void setFrame(DataTransferModelingTool frame) {
		this.frame = frame;
	}

}
