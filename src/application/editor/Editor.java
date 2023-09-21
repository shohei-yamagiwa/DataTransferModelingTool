package application.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import algorithms.DataTransferModelAnalyzer;
import algorithms.Validation;
import application.layouts.*;
import code.ast.CompilationUnit;
import models.Edge;
import models.EdgeAttribute;
import models.Node;
import models.dataConstraintModel.Channel;
import models.dataConstraintModel.ChannelMember;
import models.dataConstraintModel.ResourcePath;
import models.dataFlowModel.DataTransferModel;
import models.dataFlowModel.DataTransferChannel;
import models.dataFlowModel.PushPullAttribute;
import models.dataFlowModel.DataFlowEdge;
import models.dataFlowModel.DataFlowGraph;
import models.dataFlowModel.ResourceNode;
import models.visualModel.FormulaChannel;
import parser.Parser;
import parser.Parser.TokenStream;
import parser.exceptions.ExpectedAssignment;
import parser.exceptions.ExpectedChannel;
import parser.exceptions.ExpectedChannelName;
import parser.exceptions.ExpectedEquals;
import parser.exceptions.ExpectedFormulaChannel;
import parser.exceptions.ExpectedGeometry;
import parser.exceptions.ExpectedInOrOutOrRefKeyword;
import parser.exceptions.ExpectedIoChannel;
import parser.exceptions.ExpectedLeftCurlyBracket;
import parser.exceptions.ExpectedModel;
import parser.exceptions.ExpectedNode;
import parser.exceptions.ExpectedRHSExpression;
import parser.exceptions.ExpectedResource;
import parser.exceptions.ExpectedRightBracket;
import parser.exceptions.ExpectedStateTransition;
import parser.exceptions.WrongLHSExpression;
import parser.exceptions.WrongRHSExpression;
import parser.ParserDTRAM;

public class Editor {
	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;

	protected DataTransferModel model = null;
	protected mxGraph graph = null;
	private mxGraphComponent  graphComponent = null;

	protected DataFlowGraph dataFlowGraph = null;

	protected String curFileName = null;
	protected String curFilePath = null;
	protected ArrayList<CompilationUnit> codes = null;
	
	private boolean bReflectingArchitectureModel = false;

	public Editor(mxGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
		this.graph = graphComponent.getGraph();
		
		graphComponent.setCellEditor(new DataTransferModelingCellEditor(graphComponent, this));
	}

	public mxGraph getGraph() {
		return graph;
	}

	public mxGraphComponent getGraphComponent() {
		return this.graphComponent;
	}

	public DataTransferModel getModel() {
		if (model == null) {
			model = new DataTransferModel();
		}
		return model;
	}

	public DataFlowGraph getDataFlowGraph() {
		if (dataFlowGraph == null) {
			analyzeDataTransferModel(getModel());
		}
		return dataFlowGraph;
	}

	public DataFlowGraph analyzeDataTransferModel(DataTransferModel model) {
		DataFlowGraph flowGraph = DataTransferModelAnalyzer.createDataFlowGraphWithStateStoringAttribute(model);
		dataFlowGraph = DataTransferModelAnalyzer.annotateWithSelectableDataTransferAttiribute(flowGraph);
		updateEdgeAttiributes(dataFlowGraph);
		return dataFlowGraph;
	}

	public void resetDataFlowGraph() {
		dataFlowGraph = null;
	}

	public void setDataFlowGraph(DataFlowGraph dataFlowGraph) {
		this.dataFlowGraph = dataFlowGraph;
	}

	public ArrayList<CompilationUnit> getCodes() {
		return codes;
	}

	public void setCodes(ArrayList<CompilationUnit> codes) {
		this.codes = codes;
	}

	public String getCurFileName() {
		return curFileName;
	}

	public String getCurFilePath() {
		return curFilePath;
	}

	public void setCurFilePath(String curFilePath) {
		this.curFilePath = curFilePath;
		this.curFileName = new File(curFilePath).getName();
	}

	public void clear() {
		model = null;
		((mxGraphModel) graph.getModel()).clear();
		dataFlowGraph = null;
		curFilePath = null;
		curFileName = null;
		codes = null;
	}

