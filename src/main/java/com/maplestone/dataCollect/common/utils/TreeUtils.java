package com.maplestone.dataCollect.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 原始数组数据 转树型结构
 * @Author hmx
 * @CreateTime 2020-11-09 17:11
 */

public class TreeUtils {

    public static void main(String[] args) {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("pid", 0);
        map.put("name", "甘肃省");
        data.add(map);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", 2);
        map2.put("pid", 1);
        map2.put("name", "天水市");
        data.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("id", 3);
        map3.put("pid", 2);
        map3.put("name", "秦州区");
        data.add(map3);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("id", 4);
        map4.put("pid", 0);
        map4.put("name", "北京市");
        data.add(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("id", 5);
        map5.put("pid", 4);
        map5.put("name", "昌平区");
        data.add(map5);
        System.out.println(JSON.toJSONString(data));
        JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(data)), "id", "pid", "children");
        System.out.println(JSON.toJSONString(result));
    }

    /**
     * 将原始数组数据 转树型结构
     * 
     * @param arr   原始数据数组
     * @param id    id字段
     * @param pid   父id字段
     * @param child 子节点字段
     * @return
     */
    public static JSONArray listToTree(JSONArray arr, String id, String pid, String child) {
        JSONArray r = new JSONArray();
        JSONObject hash = new JSONObject();
        // 将数组转为Object的形式，key为数组中的id
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = (JSONObject) arr.get(i);
            hash.put(json.getString(id), json);
        }
        // 遍历结果集
        for (int j = 0; j < arr.size(); j++) {
            // 单条记录
            JSONObject aVal = (JSONObject) arr.get(j);
            // 在hash中取出key为单条记录中pid的值
            JSONObject hashVP = (JSONObject) hash.get(aVal.get(pid).toString());
            // 如果记录的pid存在，则说明它有父节点，将她添加到孩子节点的集合中
            if (hashVP != null) {
                // 检查是否有child属性
                if (hashVP.get(child) != null) {
                    JSONArray ch = (JSONArray) hashVP.get(child);
                    ch.add(aVal);
                    hashVP.put(child, ch);
                } else {
                    JSONArray ch = new JSONArray();
                    ch.add(aVal);
                    hashVP.put(child, ch);
                }
            } else {
                r.add(aVal);
            }
        }
        return r;
    }

    /**
     * 获取目录结构中最后一个层级的id
     * 
     * @param lastIds
     * @param jsonArray
     * @return
     */
    public static List<String> getTreeLastId(List<String> lastIds, JSONArray jsonArray) {
        JSONArray lastArray = new JSONArray();
        lastArray = getTreeLast(lastArray, jsonArray);
        for (int i = 0; i < lastArray.size(); i++) {
            JSONObject jsonObject = lastArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            lastIds.add(id);
        }
        return lastIds;
    }

    /**
     * 获取最后一级
     * 
     * @param lastArray
     * @param jsonArray
     * @return
     */
    public static JSONArray getTreeLast(JSONArray lastArray, JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.get("children") == null) {
                lastArray.add(jsonObject);
            } else {
                JSONArray childrenArr = (JSONArray) jsonObject.get("children");
                getTreeLast(lastArray, childrenArr);
            }
        }
        return lastArray;
    }

    /**
     * 获取结构中所有的id
     * 
     * @param ids
     * @param jsonArray
     * @return
     */
    public static List<String> getTreeAllId(List<String> ids, JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            ids.add(id);
            if (jsonObject.get("children") != null) {
                JSONArray childrenArr = (JSONArray) jsonObject.get("children");
                getTreeAllId(ids, childrenArr);
            }
        }
        return ids;
    }

    /**
     * 从某一个层级开始找最底下的层级Id
     * 
     * @param lastIds
     * @param jsonArray
     * @return
     */
    public static List<String> getTreeLastIdByLevel(List<String> lastIds, JSONArray jsonArray, String parentId,
            String parent) {
        JSONArray objects = new JSONArray();
        objects = getTreeByLevel(objects, jsonArray, parentId, parent);
        getTreeLastId(lastIds, objects);
        return lastIds;
    }

    /**
     * 获取某一个层级下所有的id
     * 
     * @param ids
     * @param jsonArray
     * @param parentId
     * @param parent
     * @return
     */
    public static List<String> getTreeAllIdByLevel(List<String> ids, JSONArray jsonArray, String parentId,
            String parent) {
        JSONArray objects = new JSONArray();
        objects = getTreeByLevel(objects, jsonArray, parentId, parent);
        getTreeAllId(ids, objects);
        return ids;
    }

    public static JSONArray getTreeByLevel(JSONArray objects, JSONArray jsonArray, String parentId, String parent) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String pid = jsonObject.getString(parent);
            if (pid.equals(parentId)) {
                objects.add(jsonObject);
            } else {
                if (jsonObject.get("children") != null) {
                    JSONArray childrenArr = (JSONArray) jsonObject.get("children");
                    getTreeByLevel(objects, childrenArr, parentId, parent);
                }
            }
        }
        return objects;
    }

}
