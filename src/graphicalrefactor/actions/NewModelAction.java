package graphicalrefactor.actions;

import java.awt.event.ActionEvent;

import graphicalrefactor.editor.Editor;
import graphicalrefactor.views.DataTransferModelingTool;

public class NewModelAction extends AbstractSystemAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8484493203589724589L;

	public NewModelAction(DataTransferModelingTool frame) {
		super("Model", frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		frame.getEditor().clear();
		frame.setTitle(frame.title);		
	}

}
