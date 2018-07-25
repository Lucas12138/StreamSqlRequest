package cmu.db.beans;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 12:29 PM
 */
//TODO: consider create time and update time in bean (good practice, keep track of those in DB)
public class DBBean {

    public String getAutoIncrementField() {
        return null;
    }

    public Field[] getFields(HashSet<String> fieldNames) {
        if(fieldNames==null){
            return this.getClass().getFields();
        }
        Field[] fields = this.getClass().getFields();
        List<Field> newFields = new ArrayList<>();
        for (Field field : fields) {
            final String fieldName = field.getName();
            if (fieldNames.contains(fieldName)) {
                newFields.add(field);
            }
        }
        return newFields.toArray(new Field[newFields.size()]);
    }

    public Field getField(String name) {
        Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
            if (f.getName().equals(name)) return f;
        }
        return null;
    }

    /**
     * Using reflection to build this(bean itself) from resultSet
     */
    public void buildFrom(ResultSet resultSet, String tableName) {
        Field[] fields = this.getClass().getFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if (tableName != null) fieldName = tableName + "|" + fieldName;
            Class<?> fieldType = field.getType();
            try {
                // Int
                if (fieldType == int.class) {
                    field.set(this, resultSet.getInt(fieldName));
                }
                // Float
                else if (fieldType == float.class) {
                    field.set(this, resultSet.getFloat(fieldName));
                } else if (fieldType == double.class) {
                    field.set(this, resultSet.getDouble(fieldName));
                }
                // String
                else if (fieldType == String.class) {
                    field.set(this, resultSet.getString(fieldName));
                }
                // Date
                else if (fieldType == Date.class) {
                    Date javaDate = resultSet.getTimestamp(fieldName);
                    field.set(this, javaDate);
                }
                // Boolean
                else if (fieldType == boolean.class) {
                    field.set(this, resultSet.getBoolean(fieldName));
                }
                // Byte
                else if (fieldType == byte.class) {
                    field.set(this, resultSet.getByte(fieldName));
                }
                // Long
                else if (fieldType == long.class) {
                    field.set(this, resultSet.getLong(fieldName));
                } else if (fieldType == Blob.class) {
                    field.set(this, resultSet.getBlob(fieldName));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // error
            } catch (SQLException e) {
                // error
            }
        }
    }

    public String getSQLUpdateValuesFormat(String... fields) {
        StringBuilder result = new StringBuilder();

        boolean wroteOne = false;
        for (String field : fields) {
            if (wroteOne) result.append(", ");
            result.append(field).append(" = ?");
            wroteOne = true;
        }
        return result.toString();
    }
}
