package cmu.db.daos;


import cmu.db.SqlRequest;
import cmu.db.beans.AdminBean;

import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 2:28 PM
 */
public class AdminDAO<T extends AdminBean> extends DAO<T> {

    private static final String tableName = "admin";
    public static final AdminDAO<AdminBean> dao = new AdminDAO<>(AdminBean.class, tableName);

    public AdminDAO(Class<T> c, String tableName) {
        runtimeClass = c;
        table = tableName;
    }

    public List<T> getAll(String[] fields) {
        final String fieldString = fields == null ? "*" : String.join(",", fields);
        final String query = "SELECT " + fieldString + " FROM " + table;

        SqlRequest req = new SqlRequest(query);
        return read(req);
    }

    public List<T> getAllByField(String[] fields, String fieldName, String fieldValue) {
        final String fieldString = fields == null ? "*" : String.join(",", fields);
        final String query = "SELECT " + fieldString + " FROM " + table + " WHERE " + fieldName + " = ?";

        SqlRequest req = new SqlRequest(query);
        req.pushParam(fieldValue);
        return read(req);
    }
}
