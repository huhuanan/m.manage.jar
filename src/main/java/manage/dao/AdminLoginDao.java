package manage.dao;

import java.sql.SQLException;
import java.util.Date;

import m.common.dao.Dao;
import m.common.model.util.ModelUpdateUtil;
import m.system.db.DBManager;
import m.system.db.DataRow;
import m.system.exception.MException;
import manage.model.AdminLogin;

public class AdminLoginDao extends Dao {
	/**
	 * 根据用户名和密码获取oid
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public String getUserOid(String username,String password) throws SQLException{
		DataRow dr=DBManager.queryFirstRow("select oid from os_admin_login where username=? and password=?",new String[]{username,password});
		if(null!=dr){
			return dr.get(String.class,"oid");
		}else{
			return null;
		}
	}
	/**
	 * 更新最后登录时间和ip
	 * @param admin
	 * @param ip
	 * @throws MException
	 */
	public void updateLastInfo(AdminLogin admin,String ip) throws MException{
		admin.setLastLoginTime(new Date());
		admin.setLastLoginIp(ip);
		if(null==admin.getLoginCount()){
			admin.setLoginCount(1);
		}else{
			admin.setLoginCount(admin.getLoginCount()+1);
		}
		ModelUpdateUtil.updateModel(admin, new String[]{"lastLoginTime","lastLoginIp","loginCount"});
	}
	/**
	 * 更新用户的当前机构，只更新为空的
	 * @throws SQLException
	 */
	public void updateOrgGroup4Null() throws SQLException {
		DBManager.executeUpdate("update os_admin_login al set al.org_group_oid=( " + 
				" SELECT max(gl.admin_group_oid) FROM os_admin_group_link gl WHERE gl.admin_oid=al.oid and gl.type='C' " + 
				") where al.org_group_oid='' or al.org_group_oid is null");
	}
}
