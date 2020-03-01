package manage.flow.service;

import java.util.Date;

import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.service.Service;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowBusinessTest;
import manage.flow.model.FlowInstance;
import manage.flow.model.FlowSection;
import manage.model.AdminLogin;

public class FlowBusinessService extends Service implements IFlowService {
	/**
	 * 转换成流程业务信息
	 * @param oid
	 * @param type
	 * @param title
	 * @return
	 * @throws Exception
	 */
	public FlowBusiness toFlowBusiness(AdminLogin admin,String oid,String type,String title,String service) throws Exception {
		if(StringUtil.isSpace(oid)) throw new MException(this.getClass(), "业务主键不能为空");
		FlowBusiness model=ModelQueryList.getModel(FlowBusiness.class, oid,new String[] {"*"});
		if(null==model) {
			model=new FlowBusiness();
			model.setOid(oid);
			model.setType(type);
			model.setTitle(title);
			model.setBusiService(service);
			model.setApplyUser(admin);
			if(null==admin.getOrgGroup()||StringUtil.isSpace(admin.getOrgGroup().getName())) {
				throw new MException(this.getClass(), "当前登录人没有所属部门");
			}
			model.setApplyOrg(admin.getOrgGroup());
			model.setCreateDate(new Date());
			model.setDoneStatus("N");
			ModelCheckUtil.checkNotNull(model, new String[] {"type","title","busiService"});
			ModelUpdateUtil.insertModel(model);
		}else if(null==model.getCurrentSection()||StringUtil.isSpace(model.getCurrentSection().getOid())){
			model.setType(type);
			model.setTitle(title);
			model.setBusiService(service);
			model.setApplyUser(admin);
			if(null==admin.getOrgGroup()||StringUtil.isSpace(admin.getOrgGroup().getName())) {
				throw new MException(this.getClass(), "当前登录人没有所属部门");
			}
			model.setApplyOrg(admin.getOrgGroup());
			ModelUpdateUtil.updateModel(model, new String[] {"type","title","busiService","applyUser.oid","applyOrg.oid"});
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
		if(StringUtil.isSpace(oid)) throw new MException(this.getClass(), "业务主键不能为空");
		FlowBusiness model=ModelQueryList.getModel(FlowBusiness.class, oid,new String[] {"*"});
		if(null==model) {
			throw new MException(this.getClass(), "没有找到流程业务");
		}
		return model;
	}
	/**
	 * 更新流程业务的当前环节
	 * @param oid
	 * @param currentSection
	 * @param currentIndex
	 * @throws MException
	 */
	public void updateCurrentSection(String oid,FlowSection currentSection,Integer currentIndex) throws MException {
		if(null!=currentSection&&!StringUtil.isSpace(currentSection.getOid())) {
			FlowBusiness model=new FlowBusiness();
			model.setOid(oid);
			model.setCurrentSection(currentSection);
			model.setCurrentIndex(currentIndex);
			ModelUpdateUtil.updateModel(model, new String[] {"currentSection.oid","currentIndex"});
		}
	}
	
	public String save(FlowBusinessTest model,AdminLogin admin) throws Exception {
		String msg="";
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin();
			if(StringUtil.isSpace(model.getOid())) {
				model.setOid(GenerateID.generatePrimaryKey());
				model.setApplyUser(admin);
				model.setApplyOrg(admin.getOrgGroup());
				model.setCreateDate(new Date());
				model.setBusiStatus("C");
				ModelUpdateUtil.insertModel(model);
				msg="保存成功";
			}else {
				ModelCheckUtil.equals(model, new String[] {"busiStatus"}, new String[] {"C"}, "只有草稿状态才能修改");
				ModelUpdateUtil.updateModel(model, new String[] {"type","title"});
				msg="修改成功";
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
		return msg;
	}
	@Override
	public String getFlowIdentity() {
		return "flow3";
	}
	@Override
	public void startFlowAfter(FlowInstance ins) throws Exception {
		FlowBusinessTest model=new FlowBusinessTest();
		model.setOid(ins.getBusiness().getOid());
		model.setBusiStatus("S");
		ModelUpdateUtil.updateModel(model, new String[] {"busiStatus"});
	}
	@Override
	public void processFlowAfter(String param,FlowInstance ins) throws Exception {
		if(ins.getDoneStatus().equals("Y")) {
			FlowBusinessTest model=new FlowBusinessTest();
			model.setOid(ins.getBusiness().getOid());
			if(param.equals("Y")) {
				model.setBusiStatus("Y");
			}else {
				model.setBusiStatus("N");
			}
			model.setDoneDate(new Date());
			ModelUpdateUtil.updateModel(model, new String[] {"busiStatus","doneDate"});
		}
	}
}
