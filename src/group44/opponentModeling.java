package group44;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.List;

public class opponentModeling {
    private ArrayList<freqIssue> oppent_issues = new ArrayList<freqIssue>();
    private ArrayList<Double> utility = new ArrayList<Double>();
    private double current_opponent_utility_value = 0;
    public void init_freq_table(List< Issue > issues){
        for (Issue issue : issues) {
            String issue_name = issue.getName();
            freqIssue new_item = new freqIssue();
            new_item.setIssue_name(issue_name);
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String option = valueDiscrete.toString();
                new_item.add_freq(option);
            }
            oppent_issues.add(new_item);
        }
    }
    public void update_freq_table(Bid lastOffer){
        List<Issue> lastest_item = lastOffer.getIssues();
        for(Issue issue : lastest_item) {
            boolean updated = false;
            String new_option = lastOffer.getValue(issue).toString();
            String issue_name = issue.getName();
            for (freqIssue item : oppent_issues) {
                if (item.getIssue_name().equals(issue_name)) {
                    updated = true;
                    item.freq_update(new_option);
                    item.sort_freq();
                    ArrayList<freq> options = item.getOptions();
                    double n0 = 1;
                    double k = options.size();
                    for (freq option : item.getOptions()) {
                        if (option.getFrequency() != 0){
                            double predicted_value = (k-n0+1)/k;
                            option.setValuePredicted(predicted_value);
                            n0+=1;
                        }

                    }
                }
            }
            calculate_weight();
            if(!updated){
                freqIssue new_item = new freqIssue();
                new_item.setIssue_name(issue_name);
                new_item.freq_update(new_option);
                oppent_issues.add(new_item);
            }
        }
        for(Issue issue : lastest_item) {
            String new_option = lastOffer.getValue(issue).toString();
            String issue_name = issue.getName();
            for (freqIssue item : oppent_issues) {
                if (item.getIssue_name().equals(issue_name)) {
                    for (freq option : item.getOptions()) {
                        if (option.getName().equals(new_option)) {
                            current_opponent_utility_value += option.getValuePredicted() * item.getIssue_weight();
                        }
                    }
                }

            }
        }
        utility.add(current_opponent_utility_value);
    }

    public void calculate_weight(){
        double sum_weight = 0;
        for (freqIssue item : oppent_issues) {
            double sum_freq = item.sum_frequency();
            double weight = 0;
            for (freq option : item.getOptions()) {
                double frequency = option.getFrequency();
                if(frequency != 0) weight+= (Math.pow(frequency, 2)/sum_freq);
            }
            item.setIssue_weight(weight);
            sum_weight+=weight;
        }
        for (freqIssue item : oppent_issues) {
            if(item.getIssue_weight() != 0 ){
                double normalized_weight = item.getIssue_weight()/sum_weight;
                item.setIssue_weight(normalized_weight);
            }
        }


    }
    public double calculate_oppoenent_util(Bid bid) {
        double temp_util=0;
        for (Issue issue : bid.getIssues()) {
            String new_option = bid.getValue(issue).toString();
            String issue_name = issue.getName();
            for (freqIssue item : oppent_issues) {
                if (item.getIssue_name().equals(issue_name)) {
                    for (freq option : item.getOptions()) {
                        if (option.getName().equals(new_option)) {
                            temp_util += option.getValuePredicted() * item.getIssue_weight();
                        }
                    }
                }
            }
        }
        return temp_util;
    }
}
