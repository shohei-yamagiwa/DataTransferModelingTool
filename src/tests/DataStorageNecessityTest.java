package tests;

import algorithms.DataTransferModelAnalyzer;
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

public class DataStorageNecessityTest {
    public static void main(String[] args) {
        File file = new File("models/POS2.model");
        try {
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            DataTransferModel model;
            try {
                model = parser.doParse();
                System.out.println(model);
                DataFlowGraph graph = DataTransferModelAnalyzer.createDataFlowGraphWithStateStoringAttribute(model);
                for (Node n : graph.getNodes()) {
                    ResourceNode resource = (ResourceNode) n;
                    if ((StoreAttribute) resource.getAttribute() != null) {
                        System.out.println(resource.toString() + ":" + ((StoreAttribute) resource.getAttribute()).isNeeded());
                    }
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
