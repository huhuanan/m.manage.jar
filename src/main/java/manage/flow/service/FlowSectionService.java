package manage.flow.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.service.Service;
import m.system.db.SqlBuffer;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.flow.model.FlowDefine;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowSectionLink;

public class FlowSectionService extends Service {

	/**
	 * 获取下一环节
	 * @param defineOid 流程oid
	 * @param secId 环节标识
	 * @param isNext 下一步
	 * @param toParam 参数
	 * @return
	 * @throws MException
	 * @throws SQLException
	 */
	public FlowSection getNext(String defineOid,String secId,String isNext,String toParam) throws MException, SQLException {
		FlowSectionLink link=ModelQueryList.getModel(FlowSectionLink.class, new String[] {"toSection.*"},
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("fromSection.flowDefine.oid", defineOid),
				QueryCondition.eq("fromSection.identity", secId),
				QueryCondition.eq("isNext", isNext),
				QueryCondition.eq("toParam", toParam)
			})
		);
		if(null==link) throw new MException(this.getClass(), "下一步参数错误");
		if(null!=link.getToSection()&&!StringUtil.isSpace(link.getToSection().getOid())) {
			return link.getToSection();
		}else {
			return null;
		}
	}
	
	public String save(FlowSection model) throws Exception {
		String msg="";
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin();
			ModelCheckUtil.checkNotNull(model, new String[] {"name","identity","countersign","forwardable","flowDefine.oid"});
			ModelCheckUtil.checkUniqueCombine(model, new String[]{"identity","flowDefine.oid"},"标识已存在");
			ModelCheckUtil.equals(model.getFlowDefine(), new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态的流程才能编辑");
			if(model.getCountersign().equals("Y")) {
				ModelCheckUtil.checkNotNull(model, new String[] {"backSection.oid"});
			}
			if(StringUtil.isSpace(model.getOid())){
				model.setOid(GenerateID.generatePrimaryKey());
				ModelUpdateUtil.insertModel(model);
				FlowSectionLink link=new FlowSectionLink();
				link.setOid(GenerateID.generatePrimaryKey());
				link.setFromSection(model);
				link.setIsNext("N");
				link.setToParam("");
				ModelUpdateUtil.insertModel(link);
				msg="保存成功";
			}else{
				ModelUpdateUtil.updateModel(model, new String[]{"identity","name","sort","countersign","forwardable","backSection.oid","flowDefine.oid"});
				msg="修改成功";
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
		return msg;
	}
	public void deleteAll(String flowDefineOid) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin();
			List<FlowSection> list=ModelQueryList.getModelList(FlowSection.class, new String[] {"oid"}, null,
				QueryCondition.eq("flowDefine.oid", flowDefineOid));
			for(FlowSection fs : list) {
				delete(fs);
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	public void delete(FlowSection model) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin();
			model=ModelQueryList.getModel(model, new String[] {"*"});
			ModelCheckUtil.equals(model.getFlowDefine(), new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态的流程才能编辑");
			SqlBuffer sql=new SqlBuffer();
			sql.append("delete from of_flow_section_link where from_section_oid=?",model.getOid());
			sql.executeUpdate();
			ModelUpdateUtil.deleteModel(model);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	public String saveNext(FlowSectionLink link) throws Exception {
		ModelCheckUtil.checkNotNull(link, new String[] {"isNext","fromSection.oid"});
		if(link.getIsNext().equals("Y")) {
			ModelCheckUtil.checkNotNull(link, new String[] {"toSection.oid"});
			if(link.getFromSection().getOid().equals(link.getToSection().getOid())) 
				throw new MException(this.getClass(),"环节链接不能链接自己");
			ModelCheckUtil.checkUniqueCombine(link, new String[] {"fromSection.oid","isNext","toSection.oid","toParam"}, "该环节链接已存在");
		}else {
			link.setToSection(null);
			ModelCheckUtil.checkUniqueCombine(link, new String[] {"fromSection.oid","isNext","toParam"}, "该环节结束链接已存在");
		}
		link.setFromSection(ModelQueryList.getModel(link.getFromSection(), new String[] {"*"}));
		ModelCheckUtil.equals(link.getFromSection().getFlowDefine(), new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态的流程才能编辑");
		
		if(StringUtil.isSpace(link.getOid())){
			link.setOid(GenerateID.generatePrimaryKey());
			ModelUpdateUtil.insertModel(link);
			return "保存成功";
		}else {
			ModelUpdateUtil.updateModel(link,new String[] {"isNext","toSection.oid","toParam","toDesc"});
			return "修改成功";
		}
	}
	public void deleteNext(FlowSectionLink link) throws Exception {
		link=ModelQueryList.getModel(link, new String[] {"*","fromSection.flowDefine.oid"});
		ModelCheckUtil.equals(link.getFromSection().getFlowDefine(), new String[] {"issueStatus"}, new String[] {"C"}, "只有草稿状态的流程才能编辑");
		SqlBuffer sql=new SqlBuffer();
		sql.append("select count(oid) num from of_flow_section_link where from_section_oid=?",link.getFromSection().getOid());
		int num=sql.queryFirstRow().get(Integer.class,"num");
		if(num>1) {
			ModelUpdateUtil.deleteModel(link);
		}else {
			throw new MException(this.getClass(), "该环节的最后一条链接不允许删除");
		}
	}

	public void verifySection(String flowDefineOid) throws Exception {
		FlowDefine define=ModelQueryList.getModel(FlowDefine.class,flowDefineOid, new String[] {"*"});
		if(null==define.getStartSection()||StringUtil.isSpace(define.getStartSection().getOid()))
			throw new MException(this.getClass(), "未设置开始环节");
		Map<String,Boolean> fsMap=new HashMap<String,Boolean>();
		List<FlowSection> fsList=ModelQueryList.getModelList(FlowSection.class, new String[] {"*"}, null, 
			QueryCondition.eq("flowDefine.oid", flowDefineOid));
		List<FlowSectionLink> flList=ModelQueryList.getModelList(FlowSectionLink.class, new String[] {"*"}, null,
			QueryCondition.eq("fromSection.flowDefine.oid", flowDefineOid)); 
		for(FlowSection fs : fsList) {
			fsMap.put(fs.getOid(), false);
			if(fs.getOid().equals(define.getStartSection().getOid())) {
				fsMap.put(fs.getOid(), true);
			}else {
				for(FlowSectionLink fl : flList) {
					if(null!=fl.getToSection()&&StringUtil.noSpace(fl.getToSection().getOid()).equals(fs.getOid())) {
						fsMap.put(fs.getOid(), true);
						break;
					}
				}
			}
		}
		for(Boolean b : fsMap.values()) {
			if(!b) throw new MException(this.getClass(), "存在未关联的环节");
		}
		
	}
}
