package edu.neu.coe.info6205.scratchpad;

import edu.neu.coe.info6205.util.Benchmark_Timer;
import org.checkerframework.checker.units.qual.A;

import java.util.Random;

public class Assignment_2 {
    public static void main(String[] args) {

        Assignment_2 ass = new Assignment_2();

        Benchmark_Timer bt = new Benchmark_Timer<AssignmentInput>("students getting 72 marks", ass::count72);
        double duration = bt.runFromSupplier(ass::getInput, 5000);
        System.out.println("AVERAGE TIME IN ms = "+duration);


    }



    public AssignmentInput getInput() {
        Integer[] input = new Integer[500];
        Random rand = new Random();
        for(int i = 0; i< input.length; i++) {

            input[i] = rand.nextInt(101);

        }
        AssignmentInput ai = new AssignmentInput(input);
        return ai;
    }

    public void count72(AssignmentInput obj) {
        int count = 0;
        for(int i = 0; i < obj.marks.length; i++ ) {
            if(obj.marks[i] == 72) {
                count ++;
            }
        }

    }
}

