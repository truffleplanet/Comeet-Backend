package com.backend.domain.passport.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.ai.event.PassportImageGeneratedEvent;
import com.backend.domain.passport.service.command.PassportCommandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportImageEventListener {

	private final PassportCommandService passportCommandService;

	@Async("virtualThreadExecutor")
	@EventListener
	@Transactional
	public void handleImageGenerated(final PassportImageGeneratedEvent event) {
		passportCommandService.updateCoverImage(event.getPassportId(), event.getImageUrl());
		log.info("[Event] Passport 이미지 업데이트 완료 - passportId: {}", event.getPassportId());

	}
}
