package manage.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.service.Service;
import m.system.cache.CacheUtil;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import manage.dao.AdminGroupLinkDao;
import manage.dao.AdminLoginDao;
import manage.model.AdminGroup;
import manage.model.AdminGroupLink;
import manage.model.AdminLogin;

public class AdminGroupLinkService extends Service {
	/**
	 * 批量设置组链接  先删除，后插入
	 * @param adminGroupOid
	 * @param adminOids
	 * @throws Exception
	 */
	public void setLink(String adminGroupOid,String[] adminOids) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin();
			AdminGroup adminGroup=ModelQueryList.getModel(AdminGroup.class, adminGroupOid, new String[] {"*"});
			if(null==adminGroup) throw new MException(this.getClass(), "组不存在");
			AdminGroupLinkDao dao=getDao(AdminGroupLinkDao.class);
			dao.delete4GroupOid(adminGroup.getOid());
			if(null!=adminOids&&adminOids.length>0) {
				List<AdminGroupLink> list=new ArrayList<AdminGroupLink>();
				for(String oid : adminOids) {
					AdminGroupLink link=new AdminGroupLink();
					link.setOid(GenerateID.generatePrimaryKey());
					link.setAdminGroup(adminGroup);
					link.setType(adminGroup.getType());
					link.setAdmin(new AdminLogin());
					link.getAdmin().setOid(oid);
					list.add(link);
				}
				ModelUpdateUtil.insertModels(list.toArray(new AdminGroupLink[] {}));
			}
			//查找被去掉机构的用户
			List<AdminLogin> adminList=ModelQueryList.getModelList(AdminLogin.class, new String[] {"oid"}, null, 
				QueryCondition.and(new QueryCondition[] {
					QueryCondition.eq("orgGroup.oid", adminGroup.getOid()),
					QueryCondition.notIn("oid", 
						ModelQueryList.instance(AdminGroupLink.class, new String[] {"admin.oid"}, null,
							QueryCondition.eq("adminGroup.oid", adminGroup.getOid())
						)
					)
				})
			);
			for(AdminLogin admin : adminList) {
				admin.setOrgGroup(null);
				CacheUtil.clear(AdminLogin.class,admin.getOid());//清除admin缓存
			}
			if(adminList.size()>0)
				ModelUpdateUtil.updateModels(adminList.toArray(new AdminLogin[] {}), new String[] {"orgGroup.oid"});

			adminList=ModelQueryList.getModelList(AdminLogin.class, new String[] {"oid"}, null, 
				QueryCondition.isEmpty("orgGroup.oid"));
			List<String> oidList=new ArrayList<String>();
			for(AdminLogin admin : adminList) {
				oidList.add(admin.getOid());
			}
			getDao(AdminLoginDao.class).updateOrgGroup4Null();//更新当前机构为空的用户
			adminList=ModelQueryList.getModelList(AdminLogin.class, new String[] {"oid"}, null, 
				QueryCondition.and(new QueryCondition[] {
					QueryCondition.notEmpty("orgGroup.oid"),
					QueryCondition.in("oid", oidList.toArray(new String[] {}))
				})
			);
			for(AdminLogin admin : adminList) {
				CacheUtil.clear(AdminLogin.class,admin.getOid());//清除admin缓存
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	/**
	 * 获取组对应的链接map
	 * @param adminGroupOid
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	public Map<String,Boolean> getLink(String adminGroupOid) throws SQLException, MException{
		List<AdminGroupLink> list=ModelQueryList.getModelList(AdminGroupLink.class, new String[] {"*"}, null, QueryCondition.eq("adminGroup.oid", adminGroupOid));
		Map<String,Boolean> map=new HashMap<String, Boolean>();
		for(AdminGroupLink link : list) {
			map.put(link.getAdmin().getOid(), true);
		}
		return map;
	}
	/**
	 * 获取所有组链接
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	public List<AdminGroupLink> getAllLink() throws SQLException, MException{
		return ModelQueryList.getModelList(AdminGroupLink.class, new String[] {"*","adminGroup.*","adminGroup.parent.*"}, null,null);
	}
}
