package nextstep.subway.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.station.TestStationFactory.역_생성;
import static nextstep.subway.utils.TestRequestFactory.요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {
    String DEFAULT_PATH = "/lines";

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green");

        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(new LineRequest("2호선", "green", 1L, 2L, 10));

        // then
        지하철_노선_생성_실패됨(response);
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        지하철_노선_종점역_추가하여_생성_요청("구로역", "신도림역", "1호선", "blue");
        지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green");

        // when
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        // then
        지하철_노선_목록_응답됨(Arrays.asList("1호선", "2호선"), response);
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        LineResponse savedLineResponse = 지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green")
                .body().as(LineResponse.class);

        // when
        // 지하철_노선_수정_요청
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(savedLineResponse.getName(), "red", savedLineResponse.getId());

        // then
        // 지하철_노선_수정됨
        지하철_노선_수정됨(savedLineResponse, response);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        LineResponse savedLineResponse = 지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green")
                .body().as(LineResponse.class);

        // when
        ExtractableResponse<Response> response = 지하철_노선_제거_요청(savedLineResponse.getId());

        // then
        // 지하철_노선_삭제됨
        지하철_노선_삭제됨(response);
    }

    @DisplayName("노선 생성시 두 종점역 추가하기")
    @Test
    void createLineWithStations() {
        // given, when
        ExtractableResponse<Response> saveLineResponse = 지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green");

        // then
        지하철_노선_생성_응답됨(saveLineResponse);
    }

    @DisplayName("노선 조회, 역 목록 추가")
    @Test
    void findLineWithStations() {
        // given
        LineResponse saveLineResponse = 지하철_노선_종점역_추가하여_생성_요청("강남역", "역삼역", "2호선", "green")
                .body().as(LineResponse.class);

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(saveLineResponse.getId());

        // then
        지하철_노선_역_목록_응답됨(Arrays.asList(saveLineResponse.getStations().get(0).getId(), saveLineResponse.getStations().get(1).getId()), response);
    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청(LineRequest lineRequest) {
        return 요청(HttpMethod.POST,DEFAULT_PATH, lineRequest);
    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
        return 요청(HttpMethod.GET, DEFAULT_PATH, null);
    }

    private ExtractableResponse<Response> 지하철_노선_조회_요청(Long id) {
        return 요청(HttpMethod.GET, DEFAULT_PATH + "/" + id, null);
    }

    private ExtractableResponse<Response> 지하철_노선_수정_요청(String name, String color, Long id) {
        LineRequest updateLineRequest = new LineRequest(name, color);

        return 요청(HttpMethod.PUT, "/lines/" + id, updateLineRequest);
    }

    private ExtractableResponse<Response> 지하철_노선_제거_요청(Long id) {
        return 요청(HttpMethod.DELETE, "/lines/" + id, null);
    }

    private ExtractableResponse<Response> 지하철_노선_종점역_추가하여_생성_요청(String upStationName, String downStationName, String name, String color) {
        StationResponse upStation = 역_생성(upStationName);
        StationResponse downStation = 역_생성(downStationName);

        LineRequest lineRequestWithStations = new LineRequest(name, color, upStation.getId(), downStation.getId(), 10);
        return 지하철_노선_생성_요청(lineRequestWithStations);
    }

    private void 지하철_노선_생성_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_노선_생성_실패됨(ExtractableResponse<Response> response) {
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(response.body().asString()).isEqualTo("노선 이름이 이미 존재합니다.[2호선]")
        );
    }

    private void 지하철_노선_목록_응답됨(List<String> lineRequestNames, ExtractableResponse<Response> response) {
        List<String> lineResponseNames = response.jsonPath().getList(".", LineResponse.class)
                .stream()
                .map(lineResponse -> lineResponse.getName())
                .collect(Collectors.toList());

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(lineResponseNames).containsAll(lineRequestNames)
        );
    }

    private void 지하철_노선_수정됨(LineResponse savedLineResponse, ExtractableResponse<Response> response) {
        LineResponse updatedLineResponse = response.body().as(LineResponse.class);
        assertAll(
                () -> assertThat(updatedLineResponse.getId()).isEqualTo(savedLineResponse.getId()),
                () -> assertThat(updatedLineResponse.getColor()).isEqualTo("red")
        );
    }

    private void 지하철_노선_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 지하철_노선_역_목록_응답됨(List<Long> stationIds, ExtractableResponse<Response> response) {
        List<Long> lineResponseStationIds = response.body()
                .as(LineResponse.class)
                .getStations()
                .stream()
                .map(stationResponse -> stationResponse.getId())
                .collect(Collectors.toList());

        assertThat(lineResponseStationIds).containsAll(stationIds);
    }
}