# Table-like text renderer

To obtain a table like:

    +----+--------------------+------------+------+------+
    | id | name               | country_id | code | desc |
    +----+--------------------+------------+------+------+
    |  1 | Moscow             | 1          | MOS  | NULL |
    |  2 | New-York           | 3          | NY   | NULL |
    |  3 | Kiyv               | 2          | KI   | NULL |
    |  6 | Washington         | 3          | WS   | NULL |
    |  7 | Samara             | 1          | SM   | NULL |
    +----+--------------------+------------+------+------+

use the following code:

```java
TextTable textTable = new TextTable("id", "name", "country_id", "code", "desc");

textTable.addRow(1, "Moscow",     1, "MOS", "NULL");
textTable.addRow(2, "New-York",   3, "NY",  "NULL");
textTable.addRow(3, "Kiyv",       2, "KI",  "NULL");
textTable.addRow(6, "Washington", 4, "WS",  "NULL");
textTable.addRow(7, "Samara",     1, "SM",  "NULL");

System.out.println(textTable.render());
```

You can also pass an Object[] array to addRow method:

```java
textTable.addRow(new Object[] { 1, "Moscow", 1, "MOS", "NULL" });
```
Assign some keys to table rows:
```java
textTable.addKeyedRow("myKey", 1, "Moscow", 1, "MOS", "NULL");
```
Then you can refer to this row using the key:
```java
Object[] cells = textTable.getRow("myKey");
```
Or for removing the row:
```java
textTable.removeRow("myKey");
```
For more about the API, see the public methods of the TextTable class. 