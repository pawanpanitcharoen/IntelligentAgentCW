package group44;

public class freq {
    private String name;
    private int frequency;
    private double valuePredicted;

    public double getValuePredicted() {
        return valuePredicted;
    }

    public void setValuePredicted(double valuePredicted) {
        this.valuePredicted = valuePredicted;
    }

    public freq(String name) {
        this.name = name;
        this.frequency = 1;
    }
    public freq(){

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    public void updateFrequency(){
        this.frequency+=1;
    }

}
