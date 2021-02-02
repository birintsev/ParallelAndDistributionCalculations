package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

@Service
public class DataDecompositionPrimeNumbersParallelGenerator
implements PrimeNumbersParallelGenerator {

    private static final int MAX_THREADS_NUMBER = 10;

    private static final int MIN_PRIMAL = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(
        PrimeNumbersParallelGenerator.class
    );

    @Override
    public List<Integer> generate(int maxNumber, int threadsNumber) {
        validate(maxNumber, threadsNumber);

        return findPrimesParallel(
            maxNumber,
            threadsNumber
        );
    }

    private void validate(int maxNumber, int threadsNumber) {
        if (maxNumber < MIN_PRIMAL || threadsNumber > MAX_THREADS_NUMBER) {
            throw new IllegalArgumentException(
                "maxNumber "
                    + maxNumber
                    + " must be greater than "
                    + MIN_PRIMAL
                    + " and threadsNumber ("
                    + threadsNumber
                    + ") must be less than "
                    + MAX_THREADS_NUMBER
            );
        }
    }

    /**
     * @return all prime numbers that belong to [2; sqrt(maxNumber)] interval
     * */
    private List<Integer> findBasePrimes(int maxNumber) {
        int sqrtMax = ceilSqrt(maxNumber);
        List<Integer> basePrimes = new ArrayList<>();
        boolean[] isNotPrime = new boolean[sqrtMax + 1];
        for (int integer = 2; integer <= sqrtMax; integer++) {
            if (isNotPrime[integer]) {
                continue;
            }
            for (int mult = 2; mult * integer < isNotPrime.length; mult++) {
                isNotPrime[integer * mult] = true;
            }
        }
        for (int integer = 2; integer <= sqrtMax; integer++) {
            if (!isNotPrime[integer]) {
                basePrimes.add(integer);
            }
        }
        return basePrimes;
    }

    private List<Integer> findPrimesParallel(int maxNumber, int threadNumber) {
        List<Integer> basePrimes = findBasePrimes(maxNumber);
        List<Integer> primes = new ArrayList<>();
        List<Integer> primeCandidates = range(ceilSqrt(maxNumber), maxNumber);
        List<Finder> threads =
            splitPrimeCandidatesByThreads(
                basePrimes,
                primeCandidates,
                threadNumber
            )
                .stream()
                .peek(Finder::start)
                .peek(thread -> {
                    try {
                        thread.join();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        threads.forEach(
            finder -> primes.addAll(finder.getPrimes())
        );
        return primes;
    }

    private List<Finder> splitPrimeCandidatesByThreads(
        List<Integer> basePrimes,
        List<Integer> primeCandidates,
        int threadNumber
    ) {
        List<Finder> threads = new ArrayList<>();
        int numbersPerThread = primeCandidates.size() / threadNumber;
        for (int i = 0; i < threadNumber; i+= numbersPerThread) {
            threads.add(
                new Finder(
                    primeCandidates.subList(
                        i,
                        min(
                            i + numbersPerThread - 1,
                            primeCandidates.size()
                        )
                    ),
                    basePrimes
                )
            );
        }
        return threads;
    }

    private int ceilSqrt(int i) {
        return (int) ceil(sqrt(i));
    }

    private static List<Integer> range(int fromInclusive, int toInclusive) {
        return IntStream
            .range(fromInclusive, toInclusive)
            .boxed()
            .collect(Collectors.toList());
    }

    private static class Finder extends Thread {

        private final List<Integer> candidates;

        private final List<Integer> basePrimes;

        private List<Integer> primes;

        public Finder(
            List<Integer> candidates,
            List<Integer> basePrimes
        ) {
            this.candidates = candidates;
            this.basePrimes = basePrimes;
        }

        @Override
        public void run() {
            List<Integer> _primes;
            int min;
            int max;
            if (candidates.isEmpty()) {
                primes = Collections.emptyList();
                return;
            }
            min = candidates.stream()
                .min(Integer::compareTo)
                .orElseThrow(RuntimeException::new);
            max = candidates.stream()
                .max(Integer::compareTo)
                .orElseThrow(RuntimeException::new);
            _primes = new ArrayList<>(candidates);
            for (int basePrime : basePrimes) {
                int mult = min / basePrime;
                int curr;
                while ((curr = (mult++) * basePrime) <= max) {
                    _primes.remove((Object) curr);
                }
            }
            primes = _primes;
        }

        public List<Integer> getPrimes() {
            if (primes == null) {
                throw new IllegalStateException(
                    "The primes have not been calculated yet."
                        + " Invoke Thread::start before this method"
                );
            }
            return primes;
        }
    }
}
