package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryReportResponseRepository implements ReportResponseRepository {

    private final Map<UUID, ReportResponse> responses = new HashMap<>();

    @Override
    public ReportResponse save(ReportResponse response) {
        responses.put(response.getId(), response);
        return response;
    }

    @Override
    public Optional<ReportResponse> findById(UUID id) {
        return Optional.ofNullable(responses.get(id));
    }

    @Override
    public List<ReportResponse> findByReportId(UUID reportId) {
        return responses.values().stream()
                .filter(response -> response.getReportId().equals(reportId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        responses.remove(id);
    }

    @Override
    public void deleteByReportId(UUID reportId) {
        responses.entrySet().removeIf(entry -> entry.getValue().getReportId().equals(reportId));
    }
}
