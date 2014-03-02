package rickbw.incubator.token;


public final class UserId2 extends Stringish<UserId2> {

    private static final long serialVersionUID = -7505206403614873841L;

    private static final Behavior<UserId2> behavior = behavior().
            minimumLength(5).
            maximumLength(10).
            normalizeToLowerCase().
            trimWhitespace().
            build();


    public static UserId2 parse(String value) {
        return new UserId2(value);
    }


    private UserId2(String value) {
        super(behavior, value);
    }

}
