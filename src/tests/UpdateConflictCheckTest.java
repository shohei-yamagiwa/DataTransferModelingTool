package tests;

import algorithms.Validation;
import models.dataFlowModel.DataTransferModel;
import parser.Parser;
import parser.exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class UpdateConflictCheckTest {
    public static void main(String[] args) {
        File file = new File("models/POS2.model");
        try {
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            try {
                DataTransferModel model = parser.doParse();
                System.out.println(Validation.checkUpdateConflict(model));
            } catch (ExpectedRightBracket e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedChannel e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedChannelName e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedLeftCurlyBracket e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedInOrOutOrRefKeyword e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedStateTransition e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedEquals e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedRHSExpression e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WrongLHSExpression e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WrongRHSExpression e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExpectedAssignment e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
