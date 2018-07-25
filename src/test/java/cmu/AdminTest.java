package cmu;

import cmu.db.beans.AdminBean;
import cmu.db.daos.AdminDAO;
import cmu.db.daos.AdminStreamerDAO;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * http://www.lucas-liu.com
 *
 * @author lucas
 * @create 2018-07-25 4:32 PM
 */
public class AdminTest {

    private List<AdminBean> originalAdmins = new ArrayList<>();
    {
        // see resource/dbSetup.sql
        originalAdmins.add(new AdminBean(1, "A", "a"));
        originalAdmins.add(new AdminBean(2, "B", "b"));
        originalAdmins.add(new AdminBean(3, "C", "c"));
        originalAdmins.add(new AdminBean(4, "D", "d"));
        originalAdmins.add(new AdminBean(5, "E", "e"));
    }

    // Please run the test to test all the CRUD, or run the read, insert, etc. in default sequence

    @Test
    public void test() {
        readTest();
        insertTest();
        upsertTest();
        updateTest();
        deleteTest();
        streamedReadTest();
    }

    @Test
    public void readTest() {
        List<AdminBean> queriedAdmins = AdminDAO.dao.getAll(null);
        Assert.assertEquals(originalAdmins, queriedAdmins);
    }

    @Test
    public void insertTest() {
        AdminBean originalAdmin = new AdminBean(12138, "Lucas", ":-)");
        AdminDAO.dao.insert(originalAdmin);
        AdminBean queriedAdmin = AdminDAO.dao.getAllByField(null, "adminId", "12138").get(0);
        Assert.assertEquals(originalAdmin, queriedAdmin);
    }

    @Test
    public void upsertTest() {
        AdminBean originalAdmin = new AdminBean(12138, "Liu", ":-)");
        AdminDAO.dao.upsert(originalAdmin);
        AdminBean queriedAdmin = AdminDAO.dao.getAllByField(null, "adminId", "12138").get(0);
        Assert.assertEquals(originalAdmin, queriedAdmin);
    }

    @Test
    public void updateTest() {
        AdminBean originalAdmin = new AdminBean(12138, "Lucas Liu", "the most secure password");
        AdminDAO.dao.update(originalAdmin, "adminId", "userName", "password");
        AdminBean queriedAdmin = AdminDAO.dao.getAllByField(null, "adminId", "12138").get(0);
        Assert.assertEquals(originalAdmin, queriedAdmin);
    }

    @Test
    public void deleteTest() {
        AdminDAO.dao.deleteByKey("adminId", "12138");
        List<AdminBean> queriedAdmins = AdminDAO.dao.getAll(null);
        Assert.assertEquals(originalAdmins, queriedAdmins);
    }

    @Test
    public void streamedReadTest() {
        StringBuilder originalProcessedPwdSB = new StringBuilder();
        originalAdmins.stream().map(bean -> bean.password).forEach(originalProcessedPwdSB::append);
        String originalProcessedPwd = originalProcessedPwdSB.toString();

        List<Integer> partialAdminIds = new ArrayList<>();
        partialAdminIds.add(1);
        partialAdminIds.add(2);
        partialAdminIds.add(3);
        partialAdminIds.add(4);
        partialAdminIds.add(5);
        String queriedProcessedPwd = AdminStreamerDAO.dao.concatPassword(partialAdminIds, null);
        Assert.assertEquals(originalProcessedPwd, queriedProcessedPwd);
    }
}
