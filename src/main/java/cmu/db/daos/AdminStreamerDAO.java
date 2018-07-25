package cmu.db.daos;

import cmu.db.SqlRequest;
import cmu.db.beans.AdminBean;

import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 5:43 PM
 */
public class AdminStreamerDAO<T extends AdminBean> extends DAO<T> {

    private static final String tableName = "admin";
    public static final AdminStreamerDAO<AdminBean> dao = new AdminStreamerDAO<>(AdminBean.class, tableName);

    public AdminStreamerDAO(Class<T> c, String tableName) {
        runtimeClass = c;
        table = tableName;
    }

    /**
     * This is only a simple example showing the usage of streamedRead.
     * // TODO: If there're more methods like these, or even more complex ones,
     * // TODO: we should consider puting streaming CRUD into a higher level class and
     * // TODO: let those process logic remain here in DAO
     */
    public String concatPassword(List<Integer> adminIds, String[] fields) {
        final String fieldString = fields == null ? "*" : String.join(",", fields);
        final String query = "SELECT " + fieldString + " FROM " + table;

        SqlRequest req = new SqlRequest(query);
        StringBuilder stringBuilder = new StringBuilder();
        streamedRead(adminIds, req, admins -> admins.stream().map(bean -> bean.password).forEach(stringBuilder::append));
        return stringBuilder.toString();
    }

    public interface HandlerOnList {
        public void onResultList(List<AdminBean> partialResults);
    }

    /**
     * This method is very handy when adminId is part of the primary keys
     * and the data size is very large. Streaming will solve the OOM issue.
     * Besides, streamedRead is more suitable for complex methods, rather than simple CRUD
     */
    public void streamedRead(List<Integer> adminIds, SqlRequest req, HandlerOnList handlerOnList) {
        // bufferSize depends on memory size, could be more customized
        int bufferSize = 1000;
        int buffers = (int) Math.ceil(1.0 * adminIds.size() / bufferSize);

        for (int i = 0; i < buffers; i += 1) {
            int fromInclusive = i * bufferSize;
            int toExclusive = Math.min(fromInclusive + bufferSize, adminIds.size());
            List<Integer> adminSubset = adminIds.subList(fromInclusive, toExclusive);

            List<AdminBean> partialBeans = (List<AdminBean>) read(req);
            handlerOnList.onResultList(partialBeans);
        }
    }

}
