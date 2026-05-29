package com.backend.domain.review.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.bean.enums.RoastingLevel;
import com.backend.domain.review.entity.CuppingNote;
import com.backend.domain.review.mapper.command.CuppingNoteCommandMapper;
import com.backend.domain.review.service.command.implement.CuppingNoteCommandServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuppingNoteCommandService 테스트")
class CuppingNoteCommandServiceTest {

	@Mock
	private CuppingNoteCommandMapper commandMapper;

	@InjectMocks
	private CuppingNoteCommandServiceImpl cuppingNoteCommandService;

	private CuppingNote testCuppingNote;

	@BeforeEach
	void setUp() {
		testCuppingNote = CuppingNote.builder()
			.id(1L)
			.reviewId(100L)
			.roastingLevel(RoastingLevel.MEDIUM)
			.fragranceScore(new BigDecimal("8.50"))
			.aromaScore(new BigDecimal("8.00"))
			.flavorScore(new BigDecimal("9.00"))
			.aftertasteScore(new BigDecimal("8.75"))
			.acidityScore(new BigDecimal("8.25"))
			.sweetnessScore(new BigDecimal("9.25"))
			.mouthfeelScore(new BigDecimal("8.50"))
			.totalScore(new BigDecimal("60.25"))
			.fragranceAromaDetail("꽃향기와 베리류의 향이 강하게 느껴짐")
			.flavorAftertasteDetail("블루베리와 다크 초콜릿의 풍미")
			.acidityNotes("밝고 깨끗한 산미")
			.sweetnessNotes("캐러멜과 꿀의 단맛")
			.mouthfeelNotes("크리미하고 부드러운 바디감")
			.overallNotes("균형잡힌 스페셜티 커피")
			.createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
			.updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
			.build();
	}

	@Test
	@DisplayName("커핑 노트 저장 성공")
	void insert_Success() {
		// given
		when(commandMapper.insert(any(CuppingNote.class))).thenReturn(1);

		// when
		int result = cuppingNoteCommandService.insert(testCuppingNote);

		// then
		assertThat(result).isEqualTo(1);
		verify(commandMapper, times(1)).insert(testCuppingNote);
	}

	@Test
	@DisplayName("커핑 노트 저장 실패 - 0 rows affected")
	void insert_Fail() {
		// given
		when(commandMapper.insert(any(CuppingNote.class))).thenReturn(0);

		// when & then
		assertThatThrownBy(() -> cuppingNoteCommandService.insert(testCuppingNote))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.DATABASE_ERROR);
			});

		verify(commandMapper, times(1)).insert(testCuppingNote);
	}

	@Test
	@DisplayName("커핑 노트 업데이트 성공")
	void update_Success() {
		// given
		when(commandMapper.update(any(CuppingNote.class))).thenReturn(1);

		// when
		int result = cuppingNoteCommandService.update(testCuppingNote);

		// then
		assertThat(result).isEqualTo(1);
		verify(commandMapper, times(1)).update(testCuppingNote);
	}

	@Test
	@DisplayName("커핑 노트 업데이트 실패 - 존재하지 않는 ID")
	void update_Fail_NotFound() {
		// given
		when(commandMapper.update(any(CuppingNote.class))).thenReturn(0);

		// when & then
		assertThatThrownBy(() -> cuppingNoteCommandService.update(testCuppingNote))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.DATABASE_ERROR);
			});

		verify(commandMapper, times(1)).update(testCuppingNote);
	}
}
