package guess_my_number_server;

import java.util.Random;

public class GuessEngine {
	private static final Random RANDOM = new Random();	
	
	private static int secretNumber;
	private int guess;
	
	public GuessEngine(int from, int to) {
		genSecretNumber(from, to);
	}
	
	public String toString() {
		if (getPosition() == 0)
			return "CORR";
		else if (getPosition() < 0)
			return "LO";
		else if (getPosition() > 0)
			return "HI";
		else
			return "UNDEFINED";
	}
	
	public int getPosition() {
		return guess - secretNumber;
	}

	public void setGuess(int guess) {
		this.guess = guess;
	}
	
	public void genSecretNumber(int from, int to) {
		secretNumber = RANDOM.nextInt(to - from) + 1;
		System.out.println("My secret number is " + secretNumber + ".");
	}

	public boolean isCorrect() {
		if (getPosition() == 0)
			return true;
		else
			return false;
	}
}
