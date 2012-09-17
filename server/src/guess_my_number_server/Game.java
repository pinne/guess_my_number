/**
 * Guess my number game - Server
 *
 * Communication Systems, HI1032
 * Lab assignment 3 - Client-Server programming
 *
 * Simon Kers skers@kth.se
 * Sakib Pathan sakibp@kth.se
 *                                 KTH STH 2012
 */

package guess_my_number_server;

import java.util.Random;
import java.util.StringTokenizer;

/**
 * A game of guess my number, this has all the game logic.
 * 
 * It receives Strings and parses the answer and returns
 * formatted Strings that can be sent as replies to the client.
 */
public class Game {

	private static final Random RANDOM = new Random();	
	private static int secretNumber;
	private static final int LOWEST = 0;
	private static final int HIGHEST = 100;
	private int guess;

	public Game(Session client) {
		genSecretNumber(LOWEST, HIGHEST);
	}
	
	/**
	 * Generate a secret number between lowest and highest constants.
	 */
	private void genSecretNumber(int from, int to) {
		secretNumber = RANDOM.nextInt(to - from) + 1;
		System.out.println("My secret number is " + secretNumber + ".");
	}

	/**
	 * Parse the guess and return an answer.
	 */
	public String parse(String guess) {
		if (guess.equalsIgnoreCase("QUIT")) {
			return "GOOD BYE";

		} else if (guess.length() > 0 && guess.toUpperCase().charAt(0) == 'G') {
			// Check last part of string and extract the number
			String guessNumber = new String();
			StringTokenizer st = new StringTokenizer(guess);
			guessNumber = st.nextToken(); // First word

			if (st.hasMoreTokens())	
				guessNumber = st.nextToken(); // Second word
			else
				return "ERROR";

			setGuess(Integer.parseInt(guessNumber));

			// Write distance between secret number and guess.
			System.out.println(getPosition());

			if (getPosition() == 0)
				return "CORR";
			else if (getPosition() < 0)
				return "LO";
			else if (getPosition() > 0)
				return "HI";
			else
				return "UNDEFINED";
		} else {
			System.out.println("ERROR");
			return "ERROR";
		}
	}

	private int getPosition() {
		return guess - secretNumber;
	}

	public void setGuess(int guess) {
		this.guess = guess;
	}

	public boolean isCorrect() {
		if (getPosition() == 0)
			return true;
		else
			return false;
	}
}
