package th.co.heimdall.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import th.co.heimdall.core.enabled.annotation.EnableCoreCrud;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableCoreCrud
@SpringBootApplication
public class Application {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
