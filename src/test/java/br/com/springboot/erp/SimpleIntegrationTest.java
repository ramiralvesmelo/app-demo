package br.com.springboot.erp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.springboot.erp.config.TestConfig;

/**
 * Teste de integração simples para garantir que o contexto da aplicação Spring Boot
 * seja carregado corretamente sem falhas de configuração.
 *
 * ✅ Passa se todas as configurações, beans e dependências forem inicializados com sucesso.
 * ⚠️ Falha se houver erro de configuração no Spring, no JPA ou em qualquer bean obrigatório.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.org.springframework=DEBUG",
    "logging.level.org.hibernate=DEBUG",
    "spring.jpa.show-sql=true"
})
public class SimpleIntegrationTest {

    @Test
    public void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}
