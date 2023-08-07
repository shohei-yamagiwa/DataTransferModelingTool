package models.dataFlowModel;

import java.util.HashMap;
import java.util.Map;

import models.DirectedGraph;
import models.dataConstraintModel.ResourcePath;

public class DataFlowGraph extends DirectedGraph {
	protected Map<ResourcePath, ResourceNode> nodeMap = null;
	
	public DataFlowGraph() {
		super();
		nodeMap = new HashMap<>();
	}
	
	public void addNode(ResourcePath id) {
		if (nodeMap.get(id) == null) {
			ResourceNode node = new ResourceNode(id);
			addNode(node);
			nodeMap.put(id, node);			
		}
	}

	public void addEdge(ResourcePath in, ResourcePath out, DataTransferChannel dfChannelGen) {
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
