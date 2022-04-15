package com.yaoqianshu.engine.service.impl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author moulianchao
 * @date 2022/4/15 16:13
 */
public class Test {

    public static void main(String[] args) {
        Map<String, List<String>> result = new HashMap<>();
        result.put("A", Arrays.asList("C","B"));
        result.put("B", Arrays.asList("C","D"));
        result.put("C", Arrays.asList("D", "E"));
        result.put("D", Arrays.asList("F"));
        result.put("G", Arrays.asList("A"));
        boolean loop = checkLoop(result);
    }

    private static boolean checkLoop(Map<String, List<String>> result) {
        List<String> firstMap = new ArrayList<>();//初始化完的
        Set<String> secondMap = new HashSet<>();//检查完的
        Set<String> keys = result.keySet();
        for (String temp : keys) {
            try {
                initData(temp, firstMap, secondMap, result);
            } catch (Exception e) {
                System.out.println("存在循环依赖");
                break;
            }
        }
        Collections.reverse(firstMap);
        return false;
    }

    private static void initData(String data, List<String> firstMap, Set<String> secondMap, Map<String, List<String>> result) throws Exception {
        System.out.println("初始化" + data);
        if (firstMap.contains(data)) {
            return;
        }
        if (secondMap.contains(data)) {
            throw new Exception();
        }
        secondMap.add(data);
        if (result.get(data) != null) {
            for (String temp : result.get(data)) {
                initData(temp, firstMap, secondMap, result);
            }
        }
        secondMap.remove(data);
        firstMap.add(data);
    }

}
