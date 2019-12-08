package group44;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.UserModel;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userModeling {
    public void initialLinearProgram(List<Issue> issues,UserModel userModel){
        int alloptionSize = 0;
        int bidrankingSize = userModel.getBidRanking().getSize();
        Map<String ,Integer> bidmapIndex = new HashMap<String,Integer>();
        for (Issue issue : issues) {
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                bidmapIndex.put(valueDiscrete.getValue(), bidmapIndex.size());
            }
            alloptionSize += (issueDiscrete.getValues().size());
        }

        double[] objectives = new double[alloptionSize+(bidrankingSize-1)];
        Arrays.fill(objectives, 1.0);
        for(int i = 0;i< alloptionSize;i++){
            objectives[i] = 0;

        }

        LinearProgram lp = new LinearProgram(objectives);


        // C-15

        BidRanking bidRanking = userModel.getBidRanking();
        List<Bid> bidOrder = bidRanking.getBidOrder();
        for(int i=0;i<bidRanking.getAmountOfComparisons();i++){
            double[] constraint = new double[alloptionSize+(bidrankingSize-1)];
            Arrays.fill(constraint, 0.0);
            Bid lowbid = bidOrder.get(i);
            Bid highBid = bidOrder.get(i+1);

            for(Issue issue : lowbid.getIssues()) {
                String optionName = lowbid.getValue(issue).toString();
                constraint[bidmapIndex.get(optionName)] = -1;
            }

            for(Issue issue : highBid.getIssues()) {
                String optionName = highBid.getValue(issue).toString();
                constraint[bidmapIndex.get(optionName)] = 1;
            }
            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0.0,"c"+lp.getConstraints().size()));
        }


        //C1ุุ6
        for(int i =alloptionSize+1;i<objectives.length;i++){
            double[] constraint = new double[alloptionSize+(bidrankingSize-1)];
            Arrays.fill(constraint, 0.0);
            constraint[i] = 1;
            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0.0,"c"+lp.getConstraints().size()));
        }

        //C17
        for(int i =0;i<alloptionSize;i++){
            double[] constraint = new double[alloptionSize+(bidrankingSize-1)];
            constraint[i] = 1;
            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0.0,"c"+lp.getConstraints().size()));
        }

        //C18
        double[] constraint = new double[alloptionSize+(bidrankingSize-1)];
        Bid highestRankBid = userModel.getBidRanking().getMaximalBid();
        System.out.println("highestRankBid"+highestRankBid);
        for(Issue issue : highestRankBid.getIssues()){
            String optionName = highestRankBid.getValue(issue).toString();

            Integer index = bidmapIndex.get(optionName);
            System.out.println("optionName"+optionName);
            System.out.println("index"+index);
            constraint[index] = 1;
        }
        lp.addConstraint(new LinearEqualsConstraint(constraint, 1.0,"c"+lp.getConstraints().size()));


//        lp.setMinProblem(true);
//        LinearProgramSolver solver  = SolverFactory.newDefault();
//        double[] sol = solver.solve(lp);
//
//        for(int i=0;i<sol.length;i++){
//            System.out.println("x"+i +"="+sol[i]);
//        }

    }

}
