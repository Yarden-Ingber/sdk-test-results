package com.yarden.restServiceDemo;

public class Enums {

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
        Selenium("Selenium"), Images("Images"), Appium("Appium"), Core("Core");

        public final String value;

        SdkGroupsSheetTabNames(String value){
            this.value = value;
        }
    }

    public enum SdkGeneralSheetTabsNames {
        Sandbox("sandbox"), RawData("Raw data");

        public final String value;

        SdkGeneralSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum SdkSheetColumnNames {
        TestName("Test name"), Mandatory("mandatory"), IDRow("id"), Fail("_fail"), Pass("_pass"), ExtraData("_extra");

        public final String value;

        SdkSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum EyesSheetTabsNames {
        VisualTests("Visual tests"), Sandbox("sandbox");

        public final String value;

        EyesSheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum EyesSheetColumnNames {
        TestName("Test name"), Status("Status"), Url("Url"), IDRow("id"), TimestampRow("timestamp");

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

    public enum EnvVariables {
        MailjetApiKeyPublic(System.getenv("MJ_APIKEY_PUBLIC")), MailjetApiKeyPrivate(System.getenv("MJ_APIKEY_PRIVATE")),
        AwsS3SdkReportsBucketName(System.getenv("SDK_REPORTS_S3_BUCKET")), MailReportRecipient(System.getenv("MAIL_REPORT_RECIPIENT")),
        SlackSdkReleaseChannelEndpoint(System.getenv("SDK_RELEASE_SLACK_CHANNEL_ENDPOINT"));

        public final String value;

        EnvVariables(String value){
            this.value = value;
        }

    }

    public enum SpreadsheetIDs {
        SDK("1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY"), Eyes("1kCOwx8AP6Fg0ltivnw1o55IA3ZkV3hROB1dZ61FRQh8"),
        VisualGrid("1umqCfSK3UICmw_ycbyhLjxdcyksBHYxwFNSqAXAN4SQ");

        public final String value;

        SpreadsheetIDs(String value){
            this.value = value;
        }
    }

}
