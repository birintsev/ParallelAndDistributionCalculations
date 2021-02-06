package birintsev.secure.atomic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(name = "insecureStart")
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Command implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    @CommandLine.Option(
        names = {"-items", "-i"},
        type = int.class
    )
    private int itemsToReadWrite;

    private final int maxItemsToReadWrite;

    private final AtomicReference<Integer> box;

    public Command(@Value("${maxItemsToReadWrite}") int maxItemsToReadWrite) {
        this.maxItemsToReadWrite = maxItemsToReadWrite;
        box = new AtomicReference<>(null);
    }

    @Override
    public void run() {
        List<BoxWriter<Integer>> boxWriters;
        List<BoxReader<Integer>> boxReaders;

        Thread writersThread;
        Thread readersThread;

        validate();

        boxWriters = generateWriters(itemsToReadWrite, box);
        boxReaders = generateReaders(itemsToReadWrite, box);

        writersThread = new Thread(() -> {
            for (BoxWriter<Integer> boxWriter : boxWriters) {
                boxWriter.start();
            }
            for (BoxWriter<Integer> boxWriter : boxWriters) {
                try {
                    boxWriter.join();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        readersThread = new Thread(() -> {
            for (BoxReader<Integer> boxReader : boxReaders) {
                boxReader.start();
            }
            for (BoxReader<Integer> boxReader : boxReaders) {
                try {
                    boxReader.join();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        writersThread.start();
        readersThread.start();

        try {
            writersThread.join();
            readersThread.join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void validate() {
        final int minItems = 1;
        if (
            itemsToReadWrite > maxItemsToReadWrite
                || itemsToReadWrite < minItems
        ) {
            throw new IllegalArgumentException(
                "itemsToReadWrite must belong to the interval ["
                    + minItems
                    + "; "
                    + maxItemsToReadWrite
                    + "]"
            );
        }
    }

    private List<BoxReader<Integer>> generateReaders(
        int number,
        AtomicReference<Integer> atomicReference
    ) {
        List<BoxReader<Integer>> readers = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            readers.add(new BoxReader<>(atomicReference));
        }
        return readers;
    }

    private List<BoxWriter<Integer>> generateWriters(
        int number,
        AtomicReference<Integer> box
    ) {
        List<BoxWriter<Integer>> writers = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            writers.add(new BoxWriter<>(box, i));
        }
        return writers;
    }
}
