package group44;

import java.util.List;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.issue.Issue;
import static group44.GlobalUtility.generateRandomIntIntRange;

public class Agent44 extends AbstractNegotiationParty
{
    private double MINIMUM_TARGET = 0.8;
    private Bid lastOffer;
    private int minimum_index;
    private double consessRate = 0.1;
    private Negotiator negotiator;
    private opponentModel opponent_Model;

    @Override
    public void init(NegotiationInfo info)
    {
        super.init(info);
        negotiator = new Negotiator(timeline.getTotalTime());
        opponent_Model = new opponentModel();
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        List< Issue > issues = additiveUtilitySpace.getDomain().getIssues();
        opponent_Model.init_freq_table(issues);

    }

    /**
     * Makes a random offer above the minimum utility target
     * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> possibleActions)
    {
        if (lastOffer != null) {
            if (negotiator.IsAccept(timeline.getCurrentTime(), userModel, lastOffer)||timeline.getTime() >= 0.99) {
                return new Accept(getPartyId(), lastOffer);
            }
        }
        Bid offer_bid = getrandomBidWithConcession();
        if (timeline.getTime() >= 0.5){
            Bid temp = generateNashBid();
            //System.out.println("Nash bid utility :"+getUtility(temp));
            if(temp != null) offer_bid = temp;
        }
        return new Offer(getPartyId(), offer_bid);
    }

    private Bid generateNashBid(){
        calculateConcession();
        Bid nashBid = null;
        double maxbit = 0;
        BidRanking bidRanking = userModel.getBidRanking();
        for(int i=minimum_index;i<=bidRanking.getBidOrder().size()-1;i++){
            Bid bid = bidRanking.getBidOrder().get(i);
            double p=opponent_Model.calculate_oppoenent_util(bid);
            double user_utility = getUtility(bid);
            p*=user_utility;
            if(p>maxbit && user_utility>=MINIMUM_TARGET){
                maxbit = p;
                nashBid=bid;
            }
        }
        return nashBid;
    }

    private Bid getrandomBidWithConcession(){
        calculateConcession();
        BidRanking bidRanking = userModel.getBidRanking();
        minimum_index = (int) Math.ceil(bidRanking.getSize()*(1-consessRate));
        Integer randomNumber = generateRandomIntIntRange(minimum_index,bidRanking.getSize()-1);
        List<Bid> bidList = bidRanking.getBidOrder();
        return bidList.get(randomNumber);
    }


    private void calculateConcession(){
        if(timeline.getCurrentTime()%10 == 0){
            this.consessRate += 0.1;
        }
    }
    /**
     * Remembers the offers received by the opponent.
     */

    @Override
    public void receiveMessage(AgentID sender, Action action)
    {
        if (action instanceof Offer) {
            lastOffer = ((Offer) action).getBid();
            opponent_Model.update_freq_table(lastOffer);
        }

    }

    @Override
    public String getDescription()
    {
        return "Offer bid by considering the nash point to maintain fairness";
    }

    /**
     * This stub can be expanded to deal with preference uncertainty in a more sophisticated way than the default behavior.
     */
    @Override
    public AbstractUtilitySpace estimateUtilitySpace()
    {
        return super.estimateUtilitySpace();
    }

}