package com.yarden.restServiceDemo;

public class Enums {

    public enum TestResults{
        Passed("1"), Failed("-1");

        public final String value;

        TestResults(String value){
            this.value = value;
        }
    }

    public enum SheetTabsNames {
        Report("Coverage comparison"), HighLevel("Results history"), Sandbox("sandbox"), RawData("Raw data");

        public final String value;

        SheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum SheetColumnNames {
        TestName("Test name"), IDRow("id"), Fail("_fail"), Pass("_pass"), ExtraData("_extra");

        public final String value;

        SheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum HighLevelSheetColumnNames {
        Sdk("sdk"), StartTimestamp("start timestamp"), LastUpdate("last update"), ID("id"), SuccessPercentage("success percentage"), AmountOfTests("amount of tests");

        public final String value;

        HighLevelSheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum EnvVariables {
        MailjetApiKeyPublic(System.getenv("MJ_APIKEY_PUBLIC")), MailjetApiKeyPrivate(System.getenv("MJ_APIKEY_PRIVATE")),
        AwsS3SdkReportsBucketName(System.getenv("SDK_REPORTS_S3_BUCKET")), MailReportRecipient(System.getenv("MAIL_REPORT_RECIPIENT"));

        public final String value;

        EnvVariables(String value){
            this.value = value;
        }

    }

}
