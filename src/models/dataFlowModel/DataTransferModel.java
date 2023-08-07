package models.dataFlowModel;

import java.util.Set;

import models.dataConstraintModel.Channel;
import models.dataConstraintModel.DataConstraintModel;
import models.dataConstraintModel.ResourcePath;

public class DataTransferModel extends DataConstraintModel {	
	public DataFlowGraph getDataFlowGraph() {
		DataFlowGraph dataFlowGraph = new DataFlowGraph();
		for (Channel channelGen: getChannels()) {
			DataTransferChannel dfChannelGen = (DataTransferChannel)channelGen;
			Set<ResourcePath> inputResources = dfChannelGen.getInputResources();
			Set<ResourcePath> outputResources = dfChannelGen.getOutputResources();
			for (ResourcePath in: inputResources) {
				for (ResourcePath out: outputResources) {
					dataFlowGraph.addEdge(in ,out, dfChannelGen);
				}
			}
		}
		for (Channel channelGen: getIOChannels()) {
			DataTransferChannel dfChannelGen = (DataTransferChannel)channelGen;
			Set<ResourcePath> outputResources = dfChannelGen.getOutputResources();
			for (ResourcePath out: outputResources) {
				dataFlowGraph.addNode(out);
			}			
		}
		return dataFlowGraph;
	}
}
