package com.backend.domain.ai.converter;

import java.time.Duration;
import java.time.LocalDateTime;

import com.backend.domain.ai.dto.response.BatchImageGenerationResDto;
import com.backend.domain.ai.dto.response.BatchProgressResDto;
import com.backend.domain.ai.entity.BatchProgress;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BatchConverter {

	public BatchImageGenerationResDto toBatchImageGenerationResDto(final String batchId, final int totalTasks) {
		return BatchImageGenerationResDto.builder()
			.batchId(batchId)
			.totalTasks(totalTasks)
			.message(String.format("배치 작업이 시작되었습니다. 총 %d개 작업 예정입니다.", totalTasks))
			.build();
	}

	public BatchProgressResDto toBatchProgressResDto(final BatchProgress progress) {
		long elapsedSeconds = progress.getEndTime() != null
			? Duration.between(progress.getStartTime(), progress.getEndTime()).getSeconds()
			: Duration.between(progress.getStartTime(), LocalDateTime.now()).getSeconds();

		String statusMessage = progress.isCompleted()
			? String.format("배치 작업 완료 - 성공: %d, 실패: %d", progress.getCompleted().get(), progress.getFailed().get())
			: String.format("진행 중 - %d/%d (%.1f%%)", progress.getCompleted().get() + progress.getFailed().get(),
			progress.getTotal(), progress.getProgressPercentage());

		return BatchProgressResDto.builder()
			.batchId(progress.getBatchId())
			.total(progress.getTotal())
			.completed(progress.getCompleted().get())
			.failed(progress.getFailed().get())
			.remaining(progress.getRemaining())
			.progressPercentage(progress.getProgressPercentage())
			.isCompleted(progress.isCompleted())
			.startTime(progress.getStartTime())
			.endTime(progress.getEndTime())
			.elapsedSeconds(elapsedSeconds)
			.statusMessage(statusMessage)
			.build();
	}
}
