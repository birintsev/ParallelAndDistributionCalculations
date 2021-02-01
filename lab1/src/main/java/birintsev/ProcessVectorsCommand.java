package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@CommandLine.Command(name = "start", description = "Starts the work")
public class ProcessVectorsCommand implements Runnable {

    public static final int MAX_THREAD_COUNT = 10;

    public static final int MAX_VECTOR_LENGTH = 100000;

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ProcessVectorsCommand.class
    );

    private static final int MAX_VECTOR_ELEMENT = 1000;

    private static final int RANDOM_NUMBER_TO_ADD =
        (new Random(System.currentTimeMillis()).nextInt() % MAX_VECTOR_ELEMENT)
            + 1;

    @CommandLine.Option(
        names = {"vectorLength", "N"},
        type = int.class
    )
    private int vectorLength;

    @CommandLine.Option(
        names = {"threadCount", "M"},
        type = int.class,
        defaultValue = "1"
    )
    private int threadCount;

    @Override
    public void run() {
        long startTime;
        long duration;
        List<Integer> vector;
        List<Integer> result;
        List<VectorProcessor> vectorProcessors;

        validate();

        vector = generateVector(vectorLength);
        result = new ArrayList<>(vector.size());
        for (int i = 0; i < vector.size(); i++) {
            result.add(null);
        }
        vectorProcessors =
            splitVectorByThreads(
                threadCount,
                vector,
                result
            );

        startTime = System.currentTimeMillis();
        vectorProcessors.stream().map(Thread::new).forEach(Thread::start);
        duration = System.currentTimeMillis() - startTime;

        LOGGER.info(
            "Vector processing duration: "
                + Duration.ofMillis(duration)
                + "(" + duration + " ms)"
        );
    }

    private void validate() {
        if (threadCount > MAX_THREAD_COUNT) {
            throw new IllegalArgumentException(
                "Max allowed thread count is " + MAX_THREAD_COUNT
            );
        }
        if (vectorLength > MAX_VECTOR_LENGTH) {
            throw new IllegalArgumentException(
                "Max allowed vector length is " + MAX_VECTOR_LENGTH
            );
        }
    }

    private List<VectorProcessor> splitVectorByThreads(
        int amountOfThreads,
        List<Integer> inputVector,
        List<Integer> outputVector
    ) {
        List<VectorProcessor> vectorProcessors;
        // mapping thread index -> vector indexes to process
        Map<Integer, Set<Integer>> indexesToProcess;
        int effectiveAmountOfThreads = Math.min(
            amountOfThreads,
            inputVector.size()
        );
        if (amountOfThreads > effectiveAmountOfThreads) {
            LOGGER.warn(
                "Using only "
                    + effectiveAmountOfThreads
                    + " of "
                    + amountOfThreads
                    + " threads"
            );
        }
        vectorProcessors = new ArrayList<>(effectiveAmountOfThreads);
        indexesToProcess = mapThreadsIndexesToVectorIndexes(
            effectiveAmountOfThreads,
            inputVector.size()
        );
        for (int i = 0; i < effectiveAmountOfThreads; i++) {
            vectorProcessors.add(
                new VectorProcessor(
                    RANDOM_NUMBER_TO_ADD,
                    inputVector,
                    indexesToProcess.get(i),
                    outputVector
                )
            );
        }
        return vectorProcessors;
    }

    private Map<Integer, Set<Integer>> mapThreadsIndexesToVectorIndexes(
        int threadCount,
        int vectorLength
    ) {
        Set<Integer> takenElems = new HashSet<>();
        Map<Integer, Set<Integer>> threadToIndexes = new HashMap<>();
        for (int thrNum = 0; thrNum < threadCount; thrNum++) {
            threadToIndexes.put(thrNum, new HashSet<>());
        }
        for (int thrNum = 1; thrNum <= threadCount; thrNum++) {
            for (int vectElem = vectorLength; vectElem >= 1; vectElem--) {
                if (vectElem % thrNum != 0 || takenElems.contains(vectElem)) {
                    continue;
                }
                threadToIndexes.get(thrNum - 1).add(vectElem - 1);
                takenElems.add(vectElem);
            }
        }
        return threadToIndexes;
    }

    private List<Integer> generateVector(int length) {
        List<Integer> vector = new ArrayList<>(length);
        Random random;
        for (int i = 0; i < length; i++) {
            random = new Random(System.currentTimeMillis());
            vector.add(
                Math.abs(random.nextInt() % MAX_VECTOR_ELEMENT)
            );
        }
        return vector;
    }
}
