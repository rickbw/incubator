/* Copyright 2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
    public Either<InsufficientFunds, Integer> withdraw(final int amountCents) {
        if (amountCents <= this.accountBalanceCents) {
            this.accountBalanceCents -= amountCents;
            return Either.ofRight(this.accountBalanceCents);
        } else {
            return Either.ofLeft(new InsufficientFunds(this.accountBalanceCents, amountCents));
        }
    }

    public static void main(final String[] args) {
        final BankExample bankAccount = new BankExample();

        final Either<InsufficientFunds, Integer> result = bankAccount.withdraw(2000);
        if (result.right().isPresent()) {
            System.out.println("I have " + result.left() + " cents left");
        } else {
            System.err.println("Can't withdraw " + result.left().get().withdrawAttemptCents);
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
