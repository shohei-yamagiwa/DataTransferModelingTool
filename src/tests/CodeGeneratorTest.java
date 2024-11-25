package tests;

import algorithms.DataTransferModelAnalyzer;
import code.ast.CompilationUnit;
import generators.DataTransferMethodAnalyzer;
import generators.JavaCodeGenerator;
import generators.JavaMethodBodyGenerator;
import models.dataFlowModel.DataFlowGraph;
import models.dataFlowModel.DataTransferModel;
import parser.Parser;
import parser.exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class CodeGeneratorTest {
    public static void main(String[] args) {
        File file = new File("models/POS.model");
        try {
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            DataTransferModel model;
            try {
                model = parser.doParse();
                DataFlowGraph graph = DataTransferModelAnalyzer.createDataFlowGraphWithStateStoringAttribute(model);
                DataTransferModelAnalyzer.annotateWithSelectableDataTransferAttiribute(graph);
                DataTransferMethodAnalyzer.decideToStoreResourceStates(graph);
                ArrayList<CompilationUnit> codetree = JavaMethodBodyGenerator.doGenerate(graph, model, JavaCodeGenerator.doGenerate(graph, model));
                System.out.println(codetree);
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
