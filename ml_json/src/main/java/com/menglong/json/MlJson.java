package com.menglong.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.util.List;
import java.util.Map;

public class MlJson {
	/**
	 * @param jackson：json串
	 * @param clazz：实体对象
	 * @return：将Json转换为实体对象，如果Json串中的某个Key在实体对象中不存在，不会转换
	 */
	public static Object parseStringToObject(String jackson, Class clazz) {
		if(jackson!=null && !"".equals(jackson)){
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.getDeserializationConfig().set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				return objectMapper.readValue(jackson, clazz);
			} catch (Exception e) {
				throw new RuntimeException("parse jackson to object failed!", e);
			}
		}
		return null;
	}
	/**
	 * 
	 * @param strJson：json串
	 * @param valueTypeRef：要转换的自定义对象List。如new TypeReference<List<Category>>(){}
	 * @return：将Json转换为实体对象List，如果Json串中的某个Key在实体对象中不存在，不会转换
	 */
	public static <T> List<T> parseStringToListObject(String strJson, TypeReference valueTypeRef) {
		if(strJson!=null && !"".equals(strJson)){
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.getDeserializationConfig().set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				return objectMapper.readValue(strJson, valueTypeRef);
			} catch (Exception e) {
				throw new RuntimeException("parse jackson to list object failed!", e);
			}
		}
		return null;
	}
	
	public static Map<String, String> paeseStringToMap(String strJson){
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, String> map = mapper.readValue(strJson, Map.class);
			return map;
		} catch (Exception e) {
			throw new RuntimeException("parse jackson to map failed!", e);
		}
	}
	/**
	 * 将输入得Object转换为一个轻量级Json输出，为NULL的属性不序列化
	 * @param obj
	 * @return string
	 */
	public static String parseObjectToLightString(Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getSerializationConfig().set(org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		//objectMapper.getSerializationConfig().set(org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, false);
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException("parse jackson to object failed!", e);
		}
	}

}