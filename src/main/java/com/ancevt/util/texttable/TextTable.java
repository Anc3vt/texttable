/**
 * Copyright (C) 2022 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ancevt.util.texttable;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TextTable {

    private static final int DEFAULT_MAX_STRING_LENGTH = 100;
    public static final int PADDING_COLUMN_NAMES = 2;
    public static final int PADDING_COLUMN = 3;


    @Getter
    @Setter
    private int maxStringLength;

    @Getter
    @Setter
    private String[] columnNames;

    private final List<TextTableRow> rows;

    private int[] columnSizes;

    private boolean decorEnabled;

    public TextTable() {
        rows = new ArrayList<>();
        setMaxStringLength(DEFAULT_MAX_STRING_LENGTH);
        decorEnabled = true;
    }

    public TextTable(boolean decorEnabled, String... columnNames) {
        this(columnNames);
        this.decorEnabled = decorEnabled;
    }

    public TextTable(boolean decorEnabled) {
        this();
        this.decorEnabled = decorEnabled;
    }

    public TextTable(String... columnNames) {
        this();
        setColumnNames(columnNames);
    }

    public void setDecorEnabled(boolean decorEnabled) {
        this.decorEnabled = decorEnabled;
    }

    public boolean isDecorEnabled() {
        return decorEnabled;
    }

    public void addRow(Object... cells) {
        final TextTableRow row = new TextTableRow();
        row.setData(cells);
        rows.add(row);
    }

    public void addKeyedRow(Object key, Object[] cells) {
        final TextTableRow row = new TextTableRow();
        row.setData(cells);
        row.setKey(key);
        rows.add(row);
    }

    public int size() {
        return rows.size();
    }

    public Object[] getRow(Object key) {
        for (final TextTableRow row : rows)
            if (row.getKey() == key || key.equals(row.getKey()))
                return row.getData();

        return new Object[0];
    }

    public Object[] getRow(int index) {
        return rows.get(index).getData();
    }

    public void removeRow(Object key) {
        for (final TextTableRow row : rows) {
            if (row.getKey() == key || key.equals(row.getKey())) {
                rows.remove(row);
                break;
            }
        }
    }

    public boolean hasRow(Object key) {
        for (final TextTableRow row : rows)
            if (row.getKey() == key || key.equals(row.getKey()))
                return true;

        return false;
    }

    public String render() {
        final StringBuilder sb = new StringBuilder();

        detectColumnSizes();

        int terminalWidth = getTerminalWidth();

        int[] copyOfColumnSizes = null;
        if (terminalWidth != 0) {
            while (terminalWidth < getTextTableWidth()) {
                copyOfColumnSizes = Arrays.copyOf(columnSizes, columnSizes.length);

                int longestIndex = getLongestColumnIndex();
                columnSizes[longestIndex]--;
            }
        }

        sb.append(columnNames != null ? renderHeader() : renderDecor());

        if (isDecorEnabled()) sb.append('\n');

        for (int i = 0; i < size(); i++) {
            sb.append(renderLine(getRow(i)));
            sb.append('\n');
        }

        sb.append(renderDecor());

        if (copyOfColumnSizes != null) {
            columnSizes = copyOfColumnSizes;
        }

        return sb.toString();
    }

    private String renderDecor() {
        if (!decorEnabled) return "";

        final StringBuilder sb = new StringBuilder();

        sb.append('+');
        for (int size : columnSizes) {
            for (int j = 0; j < size + 1; j++) {
                sb.append('-');
            }
            sb.append('+');
        }

        return sb.toString();
    }

    private String renderHeader() {
        return renderDecor() +
            (isDecorEnabled() ? '\n' : "") +
            renderLine(columnNames) +
            '\n' +
            renderDecor();
    }

    private String renderLine(Object[] cells) {
        final StringBuilder sb = new StringBuilder();

        if (isDecorEnabled()) sb.append('|');

        for (int i = 0; i < cells.length; i++) {
            if (i >= columnSizes.length) break;

            String n = String.valueOf(cells[i]);
            if (n.length() > maxStringLength || n.length() > columnSizes[i]) {
                int min = Math.min(maxStringLength, columnSizes[i]);

                min = Math.max(0, min - 2);

                n = n.substring(0, min) + "..";
            }

            if (isDecorEnabled()) sb.append(' ');
            sb.append(n);
            final int spacesLeftToRender = getSpacesLeft(n, columnSizes[i]);
            sb.append(spaces(spacesLeftToRender));

            if (isDecorEnabled()) sb.append('|');
        }

        if (cells.length < columnSizes.length) {
            final int difference = columnSizes.length - cells.length;

            for (int i = 0; i < difference; i++) {
                sb.append(spaces(columnSizes[i + cells.length] + 1));
                sb.append('|');
            }
        }

        return sb.toString();
    }

    private static int getSpacesLeft(String string, int size) {
        return size - string.length();
    }

    private static String spaces(int count) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb.toString();
    }

    private void detectColumnSizes() {
        int columnCount = 0;
        if (columnNames != null) {
            columnCount = columnNames.length;
        } else {
            int max = 0;
            for (final TextTableRow row : rows) {
                final Object[] cells = row.getData();
                if (cells.length > max) max = cells.length;
            }
            columnCount = max;
        }

        this.columnSizes = new int[columnCount];

        if (columnNames != null) {
            for (String columnName : columnNames) {
                for (int j = 0; j < columnName.length(); j++) {
                    if (j >= columnSizes.length) break;

                    String string = columnNames[j];

                    if (string.length() > maxStringLength) {
                        string = string.substring(0, maxStringLength);
                    }


                    final int stringLength = string.length();

                    if (stringLength > columnSizes[j])
                        columnSizes[j] = stringLength + PADDING_COLUMN_NAMES;
                }
            }
        }

        for (int i = 0; i < size(); i++) {
            final Object[] rowData = getRow(i);

            for (int j = 0; j < rowData.length; j++) {
                if (j >= columnSizes.length) break;

                String string = String.valueOf(rowData[j]);
                if (string.length() > maxStringLength) {
                    string = string.substring(0, maxStringLength);
                }

                final int stringLength = string.length();

                if (stringLength > columnSizes[j])
                    columnSizes[j] = stringLength + PADDING_COLUMN;
            }
        }
    }

    private int getTextTableWidth() {
        if (decorEnabled) {
            return Arrays.stream(columnSizes).sum() + 7;
        } else {
            return Arrays.stream(columnSizes).sum();
        }
    }

    private int getLongestColumnIndex() {
        int max = 0;
        int maxIndex = 0;
        for (int index = 0; index < columnSizes.length; index++) {
            if (columnSizes[index] > max) {
                max = columnSizes[index];
                maxIndex = index;
            }
        }
        return maxIndex;
    }

    private static int getTerminalWidth() {
        try {

            int result = 0;

            if(System.getProperty("_terminal_witdh") != null) {
                result = Integer.parseInt(System.getProperty("_terminal_witdh"));
            } else {
                result = org.jline.terminal.TerminalBuilder.terminal().getWidth();
            }

            if(result == 0) return result;

            return Math.max(result, 14);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
/*
		+----+--------------------+------------+------+------+
		| id | name               | country_id | code | desc |
		+----+--------------------+------------+------+------+
		|  1 | Moscow             | 1          | MOS  | NULL |
		|  2 | New-York           | 3          | NY   | NULL |
		|  3 | Kiev               | 2          | KI   | NULL |
		|  6 | Washington         | 3          | WS   | NULL |
		|  7 | Samara             | 1          | SM   | NULL |
		+----+--------------------+------------+------+------+

		ssa_role_name  Прикрепление сканированных подлинников документов
        r_object_id    004a000000057e9a
        ssa_name       Ответственный за сканирование ОРД
        r_object_type  nt_doc_role
        i_vstamp       2
        ssa_filial_nameДемонстрационная организация
        sida_filial    000b000000007536
        ssa_role       scanning

*/

        final TextTable textTable = new TextTable("id", "name", "country_id", "code", "desc");

        textTable.addRow(1, "Moscow", 1, "MOS", "NULL");
        textTable.addRow(2, "New-York", 3, "NY", "NULL");
        textTable.addRow(3, "Kiev", 2, "KI", "NULL");
        textTable.addRow(6, "ssa_filial_name", 4, "WS", "NULL");
        textTable.addRow(7, "Samara", 1, "SM", "NULL");

        System.out.println(textTable.render());

        System.out.println();

        final TextTable textTable2 = new TextTable("id", "name", "country_id", "code", "desc");
        textTable2.setDecorEnabled(false);

        textTable2.addRow(1, "Moscow0", 1, "MOS", "NULL");
        textTable2.addRow(2, "New-York", 3, "NY", "NULL");
        textTable2.addRow(3, "Kiev", 2, "KI", "NULL");
        textTable2.addRow(6, "ssa_filial_name", 4, "WS", "NULL");
        textTable2.addRow(7, "Samara0", 1, "SM", "NULL");

        System.out.println(textTable2.render());


        TextTable textTable3 = new TextTable(false, "Key", "Value");
        textTable3.addRow("ssa_role_name", "Прикрепление сканированных подлинников документов");
        textTable3.addRow("r_object_id", "004a000000057e9a");
        textTable3.addRow("ssa_name", "Ответственный за сканирование ОРД");
        textTable3.addRow("r_object_type", "nt_doc_role");
        textTable3.addRow("i_vstamp", "2");
        textTable3.addRow("ssa_filial_name", "Демонстрационная организация");
        textTable3.addRow("sida_filial", "000b000000007536");
        textTable3.addRow("ssa_role", "scanning");


        System.out.println(textTable3.render());

        System.out.println();


        while (true) {
            String line = new Scanner(System.in).nextLine();
            if (line.isEmpty()) {
                System.setProperty("_terminal_witdh", "300");
            } else {
                System.setProperty("_terminal_witdh", line);
            }

            System.out.println("go");

            TextTable textTable4 = new TextTable(true);
            textTable4.addRow(" -a", "--archive", "same as -dR --preserve=all");
            textTable4.addRow(" ", "--attributes-only", "don't copy the file data, just the attributes");
            textTable4.addRow(" -B", "--backup[=CONTROL]", "make a backup of each existing destination file 1 2 3 4 5 6 7 8 9 100 101 102 103 104 105 106 107 108 109 200 201 202 203 204");
            textTable4.addRow(" -b", "", "like --backup but does not accept an argument");
            textTable4.addRow(" ", "--copy-contents", "copy contents of special files when recursive");
            textTable4.addRow(" -d", "", "same as --no-dereference --preserve=links");
            textTable4.addRow(" -f", "--force", "if an existing destination file cannot be 1opened, remove it and try again (this option is ignored when the -n option is also used)");
            System.out.println(textTable4.render());
            System.out.println(textTable4.getTextTableWidth());

            textTable4.setDecorEnabled(false);
            System.out.println(textTable4.render());
            System.out.println("ttw: " + textTable4.getTextTableWidth());

            System.out.println("tw: " + getTerminalWidth());
        }

    }

}

