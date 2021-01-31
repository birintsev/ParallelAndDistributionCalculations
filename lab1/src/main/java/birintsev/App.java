package birintsev;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;

@AllArgsConstructor
@SpringBootApplication(scanBasePackages = "birintsev")
public class App {

    public static void main(String[] args) {;
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
    ) {
        return args ->
            new CommandLine(new ProcessVectorsCommand()).execute(args);
    }

}
