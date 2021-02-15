package birintsev.concurrentcollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import picocli.CommandLine;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@CommandLine.Command(name = "start")
public class Command implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Command.class);

    private final static Random RANDOM = new Random(
        System.currentTimeMillis()
    );

    @CommandLine.Option(names = {"-itemsPerWriter", "-items", "-i"})
    private int itemsPerWriter;

    @CommandLine.Option(names = {"-multiboxSize", "-bufferSize"})
    private int multiboxSize;

    @CommandLine.Option(names = {"-writersAmount", "-writers"})
    private int writersAmount;

    @Override
    public void run() {
        Multibox<Integer> multibox = new Multibox<>(multiboxSize);
        List<BoxWriter<Integer>> boxWriters = generateWriters(
            multibox,
            writersAmount,
            itemsPerWriter
        );
        List<BoxReader<Integer>> boxReaders = generateReaders(
            multibox,
            writersAmount,
            itemsPerWriter
        );

        boxWriters.forEach(Thread::start);
        boxReaders.forEach(Thread::start);

        boxWriters.forEach(writer -> {
            try {
                writer.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
        boxReaders.forEach(reader -> {
            try {
                reader.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private List<Integer> generateItems(int amount) {
        return IntStream
            .rangeClosed(0, amount - 1)
            .boxed()
            .collect(Collectors.toList());
    }

    private List<BoxWriter<Integer>> generateWriters(
        Multibox<Integer> writeTo,
        final int amount,
        final int itemsPerWriter
    ) {
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(
            generateItems(amount * itemsPerWriter)
        );
        List<BoxWriter<Integer>> writers = new ArrayList<>(amount);
        IntStream.rangeClosed(0, amount - 1).forEach(
            i -> writers.add(
                new BoxWriter<>(
                    writeTo,
                    IntStream.rangeClosed(0, itemsPerWriter - 1)
                        .mapToObj(j -> priorityQueue.poll())
                        .collect(Collectors.toList()),
                    generatePriorityWithConcurrency()
                )
            )
        );
        return writers;
    }

    public List<BoxReader<Integer>> generateReaders(
        Multibox<Integer> readFrom,
        int amount,
        int itemsPerReader
    ) {
        List<BoxReader<Integer>> boxReaders = new ArrayList<>(amount);
        IntStream
            .rangeClosed(0, amount - 1)
            .forEach(
                i -> boxReaders.add(
                    new BoxReader<>(
                        readFrom,
                        generatePriorityWithConcurrency(),
                        itemsPerWriter
                    )
                )
            );
        return boxReaders;
    }

    private int generatePriorityWithConcurrency() {
        return Math.max(
            Thread.MIN_PRIORITY,
            RANDOM.nextInt(Thread.MAX_PRIORITY) / (3/*for concurrency*/)
        );
    }
}
