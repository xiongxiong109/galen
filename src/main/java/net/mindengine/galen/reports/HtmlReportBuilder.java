/*******************************************************************************
 * Copyright 2015 Ivan Shubin http://mindengine.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mindengine.galen.reports;

import net.mindengine.galen.reports.json.JsonReportBuilder;
import net.mindengine.galen.reports.json.ReportOverview;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;

public class HtmlReportBuilder {

    public void build(List<GalenTestInfo> tests, String reportFolderPath) throws IOException {
        makeSureReportFolderExists(reportFolderPath);

        JsonReportBuilder jsonBuilder = new JsonReportBuilder();
        ReportOverview reportOverview = jsonBuilder.createReportOverview(tests);

        String overviewTemplate = IOUtils.toString(getClass().getResourceAsStream("/html-report/report.tpl.html"));
        String testReportTemplate = IOUtils.toString(getClass().getResourceAsStream("/html-report/report-test.tpl.html"));

        for (GalenTestAggregatedInfo aggregatedInfo : reportOverview.getTests()) {
            String testReportJson = jsonBuilder.exportTestReportToJsonString(aggregatedInfo);
            FileUtils.writeStringToFile(new File(reportFolderPath + File.separator + aggregatedInfo.getTestId() + ".html"),
                    testReportTemplate.replace("##REPORT-DATA##", testReportJson));
        }

        String overviewJson = jsonBuilder.exportReportOverviewToJsonAsString(reportOverview);

        FileUtils.writeStringToFile(new File(reportFolderPath + File.separator + "report.html"),
                overviewTemplate.replace("##REPORT-DATA##", overviewJson));

        copyHtmlResources(reportFolderPath);
    }

    private void makeSureReportFolderExists(String reportFolderPath) throws IOException {
        FileUtils.forceMkdir(new File(reportFolderPath));
    }

    private void copyHtmlResources(String reportFolderPath) throws IOException {
        // copy sorting libs
        copyResourceToFolder("/html-report/tablesorter.css", reportFolderPath + File.separator + "tablesorter.css");
        copyResourceToFolder("/html-report/tablesorter.js", reportFolderPath + File.separator + "tablesorter.js");
        // copy galen libs
        copyResourceToFolder("/html-report/galen-report.css", reportFolderPath + File.separator + "galen-report.css");
        copyResourceToFolder("/html-report/galen-report.js", reportFolderPath + File.separator + "galen-report.js");
        copyResourceToFolder("/html-report/jquery-1.10.2.min.js", reportFolderPath + File.separator + "jquery-1.10.2.min.js");
    }

    private void copyResourceToFolder(String resourcePath, String destFileName) throws IOException {
        File destFile = new File(destFileName);

        if (!destFile.exists()) {
            if (!destFile.createNewFile()) {
                throw new RuntimeException("Cannot copy file to: " + destFile.getAbsolutePath());
            }
        }
        IOUtils.copy(getClass().getResourceAsStream(resourcePath), new FileOutputStream(destFile));
    }

}
