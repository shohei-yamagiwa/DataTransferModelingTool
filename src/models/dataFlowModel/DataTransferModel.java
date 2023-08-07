package models.dataFlowModel;

import java.util.Set;

import models.dataConstraintModel.Channel;
import models.dataConstraintModel.DataConstraintModel;
import models.dataConstraintModel.ResourcePath;

public class DataTransferModel extends DataConstraintModel {	
	public DataFlowGraph getDataFlowGraph() {
		DataFlowGraph dataFlowGraph = new DataFlowGraph();
		for (Channel channel: getChannels()) {
			DataTransferChannel dfChannel = (DataTransferChannel)channel;
			Set<ResourcePath> inputResources = dfChannel.getInputResources();
			Set<ResourcePath> outputResources = dfChannel.getOutputResources();
			for (ResourcePath in: inputResources) {
				for (ResourcePath out: outputResources) {
					dataFlowGraph.addEdge(in ,out, dfChannel);
				}
			}
		}
		for (Channel channel: getIOChannels()) {
			DataTransferChannel dfChannelGen = (DataTransferChannel)channel;
			Set<ResourcePath> outputResources = dfChannelGen.getOutputResources();
			for (ResourcePath out: outputResources) {
				dataFlowGraph.addNode(out);
			}			
		}
		return dataFlowGraph;
	}
}
