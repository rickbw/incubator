package rickbw.incubator.token;


public final class UserId1 extends Intish<UserId1> {

    private static final long serialVersionUID = 4450692424006528122L;

    private static final Behavior<UserId1> behavior = behavior().
            lowerBound(Integer.MIN_VALUE).
            upperBound(Integer.MAX_VALUE).
            build();


    public static UserId1 valueOf(int value) {
        return new UserId1(value);
    }


    private UserId1(int value) {
        super(value);
    }


    @Override
    protected Behavior<UserId1> getBehaviorUnsafe() {
        return behavior;
    }

}
