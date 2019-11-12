package manage.util.page.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import manage.util.page.viewui.AlertMeta.AlertType;

@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.METHOD})//定义注解的作用目标**作用范围字段、枚举的常量/方法  
@Documented//说明该注解将被包含在javadoc中
public @interface FormAlertMeta {
	/**
	 * 类型 
	 * @return
	 */
	AlertType type() default AlertType.INFO;
	/**
	 * 自定义图标 
	 * @return
	 */
	String icon() default "";
	/**
	 * 标题  支持变量 #{变量名}
	 * @return
	 */
	String title();
	/**
	 * 内容  支持变量 #{变量名}
	 * @return
	 */
	String desc() default "";
}
