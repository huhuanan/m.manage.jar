package manage.flow.service;

import manage.flow.model.FlowInstance;

public interface IFlowService {
	/**
	 * 获取该服务类对应的流程标识
	 * @return
	 */
	public String getFlowIdentity();
	/**
	 * 开始流程后执行
	 * @param ins 流程实例
	 * @throws Exception 抛异常回滚流程
	 */
	public void startFlowAfter(FlowInstance ins) throws Exception;
	/**
	 * 处理流程后执行
	 * @param param 参数
	 * @param ins 流程实例
	 * @throws Exception 抛异常回滚流程
	 */
	public void processFlowAfter(String param,FlowInstance ins) throws Exception;
}
