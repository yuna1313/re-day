package com.reday.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.VerificationCode;

@SpringBootTest
@AutoConfigureMockMvc
class GuestSessionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private EmailVerificationStore emailVerificationStore;

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

	@Test
	void 게스트_가입_후에도_일정과_회고가_유지되고_이전_토큰은_만료된다() throws Exception {
		JsonNode guest = startGuestSession();
		String guestToken = guest.get("accessToken").asText();
		int scheduleId = createSchedule(guestToken);
		String today = LocalDate.now().toString();
		mvc.perform(post("/api/v1/reflections")
				.header(HttpHeaders.AUTHORIZATION, bearer(guestToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"reflectionDate":"%s","content":"게스트 회고"}
					""".formatted(today)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		Email email = Email.of("guest-" + UUID.randomUUID() + "@example.com");
		String signup = """
			{"nickname":"새 회원","email":"%s","password":"Valid1234!","passwordConfirm":"Valid1234!","agreeTerms":true}
			""".formatted(email.value());
		mvc.perform(post("/api/v1/auth/guest/upgrade")
				.header(HttpHeaders.AUTHORIZATION, bearer(guestToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(signup))
			.andExpect(status().isForbidden());

		emailVerificationStore.save(EmailVerification.create(
			email, VerificationCode.of("123456"), LocalDateTime.now(), Duration.ofMinutes(5)
		));
		emailVerificationStore.complete(email);
		String upgraded = mvc.perform(post("/api/v1/auth/guest/upgrade")
				.header(HttpHeaders.AUTHORIZATION, bearer(guestToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(signup))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.member.memberId").value(guest.at("/member/memberId").asInt()))
			.andReturn().getResponse().getContentAsString();
		String memberToken = objectMapper.readTree(upgraded).at("/data/accessToken").asText();

		mvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(email.value()));
		mvc.perform(get("/api/v1/schedules/{id}", scheduleId).header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(jsonPath("$.data.title").value("게스트 일정"));
		mvc.perform(get("/api/v1/reflections/{date}", today).header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(jsonPath("$.data.content").value("게스트 회고"));
		mvc.perform(get("/api/v1/schedules/{id}", scheduleId).header(HttpHeaders.AUTHORIZATION, bearer(guestToken)))
			.andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + guest.get("refreshToken").asText() + "\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void 데모_세션에만_예시_일정과_미루기_인사이트가_있다() throws Exception {
		String response = mvc.perform(post("/api/v1/auth/guest")
				.contentType(MediaType.APPLICATION_JSON).content("{\"demo\":true}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String demoToken = objectMapper.readTree(response).at("/data/accessToken").asText();
		String guestToken = startGuestSession().get("accessToken").asText();
		String today = LocalDate.now().toString();
		var schedules = "/api/v1/schedules?viewType=WEEK&startDate=" + today + "&endDate=" + today;

		mvc.perform(get(schedules).header(HttpHeaders.AUTHORIZATION, bearer(demoToken)))
			.andExpect(jsonPath("$.data.schedules.length()").value(3));
		mvc.perform(get(schedules).header(HttpHeaders.AUTHORIZATION, bearer(guestToken)))
			.andExpect(jsonPath("$.data.schedules").isEmpty());
		mvc.perform(get("/api/v1/analytics/insights?periodType=LAST_7_DAYS")
				.header(HttpHeaders.AUTHORIZATION, bearer(demoToken)))
			.andExpect(jsonPath("$.data.topDeferReasons[0].deferReasonCode").value("NO_TIME"));
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
