package ai.classifai.core.data.type.tabular;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class TabularQueryGen {
    private final TabularUtils tabularUtils = new TabularUtils();

    public Map<String, String> extractHeaders(List<String[]> dataFromFile, List<String> headerNames) {
        List<String> headerTypes;
        List<Map<String, String>> listOfMap = new LinkedList<>();

        for(String[] array : dataFromFile) {
            listOfMap.add(extractTypes(identifyEachDataType(array), dataFromFile));
        }

        headerTypes = extractTypesFromLoop(listOfMap);
        return mapHeaderNamesToHeaderTypes(headerNames, headerTypes);
    }

    public List<Map<String, String>> identifyEachDataType(String[] dataList) {
        List<Map<String, String>> list = new LinkedList<>();

        for (String object : dataList) {
            Map<String, String> map = new HashMap<>();
            if (object != null && tabularUtils.isDateObject(object))
            {
                map.put(object, "VARCHAR(100)");
            }

            else if(object != null && !tabularUtils.isDateObject(object)) {
                if (NumberUtils.isCreatable(object)) {
                    try {
                        if (Integer.valueOf(object).equals(Integer.parseInt(object))) {
                            map.put(object, "INT");
                        }
                    } catch (NumberFormatException e) {
                        if (Double.valueOf(object).equals(Double.parseDouble(object))) {
                            map.put(object, "DECIMAL");
                        } else {
                            log.info("Number format error. Data is not numeric");
                        }
                    }
                }

                else {
                    map.put(object, "VARCHAR(2000)");
                }
            }

            if(object == null) {
                map.put(null, "VARCHAR(2000)");
            }
            list.add(map);
        }

        return list;
    }

    private Map<String, String> mapHeaderNamesToHeaderTypes(List<String> keys, List<String> values) {
        return IntStream.range(0, keys.size())
                .collect(LinkedHashMap::new, (x, i) -> x.put(keys.get(i), values.get(i)), Map::putAll);
    }

    private Map<String, String> extractTypes(List<Map<String, String>> mapDataType, List<String[]> dataFromFile) {
        List<String> columnName = Arrays.asList(dataFromFile.get(0));
        List<String> list = new LinkedList<>();
        Map<String, String> map = new LinkedHashMap<>();
        for(Map<String, String> dataTypeMap : mapDataType) {
            for(Map.Entry<String, String> entry : dataTypeMap.entrySet()) {
                list.add(entry.getValue());
            }
        }

        for(int i = 0; i < list.size(); i++) {
            map.put(columnName.get(i), list.get(i));
        }
        return map;
    }

    private LinkedList<String> extractTypesFromLoop(List<Map<String, String>> listOfMap) {
        Map<String, Map<String, Integer>> countMap = new LinkedHashMap<>();
        Map<String, String> nameMap = new LinkedHashMap<>();

        // name : {value: count, value: count}
        for(Map<String, String> map : listOfMap) {
            for(Map.Entry<String, String> entry : map.entrySet()) {
                Map<String, Integer> temp = new LinkedHashMap<>();
                if(!countMap.containsKey(entry.getKey())) {
                    temp.put(entry.getValue(), 1);
                    countMap.put(entry.getKey(), temp);
                } else {
                    Map<String, Integer> value = countMap.get(entry.getKey());
                    if(value.containsKey(entry.getValue())) {
                        Integer count = value.get(entry.getValue()) + 1;
                        value.put(entry.getValue(), count);
                        countMap.put(entry.getKey(), value);
                    }
                    else if(!value.containsKey(entry.getKey())){
                        value.put(entry.getValue(), 1);
                        countMap.put(entry.getKey(), value);
                    }
                }
            }
        }

        for(Map.Entry<String, Map<String, Integer>> entry : countMap.entrySet()) {
            Map<String, Integer> valueCount = entry.getValue();

            if(valueCount.containsKey("DECIMAL")) {
                nameMap.put(entry.getKey(), "DECIMAL");
            }

            else if(valueCount.containsKey("INT") && !valueCount.containsKey("DECIMAL")) {
                nameMap.put(entry.getKey(), "INT");
            }

            else {
                Map.Entry<String, Integer> map = null;
                for(Map.Entry<String, Integer> entryMap : valueCount.entrySet()){
                    if(map == null || entryMap.getValue().compareTo(map.getValue()) > 0) {
                        map = entryMap;
                    }
                }
                assert map != null;
                nameMap.put(entry.getKey(), map.getKey());
            }

        }
        return new LinkedList<>(nameMap.values());
    }
}
