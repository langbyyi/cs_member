package cloud.hilang.cs_member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("cloud.hilang.cs_member.mapper")
@ConfigurationPropertiesScan("cloud.hilang.cs_member.config")
public class CsMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsMemberApplication.class, args);
    }

}
