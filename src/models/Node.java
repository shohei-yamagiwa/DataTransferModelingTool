package models;

import java.util.HashSet;
import java.util.Set;

public class Node implements Cloneable {
	private Set<Edge> inEdges = null;
	private Set<Edge> outEdges = null;
	private NodeAttribute attribute;
	
	public Node() {
		inEdges = new HashSet<>();
		outEdges = new HashSet<>();
	}
	
	public Set<Edge> getInEdges() {
		return inEdges;
	}
	
	public void setInEdges(Set<Edge> inEdges) {
		this.inEdges = inEdges;
	}
	
	public Set<Edge> getOutEdges() {
		return outEdges;
	}
	
	public void setOutEdges(Set<Edge> outEdges) {
		this.outEdges = outEdges;
	}
	
	public void addInEdge(Edge edge) {
		inEdges.add(edge);
	}
	
	public void addOutEdge(Edge edge) {
		outEdges.add(edge);
	}
	
	public void removeInEdge(Edge edge) {
		inEdges.remove(edge);
	}
	
	public void removeOutEdge(Edge edge) {
		outEdges.remove(edge);
	}

	public void clearInEdges() {
		inEdges.clear();
	}

	public void clearOutEdges() {
		outEdges.clear();
	}
	
	public int getIndegree() {
		return inEdges.size();
	}
	
	public int getOutdegree() {
		return outEdges.size();
	}
	
	public Set<Node> getPredecessors() {
		Set<Node> predecessors = new HashSet<Node>();
		for (Edge edge: inEdges) {
			predecessors.add(edge.getSource());
		}
		return predecessors;
	}
	
	public Set<Node> getSuccessors() {
		Set<Node> successors = new HashSet<Node>();
		for (Edge edge: outEdges) {
			successors.add(edge.getDestination());
		}
		return successors;
	}

	public NodeAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(NodeAttribute attribute) {
		this.attribute = attribute;
	}
}
