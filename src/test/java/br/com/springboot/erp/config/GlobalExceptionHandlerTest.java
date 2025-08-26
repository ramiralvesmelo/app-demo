package br.com.springboot.erp.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.NoSuchElementException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    // ===== DTOs =====
    static class BodyDTO {
        @NotBlank public String name;
        @Positive public Integer qty;
    }
    static class SearchForm {
        @Positive public Integer page; // @Valid em @ModelAttribute -> BindException
    }

    // ===== Controller mínimo para provocar erros =====
    @RestController
    @RequestMapping("/ex")
    static class DummyController {
    	
        @InitBinder
        void initBinder(WebDataBinder binder) {
            binder.initDirectFieldAccess(); // <- permite setar campos sem getters/setters
        }    	

        // 400 - JSON malformado -> HttpMessageNotReadableException
        @PostMapping(value="/unreadable", consumes="application/json")
        public String unreadable(@RequestBody BodyDTO dto){ return "ok"; }

        // 400 - body inválido -> MethodArgumentNotValidException
        @PostMapping(value="/not-valid", consumes="application/json")
        public String notValid(@Valid @RequestBody BodyDTO dto){ return "ok"; }

        // 400 - type mismatch -> MethodArgumentTypeMismatchException
        @GetMapping("/type-mismatch")
        public String typeMismatch(@RequestParam("qty") Integer qty) { return "ok"; }

        // 400 - @ModelAttribute inválido -> BindException
        @GetMapping("/bind-valid")
        public String bindValid(@Valid @ModelAttribute SearchForm form) { return "ok"; }

        // 400 - missing param -> MissingServletRequestParameterException
        @GetMapping("/missing")
        public String missing(@RequestParam("required") String reqParam) { return "ok"; }

        // 404 - not found
        @GetMapping("/not-found")
        public String notFound() { throw new NoSuchElementException("Recurso não encontrado"); }
    }

    @BeforeEach
    void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet(); // importante

        mvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)            // ⬅️ habilita @Valid
                .build();
    }

    @Test
    void unreadable_400() throws Exception {
        String badJson = "{ \"name\":\"abc\", \"qty\": 1"; // malformado
        mvc.perform(post("/ex/unreadable").contentType(APPLICATION_JSON).content(badJson))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message", containsString("JSON inválido")));
    }

    @Test
    void bodyNotValid_400() throws Exception {
        String invalid = "{\"name\":\"\",\"qty\":-5}";
        mvc.perform(post("/ex/not-valid").contentType(APPLICATION_JSON).content(invalid))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void typeMismatch_400() throws Exception {
        mvc.perform(get("/ex/type-mismatch").param("qty", "abc"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Parâmetro inválido."));
    }

    @Test
    void bindException_400() throws Exception {
        // page=-1 viola @Positive -> BindException
        mvc.perform(get("/ex/bind-valid").param("page", "-1"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Parâmetro inválido."));
    }

    @Test
    void missingParam_400() throws Exception {
        mvc.perform(get("/ex/missing")) // sem ?required=
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message", containsString("Parâmetro obrigatório ausente")));
    }

    @Test
    void notFound_404() throws Exception {
        mvc.perform(get("/ex/not-found"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Recurso não encontrado"));
    }

    @Test
    void methodNotAllowed_405() throws Exception {
        mvc.perform(get("/ex/not-valid")) // endpoint só aceita POST
           .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unsupportedMediaType_415() throws Exception {
        mvc.perform(post("/ex/not-valid").contentType(TEXT_PLAIN).content("x"))
           .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void illegalArgument_directCall() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/ex/illegal");
        ResponseEntity<?> resp = handler.handleIllegalArgument(new IllegalArgumentException("mensagem direta"), req);
        org.junit.jupiter.api.Assertions.assertEquals(400, resp.getStatusCodeValue());
    }
}
