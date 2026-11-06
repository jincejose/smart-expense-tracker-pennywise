package com.pennywise.tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import com.pennywise.tracker.repository.ExpenseRepository;
import com.pennywise.tracker.repository.BudgetRepository;
import com.pennywise.tracker.repository.AppUserRepository;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "USER")
class ExpenseTrackerIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ExpenseRepository expenseRepository;
    @Autowired BudgetRepository budgetRepository;
    @Autowired AppUserRepository appUserRepository;
    @Autowired ObjectMapper objectMapper;

    @Test void serverRenderedPagesLoad() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("overview"));
        mvc.perform(get("/transactions")).andExpect(status().isOk()).andExpect(view().name("transactions"));
        mvc.perform(get("/budgets")).andExpect(status().isOk()).andExpect(view().name("budgets"));
        mvc.perform(get("/reports")).andExpect(status().isOk()).andExpect(view().name("reports"));
        mvc.perform(get("/expenses/new")).andExpect(status().isOk()).andExpect(view().name("expense-form"));
    }

    @Test void loginAndLogoutWork() throws Exception {
        mvc.perform(get("/").accept(MediaType.TEXT_HTML).with(anonymous()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login").with(anonymous()))
                .andExpect(status().isOk()).andExpect(view().name("login"));
        var login = mvc.perform(post("/login").with(anonymous()).with(csrf())
                        .param("username", "admin").param("password", "pennywise"))
                .andExpect(status().is3xxRedirection()).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        String dashboard = mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var csrfField = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(dashboard);
        org.junit.jupiter.api.Assertions.assertTrue(csrfField.find(), "Dashboard logout form should include a CSRF token");
        mvc.perform(post("/logout").session(session).param("_csrf", csrfField.group(1)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout"));
    }

    @Test void visitorsCanCreateAnAccountAndSignIn() throws Exception {
        mvc.perform(get("/register").with(anonymous()))
                .andExpect(status().isOk()).andExpect(view().name("register"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Username")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Password")));

        mvc.perform(post("/register").with(anonymous()).with(csrf())
                        .param("username", "New.Tester").param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?created"));
        var savedUser = appUserRepository.findByUsername("new.tester").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("safe-password-123", savedUser.getPasswordHash());

        var login = mvc.perform(post("/login").with(anonymous()).with(csrf())
                        .param("username", "NEW.TESTER").param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        mvc.perform(get("/").with(anonymous()).with(httpBasic("NEW.TESTER", "safe-password-123")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("new.tester")));

        mvc.perform(post("/register").with(anonymous()).with(csrf())
                        .param("username", "new.tester").param("password", "different-password"))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("already taken")));
    }

    @Test void postmanApiRequiresBasicAuthentication() throws Exception {
        mvc.perform(get("/api/expenses").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/expenses").with(anonymous()).with(httpBasic("admin", "pennywise")))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test void expenseApiCreatesValidExpenseAndRejectsInvalidAmount() throws Exception {
        String json = "{\"title\":\"Postman lunch\",\"category\":\"Food & dining\",\"amount\":18.40,\"spentOn\":\"2026-11-06\",\"paymentMethod\":\"Card\",\"note\":\"Test run\"}";
        var created = mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Postman lunch"))
                .andExpect(jsonPath("$.amount").value(18.40));
        long id = objectMapper.readTree(created.andReturn().getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/expenses/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated lunch\",\"category\":\"Food & dining\",\"amount\":20,\"spentOn\":\"2026-11-06\",\"paymentMethod\":\"Cash\",\"note\":\"Updated via API\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated lunch"));
        mvc.perform(delete("/api/expenses/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Invalid\",\"category\":\"Other\",\"amount\":0,\"spentOn\":\"2026-11-06\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }

    @Test void healthAndBudgetApiRespond() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
        mvc.perform(get("/api/budgets")).andExpect(status().isOk()).andExpect(jsonPath("$[0].category").exists());
    }

    @Test void expenseFormSupportsCreateEditAndDelete() throws Exception {
        mvc.perform(post("/expenses").with(csrf()).param("title", "Form test lunch").param("category", "Food & dining")
                        .param("amount", "12.50").param("spentOn", "2026-11-06").param("paymentMethod", "Card").param("note", "MVC test"))
                .andExpect(status().is3xxRedirection());
        var saved = expenseRepository.findAllByOrderBySpentOnDescIdDesc().stream().filter(e -> e.getTitle().equals("Form test lunch")).findFirst().orElseThrow();
        mvc.perform(post("/expenses/{id}/edit", saved.getId()).with(csrf()).param("title", "Edited form lunch").param("category", "Food & dining")
                        .param("amount", "14.00").param("spentOn", "2026-11-06").param("paymentMethod", "Cash").param("note", "Updated"))
                .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertEquals("Edited form lunch", expenseRepository.findById(saved.getId()).orElseThrow().getTitle());
        mvc.perform(post("/expenses/{id}/delete", saved.getId()).with(csrf())).andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertFalse(expenseRepository.existsById(saved.getId()));
    }

    @Test void budgetFormSavesChangesAndReportExportsCsv() throws Exception {
        var params = new java.util.LinkedHashMap<String, String>();
        int[] amount = {515, 300, 220, 140, 160, 240, 300, 100};
        for (int i = 0; i < com.pennywise.tracker.service.ExpenseService.categories().size(); i++) {
            String category = com.pennywise.tracker.service.ExpenseService.categories().get(i);
            params.put("limits[" + category + "]", String.valueOf(amount[i]));
        }
        var request = post("/budgets").with(csrf());
        params.forEach(request::param);
        mvc.perform(request).andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("515.00"), budgetRepository.findByCategory("Food & dining").orElseThrow().getAmount());
        mvc.perform(get("/reports/export").param("month", "2026-11"))
                .andExpect(status().isOk()).andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("pennywise-2026-11.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }
}
