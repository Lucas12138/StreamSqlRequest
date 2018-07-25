package cmu.db;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-03 11:38 PM
 */

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO: get rid of sout if logging is available
// TODO: consider multiple statements
// TODO: consider timeout
// TODO: consider batch process and fetch size
// TODO: check status and return boolean
public class SqlRequest {

    // TODO: credentials should be added to environment variables or properties files
    private final String USERNAME = "lucas";
    private final String PASSWORD = "lucas";
    private final String M_CONN_STRING = "jdbc:mysql://localhost:3306/";
    private final String DBNAME = "company";

    private Connection conn = null;
    private String sqlString;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private Handler resultHandler;
    private List<Parameter> parameters = new ArrayList<>();

    public SqlRequest(String sqlString) {
        this.sqlString = sqlString;
    }

    private boolean openConnection() {
        try {
            conn = DriverManager.getConnection(M_CONN_STRING + DBNAME, USERNAME, PASSWORD);
            return true;
        } catch (SQLException e) {
            // System.err.println(e);
            return false;
        }

    }

    public Connection getConnection() {
        if (conn == null) {
            if (openConnection()) {
                // System.out.println("Connection opened");
                return conn;
            } else {
                return null;
            }
        }
        return conn;
    }

    public void close() {
        // System.out.println("Closing connection");
        try {
            conn.close();
            conn = null;
        } catch (Exception e) {
            // error
        }
    }

    private boolean isReadType() {
        String reqStr = this.sqlString.toUpperCase();
        return reqStr.toUpperCase().startsWith("SELECT ");
    }

