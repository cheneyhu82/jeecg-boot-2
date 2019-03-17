package org.jeecg.modules.online.cgreport.entity;

import java.io.Serializable;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @Description: 在线报表配置
 * @author: jeecg-boot
 * @date: 2019-03-08
 * @version: V1.0
 */
@Data
@TableName("onl_cgreport_head")
public class OnlCgreportHead implements Serializable {
	private static final long serialVersionUID = 1L;

	/** id */
	@TableId(type = IdType.UUID)
	private String id;
	/** 报表编码 */
	private String code;
	/** 报表名字 */
	private String name;
	/** 报表SQL */
	private String cgrSql;
	/** 返回值字段 */
	private String returnValField;
	/** 返回文本字段 */
	private String returnTxtField;
	/** 返回类型，单选或多选 */
	private String returnType;
	/** 动态数据源 */
	private String dbSource;
	/** 描述 */
	private String content;
	/** 修改时间 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/** 修改人id */
	private String updateBy;
	/** 创建时间 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/** 创建人id */
	private String createBy;

}
