package com.backend.domain.review.service.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.backend.domain.review.dto.request.ReviewReqDto;
import com.backend.domain.review.dto.response.ReviewedResDto;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("ReviewFacadeService store statistics test")
class ReviewFacadeStoreStatsTest {

	private static final long TEST_USER_ID = 1L;
	private static final long TEST_ROASTERY_ID = 9_920_001L;
	private static final long TEST_STORE_ID = 9_920_001L;
	private static final long TEST_MENU_ID = 9_920_001L;
	private static final long FIRST_VISIT_ID = 9_920_001L;
	private static final long SECOND_VISIT_ID = 9_920_002L;

	@Autowired
	private ReviewFacadeService reviewFacadeService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		cleanup();
		jdbcTemplate.update("""
			INSERT INTO roasteries (id, name)
			VALUES (?, ?)
			""", TEST_ROASTERY_ID, "store-stats-test-roastery");
		jdbcTemplate.update("""
			INSERT INTO stores (
				id, roastery_id, owner_id, name, address, latitude, longitude, average_rating,
				review_count, visit_count, is_closed
			)
			VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, false)
			""", TEST_STORE_ID, TEST_ROASTERY_ID, TEST_USER_ID, "store-stats-test-store",
			"Seoul store stats test road", new BigDecimal("37.50000000"), new BigDecimal("127.00000000"));
		insertVisit(FIRST_VISIT_ID);
		insertVisit(SECOND_VISIT_ID);
	}

	@AfterEach
	void tearDown() {
		cleanup();
	}

	@Test
	@DisplayName("store review stats are updated incrementally while unrated reviews still count as reviews")
	void reviewStats_AreUpdatedIncrementally() {
		ReviewedResDto unratedReview = reviewFacadeService.createReview(
			TEST_USER_ID,
			reviewRequest(FIRST_VISIT_ID, "unrated review", null)
		);
		ReviewedResDto ratedReview = reviewFacadeService.createReview(
			TEST_USER_ID,
			reviewRequest(SECOND_VISIT_ID, "rated review", new BigDecimal("4.0"))
		);

		assertStoreStats(2, 1, "4.00", "4.00");

		reviewFacadeService.updateReview(
			unratedReview.reviewInfo().reviewId(),
			TEST_USER_ID,
			reviewRequest(FIRST_VISIT_ID, "unrated review now rated", new BigDecimal("5.0"))
		);

		assertStoreStats(2, 2, "9.00", "4.50");

		reviewFacadeService.deleteReview(ratedReview.reviewInfo().reviewId(), TEST_USER_ID);

		assertStoreStats(1, 1, "5.00", "5.00");

		reviewFacadeService.deleteReview(unratedReview.reviewInfo().reviewId(), TEST_USER_ID);

		assertStoreStats(0, 0, "0.00", "0.00");
	}

	private ReviewReqDto reviewRequest(final long visitId, final String content, final BigDecimal rating) {
		return new ReviewReqDto(
			visitId,
			TEST_MENU_ID,
			TEST_STORE_ID,
			content,
			true,
			null,
			rating,
			List.of()
		);
	}

	private void insertVisit(final long visitId) {
		jdbcTemplate.update("""
			INSERT INTO visits (id, user_id, menu_id, latitude, longitude, is_verified)
			VALUES (?, ?, ?, ?, ?, true)
			""", visitId, TEST_USER_ID, TEST_MENU_ID,
			new BigDecimal("37.50000000"), new BigDecimal("127.00000000"));
	}

	private void assertStoreStats(
		final int reviewCount,
		final int ratingCount,
		final String ratingSum,
		final String averageRating
	) {
		StoreStats stats = jdbcTemplate.queryForObject("""
			SELECT review_count, rating_count, rating_sum, average_rating
			FROM stores
			WHERE id = ?
			""", (rs, rowNum) -> new StoreStats(
			rs.getInt("review_count"),
			rs.getInt("rating_count"),
			rs.getBigDecimal("rating_sum"),
			rs.getBigDecimal("average_rating")
		), TEST_STORE_ID);

		assertThat(stats).isNotNull();
		assertThat(stats.reviewCount()).isEqualTo(reviewCount);
		assertThat(stats.ratingCount()).isEqualTo(ratingCount);
		assertThat(stats.ratingSum()).isEqualByComparingTo(ratingSum);
		assertThat(stats.averageRating()).isEqualByComparingTo(averageRating);
	}

	private void cleanup() {
		jdbcTemplate.update("""
			DELETE FROM tasting_notes
			WHERE review_id IN (
				SELECT id
				FROM reviews
				WHERE visit_id IN (?, ?)
			)
			""", FIRST_VISIT_ID, SECOND_VISIT_ID);
		jdbcTemplate.update("DELETE FROM reviews WHERE visit_id IN (?, ?)", FIRST_VISIT_ID, SECOND_VISIT_ID);
		jdbcTemplate.update("DELETE FROM visits WHERE id IN (?, ?)", FIRST_VISIT_ID, SECOND_VISIT_ID);
		jdbcTemplate.update("DELETE FROM stores WHERE id = ?", TEST_STORE_ID);
		jdbcTemplate.update("DELETE FROM roasteries WHERE id = ?", TEST_ROASTERY_ID);
	}

	private record StoreStats(
		int reviewCount,
		int ratingCount,
		BigDecimal ratingSum,
		BigDecimal averageRating
	) {
	}
}
