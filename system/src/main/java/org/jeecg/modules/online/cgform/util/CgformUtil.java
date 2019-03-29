package org.jeecg.modules.online.cgform.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.jsonschema.CommonProperty;
import org.jeecg.common.jsonschema.JsonSchemaDescrip;
import org.jeecg.common.jsonschema.JsonschemaUtil;
import org.jeecg.common.jsonschema.validate.NumberProperty;
import org.jeecg.common.jsonschema.validate.StringProperty;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.entity.OnlCgformIndex;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecgframework.poi.excel.entity.params.ExcelExportEntity;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CgformUtil {
	
	public static final String SQL_SELECT = "SELECT ";
	public static final String SQL_FROM = " FROM ";
	public static final String SQL_AND = " AND ";
	public static final String SQL_LIMIT = " LIMIT ";
	public static final String SQL_COUNT = " COUNT(*) ";
	public static final String SQL_WHERE_TRUE = " where 1=1  ";
	public static final String SQL_ORDER = " ORDER BY ";
	public static final String SQL_ASC = "asc";
	public static final String SQL_DESC = "desc";
	public static final String SQL_EQ = "=";
	public static final String SQL_GE = ">=";
	public static final String SQL_LE = "<=";
	
	/*** 创建时间字段 */
	public static final String CREATE_TIME = "CREATE_TIME";
	
	/*** 创建人字段 */
	public static final String CREATE_BY="CREATE_BY";
	
	/*** 修改时间字段 */
	public static final String UPDATE_TIME = "UPDATE_TIME";
	
	/*** 修改人字段 */
	public static final String UPDATE_BY="UPDATE_BY";
	
	
	/**
	 * 查询条件有折叠功能，此值表示折叠前要显示几个
	 */
	public static final int  QUERY_FIELD_SHOW_NUM = 2;
	
	/**
	 * 单引号
	 */
	public static final String SQL_SQ = "'";
	/**
	 * 逗号
	 */
	public static final String SQL_COMMA = ",";

	
	/**
 	  * 查询模式 普通查询
	 */
	public static final String QUERY_MODE_SINGLE = "single";
	
	// 字段dbtype
	public static final String NUM_TYPE_INT = "int";
	public static final String NUM_TYPE_DOUBLE = "double";
	public static final String NUM_TYPE_DECIMAL = "BigDecimal";
	
	/**
	  * 主键字段
	 */
	public static final String P_KEY = "id";
	public static final String YES = "1";
	
	
	public static final String SYNC_FORCE="force";
	public static final String SYNC_NORMAL="normal";
	
	
	/**
	  * 拼装查询动态列表数据的sql 根据配置的列只查询对应的列
	 * @param tbname
	 * @param fieldList
	 * @param sb
	 * @return select A1,A2,...An from tbname
	 */
	public static void getAutoListBaseSql(String tbname,List<OnlCgformField> fieldList,StringBuffer sb) {
		sb.append(SQL_SELECT);
		int size = fieldList.size();
		for(int i=0;i<size;i++) {
			OnlCgformField item = fieldList.get(i);
			if(i==size-1) {
				sb.append(item.getDbFieldName()+" ");
			}else {
				sb.append(item.getDbFieldName()+SQL_COMMA);
			}
		}
		sb.append(SQL_FROM+tbname);
	}
	
	/**
	  * 拼装条件
	 * @param params 前端传过来的查询条件
	 * @param fieldList
	 * @param sb
	 * @return and A1=1 and A2=2 ... and An=n
	 */
	public static String getAutoListConditionSql(List<OnlCgformField> fieldList,Map<String,Object> params) {
		StringBuffer sb = new StringBuffer();
		for (OnlCgformField item : fieldList) {
			//1.判断是否查询
			if(1==item.getIsQuery()) {
				String field = item.getDbFieldName();
				String dbtype = item.getDbType();
				//2.判断是否是简单查询
				if(QUERY_MODE_SINGLE.equals(item.getQueryMode())) {
					Object value = params.get(field);
					if(value!=null) {
						sb.append(SQL_AND+field+SQL_EQ);
						if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
							sb.append(value.toString());
						}else {
							sb.append(SQL_SQ+value.toString()+SQL_SQ);
						}
					}
				}else {
					//范围查询走此逻辑
					Object value_begin =  params.get(field+"_begin");
					if(value_begin!=null) {
						sb.append(SQL_AND+field+SQL_GE);
						if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
							//如果数字类型直接拼在SQL后面
							sb.append(value_begin.toString());
						}else {
							//如果字符串类型需要加单引号
							sb.append(SQL_SQ+value_begin.toString()+SQL_SQ);
						}
					}
					Object value_end =  params.get(field+"_end");
					if(value_end!=null) {
						sb.append(SQL_AND+field+SQL_LE);
						if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
							sb.append(value_begin.toString());
						}else {
							sb.append(SQL_SQ+value_begin.toString()+SQL_SQ);
						}
					}
				}
			}
		}
		return sb.toString();
	}

	
	public static Map<String, Object> getParameterMap(HttpServletRequest request) {
		// 参数Map
		Map<?, ?> properties = request.getParameterMap();
		// 返回值Map 
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Iterator<?> entries = properties.entrySet().iterator();
		
		Map.Entry<String, Object> entry;
		String name = "";
		String value = "";
		Object valueObj =null;
		while (entries.hasNext()) {
			entry = (Map.Entry<String, Object>) entries.next();
			name = (String) entry.getKey();
			valueObj = entry.getValue();
			if (null == valueObj) {
				value = "";
			} else if (valueObj instanceof String[]) {
				String[] values = (String[]) valueObj;
				for (int i = 0; i < values.length; i++) {
					value = values[i] + ",";
				}
				value = value.substring(0, value.length() - 1);
			} else {
				value = valueObj.toString();
			}
			returnMap.put(name, value);
		} 
		return returnMap;
	}
	
	/**
	 * 根据字段集合组装JSON schema
	 * @param fieldList
	 * @return
	 */
	public static JSONObject getJsonSchemaByCgformFieldList(List<OnlCgformField> fieldList) {
		JSONObject json = new JSONObject();
		List<String> required = new ArrayList<String>();
		List<CommonProperty> props = new ArrayList<CommonProperty>();
		ISysDictService sysDictService = SpringContextUtils.getBean(ISysDictService.class);
		for (OnlCgformField item : fieldList) {
			String field = item.getDbFieldName();
			if(P_KEY.equals(field)) {
				continue;
			}
			String title = item.getDbFieldTxt();
			//获取required
			if(YES.equals(item.getFieldMustInput())) {
				required.add(field);
			}
			String dbType = item.getDbType();
			String filedShowType = item.getFieldShowType();
			
			//TODO 此处只处理数据字典 没处理字典表
			String dictCode = item.getDictField();
			CommonProperty prop = null;
			if(NUM_TYPE_INT.equals(dbType)) {
				prop = new NumberProperty(field, title, "integer");
			}else if(NUM_TYPE_DOUBLE.equals(dbType)){
				prop = new NumberProperty(field, title, "number");
			}else {
				if("list,radio,checkbox".indexOf(filedShowType)>=0) {
					List<Map<String,Object>> include = sysDictService.queryDictItemsByCode(dictCode);
					prop = new StringProperty(field, title, filedShowType, item.getDbLength(), include);
				}else {
					prop = new StringProperty(field, title, filedShowType, item.getDbLength());
				}
			}
			prop.setOrder(item.getOrderNum());
			props.add(prop);
		}
		if(required.size()>0) {
			JsonSchemaDescrip descrip = new JsonSchemaDescrip(required);
			json = JsonschemaUtil.getJsonSchema(descrip, props);
		}else {
			JsonSchemaDescrip descrip = new JsonSchemaDescrip();
			json = JsonschemaUtil.getJsonSchema(descrip, props);
		}
		return json;
	}
	
	/**
	 * 表单数据保存SQL获取 默认id是主键并且为uuid
	 */
	public static String getFormDataSaveSql(String tbname,List<OnlCgformField> fieldList,JSONObject json) {
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		List<String> sysFields = Lists.newArrayList(CREATE_BY,CREATE_TIME);
		for (OnlCgformField item : fieldList) {
			String key = item.getDbFieldName();
			String dbType = item.getDbType();
			if(null==key) {
				log.info("--------online保存表单数据遇见空字段------->>"+item.getId());
				continue;
			}
			if(P_KEY.equals(key)) {
				continue;
			}
			if(json.get(key)==null) {
				continue;
			}
			
			if("".equals(json.get(key))) {
				if(NUM_TYPE_INT.equals(dbType)
						||NUM_TYPE_DOUBLE.equals(dbType)
						||NUM_TYPE_DECIMAL.equals(dbType)
						||dbType.toLowerCase().indexOf("date")>=0) {
					continue;
				}
			}
			
			if(CREATE_BY.equals(key.toUpperCase()) || CREATE_TIME.equals(key.toUpperCase())) {
				sysFields.remove(key.toUpperCase());
			}
			
			//拼接insert into (这里面的东西)
			sb1.append(SQL_COMMA+key);
			
			//拼接insert into (...) values (这里面的东西) 
			switch (dbType) {
			case NUM_TYPE_INT:
				sb2.append(SQL_COMMA+json.getIntValue(key));
				break;
			case NUM_TYPE_DOUBLE:
				sb2.append(SQL_COMMA+json.getDoubleValue(key));
				break;
			case NUM_TYPE_DECIMAL:
				sb2.append(SQL_COMMA+json.getBigDecimal(key));
				break;
			default:
				sb2.append(SQL_COMMA+SQL_SQ+json.getString(key)+SQL_SQ);
				break;
			}
			
		}
		
		String uuid = UUID.randomUUID().toString().replace("-","");
		
		Map<String,String> map = initSystermFieldsForAdd(sysFields);
		String sql = "insert into "+tbname+"("+P_KEY+sb1.toString()+map.get("sys_fields")+") values("+SQL_SQ+uuid+SQL_SQ+sb2.toString()+map.get("sys_def_val")+")";
		log.info("--动态表单保存sql-->"+sql);
		return sql;
	}
	
	/**
	 * 表单数据修改SQL获取 默认id是主键并且为uuid
	 */
	public static String getFormDataEditSql(String tbname,List<OnlCgformField> fieldList,JSONObject json) {
		StringBuffer sb = new StringBuffer();
		int size = fieldList.size();
		List<String> sysFields = Lists.newArrayList(UPDATE_BY,UPDATE_TIME);
		for(int i=0;i<size;i++) {
			OnlCgformField item  = fieldList.get(i);
			String key = item.getDbFieldName();
			String dbType = item.getDbType();
			if(null==key) {
				log.info("--------online修改表单数据遇见空字段------->>"+item.getId());
				continue;
			}
			if(P_KEY.equals(key)) {
				continue;
			}
			if(json.get(key)==null) {
				continue;
			}
			
			if("".equals(json.get(key))) {
				if(NUM_TYPE_INT.equals(dbType)
						||NUM_TYPE_DOUBLE.equals(dbType)
						||NUM_TYPE_DECIMAL.equals(dbType)
						||dbType.toLowerCase().indexOf("date")>=0) {
					continue;
				}
			}
			
			if(UPDATE_BY.equals(key.toUpperCase()) || UPDATE_TIME.equals(key.toUpperCase())) {
				sysFields.remove(key.toUpperCase());
			}
			sb.append(key+SQL_EQ);
			switch (dbType) {
			case NUM_TYPE_INT:
				sb.append(json.getIntValue(key));
				break;
			case NUM_TYPE_DOUBLE:
				sb.append(json.getDoubleValue(key));
				break;
			case NUM_TYPE_DECIMAL:
				sb.append(json.getBigDecimal(key));
				break;
			default:
				sb.append(SQL_SQ+json.getString(key)+SQL_SQ);
				break;
			}
			if(size>i+1) {
				sb.append(SQL_COMMA);
			}
		}
		String condition = sb.toString();
		if(condition.endsWith(SQL_COMMA)) {
			condition = condition.substring(0,condition.length()-1);
		}
		String sysFieldsInit = initSystermFieldsForUpdate(sysFields);
		String sql = "update "+tbname+" set "+condition+sysFieldsInit+ SQL_WHERE_TRUE +SQL_AND + P_KEY + SQL_EQ +"'"+json.getString(P_KEY)+"'";
		log.info("--动态表单编辑sql-->"+sql);
		return sql;
	}
	
	/**
	 * 获取根据id查询的sql 
	 * @param tbname
	 * @param fieldList
	 * @return
	 */
	public static String getSelectFormSql(String tbname,List<OnlCgformField> fieldList,String id) {
		StringBuffer sb = new StringBuffer();
		//TODO  这边暂时将id也返回给前端了
		sb.append(SQL_SELECT);
		int size = fieldList.size();
		for(int i=0;i<size;i++) {
			String key = fieldList.get(i).getDbFieldName();
			sb.append(key);
			if(size>i+1) {
				sb.append(SQL_COMMA);
			}
		}
		sb.append(SQL_FROM+tbname+SQL_WHERE_TRUE + SQL_AND + P_KEY + SQL_EQ+"'"+id+"'");
		return sb.toString();
	}
	
	/**
	  * 获取系统字段的设置默认值
	 * @param isUpdate
	 * @return sys_fields sys_def_val
	 */
	public static Map<String,String> initSystermFieldsForAdd(List<String> fields){
		Map<String,String> map = new HashMap<>();
		String username = "jeecg-boot-online";
		SysUser sysUser= (SysUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			username = sysUser.getUsername();
		}
		String date = DateUtils.formatDateTime();
		String sys_fields="";
		String sys_def_val="";
		for (String string : fields) {
			if(CREATE_TIME.equals(string)) {
				sys_fields +=","+string;
				sys_def_val+=",'"+date+"'";
			}else if(CREATE_BY.equals(string)) {
				sys_fields +=","+string;
				sys_def_val+=",'"+username+"'";
			}
		}
		map.put("sys_fields", sys_fields);
		map.put("sys_def_val", sys_def_val);
		return map;
	}
	
	public static String initSystermFieldsForUpdate(List<String> fields) {
		String sql = "";
		Map<String,String> map = new HashMap<>();
		String username = "jeecg-boot-online";
		SysUser sysUser= (SysUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			username = sysUser.getUsername();
		}
		String date = DateUtils.formatDateTime();
		for (String string : fields) {
			if(UPDATE_TIME.equals(string)) {
				sql+=SQL_COMMA+UPDATE_TIME+SQL_EQ+SQL_SQ+date+SQL_SQ;
			}else if(UPDATE_BY.equals(string)) {
				sql+=SQL_COMMA+UPDATE_BY+SQL_EQ+SQL_SQ+username+SQL_SQ;
			}
		}
		return sql;
	}
	
	public static boolean compareValue(Object oldvalue,Object newvalue){
		if(oldvalue==null){
			if(newvalue!=null){
				return false;
			}
		}else{
			if(newvalue==null){
				return false;
			}else{
				if(!oldvalue.equals(newvalue)){
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean databaseFieldIsChange(OnlCgformField oldColumn,OnlCgformField newColumn) {
		if (!compareValue(oldColumn.getDbFieldName(), newColumn.getDbFieldName())
				|| !compareValue(oldColumn.getDbFieldTxt(),newColumn.getDbFieldTxt())
				|| !compareValue(oldColumn.getDbLength(),newColumn.getDbLength())
				|| !compareValue(oldColumn.getDbPointLength(),newColumn.getDbPointLength())
				|| !compareValue(oldColumn.getDbType(),newColumn.getDbType())
				|| !compareValue(oldColumn.getDbIsNull(),newColumn.getDbIsNull())
				//|| !compareValue(oldColumn.getOrderNum(),newColumn.getOrderNum())
				|| !compareValue(oldColumn.getDbIsKey(),newColumn.getDbIsKey())
				|| !compareValue(oldColumn.getMainTable(),newColumn.getMainTable())
				|| !compareValue(oldColumn.getMainField(),newColumn.getMainField())
				||!compareValue(oldColumn.getDbDefaultVal(),newColumn.getDbDefaultVal())) {
			return true;
		}
		return false;
	}
	
	public static boolean databaseIndexIsChange(OnlCgformIndex oldIndex,OnlCgformIndex newIndex) {
		if (!compareValue(oldIndex.getIndexName(), newIndex.getIndexName())
				|| !compareValue(oldIndex.getIndexField(),newIndex.getIndexField())
				|| !compareValue(oldIndex.getIndexType(),newIndex.getIndexType())) {
			return true;
		}
		return false;
	}
	
	public static boolean databaseTableIsChange(OnlCgformHead oldTable,OnlCgformHead newTable) {
		if (!compareValue(oldTable.getTableName(), newTable.getTableName()) || !compareValue(oldTable.getTableTxt(), newTable.getTableTxt())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取查询列表的条件SQL
	 * @param fieldList
	 * @param params
	 * @return
	 */
	public static String getQueryListDataCondition(String tbname,List<OnlCgformField> fieldList,Map<String, Object> params) {
		
		for (String key : params.keySet()) {
			System.out.println(key+"--"+params.get(key));
		}
		StringBuffer sb = new StringBuffer();
		StringBuffer selectField = new StringBuffer();
		for (OnlCgformField item : fieldList) {
			String field = item.getDbFieldName();
			String dbtype = item.getDbType();
			if(item.getIsShowList()==1) {
				selectField.append(SQL_COMMA+field);
			}
			if(item.getIsQuery()==1) {
				if(QUERY_MODE_SINGLE.equals(item.getQueryMode())) {
					//单条件查询
					if(params.get(field)==null) {
						continue;
					}
					sb.append(SQL_AND+field+SQL_EQ);
					if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
						sb.append(params.get(field).toString());
					}else{
						sb.append(SQL_SQ+params.get(field).toString()+SQL_SQ);
					}
				}else {
					Object beginVal = params.get(field+"_begin");
					if(beginVal!=null) {
						sb.append(SQL_AND+field+SQL_GE);
						if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
							sb.append(beginVal.toString());
						}else{
							sb.append(SQL_SQ+beginVal.toString()+SQL_SQ);
						}
					}
					Object endVal = params.get(field+"_end");
					if(endVal!=null) {
						sb.append(SQL_AND+field+SQL_GE);
						if(NUM_TYPE_INT.equals(dbtype)||NUM_TYPE_DOUBLE.equals(dbtype)||NUM_TYPE_DECIMAL.equals(dbtype)) {
							sb.append(endVal.toString());
						}else{
							sb.append(SQL_SQ+endVal.toString()+SQL_SQ);
						}
					}
					
				}
			}
		}
		String where = sb.toString();
		String sql = SQL_SELECT+selectField.toString().substring(1)+SQL_FROM+tbname+SQL_WHERE_TRUE+where;
		return sql;
	}
	
	/**
	  * 把基础数据转换成Excel导出的数据
	 * @param lists 列集合
	 * @param pkField 如果pkField为null 则不过滤主键
	 * @return
	 */
	public static List<ExcelExportEntity> convertToExportEntity(List<OnlCgformField> lists,String pkField) {
		List<ExcelExportEntity> entityList = new ArrayList<ExcelExportEntity>();
		for (int i = 0; i < lists.size(); i++) {
			if(null!=pkField && pkField.equals(lists.get(i).getDbFieldName())) {
				continue;
			}
			if (lists.get(i).getIsShowList()==1) {
				ExcelExportEntity entity = new ExcelExportEntity(lists.get(i).getDbFieldTxt(), lists.get(i).getDbFieldName());
				int columnWidth = lists.get(i).getDbLength() == 0 ? 12 : lists.get(i).getDbLength() > 30 ? 30 : lists.get(i).getDbLength();
				if (lists.get(i).getFieldShowType().equals("date")) {
					entity.setFormat("yyyy-MM-dd");
				} else if (lists.get(i).getFieldShowType().equals("datetime")) {
					entity.setFormat("yyyy-MM-dd HH:mm:ss");
				}
				entity.setWidth(columnWidth);
				entityList.add(entity);
			}
		}
		return entityList;
	}
			

}
