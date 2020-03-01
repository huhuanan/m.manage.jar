package manage.dao;

import java.sql.SQLException;

import m.common.dao.Dao;
import m.system.db.DBManager;

public class AdminGroupLinkDao extends Dao {
	/**
	 * 删除组对应的所有链接
	 * @param groupOid
	 * @throws SQLException
	 */
	public void delete4GroupOid(String groupOid) throws SQLException {
		DBManager.executeUpdate("delete from os_admin_group_link where admin_group_oid=?",new String[] {groupOid});
	}
	
}
