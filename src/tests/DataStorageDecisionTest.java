package tests;

import algorithms.DataTransferModelAnalyzer;
import generators.DataTransferMethodAnalyzer;
import models.Node;
import models.dataFlowModel.DataFlowGraph;
import models.dataFlowModel.DataTransferModel;
import models.dataFlowModel.ResourceNode;
import models.dataFlowModel.StoreAttribute;
import parser.Parser;
import parser.exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DataStorageDecisionTest {
    public static void main(String[] args) {
        File file = new File("models/POS2.model");
        try {
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            DataTransferModel model = null;
            try {
                model = parser.doParse();
                System.out.println(model);
                DataFlowGraph graph = DataTransferModelAnalyzer.createDataFlowGraphWithStateStoringAttribute(model);
                DataTransferModelAnalyzer.annotateWithSelectableDataTransferAttiribute(graph);
                DataTransferMethodAnalyzer.decideToStoreResourceStates(graph);
                for (Node n : graph.getNodes()) {
                    System.out.println(((ResourceNode) n).getResource().getResourceName() + ":" + ((StoreAttribute) ((ResourceNode) n).getAttribute()).isStored());
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