    public void treat() {
        try {
            conn = getConnection();
            String[] requests = new String[]{sqlString};

            int parameterCount = 0;
            for (String requestStringLocal: requests) {
                statement = conn.prepareStatement(requestStringLocal, Statement.RETURN_GENERATED_KEYS);

                int parameterSizeLocal = this.parameters.size();

                for (int i = parameterCount; i < parameterCount + parameterSizeLocal; i += 1) {
                    parameters.get(i).setToStatement(statement, i + 1);
                }
                parameterCount += parameterSizeLocal;


                DBResult result = new DBResult();
                result.statement = statement;

                if (isReadType()) {
                    result.affectedRowNum = 0;
                    resultSet = statement.executeQuery();
                    result.resultSet = resultSet;
                } else {
                    boolean success = false;
                    for (int i = 0; i < 15; i += 1) {
                        try {
                            result.affectedRowNum = statement.executeUpdate();
                            success = true;
                            break;
                        } catch (MySQLIntegrityConstraintViolationException | MySQLSyntaxErrorException se) {
                            if (se.getMessage().contains("doesn't exist")) {
                                break;
                            }
                            throw se;
                        } catch (Exception e) {
                            try {
                                // avoid DB deadlocks
                                int mod = i + 1;
                                Thread.sleep(i < 10 ? new Random().nextInt(mod * 1000) + (mod * 1000) : new Random().nextInt(mod * 5000) + (mod * 1000));
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    if (!success) {
                        throw new Exception("Failed to execute request!");
                    }
                    // store generated keys
                    ResultSet generatedKeys = result.statement.getGeneratedKeys();
                    while (generatedKeys.next()) {
                        result.generatedKeys.add(generatedKeys.getLong(1));
                    }
                }

                if (!conn.getAutoCommit()) {
                    conn.commit();
                }

                if (resultHandler != null) {
                    resultHandler.onResult(result);
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            // error
        } catch (Exception e) {
            // error
        } finally {
            try { if (resultSet  != null) resultSet.close();  } catch(Exception e) { e.printStackTrace();}
            try { if (statement  != null) statement.close();  } catch(Exception e) { e.printStackTrace();}
            try { if (conn != null) conn.close(); } catch(Exception e) { e.printStackTrace();}
        }
    }

    public void treat(Handler handler) {
        this.resultHandler = handler;
        treat();
    }

    // Handler for callback
    public interface Handler {
        public void onResult(DBResult result) throws Exception;
    }

    /**
     * Important:
     * DBType, Parameter, pushParam, pushMember, pushMembers all serve for simplifying
     * adding parameters to the request. (This can save a lot of duplicate code, such like
     * setInt, setString, etc.)
     */
    private enum DBType {
        BOOL,
        INT,
        FLOAT,
        DOUBLE,
        DATE,
        STRING,
        BYTE,
        LONG,
        BLOB,
        INPUT_STREAM
    }

    private class Parameter {
        public DBType type;

        public int intV;
        public float floatV;
        public double doubleV;
        public boolean boolV;
        public Date dateV;
        public String stringV;
        public byte byteV;
        public long longV;
        public Blob blobV;
        public InputStream inputStreamV;

        public Parameter(int v) {
            type = DBType.INT;
            intV = v;
        }

        public Parameter(Blob v) {
            type = DBType.BLOB;
            blobV = v;
        }

        public Parameter(float v) {
            type = DBType.FLOAT;
            floatV = v;
        }

        public Parameter(double v) {
            type = DBType.DOUBLE;
            doubleV = v;
        }

        public Parameter(boolean v) {
            type = DBType.BOOL;
            boolV = v;
        }

        public Parameter(Date v) {
            type = DBType.DATE;
            dateV = v;
        }

        public Parameter(String v) {
            type = DBType.STRING;
            stringV = v;
        }

        public Parameter(Byte v) {
            type = DBType.BYTE;
            byteV = v;
        }

        public Parameter(long v) {
            type = DBType.LONG;
            longV = v;
        }

        public Parameter(InputStream v) {
            type = DBType.INPUT_STREAM;
            inputStreamV = v;
        }

        public void setToStatement(PreparedStatement s, int index) throws SQLException {
            switch (type) {
                case INT:
                    s.setInt(index, intV);
                    break;

                case FLOAT:
                    s.setFloat(index, floatV);
                    break;

                case DOUBLE:
                    s.setDouble(index, doubleV);
                    break;

                case BOOL:
                    s.setBoolean(index, boolV);
                    break;

                case DATE:
                    Timestamp newDate = null;
                    if (dateV != null) {
                        newDate = new Timestamp(dateV.getTime());
                    }
                    s.setTimestamp(index, newDate);
                    break;

                case STRING:
                    s.setString(index, stringV);
                    break;

                case BYTE:
                    s.setByte(index, byteV);
                    break;

                case LONG:
                    s.setLong(index, longV);
                    break;

                case BLOB:
                    s.setBlob(index, blobV);

                case INPUT_STREAM:
                    s.setBlob(index, inputStreamV);

                default:
                    break;
            }
        }
    }

    public SqlRequest pushParam(int v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(float v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(double v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(boolean v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(Date v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(String v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(Byte v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(long v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(Blob v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushParam(InputStream v) {
        parameters.add(new Parameter(v));
        return this;
    }

    public SqlRequest pushMembers(Object obj, String... memberNames) {

        Field[] fields = obj.getClass().getFields();

        for (String memberName : memberNames) {
            for (Field field : fields) {
                if (field.getName().equals(memberName)) {
                    pushMember(obj, field);
                    break;
                }
            }
        }
        return this;
    }

    public SqlRequest pushMember(Object obj, String memberName) {

        Field[] fields = obj.getClass().getFields();

        for (Field field : fields) {
            if (field.getName().equals(memberName)) {
                pushMember(obj, field);
                return this;
            }
        }
        return this;
    }

    public SqlRequest pushMember(Object obj, Field member) {
        Class<?> fieldType = member.getType();

        try {
            // Int
            if (fieldType == int.class) {
                pushParam( member.getInt(obj));
            }
            // Float
            else if (fieldType == float.class) {
                pushParam( member.getFloat(obj));
            }
            // Double
            else if (fieldType == double.class){
                pushParam(member.getDouble(obj));
            }
            // String
            else if (fieldType == String.class) {
                pushParam( (String) member.get(obj));
            }
            // Date
            else if (fieldType == Date.class) {
                pushParam( (Date) member.get(obj));
            }
            // Boolean
            else if (fieldType == boolean.class) {
                pushParam( member.getBoolean(obj));
            }
            // Byte
            else if (fieldType == byte.class) {
                pushParam( member.getByte(obj));
            }
            // Long
            else if (fieldType == long.class) {
                pushParam( member.getLong(obj));
            }
            else {
                // error
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // error
        }
        return this;
    }


}