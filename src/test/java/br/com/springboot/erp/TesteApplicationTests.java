package br.com.springboot.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TesteApplicationTests {

    @Test
    void contextLoads() {
        // Testa se o contexto do Spring sobe sem exceptions
    }

    @Test
    void mainMethodRuns() {
        // Executa o main apenas para garantir que não lança erros
        TesteApplication.main(new String[]{});
        assertThat(true).isTrue(); // dummy assert só pra marcar execução
    }
}
