package birintsev.secure.semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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


    public Command(@Value("${maxItemsToReadWrite}") int maxItemsToReadWrite) {
        this.maxItemsToReadWrite = maxItemsToReadWrite;
    }

    @Override
    public void run() {
        long startTime;
        long duration;
        Box<Integer> box = new Box<>(null);

        Semaphore boxWasWritten = new Semaphore(0);
        Semaphore boxWasRead = new Semaphore(1);

        List<BoxWriter<Integer>> boxWriters;
        List<BoxReader<Integer>> boxReaders;

        Thread writersThread;
        Thread readersThread;

        validate();

        boxWriters = generateWriters(
            itemsToReadWrite,
            box,
            boxWasWritten,
            boxWasRead
        );
        boxReaders = generateReaders(
            itemsToReadWrite,
            box,
            boxWasWritten,
            boxWasRead
        );

        startTime = System.currentTimeMillis();
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
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Time took: " + duration + "ms");
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
        Box<Integer> box,
        Semaphore boxWasWritten,
        Semaphore boxWasRead
    ) {
        List<BoxReader<Integer>> readers = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            readers.add(new BoxReader<>(box, boxWasWritten, boxWasRead));
        }
        return readers;
    }

    private List<BoxWriter<Integer>> generateWriters(
        int number,
        Box<Integer> box,
        Semaphore boxWasWritten,
        Semaphore boxWasRead
    ) {
        List<BoxWriter<Integer>> writers = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            writers.add(new BoxWriter<>(box, i, boxWasWritten, boxWasRead));
        }
        return writers;
    }
}
