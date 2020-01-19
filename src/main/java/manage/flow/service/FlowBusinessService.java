package manage.flow.service;

import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.service.Service;
import m.system.exception.MException;
import m.system.util.StringUtil;
import manage.flow.model.FlowBusiness;

public class FlowBusinessService extends Service {
	/**
	 * 转换成流程业务信息
	 * @param oid
	 * @param type
	 * @param title
	 * @return
	 * @throws Exception
	 */
	public FlowBusiness toFlowBusiness(String oid,String type,String title) throws Exception {
		FlowBusiness model=ModelQueryList.getModel(FlowBusiness.class, oid,new String[] {"*"});
		if(null==model) {
			if(StringUtil.isSpace(type)||StringUtil.isSpace(title)) 
				throw new MException(this.getClass(), "业务类型和业务标题不能为空");
			model=new FlowBusiness();
			model.setOid(oid);
			model.setType(type);
			model.setTitle(title);
			ModelUpdateUtil.insertModel(model);
		}
		return model;
	}
	/**
	 * 获取流程业务，找不到报错
	 * @param oid
	 * @return
	 * @throws Exception
	 */
	public FlowBusiness toFlowBusiness(String oid) throws Exception {
		FlowBusiness model=ModelQueryList.getModel(FlowBusiness.class, oid,new String[] {"*"});
		if(null==model) {
			throw new MException(this.getClass(), "没有找到流程业务");
		}
		return model;
	}
}
