package graphicalrefactor.actions;

import java.awt.event.ActionEvent;

import graphicalrefactor.editor.Editor;
import graphicalrefactor.views.DataTransferModelingTool;

public class SaveAction extends AbstractSystemAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5660460585305281982L;

	public SaveAction(DataTransferModelingTool frame) {
		super("Save", frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Editor editor = frame.getEditor();
		if (editor != null && editor.getCurFileName() != null) {
			editor.save();
		}
	}

}
