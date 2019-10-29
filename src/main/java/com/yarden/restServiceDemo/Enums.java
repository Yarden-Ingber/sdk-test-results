package com.yarden.restServiceDemo;

public class Enums {

    public enum TestResults{
        Passed("1"), Failed("-1");

        String value;

        TestResults(String value){
            this.value = value;
        }
    }

    public enum SheetTabsNames {
        Report("Coverage comparing"), HighLevel("Results history"), Sandbox("sandbox");

        String value;

        SheetTabsNames(String value){
            this.value = value;
        }
    }

    public enum SheetColumnNames {
        TestName("Test name"), IDRow("id"), Fail("_fail"), Pass("_pass"), ExtraData("_extra");

        String value;

        SheetColumnNames(String value){
            this.value = value;
        }
    }

    public enum HighLevelSheetColumnNames {
        Sdk("sdk"), StartTimestamp("start timestamp"), LastUpdate("last update"), ID("id"), SuccessPercentage("success percentage"), AmountOfTests("amount of tests");

        String value;

        HighLevelSheetColumnNames(String value){
            this.value = value;
        }
    }

}
