package rickbw.incubator.choice;



public final class BankExample {

    private int accountBalanceCents = 100; // :-(


    /**
     * Attempt to withdraw the given amount of money from the bank account.
     *
     * @return The resulting account balance, if the withdrawal was
     *         successful, or {@link InsufficientFunds} if the account would
     *         be overdrawn.
     */
    public Either<Integer, InsufficientFunds> withdraw(final int amountCents) {
        if (amountCents <= this.accountBalanceCents) {
            this.accountBalanceCents -= amountCents;
            return Either.first(this.accountBalanceCents);
        } else {
            return Either.second(new InsufficientFunds(this.accountBalanceCents, amountCents));
        }
    }

    public static void main(final String[] args) {
        final BankExample bankAccount = new BankExample();

        final Either<Integer, InsufficientFunds> result = bankAccount.withdraw(2000);
        if (result.isFirstPresent()) {
            System.out.println("I have " + result.first() + " cents left");
        } else {
            System.err.println("Can't withdraw " + result.second().withdrawAttemptCents);
        }
    }

    public static final class InsufficientFunds {
        /**
         * The amount of money in the account at the time of the withdrawal
         * attempt.
         */
        public final int accountValueCents;

        /**
         * The amount that the customer attempted to withdraw.
         */
        public final int withdrawAttemptCents;

        private InsufficientFunds(final int accountValueCents, final int withdrawAttemptCents) {
            this.accountValueCents = accountValueCents;
            assert this.accountValueCents > 0;
            this.withdrawAttemptCents = withdrawAttemptCents;
            assert this.withdrawAttemptCents > this.accountValueCents;
        }
    }

}
