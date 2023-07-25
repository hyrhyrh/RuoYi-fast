package com.ruoyi.project.monitor.job.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapDemo {
    public static void main(String[] args) {
        HashMap hashMap = new HashMap();
        hashMap.put("key1", "value1");
        hashMap.put("key2", "value2");
        hashMap.put("key3", "value3");
        hashMap.put("key4", "value4");
        /*1、先用keySet()取出所有key值，再取出对应value--增强for循环遍历*/
        System.out.println("====1、先用 hashMap.keySet() 方法取出所有的 key 的集合，再用增强循环 for 遍历====");
        Set keyset = hashMap.keySet();
        for (Object key : keyset) {
            System.out.println(key + "-" + hashMap.get(key));
        }
        /*2、先用hashmap.keySet() 方法取出所有 key 的集合，再用迭代器遍历取 value*/
        System.out.println("====2、先用hashmap.keySet() 方法取出所有 key 的集合，再用迭代器遍历取 value====");
        Iterator iterator = keyset.iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            System.out.println(key + "-" + hashMap.get(key));
        }
        /*3、先用 hashMap.通过entrySet() 方法取出所有的 key-value 的集合，再用增强循环 for 遍历一一取出 key 和 value*/
        System.out.println("====3、先用 hashMap.通过entrySet() 方法取出所有的 key-value 的集合，再用增强循环 for 遍历一一取出 key 和 value====");
        Set set = hashMap.entrySet();
        for (Object key : set) {
            Map.Entry entry = (Map.Entry) key;
            System.out.println(entry.getKey() + "-" + entry.getValue());
        }
        /*4、先用 hashMap.entrySet() 方法取出所有的 key-value 的集合，再用迭代器遍历一一取出 key 和 value*/
        System.out.println("====4、先用 hashMap.entrySet() 方法取出所有的 key-value 的集合，再用迭代器遍历一一取出 key 和 value====");
        Set set1 = hashMap.entrySet();
        Iterator iterator1 = set1.iterator();
        while (iterator1.hasNext()) {
            Object itset = iterator1.next();
            Map.Entry entry = (Map.Entry) itset;
            System.out.println(entry.getKey() + "-" + entry.getValue());
        }
    }
}
