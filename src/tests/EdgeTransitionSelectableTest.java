package tests;

import algorithms.DataTransferModelAnalyzer;
import models.Edge;
import models.dataFlowModel.DataFlowEdge;
import models.dataFlowModel.DataFlowGraph;
import models.dataFlowModel.DataTransferModel;
import models.dataFlowModel.PushPullAttribute;
import parser.Parser;
import parser.exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class EdgeTransitionSelectableTest {
    public static void main(String[] args) {
        File file = new File("models/POS2.model");
        try {
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            DataTransferModel model;
            try {
                model = parser.doParse();
                System.out.println(model);
                DataFlowGraph graph = DataTransferModelAnalyzer.createDataFlowGraphWithStateStoringAttribute(model);
                DataTransferModelAnalyzer.annotateWithSelectableDataTransferAttiribute(graph);
                for (Edge e : graph.getEdges()) {
                    DataFlowEdge re = (DataFlowEdge) e;
                    System.out.println(re.getSource() + "-" + re.getDestination() + ":" + ((PushPullAttribute) (re.getAttribute())).getOptions());
                }
            } catch (ExpectedChannel | ExpectedChannelName | ExpectedLeftCurlyBracket | ExpectedInOrOutOrRefKeyword
                     | ExpectedStateTransition | ExpectedEquals | ExpectedRHSExpression | WrongLHSExpression
                     | WrongRHSExpression | ExpectedRightBracket | ExpectedAssignment e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
