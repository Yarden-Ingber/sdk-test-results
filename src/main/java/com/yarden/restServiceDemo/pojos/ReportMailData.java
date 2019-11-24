package com.yarden.restServiceDemo.pojos;

import com.yarden.restServiceDemo.HTMLTableBuilder;

public class ReportMailData {

    private String mailTextPart;
    HTMLTableBuilder detailedMissingTestsTable = null;
    HTMLTableBuilder highLevelReportTable = null;
    HTMLTableBuilder detailedPassedTestsTable = null;
    private String reportTitle = "";
    private String changeLog = "";
    private String version = "";
    private String coverageGap = "";
    private boolean shouldAddTestResults = false;
    private boolean shouldAddCoverageGap = false;

    public String getReportTitle() {
        return reportTitle;
    }

    public ReportMailData setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
        return this;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public ReportMailData setChangeLog(String changeLog) {
        this.changeLog = changeLog;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ReportMailData setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCoverageGap() {
        return coverageGap;
    }

    public ReportMailData setCoverageGap(String coverageGap) {
        this.coverageGap = coverageGap;
        return this;
    }

    public Boolean isShouldAddTestResults() {
        return shouldAddTestResults;
    }

    public ReportMailData setShouldAddTestResults(Boolean shouldAddTestResults) {
        this.shouldAddTestResults = shouldAddTestResults;
        return this;
    }

    public boolean isShouldAddCoverageGap() {
        return shouldAddCoverageGap;
    }

    public ReportMailData setShouldAddCoverageGap(boolean shouldAddCoverageGap) {
        this.shouldAddCoverageGap = shouldAddCoverageGap;
        return this;
    }

    public String getMailTextPart() {
        return mailTextPart;
    }

    public ReportMailData setMailTextPart(String mailTextPart) {
        this.mailTextPart = mailTextPart;
        return this;
    }

    public HTMLTableBuilder getDetailedMissingTestsTable() {
        return detailedMissingTestsTable;
    }

    public ReportMailData setDetailedMissingTestsTable(HTMLTableBuilder detailedMissingTestsTable) {
        this.detailedMissingTestsTable = detailedMissingTestsTable;
        return this;
    }

    public HTMLTableBuilder getHighLevelReportTable() {
        return highLevelReportTable;
    }

    public ReportMailData setHighLevelReportTable(HTMLTableBuilder highLevelReportTable) {
        this.highLevelReportTable = highLevelReportTable;
        return this;
    }

    public HTMLTableBuilder getDetailedPassedTestsTable() {
        return detailedPassedTestsTable;
    }

    public ReportMailData setDetailedPassedTestsTable(HTMLTableBuilder detailedPassedTestsTable) {
        this.detailedPassedTestsTable = detailedPassedTestsTable;
        return this;
    }
}
