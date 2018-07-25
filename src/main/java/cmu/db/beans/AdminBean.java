package cmu.db.beans;

import java.util.Objects;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 2:27 PM
 */
public class AdminBean extends DBBean {

    public int adminId;
    public String userName;
    public String password;

    public AdminBean() {
        super();
    }

    public AdminBean(int adminId, String userName, String password) {
        this.adminId = adminId;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminBean adminBean = (AdminBean) o;
        return adminId == adminBean.adminId &&
                Objects.equals(userName, adminBean.userName) &&
                Objects.equals(password, adminBean.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, userName, password);
    }
}
