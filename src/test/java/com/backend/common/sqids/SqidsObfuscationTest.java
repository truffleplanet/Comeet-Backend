package com.backend.common.sqids;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.context.ActiveProfiles;
import com.backend.common.sqids.config.SqidWebMvcConfig;
import com.backend.common.sqids.domain.Sqid;
import com.backend.common.sqids.service.IdObfuscator;
import com.backend.domain.review.dto.request.ReviewReqDto;
import com.backend.domain.store.dto.response.StoreResDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("local")
class SqidsObfuscationTest {

	@Autowired
	private IdObfuscator idObfuscator;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Sqids 인코딩 및 디코딩이 정상적으로 대칭성을 유지하는지 확인한다")
	void testObfuscationSymmetry() {
		// given
		Long originalId = 2026L;

		// when
		String encoded = idObfuscator.encode(originalId);
		Long decoded = idObfuscator.decode(encoded);

		// then
		assertThat(encoded).isNotNull().isNotBlank().hasSizeGreaterThanOrEqualTo(8);
		assertThat(decoded).isEqualTo(originalId);
	}

	@Test
	@DisplayName("Jackson 직렬화 및 역직렬화 시 Sqid 객체가 정상적으로 String으로 직렬화 및 Long으로 역직렬화되는지 확인한다")
	void testJacksonSerialization() throws Exception {
		// given
		TestDto dto = new TestDto(Sqid.of(456L), "TestName");

		// when
		String json = objectMapper.writeValueAsString(dto);
		TestDto deserialized = objectMapper.readValue(json, TestDto.class);

		// then
		assertThat(json).contains("\"id\":");
		// should be encoded as a string (not a number)
		assertThat(json).matches(".*\"id\":\"[a-zA-Z0-9]{8,}\".*");
		assertThat(deserialized.id().value()).isEqualTo(456L);
	}

	@Test
	@DisplayName("외부 응답 DTO의 Long ID 필드는 자동으로 Sqids 문자열로 직렬화한다")
	void testExternalDtoLongIdSerialization() throws Exception {
		// given
		StoreResDto dto = StoreResDto.builder()
			.id(1L)
			.name("Comeet Store")
			.latitude(BigDecimal.valueOf(37.5))
			.longitude(BigDecimal.valueOf(127.0))
			.distance(12.5)
			.build();

		// when
		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(dto));

		// then
		assertThat(json.get("id").isTextual()).isTrue();
		assertThat(json.get("distance").isNumber()).isTrue();
		assertThat(idObfuscator.decode(json.get("id").asText())).isEqualTo(1L);
	}

	@Test
	@DisplayName("외부 요청 DTO의 Long ID 필드는 Sqids 문자열에서 자동으로 Long으로 역직렬화한다")
	void testExternalDtoLongIdDeserialization() throws Exception {
		// given
		String visitId = idObfuscator.encode(11L);
		String menuId = idObfuscator.encode(12L);
		String storeId = idObfuscator.encode(13L);
		String flavorId = idObfuscator.encode(14L);
		String json = """
			{
			  "visitId": "%s",
			  "menuId": "%s",
			  "storeId": "%s",
			  "content": "좋아요",
			  "isPublic": true,
			  "rating": 4.5,
			  "flavorIdList": ["%s"]
			}
			""".formatted(visitId, menuId, storeId, flavorId);

		// when
		ReviewReqDto dto = objectMapper.readValue(json, ReviewReqDto.class);

		// then
		assertThat(dto.visitId()).isEqualTo(11L);
		assertThat(dto.menuId()).isEqualTo(12L);
		assertThat(dto.storeId()).isEqualTo(13L);
		assertThat(dto.flavorIdList()).containsExactly(14L);
	}

	@Test
	@DisplayName("외부 요청 DTO의 raw 숫자 ID는 허용하지 않는다")
	void testRawNumericIdDeserializationRejected() {
		// given
		String json = """
			{
			  "visitId": 11,
			  "menuId": "%s",
			  "storeId": "%s",
			  "isPublic": true
			}
			""".formatted(idObfuscator.encode(12L), idObfuscator.encode(13L));

		// then
		assertThatThrownBy(() -> objectMapper.readValue(json, ReviewReqDto.class))
			.hasRootCauseInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("MVC Long 경로 파라미터는 Sqids 문자열만 Long으로 변환한다")
	void testMvcLongConverter() {
		// given
		String encoded = idObfuscator.encode(99L);
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		new SqidWebMvcConfig(idObfuscator).addFormatters(conversionService);

		// then
		assertThat(conversionService.convert(encoded, Long.class)).isEqualTo(99L);
		assertThatThrownBy(() -> conversionService.convert("99", Long.class))
			.isInstanceOf(RuntimeException.class);
	}

	private record TestDto(Sqid id, String name) {}
}
