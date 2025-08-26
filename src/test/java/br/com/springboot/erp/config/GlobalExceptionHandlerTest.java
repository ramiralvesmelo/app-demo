	package br.com.springboot.erp.config;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    // DTO simples para @Valid
    static class DummyDTO {
        @NotBlank public String name;
        @Positive public Integer qty;
    }

    // Controller mínimo só para provocar exceções
    @RestController
    @RequestMapping("/ex")
    static class DummyController {
        // JSON malformado -> HttpMessageNotReadableException
        @PostMapping(value="/unreadable", consumes=MediaType.APPLICATION_JSON_VALUE)
        public String unreadable(@RequestBody DummyDTO dto){ return "ok"; }
        // Body inválido -> MethodArgumentNotValidException
        @PostMapping(value="/not-valid", consumes=MediaType.APPLICATION_JSON_VALUE)
        public String notValid(@Valid @RequestBody DummyDTO dto){ return "ok"; }
    }

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler()) // 👉 seu advice
                .build();
    }

    @Test
    void unreadable_400() throws Exception {
        String badJson = "{ \"name\":\"abc\", \"qty\": 1"; // malformado (falta })
        mvc.perform(post("/ex/unreadable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message", containsString("JSON inválido")));
    }

    @Test
    void notValid_400() throws Exception {
        String invalid = "{"
                + "\"name\":\"\","   // @NotBlank
                + "\"qty\": -5"      // @Positive
                + "}";
        mvc.perform(post("/ex/not-valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.error").value("Bad Request"))
           .andExpect(jsonPath("$.path").value("/ex/not-valid"));
    }
    
    @Test
    void handleIllegalArgument_directCall() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/ex/illegal");

        ResponseEntity<?> resp = handler.handleIllegalArgument(
                new IllegalArgumentException("mensagem direta"), req);

        assertEquals(400, resp.getStatusCodeValue());
        // opcional: validar campos do ErrorResponse via cast
    }    
}
