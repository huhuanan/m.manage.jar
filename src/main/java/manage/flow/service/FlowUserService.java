package manage.flow.service;

import java.util.ArrayList;
import java.util.List;

import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.service.Service;
import m.system.exception.MException;
import m.system.util.StringUtil;
import manage.flow.model.FlowUser;

public class FlowUserService extends Service {
	/**
	 * 转换流程处理人
	 * @param oid
	 * @param realname
	 * @return
	 * @throws Exception
	 */
	public FlowUser toUser(String oid,String realname) throws Exception {
		FlowUser model=ModelQueryList.getModel(FlowUser.class, oid,new String[] {"*"});
		if(null==model) {
			if(StringUtil.isSpace(realname)) throw new MException(this.getClass(), "流程处理人不存在");
			model=new FlowUser();
			model.setOid(oid);
			model.setRealname(realname);
			ModelUpdateUtil.insertModel(model);
		}
		return model;
	}
	/**
	 * 转换流程处理人
	 * @param oids
	 * @param realnames
	 * @return
	 * @throws Exception
	 */
	public FlowUser[] toUsers(String[] oids,String[] realnames) throws Exception {
		if(oids.length!=realnames.length) throw new MException(this.getClass(), "参数错误");
		List<FlowUser> list=new ArrayList<FlowUser>();
		for(int i=0;i<oids.length;i++) {
			list.add(toUser(oids[i],realnames[i]));
		}
		return list.toArray(new FlowUser[] {});
	}
}
