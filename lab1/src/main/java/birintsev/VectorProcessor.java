package birintsev;

import java.util.List;
import java.util.Set;

public class VectorProcessor implements Runnable {

    private final double numberToAdd;

    private final List<Integer> inputVector;

    private final Set<Integer> indexesToProcess;

    private final List<Integer> outputVector;

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
        for (int index : indexesToProcess) {
            int res = inputVector.get(index);
            for (int i = 0; i < index; i++) {
                res += numberToAdd;
            }
            outputVector.set(index, res);
        }
    }
}
