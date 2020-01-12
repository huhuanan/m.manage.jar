package manage.util.page.table;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import manage.util.page.button.ButtonMeta;
import manage.util.page.button.DropButtonMeta;
import manage.util.page.query.QueryMeta;
import manage.util.page.viewui.AlertMeta;
import manage.util.page.viewui.ViewUIMeta;

@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.METHOD})//定义注解的作用目标**作用范围字段、枚举的常量/方法  
@Documented//说明该注解将被包含在javadoc中 
public @interface ActionTableMeta {
	/**
	 * 标题, 菜单打开不显示, 其他打开方式存在则显示
	 * @return
	 */
	String title() default "";
	/**
	 * alert组件
	 * @return
	 */
	AlertMeta alert() default @AlertMeta(title="");
	/**
	 * html标签段
	 * @return
	 */
	ViewUIMeta[] viewui() default {};
	/**
	 * 默认隐藏的列
	 * @return
	 */
	String[] hiddenCols() default {};
	/**
	 * 扩展行  需要有扩展列
	 * @return
	 */
	TableExpandRow expandRow() default @TableExpandRow();
	/**
	 * 模型类全名
	 * @return
	 */
	String modelClass();
	/**
	 * 数据接口地址
	 * @return
	 */
	String dataUrl();
	/**
	 * table的高度
	 * @return
	 */
	int tableHeight() default 0;
	/**
	 * 混合查询属性
	 * @return
	 */
	String searchField() default "";
	/**
	 * 混合查询提示
	 * @return
	 */
	String searchHint() default "";
	/**
	 * 合并行开始列索引
	 * @return
	 */
	int rowspanIndex() default -1;
	/**
	 * 合并行总列数
	 * @return
	 */
	int rowspanNum() default 0;
	/**
	 * 排序[]  ["field desc","field asc"]
	 * @return
	 */
	String[] orders() default {};
	/**
	 * 每条数据已card样式展示
	 * @return
	 */
	boolean cardMode() default false;
	/**
	 * card列数
	 * @return
	 */
	int cardColNum() default 5;
	/**
	 * card的html代码   支持参数 {{row['列field字段']}} row是当前行  支持插槽 #{slot:列field字段}
	 * @return
	 */
	String cardHtml() default "";
	/**
	 * 列描述
	 * @return
	 */
	ActionTableColMeta[] cols();
	/**
	 * 按钮
	 * @return
	 */
	ButtonMeta[] buttons() default {};
	/**
	 * 下拉按钮组
	 * @return
	 */
	DropButtonMeta[] dropButtons() default {};
	/**
	 * 查询条件
	 * @return
	 */
	QueryMeta[] querys() default {};
}
