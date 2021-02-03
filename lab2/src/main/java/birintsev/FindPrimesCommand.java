package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "primals")
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FindPrimesCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        FindPrimesCommand.class
    );

    @CommandLine.Option(
        names = {"threadCount", "N"},
        type = int.class
    )
    private int threadsNumber;

    @CommandLine.Option(
        names = {"maxNumber", "M"},
        type = int.class
    )
    private int maxNumber;

    private final PrimeNumbersParallelGenerator generator;

    public FindPrimesCommand(PrimeNumbersParallelGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long duration;
        List<Integer> primes = generator.generate(maxNumber, threadsNumber);
        duration = System.currentTimeMillis() - startTime;

        LOGGER.info(
            "Prime numbers: "
                + listToString(primes)
                + ". Time spent: "
                + Duration.ofMillis(duration)
                + " ("
                + duration
                + "ms)"
        );
    }

    public static String listToString(List<Integer> primes) {
        final int maxListPrintLength = 1000;
        List<Integer> list = new ArrayList<>(primes);
        String s;
        list.sort(Integer::compareTo);
        s = list.toString();
        if (s.length() > maxListPrintLength) {
            s = s.substring(0, maxListPrintLength) + "...";
        }
        return s;
    }
}
