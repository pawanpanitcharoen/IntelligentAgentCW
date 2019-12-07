package group44;

import genius.core.Bid;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.UserModel;

import java.util.ArrayList;
import java.util.List;

public class Negotiator {
    private double totaltime;
    private double[] phasesRage = new double[3];

    private int currentPhase = 0;
    private int lowerBound = 0;
    private int upperBound = 0;
    private ArrayList<Double> Last15Round;
    private double previousRoundTime = 0.0;
    public Negotiator(double totaltime){
        this.totaltime = totaltime;
        phasesRage[0] = totaltime*28/36;
        phasesRage[1] = totaltime*7/36;
        phasesRage[2] = totaltime*1/36;
        this.Last15Round = new ArrayList<Double>();
    }
    private void calculateCurrentPhase(double currentTime){
        if( currentTime <= this.phasesRage[0] ){
            this.currentPhase = 1;
        }else if( currentTime <= this.phasesRage[0]+phasesRage[1]){
            this.currentPhase = 2;
        }else{
            this.currentPhase = 3;
        }
    }



    //    @Override
    public Boolean IsAccept(Double currentTime, UserModel userModel,Bid lastOffer) {
        calculateCurrentPhase(currentTime);
        BidRanking bidRanking = userModel.getBidRanking();
        double averageLast15Round;
        boolean acceptWithoutCondition = false;
        switch (currentPhase){
            case 1:{
                lowerBound = bidRanking.getSize()*7/8;
                upperBound = bidRanking.getSize()-1;
                break;
            } case 2:{
                lowerBound = bidRanking.getSize()*5/8;
                upperBound = bidRanking.getSize()*7/8;
                break;
            } case 3: {
                upperBound = bidRanking.getSize() * 5 / 8;
                lowerBound = 0;
                if(Last15Round.size() == 15 ) {
                    double totalLast15Round = 0.0;
                    for(double eachRoundTime : Last15Round){
                        totalLast15Round += eachRoundTime;
                    }
                    acceptWithoutCondition = totalLast15Round / 15 < 15;
                    Last15Round.remove(0);
                }
                if(previousRoundTime != 0.0) {
                    Last15Round.add(currentTime - previousRoundTime);
                }
                previousRoundTime = currentTime;
                System.out.println("Last15Round :"+ Last15Round.toString());
                System.out.println("acceptWithoutCondition :"+ acceptWithoutCondition);
            }
        }
        int index = GlobalUtility.generateRandomIntIntRange(lowerBound,upperBound);
        // if index -1 mean not found in bid ranking
        if(acceptWithoutCondition || bidRanking.indexOf(lastOffer) > index){
            return true;
        }
        return false;
    }

    public Integer counterOffer(){
        return GlobalUtility.generateRandomIntIntRange(lowerBound,upperBound);
    }
}
