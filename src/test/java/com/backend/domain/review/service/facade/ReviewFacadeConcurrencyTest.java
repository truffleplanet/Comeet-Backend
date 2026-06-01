package com.backend.domain.review.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.review.dto.request.ReviewReqDto;
import com.backend.domain.review.service.query.ReviewQueryService;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("ReviewFacadeService concurrency test")
class ReviewFacadeConcurrencyTest {

	private static final long TEST_USER_ID = 1L;
	private static final long TEST_ROASTERY_ID = 9_910_001L;
	private static final long TEST_STORE_ID = 9_910_001L;
	private static final long TEST_VISIT_ID = 9_910_001L;
	private static final long TEST_MENU_ID = 9_910_001L;

	@Autowired
	private ReviewFacadeService reviewFacadeService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoSpyBean
	private ReviewQueryService reviewQueryService;

	@BeforeEach
	void setUp() {
		cleanup();
		jdbcTemplate.update("""
			INSERT INTO roasteries (id, name)
			VALUES (?, ?)
			""", TEST_ROASTERY_ID, "concurrency-test-roastery");
		jdbcTemplate.update("""
			INSERT INTO stores (
				id, roastery_id, owner_id, name, address, latitude, longitude, average_rating,
				review_count, visit_count, is_closed
			)
			VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, false)
			""", TEST_STORE_ID, TEST_ROASTERY_ID, TEST_USER_ID, "concurrency-test-store",
			"Seoul concurrency test road", new BigDecimal("37.50000000"), new BigDecimal("127.00000000"));
		jdbcTemplate.update("""
			INSERT INTO visits (id, user_id, menu_id, latitude, longitude, is_verified)
			VALUES (?, ?, ?, ?, ?, true)
			""", TEST_VISIT_ID, TEST_USER_ID, TEST_MENU_ID,
			new BigDecimal("37.50000000"), new BigDecimal("127.00000000"));
	}

	@AfterEach
	void tearDown() {
		cleanup();
	}

	@Test
	@DisplayName("only one concurrent review creation succeeds and duplicate losers become business exceptions")
	void createReview_ConcurrentDuplicateRequest_ConvertsToBusinessException() throws Exception {
		doReturn(false).when(reviewQueryService).existsByVisitId(TEST_VISIT_ID);

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger businessExceptionCount = new AtomicInteger();
		AtomicInteger otherExceptionCount = new AtomicInteger();

		ReviewReqDto reqDto = new ReviewReqDto(
			TEST_VISIT_ID,
			TEST_MENU_ID,
			TEST_STORE_ID,
			"Concurrent duplicate review probe",
			true,
			"http://example.com/concurrency-review.jpg",
			new BigDecimal("4.0"),
			List.of()
		);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await();
					reviewFacadeService.createReview(TEST_USER_ID, reqDto);
					successCount.incrementAndGet();
				} catch (BusinessException e) {
					if (e.getErrorCode() == ErrorCode.REVIEW_ALREADY_EXISTS_FOR_VISIT) {
						businessExceptionCount.incrementAndGet();
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

		startLatch.countDown();
		assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
		executorService.shutdown();

		Integer persistedReviewCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM reviews WHERE visit_id = ?",
			Integer.class,
			TEST_VISIT_ID
		);

		System.out.printf(
			"%n[concurrency probe] success=%d, business=%d, other=%d, persisted=%d%n",
			successCount.get(),
			businessExceptionCount.get(),
			otherExceptionCount.get(),
			persistedReviewCount
		);
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(businessExceptionCount.get()).isEqualTo(threadCount - 1);
		assertThat(otherExceptionCount.get()).isZero();
		assertThat(persistedReviewCount).isEqualTo(1);
	}

	private void cleanup() {
		jdbcTemplate.update("DELETE FROM tasting_notes WHERE review_id IN (SELECT id FROM reviews WHERE visit_id = ?)",
			TEST_VISIT_ID);
		jdbcTemplate.update("DELETE FROM reviews WHERE visit_id = ?", TEST_VISIT_ID);
		jdbcTemplate.update("DELETE FROM visits WHERE id = ?", TEST_VISIT_ID);
		jdbcTemplate.update("DELETE FROM stores WHERE id = ?", TEST_STORE_ID);
		jdbcTemplate.update("DELETE FROM roasteries WHERE id = ?", TEST_ROASTERY_ID);
	}
}
