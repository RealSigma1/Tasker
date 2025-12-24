package com.example.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {

    private static final String BASE = "/api/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listTasks_doesNotReturn5xx() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void createTask_returnsBodyWithId_andLocationHeaderPointsToResource() throws Exception {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("title", "Buy milk");
        req.put("description", "2 bottles");
        req.put("deadline", "2025-12-31T10:00:00");

        MvcResult res = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = res.getResponse().getContentAsString();

        String id = JsonPath.read(body, "$.id");
        String title = JsonPath.read(body, "$.title");
        Boolean completed = JsonPath.read(body, "$.completed");

        assertThat(id).isNotBlank();
        assertThat(title).isEqualTo("Buy milk");
        assertThat(completed).isEqualTo(false);

        String location = res.getResponse().getHeader("Location");
        assertThat(location).isEqualTo(BASE + "/" + id);
    }

    @Test
    void createThenGetById_returnsSameTask() throws Exception {
        String id = createTask("T-get", "desc");

        MvcResult getRes = mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = getRes.getResponse().getContentAsString();
        String gotId = JsonPath.read(body, "$.id");
        String title = JsonPath.read(body, "$.title");

        assertThat(gotId).isEqualTo(id);
        assertThat(title).isEqualTo("T-get");
    }

    @Test
    void list_containsCreatedTasksByTitle() throws Exception {
        String t1 = "List-" + UUID.randomUUID();
        String t2 = "List-" + UUID.randomUUID();

        createTask(t1, "d1");
        createTask(t2, "d2");

        MvcResult res = mockMvc.perform(get(BASE))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = res.getResponse().getContentAsString();

        List<String> titles = JsonPath.read(body, "$[*].title");
        assertThat(titles).contains(t1, t2);
    }

    @Test
    void updateTask_changesTitleAndDescription() throws Exception {
        String id = createTask("Old title", "Old desc");

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("title", "New title");
        update.put("description", "New desc");

        mockMvc.perform(put(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().is2xxSuccessful());

        MvcResult getRes = mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = getRes.getResponse().getContentAsString();
        assertThat((String) JsonPath.read(body, "$.title")).isEqualTo("New title");
        assertThat((String) JsonPath.read(body, "$.description")).isEqualTo("New desc");
    }

    @Test
    void deleteTask_thenGetReturns4xx() throws Exception {
        String id = createTask("To delete", "temp");

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createTask_withInvalidBody_returns4xx() throws Exception {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("title", "");
        req.put("description", "desc");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getUnknownUuid_returns4xx_not5xx() throws Exception {
        String unknown = UUID.randomUUID().toString();

        mockMvc.perform(get(BASE + "/" + unknown))
                .andExpect(status().is4xxClientError());
    }

    private String createTask(String title, String description) throws Exception {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("title", title);
        req.put("description", description);

        MvcResult res = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        assertThat(id).isNotBlank();
        return id;
    }
}
