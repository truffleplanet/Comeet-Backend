package com.backend.domain.ownerapplication.service.facade;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationApproveReqDto;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("OwnerApplicationFacadeService 동시성 제어 테스트")
class OwnerApplicationFacadeConcurrencyTest {

	private static final Long USER_ID = 9_940_001L;
	private static final Long ADMIN_ID = 9_940_002L;
	private static final Long ROASTERY_ID = 9_940_001L;

	@Autowired
	private OwnerApplicationFacadeService ownerApplicationFacadeService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
	@DisplayName("동일 사용자가 동시에 가맹점주 신청을 생성해도 PENDING 신청은 1건만 생성된다")
	void apply_Concurrency_OnlyOnePendingApplication() throws InterruptedException {
		// given
		prepareUser(USER_ID, "USER");
		prepareRoastery();

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger alreadyPendingCount = new AtomicInteger();
		AtomicInteger otherExceptionCount = new AtomicInteger();

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await();
					ownerApplicationFacadeService.apply(USER_ID, createReqDto());
					successCount.incrementAndGet();
				} catch (BusinessException e) {
					if (e.getErrorCode() == ErrorCode.OWNER_APPLICATION_ALREADY_PENDING) {
						alreadyPendingCount.incrementAndGet();
					} else {
						otherExceptionCount.incrementAndGet();
					}
				} catch (Exception e) {
					otherExceptionCount.incrementAndGet();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		// when
		startLatch.countDown();
		doneLatch.await();
		executorService.shutdown();

		// then
		Integer pendingCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM owner_applications WHERE user_id = ? AND status = 'PENDING'",
			Integer.class,
			USER_ID
		);

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(alreadyPendingCount.get()).isEqualTo(threadCount - 1);
		assertThat(otherExceptionCount.get()).isZero();
		assertThat(pendingCount).isEqualTo(1);
	}

	@Test
	@DisplayName("동일 신청을 동시에 승인해도 1건만 승인되고 가맹점/검토 이력도 1건만 생성된다")
	void approve_Concurrency_OnlyOneApprovalWins() throws InterruptedException {
		// given
		prepareUser(USER_ID, "USER");
		prepareUser(ADMIN_ID, "ADMIN");
		prepareRoastery();
		ownerApplicationFacadeService.apply(USER_ID, createReqDto());
		Long applicationId = findApplicationId();

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger notPendingCount = new AtomicInteger();
		AtomicInteger otherExceptionCount = new AtomicInteger();

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await();
					ownerApplicationFacadeService.approve(
						applicationId,
						ADMIN_ID,
						new OwnerApplicationApproveReqDto("동시 승인 검증")
					);
					successCount.incrementAndGet();
				} catch (BusinessException e) {
					if (e.getErrorCode() == ErrorCode.OWNER_APPLICATION_NOT_PENDING) {
						notPendingCount.incrementAndGet();
					} else {
						otherExceptionCount.incrementAndGet();
					}
				} catch (Exception e) {
					otherExceptionCount.incrementAndGet();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		// when
		startLatch.countDown();
		doneLatch.await();
		executorService.shutdown();

		// then
		String status = jdbcTemplate.queryForObject(
			"SELECT status FROM owner_applications WHERE id = ?",
			String.class,
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

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(notPendingCount.get()).isEqualTo(threadCount - 1);
		assertThat(otherExceptionCount.get()).isZero();
		assertThat(status).isEqualTo("APPROVED");
		assertThat(role).isEqualTo("OWNER");
		assertThat(storeCount).isEqualTo(1);
		assertThat(historyCount).isEqualTo(1);
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
			"owner-application-test-" + userId,
			"owner-application-test-" + userId + "@example.com",
			"test" + userId,
			"owner-application-test-" + userId,
			role
		);
	}

	private void prepareRoastery() {
		jdbcTemplate.update("""
			INSERT INTO roasteries (id, name)
			VALUES (?, ?)
			ON DUPLICATE KEY UPDATE name = VALUES(name)
			""", ROASTERY_ID, "Owner Application Test Roastery");
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
			"Owner Application Test Store",
			"Specialty coffee",
			"서울시 강남구 테헤란로 940",
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
