package group44;
import genius.core.issue.Issue;
import java.util.ArrayList;
import java.util.Collections;

public class freqIssue {
    private ArrayList<freq> options = new ArrayList<>();
    private String issue_name;
    private double issue_weight;

    public double getIssue_weight() {
        return issue_weight;
    }

    public void setIssue_weight(double issue_weight) {
        this.issue_weight = issue_weight;
    }

    public int sum_frequency(){
        int sum = 0;
        for (freq item : options) {
            sum+=item.getFrequency();
        }
        return sum;
    }

    public String getIssue_name() {
        return issue_name;
    }

    public void setIssue_name(String issue_name) {
        this.issue_name = issue_name;
    }

    public void sort_freq(){
        Collections.sort(
                options,
                (option1, option2) -> option2.getFrequency()
                        - option1.getFrequency());
    }

    public ArrayList<freq> getOptions() {
        return options;
    }

    public void add_freq(String option){
        freq new_freq = new freq();
        new_freq.setName(option);
        new_freq.setFrequency(0);
        options.add(new_freq);
    }

    public void freq_update(String option) {
        if (options.size() == 0) {
            freq new_freq = new freq(option);
            options.add(new_freq);
        } else {
            boolean updated = false;
            for (freq item : options) {
                if (item.getName().equals(option)) {
                    item.updateFrequency();
                    updated = true;
                }
            }
            if (!updated) {
                freq new_freq = new freq(option);
                options.add(new_freq);
            }
        }
    }
}

