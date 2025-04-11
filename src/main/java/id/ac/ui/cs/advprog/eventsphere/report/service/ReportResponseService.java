package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.UserRole;

import java.util.List;
import java.util.UUID;

public interface ReportResponseService {
    ReportResponse addResponse(UUID reportId, UUID responderId, UserRole responderRole, String content);
    List<ReportResponse> getResponsesByReportId(UUID reportId);
}
