package graphicalrefactor.actions;

import java.awt.event.ActionEvent;

import graphicalrefactor.editor.Editor;

public class TreeLayoutAction extends AbstractEditorAction {

	public TreeLayoutAction(Editor editor) {
		super("Tree Layout", editor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		editor.setTreeLayout();
	}

}
