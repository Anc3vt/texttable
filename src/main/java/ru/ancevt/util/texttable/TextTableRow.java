package ru.ancevt.util.texttable;

import lombok.Data;

@Data
class TextTableRow {
    private Object[] data;
    private Object key;
}
