package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;

public class VectorProcessor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        VectorProcessor.class
    );

    private final double numberToAdd;

    private final List<Integer> inputVector;

    private final Set<Integer> indexesToProcess;

    private volatile List<Integer> outputVector;

    public VectorProcessor(
        double numberToAdd,
        List<Integer> inputVector,
        Set<Integer> indexesToProcess,
        List<Integer> outputVector
    ) {
        this.numberToAdd = numberToAdd;
        this.inputVector = inputVector;
        this.indexesToProcess = indexesToProcess;
        this.outputVector = outputVector;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        LOGGER.info(
            threadName
                + " started to process ("
                + indexesToProcess.size()
                + " vector elements)"
        );
        for (int index : indexesToProcess) {
            int res = inputVector.get(index);
            for (int i = 0; i < index; i++) {
                res += numberToAdd;
            }
            outputVector.set(index, res);
        }
    }
}
