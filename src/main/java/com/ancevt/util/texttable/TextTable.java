/*
 *   Args II
 *   Copyright (C) 2020 Ancevt (i@ancevt.ru)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ancevt.util.texttable;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class TextTable {
	
	private static final int DEFAULT_MAX_STRING_LENGTH = 100;
	
	public static void main(String[] args) {
/*
		+----+--------------------+------------+------+------+
		| id | name               | country_id | code | desc |
		+----+--------------------+------------+------+------+
		|  1 | Moscow             | 1          | MOS  | NULL |
		|  2 | New-York           | 3          | NY   | NULL |
		|  3 | Kiyv               | 2          | KI   | NULL |
		|  6 | Washington         | 3          | WS   | NULL |
		|  7 | Samara             | 1          | SM   | NULL |
		+----+--------------------+------------+------+------+
*/
		
		final TextTable textTable = new TextTable("id", "name", "country_id", "code", "desc");

		textTable.addRow(1, "Moscow",     1, "MOS", "NULL");
		textTable.addRow(2, "New-York",   3, "NY",  "NULL");
		textTable.addRow(3, "Kiyv",       2, "KI",  "NULL");
		textTable.addRow(6, "Washington", 4, "WS",  "NULL");
		textTable.addRow(7, "Samara",     1, "SM",  "NULL");

		System.out.println(textTable.render());

		System.out.println();

		final TextTable textTable2 = new TextTable("id", "name", "country_id", "code", "desc");
		textTable2.setDecorEnabled(false);

		textTable2.addRow(1, "Moscow0",     1, "MOS", "NULL");
		textTable2.addRow(2, "New-York",   3, "NY",  "NULL");
		textTable2.addRow(3, "Kiyv",       2, "KI",  "NULL");
		textTable2.addRow(6, "Washington", 4, "WS",  "NULL");
		textTable2.addRow(7, "Samara0",     1, "SM",  "NULL");

		System.out.println(textTable2.render());
	}

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
		for(final TextTableRow row : rows)
			if(row.getKey() == key || key.equals(row.getKey()))
				return row.getData();
		
		return new Object[0];
	}
	
	public Object[] getRow(int index) {
		return rows.get(index).getData();
	}
	
	public void removeRow(Object key) {
		for(final TextTableRow row : rows) {
			if(row.getKey() == key || key.equals(row.getKey())) {
				rows.remove(row);
				break;
			}
		}
	}
	
	public boolean hasRow(Object key) {
		for(final TextTableRow row : rows)
			if(row.getKey() == key || key.equals(row.getKey()))
				return true;
		
		return false;
	}
	
	public String render() {
		final StringBuilder sb = new StringBuilder();
		
		detectColumnSizes();
		
		sb.append(columnNames != null ? renderHeader() : renderDecor());

		if(isDecorEnabled()) sb.append('\n');
		
		for(int i = 0; i < size(); i ++) {
			sb.append(renderLine(getRow(i)));
			sb.append('\n');
		}
		
		sb.append(renderDecor());
		
		return sb.toString();
	}

	private String renderDecor() {
		if(!decorEnabled) return "";

		final StringBuilder sb = new StringBuilder();
		
		sb.append('+');
		for (int size : columnSizes) {
			for(int j = 0; j < size + 1; j ++) {
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

		if(isDecorEnabled()) sb.append('|');
		
		for (int i = 0; i < cells.length; i++) {
			if(i >= columnSizes.length) break;
			
			String n = String.valueOf(cells[i]);
			if(n.length() > maxStringLength) {
				n = n.substring(0, maxStringLength - 2) + "..";
			}
			
			sb.append(' ');
			sb.append(n);
			final int spacesLeftToRender = getSpacesLeft(n, columnSizes[i]);
			sb.append(spaces(spacesLeftToRender));


			if(isDecorEnabled()) sb.append('|');
		}
		
		if (cells.length < columnSizes.length) {
			final int difference = columnSizes.length - cells.length;
			
			for(int i = 0; i < difference; i ++) {
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
		for(int i = 0; i < count; i ++) sb.append(' ');
		return sb.toString();
	}
	
	private void detectColumnSizes() {
		int columnCount = 0;
		if (columnNames != null) {
			columnCount = columnNames.length;
		} else {
			int max = 0;
			for(final TextTableRow row : rows) {
				final Object[] cells = row.getData();
				if(cells.length > max) max = cells.length;
			}
			columnCount = max;
		}

		this.columnSizes = new int[columnCount];

		if(columnNames != null) {
			for (String columnName : columnNames) {
				for (int j = 0; j < columnName.length(); j++) {
					if (j >= columnSizes.length) break;

					String string = columnNames[j];

					if (string.length() > maxStringLength) {
						string = string.substring(0, maxStringLength);
					}


					final int stringLength = string.length();

					if (stringLength > columnSizes[j])
						columnSizes[j] = stringLength + 1;
				}
			}
		}
		
		for(int i = 0; i < size(); i ++) {
			final Object[] rowData = getRow(i);
			
			for(int j = 0; j < rowData.length; j ++) {
				if (j >= columnSizes.length) break;
				
				String string = String.valueOf(rowData[j]);
				if (string.length() > maxStringLength) {
					string = string.substring(0, maxStringLength);
				}
				
				final int stringLength = string.length();
				
				if (stringLength > columnSizes[j])
					columnSizes[j] = stringLength + 2;
			}
		}
	}

}

