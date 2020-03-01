package manage.service;

import java.sql.SQLException;
import java.util.List;

import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.model.util.QueryOrder;
import m.common.service.Service;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.model.AdminGroup;
import manage.model.AdminGroupLink;
import manage.model.AdminLogin;

public class AdminGroupService extends Service {

	public String save(AdminGroup model) throws MException, SQLException {
		ModelCheckUtil.checkNotNull(model, new String[] {"num","name"});
		ModelCheckUtil.checkUniqueCombine(model, new String[]{"name","parent.oid"});
		ModelCheckUtil.checkUniqueCombine(model, new String[]{"num"});
		if(StringUtil.isSpace(model.getOid())){
			model.setOid(GenerateID.generatePrimaryKey());
			model.setStatus("0");
			ModelUpdateUtil.insertModel(model);
			return "保存成功";
		}else{
			ModelUpdateUtil.updateModel(model, new String[]{"name","num","business","description","sort"});
			return "修改成功";
		}
	}
	public List<AdminGroup> getOrgList(String adminOid) throws SQLException, MException{
		return ModelQueryList.getModelList(AdminGroup.class, new String[] {"*","parent.*"}, null, 
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("type", "C"),
				QueryCondition.in("oid", ModelQueryList.instance(AdminGroupLink.class, new String[] {"adminGroup.oid"}, null, QueryCondition.eq("admin.oid", adminOid)))
			}), QueryOrder.asc("sort")
		);
	}
	public List<AdminGroup> getList(AdminGroup model) throws SQLException, MException {
		QueryCondition con=null;
		if(null!=model&&!StringUtil.isSpace(model.getType())) {
			con=QueryCondition.eq("type", model.getType());
		}
		return ModelQueryList.getModelList(AdminGroup.class,new String[] {"*","parent.*"},null,
			con,QueryOrder.asc("sort"));
	}
	public List<AdminGroup> getList4Admin(String adminOid) throws SQLException, MException{
		return ModelQueryList.getModelList(AdminGroup.class,new String[] {"*","parent.*"},null,
			QueryCondition.or(new QueryCondition[] {
				QueryCondition.in("oid", ModelQueryList.instance(AdminGroupLink.class, new String[] {"adminGroup.oid"}, null, QueryCondition.eq("admin.oid", adminOid))),
				QueryCondition.in("oid", ModelQueryList.instance(AdminLogin.class, new String[] {"adminGroup.oid"}, null, QueryCondition.eq("oid", adminOid)))
			}),
			QueryOrder.asc("type"),QueryOrder.asc("parent.sort"),QueryOrder.asc("sort")
		);
	}
}
