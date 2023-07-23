package models.dataFlowModel;

import java.util.HashMap;
import java.util.Map;

import models.DirectedGraph;
import models.dataConstraintModel.IdentifierTemplate;

public class DataFlowGraph extends DirectedGraph {
	protected Map<IdentifierTemplate, ResourceNode> nodeMap = null;
	
	public DataFlowGraph() {
		super();
		nodeMap = new HashMap<>();
	}
	
	public void addNode(IdentifierTemplate id) {
		if (nodeMap.get(id) == null) {
			ResourceNode node = new ResourceNode(id);
			addNode(node);
			nodeMap.put(id, node);			
		}
	}

	public void addEdge(IdentifierTemplate in, IdentifierTemplate out, DataTransferChannelGenerator dfChannelGen) {
		ResourceNode srcNode = nodeMap.get(in);
		if (srcNode == null) {
			srcNode = new ResourceNode(in);
			addNode(srcNode);
			nodeMap.put(in, srcNode);
		}
		ResourceNode dstNode = nodeMap.get(out);
		if (dstNode == null) {
			dstNode = new ResourceNode(out);
			addNode(dstNode);
			nodeMap.put(out, dstNode);
		}
		addEdge(new DataFlowEdge(srcNode, dstNode, dfChannelGen));
	}
}
