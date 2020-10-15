package com.yarden.restServiceDemo;

public class Enums {

    public enum Strings{
        Generic("(generic)");

        public final String value;

        Strings(String value){
            this.value = value;
        }
    }

    public enum TestResults{
        Passed("1"), Failed("-1"), Missing("");

        public final String value;

        TestResults(String value){
            this.value = value;
        }
    }

    public enum MandatoryTest{
        Mandatory("1");

        public final String value;

        MandatoryTest(String value){
            this.value = value;
        }
    }

    public enum SdkGroupsSheetTabNames {
        Selenium("Selenium"), Images("Images"), Appium("Appium"), Core("Core"), MobileNative("MobileNative");

        public final String value;

        SdkGroupsSheetTabNames(String value){
            this.value = value;
        }
    }

    public enum SdkGeneralSheetTabsNames {
        Sandbox("sandbox");

        public final String value;

        SdkGeneralSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum SdkSheetColumnNames {
        TestName("Test name"), IDRow("id"), ExtraData("_extra");

        public final String value;

        SdkSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum SdkVersionsSheetTabsNames {
        Versions("Versions");

        public final String value;

        SdkVersionsSheetTabsNames(String value) {this.value = value;}
    }

    public enum SdkVersionsSheetColumnNames {
        Sdk("sdk"), Version("latest version");

        public final String value;

        SdkVersionsSheetColumnNames(String value) {this.value = value;}
    }

    public enum EyesSheetTabsNames {
        FrontEnd("Front end"), IntegrationsTests("Integration tests"), FunctionalTests("Functional tests"), BackendTests("Backend tests"), Sandbox("sandbox");

        public final String value;

        EyesSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum EyesSheetColumnNames {
        TestName("Test name"), Feature("Feature"), FeatureSubCategory("Feature sub-category"), Status("Status"), Url("Url"), IDRow("id"), TimestampRow("timestamp");

        public final String value;

        EyesSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum VisualGridSheetTabsNames {
        Status("Status");

        public final String value;

        VisualGridSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum VisualGridSheetColumnNames {
        Timestamp("Timestamp");

        public final String value;

        VisualGridSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum KPIsSheetTabsNames {
        RawData("Raw data"), KPIs("KPIs"), EventLog("Event log");

        public final String value;

        KPIsSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum KPIsSheetColumnNames {
        Team("Team"), SubProject("Sub project"), TicketID("Ticket ID"), TicketTitle("Ticket title"), TicketUrl("Ticket url"),
        TicketType("Ticket type"), CreationDate("Creation date"), CreatedBy("Created by"), CurrentState("Current state"),
        TimeUntilLeftNewForTheFirstTime("Time until left New for the first time"), EnterForTimeCalculationState("Enter for time calculation state "),
        LeftForTimeCalculationState("Left for time calculation state "), CalculatedTimeInState("Calculated time in state "), Labels("Labels"),
        MovedToStateDone("Moved to state Done"), Timestamp("Timestamp"), CurrentTrelloList("Current trello list"), Workaround("Workaround");

        public final String value;

        KPIsSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum KPIsTicketTypes {
        Bug("Bug"), NotABug("Not a bug");

        public final String value;

        KPIsTicketTypes(String value){
            this.value = value;
        }
    }

    public enum EnvVariables {
        MailjetApiKeyPublic(System.getenv("MJ_APIKEY_PUBLIC")), MailjetApiKeyPrivate(System.getenv("MJ_APIKEY_PRIVATE")),
        AwsS3SdkReportsBucketName(System.getenv("SDK_REPORTS_S3_BUCKET")), AwsS3EyesReportsBucketName(System.getenv("EYE_REPORTS_S3_BUCKET")),
        MailReportRecipient(System.getenv("MAIL_REPORT_RECIPIENT")), SlackSdkReleaseChannelEndpoint(System.getenv("SDK_RELEASE_SLACK_CHANNEL_ENDPOINT")),
        ApiToken(System.getenv("API_TOKEN"));

        public final String value;

        EnvVariables(String value){
            this.value = value;
        }
    }

    public enum SpreadsheetIDs {
        SDK("1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY"), SdkVersions("1TOw6XqUAdKRQL6QFkpcF_mMUGfX7CmxbUsXIbolSABM"),
        Eyes("1kCOwx8AP6Fg0ltivnw1o55IA3ZkV3hROB1dZ61FRQh8"), VisualGrid("1umqCfSK3UICmw_ycbyhLjxdcyksBHYxwFNSqAXAN4SQ"),
        KPIS("1UgeW2OxvsGyNr9oyAgyDmw_9TR3r8dC6qbigKahNLPM");

        public final String value;

        SpreadsheetIDs(String value){
            this.value = value;
        }
    }

}
