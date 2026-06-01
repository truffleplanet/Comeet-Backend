package com.backend.domain.review.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.backend.domain.review.dto.request.ReviewReqDto;
import com.backend.domain.review.service.query.ReviewQueryService;
import com.backend.domain.store.service.command.StoreCommandService;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("ReviewFacadeService transaction integrity test")
class ReviewFacadeTransactionTest {

	private static final long TEST_USER_ID = 1L;
	private static final long TEST_ROASTERY_ID = 9_000_001L;
	private static final long TEST_STORE_ID = 9_000_001L;
	private static final long TEST_VISIT_ID = 9_000_001L;
	private static final long TEST_MENU_ID = 9_000_001L;

	@Autowired
	private ReviewFacadeService reviewFacadeService;

	@Autowired
	private ReviewQueryService reviewQueryService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoSpyBean
	private StoreCommandService storeCommandService;

	@BeforeEach
	void setUp() {
		cleanup();
		jdbcTemplate.update("""
			INSERT INTO roasteries (id, name)
			VALUES (?, ?)
			""", TEST_ROASTERY_ID, "transaction-test-roastery");
		jdbcTemplate.update("""
			INSERT INTO stores (
				id, roastery_id, owner_id, name, address, latitude, longitude, average_rating,
				review_count, visit_count, is_closed
			)
			VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, false)
			""", TEST_STORE_ID, TEST_ROASTERY_ID, TEST_USER_ID, "transaction-test-store",
			"Seoul transaction test road", new BigDecimal("37.50000000"), new BigDecimal("127.00000000"));
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
	@DisplayName("review creation rolls back when later store statistics update fails")
	void createReview_RollsBack_WhenStoreStatisticsUpdateFails() {
		doThrow(new RuntimeException("intentional rollback probe"))
			.when(storeCommandService)
			.applyReviewStatsDelta(anyLong(), anyInt(), anyInt(), any(BigDecimal.class));

		ReviewReqDto reqDto = new ReviewReqDto(
			TEST_VISIT_ID,
			TEST_MENU_ID,
			TEST_STORE_ID,
			"Transaction rollback probe review",
			true,
			"http://example.com/review.jpg",
			new BigDecimal("4.5"),
			List.of()
		);

		assertThatThrownBy(() -> reviewFacadeService.createReview(TEST_USER_ID, reqDto))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("intentional rollback probe");

		boolean exists = reviewQueryService.existsByVisitId(TEST_VISIT_ID);
		System.out.printf("%n[transaction probe] review remains after failure: %s%n", exists);
		assertThat(exists)
			.as("No review row should remain after the downstream failure")
			.isFalse();
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
