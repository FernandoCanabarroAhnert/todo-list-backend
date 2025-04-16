package com.test.todo_list_backend.utils;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.todo_list_backend.models.dtos.LoginRequestDTO;

public class TokenUtils {

    public static String obtainAccessToken(MockMvc mockMvc, String username, String password, ObjectMapper objectMapper) throws Exception {
        ResultActions result = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequestDTO(username,password)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        String resulString = result.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resulString).get("token").toString();
    }

}
