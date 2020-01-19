package manage.flow.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.service.Service;
import m.system.db.DBManager;
import m.system.db.DataRow;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.flow.model.FlowDefine;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowSectionLink;

public class FlowDefineService extends Service {

	/**
	 * 根据流程标识获取流程（已发布）
	 * @param flowId
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	public FlowDefine getIssue(String flowId) throws SQLException, MException {
		return ModelQueryList.getModel(FlowDefine.class, new String[] {"*","startSection.*"},
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("identity", flowId),
				QueryCondition.eq("issueStatus", "Y")
			})
		);
	}
	public String save(FlowDefine model) throws MException, SQLException {
		ModelCheckUtil.checkNotNull(model, new String[] {"name","identity","version"});
		ModelCheckUtil.checkUniqueCombine(model, new String[]{"identity","version"},"标识已存在");
		if(StringUtil.isSpace(model.getOid())){
			model.setOid(GenerateID.generatePrimaryKey());
			model.setIssueStatus("C");
			ModelUpdateUtil.insertModel(model);
			return "保存成功";
		}else{
			ModelCheckUtil.equals(model, new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态才能修改");
			ModelUpdateUtil.updateModel(model, new String[]{"name","description","startSection.oid"});
			return "修改成功";
		}
	}
	public void issue(FlowDefine model) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(model.getOid());
			ModelCheckUtil.checkNotNull(model, new String[] {"startSection.oid"});
			save(model);
			getService(FlowSectionService.class).verifySection(model.getOid());
			DBManager.executeUpdate("update of_flow_define set issue_status='N' where issue_status='Y' and identity=?",new String[] {model.getIdentity()});
			model.setIssueDate(new Date());
			model.setIssueStatus("Y");
			ModelUpdateUtil.updateModel(model, new String[] {"issueDate","issueStatus"});
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	public void delete(FlowDefine model) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(model.getOid());
			ModelCheckUtil.equals(model, new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态的流程才能删除");
			getService(FlowSectionService.class).deleteAll(model.getOid());
			ModelUpdateUtil.deleteModel(model);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	public FlowDefine saveNew(FlowDefine model) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(model.getOid());
			model=ModelQueryList.getModel(FlowDefine.class, model.getOid(), new String[] {"*"});
			ModelCheckUtil.equals(model, new String[] {"issueStatus"}, new String[] {"Y"}, "只有发布状态的流程才能生成");
			DataRow dr=DBManager.queryFirstRow("select count(oid) num from of_flow_define where issue_status='C' and identity=?",new String[] {model.getIdentity()});
			if(dr.get(Integer.class,"num")>0) throw new MException(this.getClass(),"该流程存在草稿, 不能再次生成!");
			List<FlowSection> fsList=ModelQueryList.getModelList(FlowSection.class, new String[] {"*"}, null, 
				QueryCondition.eq("flowDefine.oid", model.getOid()));
			List<FlowSectionLink> flList=ModelQueryList.getModelList(FlowSectionLink.class, new String[] {"*"}, null,
				QueryCondition.eq("fromSection.flowDefine.oid", model.getOid()));
			Map<String,FlowSection> fsMap=new HashMap<String, FlowSection>();
			for(FlowSection fs : fsList) {
				fsMap.put(fs.getOid(), fs);
				fs.setFlowDefine(model);
				fs.setOid(GenerateID.generatePrimaryKey());
			}
			for(FlowSection fs : fsList) {
				if(null!=fs.getBackSection()&&!StringUtil.isSpace(fs.getBackSection().getOid())) {
					fs.setBackSection(fsMap.get(fs.getBackSection().getOid()));
				}
			}
			for(FlowSectionLink fl : flList) {
				if(null!=fl.getFromSection()&&!StringUtil.isSpace(fl.getFromSection().getOid())) {
					fl.setFromSection(fsMap.get(fl.getFromSection().getOid()));
				}
				if(null!=fl.getToSection()&&!StringUtil.isSpace(fl.getToSection().getOid())) {
					fl.setToSection(fsMap.get(fl.getToSection().getOid()));
				}
				fl.setOid(GenerateID.generatePrimaryKey());
			}
			if(null!=model.getStartSection()&&!StringUtil.isSpace(model.getStartSection().getOid())) {
				model.setStartSection(fsMap.get(model.getStartSection().getOid()));
			}
			dr=DBManager.queryFirstRow("select max(version)+1 version from of_flow_define where issue_status!='C' and identity=?",new String[] {model.getIdentity()});
			model.setOid(GenerateID.generatePrimaryKey());
			model.setIssueDate(null);
			model.setIssueStatus("C");
			model.setVersion(dr.get(Integer.class, "version"));
			ModelUpdateUtil.insertModel(model);
			ModelUpdateUtil.insertModels(fsList.toArray(new FlowSection[] {}));
			ModelUpdateUtil.insertModels(flList.toArray(new FlowSectionLink[] {}));
			tm.commit();
			return model;
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}

}
