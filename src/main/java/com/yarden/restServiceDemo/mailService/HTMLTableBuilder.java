package com.yarden.restServiceDemo.mailService;

public class HTMLTableBuilder {

    private int columns;
    private final StringBuilder table = new StringBuilder();
    public static String TABLE_START_BORDER = "<table border=\"1\">";
    public static String TABLE_START = "<table>";
    public static String TABLE_END = "</table>";
    public static String HEADER_START = "<th>";
    public static String HEADER_END = "</th>";
    public static String ROW_START = "<tr>";
    public static String ROW_END = "</tr>";
    public static String COLUMN_START = "<td>";
    public static String COLUMN_END = "</td>";


    /**
     * @param border
     * @param rows
     * @param columns
     */
    public HTMLTableBuilder(boolean border, int rows, int columns) {
        this.columns = columns;
        table.append(border ? TABLE_START_BORDER : TABLE_START);
        table.append(TABLE_END);
    }


    /**
     * @param values
     */
    public void addTableHeader(String... values) {
        if (values.length != columns) {
            System.out.println("Error column lenth");
        } else {
            int lastIndex = table.lastIndexOf(TABLE_END);
            if (lastIndex > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(ROW_START);
                for (String value : values) {
                    sb.append(HEADER_START);
                    sb.append(value);
                    sb.append(HEADER_END);
                }
                sb.append(ROW_END);
                table.insert(lastIndex, sb.toString());
            }
        }
    }


    /**
     * @param values
     */
    public void addRowValues(boolean isCenter, String... values) {
        if (values.length != columns) {
            System.out.println("Error column lenth");
        } else {
            int lastIndex = table.lastIndexOf(ROW_END);
            if (lastIndex > 0) {
                int index = lastIndex + ROW_END.length();
                StringBuilder sb = new StringBuilder();
                sb.append(ROW_START);
                for (String value : values) {
                    if (value.toLowerCase().equals("pass")) {
                        sb.append(COLUMN_START.replace("td", "td align=\"center\" class=\"pass\""));
                    } else if (value.toLowerCase().equals("fail")) {
                        sb.append(COLUMN_START.replace("td", "td align=\"center\" class=\"fail\""));
                    } else if (isCenter){
                        sb.append(COLUMN_START.replace("td", "td align=\"center\""));
                    } else {
                        sb.append(COLUMN_START);
                    }
                    sb.append(value);
                    sb.append(COLUMN_END);
                }
                sb.append(ROW_END);
                table.insert(index, sb.toString());
            }
        }
    }

    @Override
    public String toString() {
        return table.toString();
    }

}
