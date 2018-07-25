package cmu.db.daos;

import cmu.db.SqlRequest;
import cmu.db.beans.DBBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 12:26 PM
 */
public class DAO<T extends DBBean> {

    /**
     * CRUD -> create (insert, upsert), read, update, delete
     * insert and upsert is based on write()
     */
    protected Class<T> runtimeClass;
    protected  String table = "";

    public List<T> read(SqlRequest req) {
        List<T> result = new ArrayList<>();
        req.treat(res -> {
            try {
                while (res.resultSet.next()) {
                    T item = runtimeClass.newInstance();
                    item.buildFrom(res.resultSet, null);
                    result.add(item);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                // error
            }
        });
        return result;
    }

    public int update(T item, String idField, String... fieldsToUpdate ) {
        if (item.getField(idField) == null) {
            return 0;
        }

        int[] affected = { 0 };
        SqlRequest req = new SqlRequest("UPDATE " + table +
                " SET " + item.getSQLUpdateValuesFormat(fieldsToUpdate) +
                " WHERE " + idField + " = ?");
        req.pushMembers(item, fieldsToUpdate);
        req.pushMember(item, idField);
        req.treat(res -> affected[0] = res.affectedRowNum);
        return affected[0];
    }

    public boolean deleteByKey(String keyName, String keyValue) {
        int[] affected = { 0 };
        SqlRequest req = new SqlRequest("DELETE FROM " + table + " WHERE " + keyName + " = ?");
        req.pushParam(keyValue);
        req.treat(res -> affected[0] = res.affectedRowNum);
        return affected[0] > 0;
    }

    public int write(SqlRequest req) {
        final int[] affectedRowNum = {0};
        req.treat(res -> affectedRowNum[0] = res.affectedRowNum);
        return affectedRowNum[0];
    }

    public int insert(T record) {
        List<T> records = new ArrayList<>();
        records.add(record);
        return insert(records);
    }

    public int insert(List<T> records) {
        final int len = records.size();
        if (len < 1) {
            return 0;
        }

        SqlRequest req = new SqlRequest(makeInsertSql(records.get(0), len));
        for (T record : records) {
            setInsertValues(req, record, null);
        }
        return write(req);
    }

    public int upsert(T record) {
        List<T> records = new ArrayList<>();
        records.add(record);
        return upsert(records);
    }

    public int upsert(List<T> records) {
        final int len = records.size();
        if (len < 1) {
            return 0;
        }

        SqlRequest req = new SqlRequest(makeUpsertSql(records.get(0), len, null));
        for (T record : records) {
            setInsertValues(req, record, null);
        }
        return write(req);
    }

    protected String makeUpsertSql(T sample, int count, HashSet<String> desiredFields) {
        String[] v = getSqlComponents(sample, desiredFields);
        final String column = v[0];
        final String value = v[1];
        final String update = v[2];

        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO " + table + column + " VALUES ");
        for (int i = 0; i < count; i += 1) {
            if (i > 0) {
                b.append(",");
            }
            b.append(value);
        }
        b.append("\n ON DUPLICATE KEY UPDATE \n");
        b.append(update);
        return b.toString();
    }

    protected String makeInsertSql(T sample, int count) {
        String[] v = getSqlComponents(sample, null);
        final String column = v[0];
        final String value = v[1];

        StringBuilder b = new StringBuilder();
        b.append("INSERT IGNORE INTO " + table + column + " VALUES ");
        for (int i = 0; i < count; i += 1) {
            if (i > 0) {
                b.append(",");
            }
            b.append(value);
        }
        return b.toString();
    }

    protected void setInsertValues(SqlRequest req, T bean, HashSet<String> desiredFields) {
        Field[] fields = bean.getFields(desiredFields);
        String autoIncrementField = bean.getAutoIncrementField();

        for (Field field : fields) {
            final String fieldName = field.getName();
            if (autoIncrementField != null && autoIncrementField.equals(fieldName)) {
                continue;
            }
            req.pushMember(bean, field);
        }
    }

    private String[] getSqlComponents(T bean, HashSet<String> desiredFields) {
        Field[] fields = bean.getFields(desiredFields);
        String autoIncrementField = bean.getAutoIncrementField();

        StringBuilder columns = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" \n(");
        StringBuilder updates = new StringBuilder(" ");

        int writtenCount = 0;
        for (Field field : fields) {
            final String fieldName = field.getName();
            if (autoIncrementField != null && autoIncrementField.equals(fieldName)) {
                continue;
            }

            if (writtenCount > 0) {
                values.append(",");
                columns.append(",");
                updates.append(",\n ");
            }

            columns.append(field.getName());
            values.append("?");
            updates.append(field.getName() + "=VALUES(" + field.getName() + ")");
            writtenCount += 1;
        }
        columns.append(")");
        values.append(")");
        return new String[]{ columns.toString(), values.toString(), updates.toString() };
    }
}
