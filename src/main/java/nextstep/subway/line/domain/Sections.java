package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

import nextstep.subway.station.domain.Station;

@Embeddable
public class Sections {

	@OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Section> sections = new ArrayList<>();

	protected Sections() {
	}

	public Sections(Section... sections) {
		this.sections.addAll(Arrays.asList(sections));
	}

	public List<Section> getSections() {
		return sections;
	}

	public void addSection(final Section section) {
		validate(section);
		findByUpStation(section.getUp())
			.ifPresent(existsSection ->
				existsSection.update(section.getDown(), existsSection.getDown(),
					existsSection.minusDistance(section.getDistance())));

		findByDownStation(section.getDown())
			.ifPresent(existsSection ->
				existsSection.update(existsSection.getUp(), section.getUp(),
					existsSection.minusDistance(section.getDistance())));

		this.sections.add(section);
	}

	private void validate(final Section section) {
		List<Station> stations = this.getStations();

		if (stations.isEmpty()) {
			return;
		}

		if (isUpAndDownExists(stations, section)) {
			throw new IllegalArgumentException("상행역과 하행역이 이미 존재합니다.");
		}

		if (isUpAndDownNotExists(stations, section)) {
			throw new IllegalArgumentException("상행역과 하행역이 모두 노선에 포함되어 있지 않습니다.");
		}
	}

	private boolean isUpAndDownExists(final List<Station> stations, final Section section) {
		return stations.contains(section.getUp()) && stations.contains(section.getDown());
	}

	private boolean isUpAndDownNotExists(final List<Station> stations, final Section section) {
		return !stations.contains(section.getUp()) && !stations.contains(section.getDown());
	}

	public Optional<Section> findByUpStation(final Station up) {
		return this.sections.stream()
			.filter(section -> section.equalsUpStation(up))
			.findFirst();
	}

	public Optional<Section> findByDownStation(final Station down) {
		return this.sections.stream()
			.filter(section -> section.equalsDownStation(down))
			.findFirst();
	}

	public List<Station> getStations() {
		return this.sections.stream()
			.flatMap(section -> section.getStations().stream())
			.distinct()
			.collect(Collectors.toList());
	}

	public void removeStation(final Station station) {
		Section toRemove = findByUpStation(station)
			.orElseGet(() -> findByDownStation(station)
				.orElseThrow(() -> new IllegalArgumentException("삭제 대상 역이 포함된 구간이 없습니다.")));

		validateRemove(toRemove);

		if (!isFinalStation(station)) {
			findByDownStation(station)
				.ifPresent(existsSection ->
					existsSection.update(existsSection.getUp(), toRemove.getDown(),
						existsSection.plusDistance(toRemove.getDistance())));
		}

		this.sections.remove(toRemove);
	}

	private boolean isFinalStation(final Station station) {
		List<Station> finalStations = findFinalStations();
		return finalStations.contains(station);
	}

	private List<Station> findFinalStations() {
		return this.sections.stream()
			.flatMap(section -> section.getStations().stream())
			.collect(Collectors.groupingBy(Station::getId))
			.values()
			.stream()
			.filter(stations -> stations.size() == 1)
			.map(stations -> stations.get(0))
			.collect(Collectors.toList());

	}

	private void validateRemove(final Section toRemove) {
		if (isSizeOne() && isLastSection(toRemove)) {
			throw new IllegalArgumentException("구간이 하나인 노선의 마지막 구간은 삭제할 수 없습니다");
		}
	}

	private boolean isSizeOne() {
		return this.sections.size() == 1;
	}

	private boolean isLastSection(final Section section) {
		return this.sections.get(this.sections.size() - 1).equals(section);
	}
}