	/**
	 * Open a given file, parse the file, construct a DataFlowModel and a mxGraph
	 * @param file given file
	 * @return a constructed DataFlowModel
	 */
	public DataTransferModel open(File file) {
		try {

			String extension ="";
			if(file != null && file.exists()) {
				// get a file's name
				String name = file.getName();

				// get a file's extension
				extension = name.substring(name.lastIndexOf("."));
			}
			if(extension.contains(".model")) {
				openModel(file);
			} else {
				ParserDTRAM parserDTRAM = new ParserDTRAM(new BufferedReader(new FileReader(file)));
				try {	
					model = parserDTRAM.doParseModel();
					graph = constructGraph(model);
					parserDTRAM.doParseGeometry(graph);
					curFilePath = file.getAbsolutePath();
					curFileName = file.getName();
					if (!Validation.checkUpdateConflict(model)) return null;
					analyzeDataTransferModel(model);
					return model;
				} catch (ExpectedChannel | ExpectedChannelName | ExpectedLeftCurlyBracket | ExpectedInOrOutOrRefKeyword
						| ExpectedStateTransition | ExpectedEquals | ExpectedRHSExpression | WrongLHSExpression
						| WrongRHSExpression | ExpectedRightBracket | ExpectedAssignment | ExpectedModel | ExpectedGeometry | ExpectedNode | ExpectedResource | ExpectedFormulaChannel | ExpectedIoChannel e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public DataTransferModel openModel(File file) {
		try {

			Parser parser = new Parser(new BufferedReader(new FileReader(file)));

			try {	
				// Parse the .model file.
				model = parser.doParse();
				
				curFilePath = file.getAbsolutePath();
				curFileName = file.getName();
				
				// Analyze the model.
				if (!Validation.checkUpdateConflict(model)) return null;
				graph = constructGraph(model);
				analyzeDataTransferModel(model);
				
				// Set DAG layout.
				setDAGLayout();
				return model;
			} catch (ExpectedChannel | ExpectedChannelName | ExpectedLeftCurlyBracket | ExpectedInOrOutOrRefKeyword
					| ExpectedStateTransition | ExpectedEquals | ExpectedRHSExpression | WrongLHSExpression
					| WrongRHSExpression | ExpectedRightBracket | ExpectedAssignment e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**--------------------------------------------------------------------------------
	 * save
	/**--------------------------------------------------------------------------------
	 * 
	 */
	public void save() {
		if (curFilePath != null) {
			try {
				File file = new File(curFilePath);
				String extension = "";
				if(file != null && file.exists()) {
					// get a file's name
					String name = file.getName();

					// get a file's extension
					extension = name.substring(name.lastIndexOf("."));
				}
				if(extension.contains(".model")) {
					saveModel(file);
				} else {
					FileWriter filewriter = new FileWriter(file);		        
					filewriter.write(toOutputString());
					filewriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveModel(File file) {
		if (curFilePath != null) {
			try {
				FileWriter filewriter = new FileWriter(file);			     
				filewriter.write(model.getSourceText());
				filewriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**--------------------------------------------------------------------------------
	 * get writing texts "dtram" file  information is written.
	 * 
	 * @return formatted "dtram" info texts.
	 */
	protected String toOutputString() {
		String fileString = "";
		
		fileString += "model {\n";
		fileString += this.model.getSourceText();
		fileString += "}\n";

		fileString += "geometry {\n";

		Object root = graph.getDefaultParent();
		for (int i = 0; i < graph.getModel().getChildCount(root); i++) {
			Object cell = graph.getModel().getChildAt(root, i);
			if (graph.getModel().isVertex(cell)) {
				mxGraphView view = graph.getView();
				mxCellState state = view.getState(cell);
				int x = (int) state.getX();
				int y = (int) state.getY();
				int w = (int) state.getWidth();
				int h = (int) state.getHeight();

				for(Channel ch: model.getChannels()) {
					if(ch instanceof FormulaChannel && state.getLabel().equals(ch.getChannelName())) {
						fileString += "\tnode fc " + state.getLabel() + ":" + x + "," + y + "," + w + "," + h+"\n";		
					} else if(ch instanceof Channel && state.getLabel().equals(ch.getChannelName())) {
						fileString +="\tnode c " + state.getLabel() + ":" + x + "," + y + "," + w + "," + h+"\n";
					}
				}

				for (ResourcePath res: model.getResourcePaths()){
					if(res instanceof ResourcePath && state.getLabel().equals(res.getResourceName()))
						fileString += "\tnode r " + state.getLabel() + ":" + x + "," + y + "," + w + "," + h + "\n";
				}

				for (Channel ioC: model.getIOChannels()) {
					if(ioC instanceof Channel && state.getLabel().equals(ioC.getChannelName())) {
						fileString += "\tnode ioc " + state.getLabel() + ":" + x + "," + y + "," + w + "," + h + "\n";
					}
				}
			}
		}		
		fileString += "}\n";
	
		return fileString;
	}
	
	/**
	 * Construct a mxGraph from DataFlowModel and DataFlowModel
	 * @param model
	 * @param dataFlowGraph
	 * @return constructed mxGraph
	 */
	public mxGraph constructGraph(DataTransferModel model) {
		bReflectingArchitectureModel  = true;
		((mxGraphModel) graph.getModel()).clear();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo1.setRelative(true);

			mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo2.setRelative(true);

			Map<DataTransferChannel, Object> channelsIn = new HashMap<>();
			Map<DataTransferChannel, Object> channelsOut = new HashMap<>();
			Map<ResourcePath, Object> resources = new HashMap<>();

			// create channel vertices
			for (Channel c: model.getChannels()) {
				DataTransferChannel channelGen = (DataTransferChannel) c;
				if (channelsIn.get(channelGen) == null || channelsOut.get(channelGen) == null) {
					Object channel = graph.insertVertex(parent, null, channelGen.getChannelName(), 150, 20, 30, 30); // insert a channel as a vertex
					mxCell port_in = new mxCell(null, geo1, "shape=ellipse;perimter=ellipsePerimeter");
					port_in.setVertex(true);
					graph.addCell(port_in, channel);		// insert the input port of a channel
					mxCell port_out = new mxCell(null, geo2, "shape=ellipse;perimter=ellipsePerimeter");
					port_out.setVertex(true);
					graph.addCell(port_out, channel);		// insert the output port of a channel
					channelsIn.put(channelGen, port_in);
					channelsOut.put(channelGen, port_out);
				}
			}

			// create resource vertices
			for (ResourcePath res: model.getResourcePaths()) {
				Object resource = graph.insertVertex(parent, null,
						res.getResourceName(), 20, 20, 80, 30,
						"shape=ellipse;perimeter=ellipsePerimeter"); // insert a resource as a vertex
				resources.put(res, resource);
			}

			// add input, output and reference edges
			for (Channel ch: model.getChannels()) {
				DataTransferChannel channelGen = (DataTransferChannel) ch;
				// input edge
				for (ResourcePath srcRes: channelGen.getInputResources()) {
					graph.insertEdge(parent, null, new SrcDstAttribute(srcRes, channelGen), resources.get(srcRes), channelsIn.get(channelGen), "movable=false");
				}
				// output edge
				for (ResourcePath dstRes: channelGen.getOutputResources()) {
					graph.insertEdge(parent, null, new SrcDstAttribute(channelGen, dstRes), channelsOut.get(channelGen), resources.get(dstRes), "movable=false");
				}
				// reference edges
				for (ResourcePath refRes: channelGen.getReferenceResources()) {
					graph.insertEdge(parent, null, null, resources.get(refRes), channelsIn.get(channelGen), "dashed=true;movable=false");
				}
			}

			for (Channel ioChannelGen: model.getIOChannels()) {
				if (channelsOut.get(ioChannelGen) == null) {
					Object channel = graph.insertVertex(parent, null, ioChannelGen.getChannelName(), 150, 20, 30, 30); // insert an I/O channel as a vertex
					mxCell port_out = new mxCell(null, geo2, "shape=ellipse;perimter=ellipsePerimeter");
					port_out.setVertex(true);
					graph.addCell(port_out, channel);		// insert the output port of a channel
					channelsOut.put((DataTransferChannel) ioChannelGen, port_out);
					for (ResourcePath outRes: ((DataTransferChannel) ioChannelGen).getOutputResources()) {
						graph.insertEdge(parent, null, null, port_out, resources.get(outRes), "movable=false");
					}
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
		setTreeLayout();

		bReflectingArchitectureModel = false;
		return graph;
	}

	public void setDAGLayout() {
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			DAGLayout ctl = new DAGLayout(graph);
			ctl.execute(parent);
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void updateEdgeAttiributes(DataFlowGraph dataFlowGraph) {
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			// add input, output and reference edges
			for (Edge e : dataFlowGraph.getEdges()) {
				if (e instanceof DataFlowEdge) {
					DataFlowEdge dataFlow = (DataFlowEdge) e;
					DataTransferChannel channelGen = dataFlow.getChannel();
					ResourceNode srcRes = (ResourceNode) dataFlow.getSource();
					// input edge
					for (Object edge: graph.getChildEdges(parent)) {
						mxCell edgeCell = (mxCell) edge;
						if (edgeCell.getValue() instanceof SrcDstAttribute) {
							SrcDstAttribute edgeAttr = (SrcDstAttribute) edgeCell.getValue();
							if (edgeAttr.getSrouce() == srcRes.getResource() && edgeAttr.getDestination() == channelGen) {
								edgeCell.setValue(dataFlow.getAttribute());
								break;
							}
						}
					}
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
		graph.refresh();
	}

	public void setTreeLayout() {
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			mxCompactTreeLayout ctl = new mxCompactTreeLayout(graph);
			ctl.setLevelDistance(100);
			//		ctl.setHorizontal(false);
			ctl.setEdgeRouting(false);
			ctl.execute(parent);
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void setCircleLayout() {
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			mxCircleLayout ctl = new mxCircleLayout(graph);
			ctl.execute(parent);
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void addResourcePath(ResourcePath res) {
		getModel().addResourcePath(res);
		resetDataFlowGraph();
		graph.getModel().beginUpdate();
		Object parent = graph.getDefaultParent();
		try {
			graph.insertVertex(parent, null, res.getResourceName(), 20, 20, 80, 30,
					"shape=ellipse;perimeter=ellipsePerimeter"); // insert a resource as a vertex
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void addChannel(DataTransferChannel channelGen) {
		getModel().addChannel(channelGen);
		resetDataFlowGraph();
		graph.getModel().beginUpdate();
		Object parent = graph.getDefaultParent();
		try {
			mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo1.setRelative(true);

			mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo2.setRelative(true);

			Object channel = graph.insertVertex(parent, null, channelGen.getChannelName(), 150, 20, 30, 30); // insert a channel as a vertex
			mxCell port_in = new mxCell(null, geo1, "shape=ellipse;perimter=ellipsePerimeter");
			port_in.setVertex(true);
			graph.addCell(port_in, channel);		// insert the input port of a channel
			mxCell port_out = new mxCell(null, geo2, "shape=ellipse;perimter=ellipsePerimeter");
			port_out.setVertex(true);
			graph.addCell(port_out, channel);		// insert the output port of a channel
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void addIOChannel(DataTransferChannel ioChannelGen) {
		getModel().addIOChannel(ioChannelGen);
		resetDataFlowGraph();
		graph.getModel().beginUpdate();
		Object parent = graph.getDefaultParent();
		try {
			mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo2.setRelative(true);

			Object channel = graph.insertVertex(parent, null, ioChannelGen.getChannelName(), 150, 20, 30, 30); // insert an I/O channel as a vertex
			mxCell port_out = new mxCell(null, geo2, "shape=ellipse;perimter=ellipsePerimeter");
			port_out.setVertex(true);
			graph.addCell(port_out, channel);		// insert the output port of a channel
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public void addFormulaChannel(FormulaChannel formulaChannelGen) {
		getModel().addChannel(formulaChannelGen);
		resetDataFlowGraph();
		graph.getModel().beginUpdate();
		Object parent = graph.getDefaultParent();
		try {
			mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo1.setRelative(true);

			mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
			geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			geo2.setRelative(true);

			Object channel = graph.insertVertex(parent, null, formulaChannelGen.getChannelName(), 150, 20, 30, 30); // insert a channel as a vertex
			mxCell port_in = new mxCell(null, geo1, "shape=ellipse;perimter=ellipsePerimeter");
			port_in.setVertex(true);
			graph.addCell(port_in, channel);		// insert the input port of a channel
			mxCell port_out = new mxCell(null, geo2, "shape=ellipse;perimter=ellipsePerimeter");
			port_out.setVertex(true);
			graph.addCell(port_out, channel);		// insert the output port of a channel
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public boolean connectEdge(mxCell edge, mxCell src, mxCell dst) {
		if (bReflectingArchitectureModel) return false;
		DataTransferModel model = getModel();
		Channel srcCh = model.getChannel((String) src.getValue());
		if (srcCh == null) {
			srcCh = model.getIOChannel((String) src.getValue());
			if (srcCh == null) {
				ResourcePath srcRes = model.getResourcePath((String) src.getValue());
				Channel dstCh = model.getChannel((String) dst.getValue());
				if (srcRes == null || dstCh == null) return false;
				// resource to channel edge
				ChannelMember srcCm = new ChannelMember(srcRes);
				((DataTransferChannel ) dstCh).addChannelMemberAsInput(srcCm);
				edge.setValue(new SrcDstAttribute(srcRes, dstCh));
				resetDataFlowGraph();
				return true;
			}
		}
		ResourcePath dstRes = model.getResourcePath((String) dst.getValue());
		if (dstRes == null) return false;
		// channel to resource edge
		ChannelMember dstCm = new ChannelMember(dstRes);
		((DataTransferChannel) srcCh).addChannelMemberAsOutput(dstCm);
		edge.setValue(new SrcDstAttribute(srcCh, dstRes));
		resetDataFlowGraph();
		return true;
	}

	public void delete() {
		for (Object obj: graph.getSelectionCells()) {
			mxCell cell = (mxCell) obj;
			if (cell.isEdge()) {
				String srcName = (String) cell.getSource().getValue();
				String dstName = (String) cell.getTarget().getValue();
				if (model.getResourcePath(srcName) != null) {
					// resource to channel edge
					Channel ch = model.getChannel(dstName);
					ch.removeChannelMember(model.getResourcePath(srcName));
				} else if (model.getResourcePath(dstName) != null) {
					// channel to resource edge
					Channel ch = model.getChannel(srcName);
					if (ch == null) {
						ch = model.getIOChannel(srcName);
					}
					ch.removeChannelMember(model.getResourcePath(dstName));
				}
			} else if (cell.isVertex()) {
				String name = (String) cell.getValue();
				if (model.getChannel(name) != null) {
					model.removeChannel(name);
				} else if (model.getIOChannel(name) != null) {
					model.removeIOChannel(name);
				} else if (model.getResourcePath(name) != null) {
					model.removeResourcePath(name);
				}
			}
		}
		graph.removeCells(graph.getSelectionCells());
		resetDataFlowGraph();
	}

	public void setChannelCode(DataTransferChannel ch, String code) {
		ch.setSourceText(code);
		TokenStream stream = new Parser.TokenStream();
		Parser parser = new Parser(stream);
		
		for (String line: code.split("\n")) {
			stream.addLine(line);
		}
		try {
			DataTransferChannel ch2 = parser.parseChannel(getModel());
			for (ChannelMember chm2: ch2.getInputChannelMembers()) {
				for (ChannelMember chm: ch.getInputChannelMembers()) {
					if (chm2.getResource() == chm.getResource()) {
						chm.setStateTransition(chm2.getStateTransition());
						break;
					}
				}
			}
			for (ChannelMember chm2: ch2.getOutputChannelMembers()) {
				for (ChannelMember chm: ch.getOutputChannelMembers()) {
					if (chm2.getResource() == chm.getResource()) {
						chm.setStateTransition(chm2.getStateTransition());
						break;
					}
				}
			}
			for (ChannelMember chm2: ch2.getReferenceChannelMembers()) {
				for (ChannelMember chm: ch.getReferenceChannelMembers()) {
					if (chm2.getResource() == chm.getResource()) {
						chm.setStateTransition(chm2.getStateTransition());
						break;
					}
				}
			}
			resetDataFlowGraph();
		} catch (ExpectedRightBracket | ExpectedChannel | ExpectedChannelName | ExpectedLeftCurlyBracket
				| ExpectedInOrOutOrRefKeyword | ExpectedStateTransition | ExpectedEquals | ExpectedRHSExpression
				| WrongLHSExpression | WrongRHSExpression | ExpectedAssignment e) {
			e.printStackTrace();
		}
	}

	private class SrcDstAttribute extends EdgeAttribute {
		private Object src;
		private Object dst;

		public SrcDstAttribute(Object src, Object dst) {
			this.src = src;
			this.dst = dst;
		}

		public Object getSrouce() {
			return src;
		}

		public Object getDestination() {
			return dst;
		}

		public String toString() {
			return "";
		}
	}
}
