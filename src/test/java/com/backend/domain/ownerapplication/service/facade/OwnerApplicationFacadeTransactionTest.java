package com.backend.domain.ownerapplication.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.backend.domain.ownerapplication.dto.request.OwnerApplicationApproveReqDto;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;
import com.backend.domain.store.dto.request.StoreCreateReqDto;
import com.backend.domain.store.service.facade.StoreFacadeService;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("OwnerApplicationFacadeService 트랜잭션 테스트")
class OwnerApplicationFacadeTransactionTest {

	private static final Long USER_ID = 9_950_001L;
	private static final Long ADMIN_ID = 9_950_002L;
	private static final Long ROASTERY_ID = 9_950_001L;

	@Autowired
	private OwnerApplicationFacadeService ownerApplicationFacadeService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoSpyBean
	private StoreFacadeService storeFacadeService;

	@AfterEach
	void cleanup() {
		jdbcTemplate.update("""
			DELETE h
			FROM owner_application_review_histories h
			JOIN owner_applications a ON a.id = h.application_id
			WHERE a.user_id IN (?, ?)
			""", USER_ID, ADMIN_ID);
		jdbcTemplate.update("DELETE FROM owner_applications WHERE user_id IN (?, ?)", USER_ID, ADMIN_ID);
		jdbcTemplate.update("DELETE FROM stores WHERE owner_id IN (?, ?)", USER_ID, ADMIN_ID);
		jdbcTemplate.update("DELETE FROM roasteries WHERE id = ?", ROASTERY_ID);
		jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?)", USER_ID, ADMIN_ID);
	}

	@Test
	@DisplayName("승인 중 가맹점 생성에서 예외가 발생하면 신청 상태, 검토 이력, 권한 변경이 모두 롤백된다")
	void approve_WhenStoreCreationFails_RollbackAll() {
		// given
		prepareUser(USER_ID, "USER");
		prepareUser(ADMIN_ID, "ADMIN");
		prepareRoastery();
		ownerApplicationFacadeService.apply(USER_ID, createReqDto());
		Long applicationId = findApplicationId();

		doThrow(new RuntimeException("의도된 승인 롤백 검증용 예외"))
			.when(storeFacadeService)
			.createStore(any(StoreCreateReqDto.class), eq(USER_ID));

		// when & then
		assertThatThrownBy(() -> ownerApplicationFacadeService.approve(
			applicationId,
			ADMIN_ID,
			new OwnerApplicationApproveReqDto("승인 검토 완료")
		))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("의도된 승인 롤백 검증용 예외");

		String status = jdbcTemplate.queryForObject(
			"SELECT status FROM owner_applications WHERE id = ?",
			String.class,
			applicationId
		);
		Long reviewedBy = jdbcTemplate.queryForObject(
			"SELECT reviewed_by FROM owner_applications WHERE id = ?",
			Long.class,
			applicationId
		);
		String role = jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String.class, USER_ID);
		Integer storeCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM stores WHERE owner_id = ?",
			Integer.class,
			USER_ID
		);
		Integer historyCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM owner_application_review_histories WHERE application_id = ?",
			Integer.class,
			applicationId
		);

		assertThat(status).isEqualTo("PENDING");
		assertThat(reviewedBy).isNull();
		assertThat(role).isEqualTo("USER");
		assertThat(storeCount).isZero();
		assertThat(historyCount).isZero();
	}

	private void prepareUser(final Long userId, final String role) {
		jdbcTemplate.update("""
			INSERT INTO users (id, name, email, nick_name, social_id, role)
			VALUES (?, ?, ?, ?, ?, ?)
			ON DUPLICATE KEY UPDATE
			    name = VALUES(name),
			    email = VALUES(email),
			    nick_name = VALUES(nick_name),
			    social_id = VALUES(social_id),
			    role = VALUES(role)
			""",
			userId,
			"owner-application-tx-test-" + userId,
			"owner-application-tx-test-" + userId + "@example.com",
			"txtest" + userId,
			"owner-application-tx-test-" + userId,
			role
		);
	}

	private void prepareRoastery() {
		jdbcTemplate.update("""
			INSERT INTO roasteries (id, name)
			VALUES (?, ?)
			ON DUPLICATE KEY UPDATE name = VALUES(name)
			""", ROASTERY_ID, "Owner Application Tx Test Roastery");
	}

	private Long findApplicationId() {
		return jdbcTemplate.queryForObject(
			"SELECT id FROM owner_applications WHERE user_id = ? AND status = 'PENDING'",
			Long.class,
			USER_ID
		);
	}

	private OwnerApplicationCreateReqDto createReqDto() {
		return new OwnerApplicationCreateReqDto(
			ROASTERY_ID,
			"Owner Application Tx Test Store",
			"Specialty coffee",
			"서울시 강남구 테헤란로 950",
			new BigDecimal("37.50120000"),
			new BigDecimal("127.03960000"),
			"123-45-67890",
			"김코밋",
			"https://example.com/business-license.pdf",
			"09:00-22:00",
			"SPECIALTY_COFFEE",
			"02-1234-5678",
			"https://example.com/thumbnail.jpg"
		);
	}
}
