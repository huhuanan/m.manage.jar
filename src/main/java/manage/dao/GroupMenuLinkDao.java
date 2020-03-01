package manage.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import m.common.dao.Dao;
import m.system.db.DBManager;
import m.system.db.DataRow;
import m.system.db.DataSet;
import m.system.db.SqlBuffer;

public class GroupMenuLinkDao extends Dao {
	public Map<String,Object> getGroupMenuLink(String group_oid) throws SQLException{
		Map<String,Object> map=new HashMap<String, Object>();
		DataSet ds=DBManager.executeQuery("SELECT menu_oid,oid FROM os_group_menu_link where admin_group_oid=?", new String[]{group_oid});
		for(DataRow dr : ds.rows()){
			map.put(dr.get(String.class,"menu_oid"), dr.get(String.class,"oid"));
		}
		return map;
	}
	public String getMenuOid(String admin_oid,String group_oid,String menu_oid) throws SQLException{
		SqlBuffer sql=new SqlBuffer();
		sql.append("SELECT mi.oid ")
		.append("FROM os_menu_info mi ")
		.append("left join os_group_menu_link gm on mi.oid=gm.menu_oid ")
		.append(" and (gm.admin_group_oid=? ",group_oid)
		.append("  or gm.admin_group_oid in(select org_group_oid from os_admin_login where oid=?)",admin_oid)
		.append("  or gm.admin_group_oid in(select admin_group_oid from os_admin_group_link where admin_oid=? and type='B')) ",admin_oid)
		.append("where mi.oid=? and (gm.menu_oid=? or mi.is_public='Y')",menu_oid,menu_oid);
		DataRow row=sql.queryFirstRow();
		if(null==row){
			return null;
		}else{
			return row.get(String.class, "oid");
		}
	}
	public void removeAllGroupMenuLink(String admin_group_oid) throws SQLException{
		DBManager.executeUpdate("delete from os_group_menu_link where admin_group_oid=?",new String[]{admin_group_oid});
	}
}
