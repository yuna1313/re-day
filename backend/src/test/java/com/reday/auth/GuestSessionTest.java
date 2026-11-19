package com.reday.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class GuestSessionTest {

	@Autowired
	private MockMvc mvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void 게스트별로_일정이_격리되고_회원_설정에는_접근할_수_없다() throws Exception {
		String owner = startGuestSession().get("accessToken").asText();
		String visitor = startGuestSession().get("accessToken").asText();
		int scheduleId = createSchedule(owner);

		mvc.perform(get("/api/v1/schedules/{id}", scheduleId).header(HttpHeaders.AUTHORIZATION, bearer(owner)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").value("게스트 일정"));
		mvc.perform(get("/api/v1/schedules/{id}", scheduleId).header(HttpHeaders.AUTHORIZATION, bearer(visitor)))
			.andExpect(jsonPath("$.success").value(false));
		mvc.perform(get("/api/v1/schedules/{id}", scheduleId)).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(owner)))
			.andExpect(status().isForbidden());
	}

	@Test
	void 게스트_토큰을_갱신해도_같은_기록을_사용한다() throws Exception {
		JsonNode guest = startGuestSession();
		int scheduleId = createSchedule(guest.get("accessToken").asText());
		String response = mvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + guest.get("refreshToken").asText() + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andReturn().getResponse().getContentAsString();
		String renewedToken = objectMapper.readTree(response).at("/data/accessToken").asText();

		mvc.perform(get("/api/v1/schedules/{id}", scheduleId).header(HttpHeaders.AUTHORIZATION, bearer(renewedToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").value("게스트 일정"));
	}

	private int createSchedule(String accessToken) throws Exception {
		String created = mvc.perform(post("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"title":"게스트 일정","startAt":"2026-01-10 09:00:00","estimatedMinutes":30}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(created).at("/data/scheduleId").asInt();
	}

	private JsonNode startGuestSession() throws Exception {
		String response = mvc.perform(post("/api/v1/auth/guest"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.member.nickname").value("게스트"))
			.andExpect(jsonPath("$.data.member.email").doesNotExist())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("data");
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}
