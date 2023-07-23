package graphicalrefactor.views;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import graphicalrefactor.editor.Editor;

public class DataTransferModelingTool extends JFrame {
	private static final long serialVersionUID = -8690140317781055614L;
	public static final String title = "Data Transfer Modeling Tool";
	
	private Editor editor;
	private mxGraph graph;
	private DataTransferModelingMenuBar menuBar;
	private mxGraphComponent graphComponent;

	public DataTransferModelingTool() {
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		graph = new mxGraph() {
			public boolean isPort(Object cell) {
				mxGeometry geo = getCellGeometry(cell);
				
				return (geo != null) ? geo.isRelative() : false;
			}
			
			public boolean isCellFoldable(Object cell, boolean collapse) {
				return false;
			}
		};
		
		editor = new Editor(graph);
		
		graphComponent = new mxGraphComponent(graph) {			
			protected mxICellEditor createCellEditor() {
				return new DataTransferModelingCellEditor(this, editor);
			}
		};
		graph.getModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				List<mxCell> terminals = new ArrayList<>();
				mxCell cell = null;
				for (Object change: ((List) evt.getProperties().get("changes"))) {
					if (change instanceof mxGraphModel.mxTerminalChange) {
						mxGraphModel.mxTerminalChange terminalChange = (mxGraphModel.mxTerminalChange) change;
						cell = (mxCell) terminalChange.getCell();
						mxCell terminal = (mxCell) terminalChange.getTerminal();
						terminals.add(terminal);
					}
				}
				if (terminals.size() == 2) {
					if (!editor.connectEdge(cell, terminals.get(0), terminals.get(1))) {
						graph.removeCells(new mxCell[] {cell});
					}
				}
			}
		});
		getContentPane().add(graphComponent);
		new mxRubberband(graphComponent);
		graph.setAllowDanglingEdges(false);
		graph.setCellsDisconnectable(true);
				
		menuBar = new DataTransferModelingMenuBar(this);
		setJMenuBar(menuBar);
		setSize(870, 640);
	}

	public mxGraph getGraph() {
		return graph;
	}

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	public Editor getEditor() {
		return editor;
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

}
