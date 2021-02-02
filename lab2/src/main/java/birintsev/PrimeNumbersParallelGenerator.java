package birintsev;

import java.util.List;

public interface PrimeNumbersParallelGenerator {

    /**
     * @param maxNumber upper (inclusive) bound to look for prime numbers
     * */
    List<Integer> generate(int maxNumber, int threadsNumber);
}
