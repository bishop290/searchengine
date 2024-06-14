package searchengine.integration.tools;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Transactional
@SpringBootTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public @interface IntegrationTest {
}
