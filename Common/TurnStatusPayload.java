package Project.Common;

public class TurnStatusPayload extends Payload{
    private boolean didTakeTurn;

    public TurnStatusPayload()
    {
        setPayloadType(PayloadType.TURN);
    }

    public boolean isDidTakeTurn() {
        return didTakeTurn;
    }

    public void setDidTakeTurn(boolean didTakeTurn) {
        this.didTakeTurn = didTakeTurn;
    }
}
