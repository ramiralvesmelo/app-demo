package br.com.springboot.erp.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.NoSuchElementException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.DummyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mvc;

    // --- DTO para validar @Valid / @NotBlank / @Positive
    static class DummyDTO {
        @NotBlank
        public String name;

        @Positive
        public Integer qty;
    }

    // --- Controller de teste: apenas dispara exceções específicas
    @RestController
    @RequestMapping("/ex")
    static class DummyController {

        @GetMapping("/illegal")
        public String illegal() {
            throw new IllegalArgumentException("bad arg");
        }

        @GetMapping("/constraint")
        public String constraint() {
            throw new javax.validation.ConstraintViolationException("violou regra", java.util.Collections.emptySet());
        }

        // JSON inválido -> HttpMessageNotReadableException
        @PostMapping(value = "/unreadable", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String unreadable(@RequestBody DummyDTO dto) {
            return "ok";
        }

        // @Valid body -> MethodArgumentNotValidException
        @PostMapping(value = "/not-valid", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String notValid(@Valid @RequestBody DummyDTO dto) {
            return "ok";
        }

        // Type mismatch em query param -> MethodArgumentTypeMismatchException/BindException
        @GetMapping("/bind")
        public String bind(@RequestParam("qty") Integer qty) {
            return "ok";
        }

        // Falta de parâmetro -> MissingServletRequestParameterException
        @GetMapping("/missing")
        public String missing(@RequestParam("q") String q) {
            return "ok";
        }

        // Recurso não encontrado -> NoSuchElementException
        @GetMapping("/not-found")
        public String notFound() {
            throw new NoSuchElementException("pedido não existe");
        }

        // Mapeia apenas GET; chamar POST gera 405
        @GetMapping("/method")
        public String onlyGet() { return "ok"; }

        // Aceita apenas JSON; enviar text/plain -> 415
        @PostMapping(value = "/consumes-json", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String onlyJson(@RequestBody DummyDTO dto) { return "ok"; }

        // Genérica -> 500
        @GetMapping("/boom")
        public String boom() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    @DisplayName("400 - IllegalArgumentException")
    void illegalArgument_400() throws Exception {
        mvc.perform(get("/ex/illegal"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message").value("bad arg"))
           .andExpect(jsonPath("$.path").value("/ex/illegal"));
    }

    @Test
    @DisplayName("400 - ConstraintViolationException")
    void constraintViolation_400() throws Exception {
        mvc.perform(get("/ex/constraint"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message", containsString("violou")))
           .andExpect(jsonPath("$.path").value("/ex/constraint"));
    }

    @Test
    @DisplayName("400 - HttpMessageNotReadableException (JSON inválido)")
    void unreadable_400() throws Exception {
        String badJson = "{ \"name\": \"abc\", \"qty\": 1"; // falta fechar }
        mvc.perform(post("/ex/unreadable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message", containsString("JSON inválido")))
           .andExpect(jsonPath("$.path").value("/ex/unreadable"));
    }

    @Test
    @DisplayName("400 - MethodArgumentNotValidException (@Valid body)")
    void methodArgumentNotValid_400() throws Exception {
        // name em branco e qty negativo violam as constraints
        String invalid = "{"
                + "\"name\":\"\","
                + "\"qty\": -5"
                + "}";

        mvc.perform(post("/ex/not-valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message", Matchers.any(String.class)))
           .andExpect(jsonPath("$.path").value("/ex/not-valid"));
    }

    @Test
    @DisplayName("400 - MethodArgumentTypeMismatch/BindException (param tipo errado)")
    void bind_400() throws Exception {
        mvc.perform(get("/ex/bind").param("qty", "abc")) // deveria ser número
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message", containsString("Parâmetro inválido")))
           .andExpect(jsonPath("$.path").value("/ex/bind"));
    }

    @Test
    @DisplayName("400 - MissingServletRequestParameterException")
    void missingParam_400() throws Exception {
        mvc.perform(get("/ex/missing")) // sem ?q=
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.message", containsString("Parâmetro obrigatório ausente")))
           .andExpect(jsonPath("$.path").value("/ex/missing"));
    }

    @Test
    @DisplayName("404 - NoSuchElementException")
    void notFound_404() throws Exception {
        mvc.perform(get("/ex/not-found"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404))
           .andExpect(jsonPath("$.error").value("Not Found"))
           .andExpect(jsonPath("$.message").value("pedido não existe"))
           .andExpect(jsonPath("$.path").value("/ex/not-found"));
    }

    @Test
    @DisplayName("405 - HttpRequestMethodNotSupportedException")
    void methodNotAllowed_405() throws Exception {
        mvc.perform(post("/ex/method"))
           .andExpect(status().isMethodNotAllowed())
           .andExpect(jsonPath("$.status").value(405))
           .andExpect(jsonPath("$.error").value("Method Not Allowed"))
           .andExpect(jsonPath("$.message", Matchers.any(String.class)))
           .andExpect(jsonPath("$.path").value("/ex/method"));
    }

    @Test
    @DisplayName("415 - HttpMediaTypeNotSupportedException")
    void unsupportedMediaType_415() throws Exception {
        mvc.perform(post("/ex/consumes-json")
                .contentType(MediaType.TEXT_PLAIN)
                .content("name=abc"))
           .andExpect(status().isUnsupportedMediaType())
           .andExpect(jsonPath("$.status").value(415))
           .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
           .andExpect(jsonPath("$.message", Matchers.any(String.class)))
           .andExpect(jsonPath("$.path").value("/ex/consumes-json"));
    }

    @Test
    @DisplayName("500 - Exception genérica")
    void generic_500() throws Exception {
        mvc.perform(get("/ex/boom"))
           .andExpect(status().isInternalServerError())
           .andExpect(jsonPath("$.status").value(500))
           .andExpect(jsonPath("$.error").value("Internal Server Error"))
           .andExpect(jsonPath("$.message").value("boom"))
           .andExpect(jsonPath("$.path").value("/ex/boom"));
    }
}
