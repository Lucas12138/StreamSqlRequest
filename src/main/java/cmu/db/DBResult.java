package cmu.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 11:35 AM
 */
public class DBResult {

    public int affectedRowNum;
    public List<Long> generatedKeys = new ArrayList<>();
    public ResultSet resultSet;
    public PreparedStatement statement;
}
