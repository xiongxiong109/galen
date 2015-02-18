package net.mindengine.galen.reports.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.mindengine.galen.reports.GalenTestAggregatedInfo;
import net.mindengine.galen.reports.GalenTestInfo;
import net.mindengine.galen.reports.TestIdGenerator;
import net.mindengine.galen.reports.TestReport;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ishubin on 2015/02/15.
 */
public class JsonReportBuilder {

    private ObjectMapper jsonMapper = createJsonMapper();


    private ObjectMapper createJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private TestIdGenerator testIdGenerator = new TestIdGenerator();


    public void build(List<GalenTestInfo> testInfos, String reportPath) throws IOException {
        ReportOverview reportOverview = createReportOverview(testInfos);

        for (GalenTestAggregatedInfo aggregatedInfo : reportOverview.getTests()) {
            exportTestReportToJson(new JsonTestReport(aggregatedInfo.getTestId(), aggregatedInfo.getTestInfo()), reportPath);
            moveAllReportFiles(aggregatedInfo.getTestInfo().getReport(), reportPath);
        }

        exportReportOverviewToJson(reportOverview, reportPath);
    }


    private void moveAllReportFiles(TestReport report, String reportPath) throws IOException {
        if (report != null && report.getFileStorage() != null) {
            report.getFileStorage().copyAllFilesTo(new File(reportPath));
        }
    }

    public ReportOverview createReportOverview(List<GalenTestInfo> testInfos) {
        ReportOverview reportOverview = new ReportOverview();
        for (GalenTestInfo testInfo : testInfos) {
            String testId = testIdGenerator.generateTestId(testInfo.getName());
            reportOverview.add(new GalenTestAggregatedInfo(testId, testInfo));
        }

        return reportOverview;
    }

    public String exportReportOverviewToJsonAsString(ReportOverview reportOverview) throws JsonProcessingException {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportOverview);
    }

    private void exportReportOverviewToJson(ReportOverview reportOverview, String reportPath) throws IOException {
        makeSureFolderExists(reportPath);
        File file = new File(reportPath + File.separator + "report.json");
        file.createNewFile();
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, reportOverview);
    }

    private void exportTestReportToJson(JsonTestReport aggregatedInfo, String reportPath) throws IOException {
        makeSureFolderExists(reportPath);

        File file = new File(reportPath + File.separator + aggregatedInfo.getTestId() + ".json");
        file.createNewFile();

        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, aggregatedInfo);
    }

    private void makeSureFolderExists(String reportPath) throws IOException {
        FileUtils.forceMkdir(new File(reportPath));
    }

    public String exportTestReportToJsonString(GalenTestAggregatedInfo info) throws JsonProcessingException {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new JsonTestReport(info.getTestId(), info.getTestInfo()));
    }
}
