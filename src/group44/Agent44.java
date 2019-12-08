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
    private opponentModeling opponent_Model;
    private userModeling user_Model;

    @Override
    public void init(NegotiationInfo info)
    {
        super.init(info);
        negotiator = new Negotiator(timeline.getTotalTime());
        opponent_Model = new opponentModeling();
        user_Model = new userModeling();
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        List< Issue > issues = additiveUtilitySpace.getDomain().getIssues();
        opponent_Model.init_freq_table(issues);
        user_Model.initialLinearProgram(issues,userModel);

    }

    /**
     * The agent offers a random bid from BidRanking which has been limiting the boundary according to our
     * concession strategy. We assume that after half of the negotiation, the agent will now have enough information
     * about the opponent to create an opponent model. The agent will attempt to offer a bid at the nash point after half match.
     * However, the agent still conceded as time pass. At each round, the agent will check whether it is going to accept
     * the offer by using the strategy that we adopt from an academic paper. Nevertheless, if the negotiation is
     * approaching the end the agent will accept an offer at the last minute to not lose the deal.
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

    /**
     * The function generates Bid by using nash bargaining solution. First, the calculateConcession function is called
     * before the agent finds a bid. Then, we estimate the utility of the opponent and the user to find out the offer at
     * the nash point.
     */
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

    /**
     * The function generates Bid according to the concession rate. The lower bound of the BidRanking expand over time
     * to expand the possibility of finding the offer that opponent wants.
     */
    private Bid getrandomBidWithConcession(){
        calculateConcession();
        BidRanking bidRanking = userModel.getBidRanking();
        minimum_index = (int) Math.ceil(bidRanking.getSize()*(1-consessRate));
        Integer randomNumber = generateRandomIntIntRange(minimum_index,bidRanking.getSize()-1);
        List<Bid> bidList = bidRanking.getBidOrder();
        return bidList.get(randomNumber);
    }

    /**
     * The function increase the concession rate after 10 per cent of the time has passed.
     */

    private void calculateConcession(){
        if(timeline.getCurrentTime()%10 == 0){
            this.consessRate += 0.1;
        }
    }


    /**
     * The agent update opponent model at every round. We adopt the opponent model from Johny Black which uses
     * a frequency table to construct weight of issue and value of the option. The agent can use information in the
     * table to estimate the utility that the opponent offered, and also estimate the utility that the opponent will get.
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


    @Override
    public AbstractUtilitySpace estimateUtilitySpace()
    {
        return super.estimateUtilitySpace();
    }

}