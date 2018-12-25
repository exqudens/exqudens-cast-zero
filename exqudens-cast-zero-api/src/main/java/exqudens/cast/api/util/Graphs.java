package exqudens.cast.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import exqudens.cast.api.model.graph.Graph;
import exqudens.cast.api.model.graph.Link;
import exqudens.cast.api.model.graph.Node;

public class Graphs {

    public static <T> List<T> graphToList(Graph graph, Class<T> objectType) {
        try {
            List<Node> nodes = graph.getNodes();
            List<Link> links = graph.getLinks();
            if (!nodes.get(0).getType().equals(objectType.getName())) {
                throw new IllegalArgumentException("wrong type: " + objectType.getName() + " expected: " + nodes.get(0).getType());
            }
            List<Object> objects = new ArrayList<>();
            for (Node node : nodes) {
                Map<String, Object> data = node.getData();
                Class<?> objectClass = Class.forName(node.getType());
                Object object = objectClass.newInstance();
                for (Entry<String, Object> entry : data.entrySet()) {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();
                    String methodName = setterName(fieldName);
                    objectClass.getMethod(methodName, value.getClass()).invoke(object, value);
                }
                objects.add(object);
            }
            for (Link link : links) {
                Object source = objects.get(link.getSource().intValue());
                Object target = objects.get(link.getTarget().intValue());
                String fieldName = link.getDestination();
                Class<?> fieldType = Class.forName(link.getType());

                if (List.class.isAssignableFrom(fieldType)) {
                    String getterName = getterName(fieldName);
                    String setterName = setterName(fieldName);
                    Object listObject = source.getClass().getMethod(getterName).invoke(source);
                    if (listObject == null) {
                        List<Object> list = new ArrayList<>();
                        list.add(target);
                        source.getClass().getMethod(setterName, List.class).invoke(source, list);
                    } else {
                        List<?> rawList = List.class.cast(listObject);
                        List<Object> list = new ArrayList<>(rawList);
                        list.add(target);
                        source.getClass().getMethod(setterName, List.class).invoke(source, list);
                    }
                } else {
                    String setterName = setterName(fieldName);
                    source.getClass().getMethod(setterName, target.getClass()).invoke(source, target);
                }
            }
            List<T> result = new ArrayList<>();
            for (Object object : objects) {
                if (objectType.equals(object.getClass())) {
                    result.add(objectType.cast(object));
                }
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Graph listToGraph(
        Class<T> objectType,
        List<T> objects,
        Function<T, Integer> idFunction,
        List<Class<?>> entityClasses,
        List<Class<?>> fieldIncludeTypes,
        List<Class<?>> fieldExcludeTypes
    ) {
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Map<Integer, T> nodeMap = getNodeMap(objectType, objects, idFunction, entityClasses, fieldIncludeTypes, fieldExcludeTypes);
        List<Entry<Integer, T>> listEntry = nodeMap.entrySet().stream().collect(Collectors.toList());
        Map<Integer, Integer> idIndexMap = IntStream.range(0, listEntry.size()).mapToObj(i -> new SimpleEntry<>(listEntry.get(i).getKey(), i)).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        for (Integer parentId : nodeMap.keySet()) {
            T parent = nodeMap.get(parentId);
            String parentType = parent.getClass().getName();
            Map<String, Object> data = toMap(parent, entityClasses);
            Node node = new Node(parentType, data);
            nodes.add(node);
            Map<Integer, Entry<T, Entry<String, String>>> unvisitedNodeMap = getUnvisitedNodeMap(objectType, parent, idFunction, entityClasses, null, null, null);
            for (Integer childId : unvisitedNodeMap.keySet()) {
                Integer source = idIndexMap.get(parentId);
                Integer target = idIndexMap.get(childId);
                String destination = unvisitedNodeMap.get(childId).getValue().getKey();
                String type = unvisitedNodeMap.get(childId).getValue().getValue();
                Link link = new Link(source, target, destination, type);
                links.add(link);
            }
        }
        return new Graph(nodes, links);
    }

    // Object section

    static Map<String, Object> toMap(
        Object object,
        List<Class<?>> entityClasses
    ) {
        try {
            List<Class<?>> hierarchy = hierarchy(object.getClass());
            Set<String> fieldNames = fieldNames(hierarchy, null, Stream.concat(Stream.of(Collection.class, Map.class), entityClasses.stream()).collect(Collectors.toList()));
            Map<String, String> methodFieldNameMap = fieldNames.stream().map(
                fieldName -> new SimpleEntry<>(getterName(fieldName), fieldName)
            ).distinct().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            Map<String, Object> map = new LinkedHashMap<>();
            for (Entry<String, String> entry : methodFieldNameMap.entrySet()) {
                String methodName = entry.getKey();
                String fieldName = entry.getValue();
                Object value = object.getClass().getMethod(methodName).invoke(object);
                if (value != null) {
                    map.put(fieldName, value);
                }
            }
            return map;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static <T> Map<Integer, T> getNodeMap(
        Class<T> objectType,
        List<T> objects,
        Function<T, Integer> idFunction,
        List<Class<?>> entityClasses,
        List<Class<?>> fieldIncludeTypes,
        List<Class<?>> fieldExcludeTypes
    ) {
        try {
            Map<Integer, T> result = new LinkedHashMap<>();
            List<Integer> ids = new ArrayList<>();
            Queue<T> queue = new LinkedList<>();
            for (T object : objects) {
                Integer id = idFunction.apply(object);
                result.putIfAbsent(id, object);
                queue.add(object);
                ids.add(id);
            }
            while (!queue.isEmpty()) {
                T nextObject = queue.remove();
                Map<Integer, Entry<T, Entry<String, String>>> children = Collections.emptyMap();
                do {
                    children = getUnvisitedNodeMap(
                        objectType,
                        nextObject,
                        idFunction,
                        entityClasses,
                        ids,
                        fieldIncludeTypes,
                        fieldExcludeTypes
                    );
                    if (children.isEmpty()) {
                        break;
                    }
                    for (Integer childId : children.keySet()) {
                        Entry<T, Entry<String, String>> entry = children.get(childId);
                        T child = entry.getKey();
                        ids.add(childId);
                        result.putIfAbsent(childId, child);
                        queue.add(child);
                    }
                } while (!children.isEmpty());
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static <T> Map<Integer, Entry<T, Entry<String, String>>> getUnvisitedNodeMap(
        Class<T> objectType,
        T object,
        Function<T, Integer> idFunction,
        List<Class<?>> entityClasses,
        List<Integer> ids,
        List<Class<?>> fieldIncludeTypes,
        List<Class<?>> fieldExcludeTypes
    ) {
        try {
            List<Class<?>> hierarchy = hierarchy(object.getClass());
            Map<Integer, Entry<T, Entry<String, String>>> result = new LinkedHashMap<>();

            Set<String> fieldNames = fieldNames(hierarchy, fieldIncludeTypes, fieldExcludeTypes);
            Map<String, String> fieldMethodMap = fieldNames.stream().map(fieldName -> new SimpleEntry<>(fieldName, getterName(fieldName))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            for (String fieldName : fieldMethodMap.keySet()) {
                String methodName = fieldMethodMap.get(fieldName);
                Method method = object.getClass().getMethod(methodName);
                Object value = method.invoke(object);
                if (value == null) {
                    continue;
                }
                Class<?> type = value.getClass();
                if (!Collection.class.isAssignableFrom(type)) {
                    if (!entityClasses.contains(value.getClass())) {
                        continue;
                    }
                    Integer id = idFunction.apply(objectType.cast(value));
                    if (ids == null || !ids.contains(id)) {
                        result.putIfAbsent(id, new SimpleEntry<>(objectType.cast(value), new SimpleEntry<>(fieldName, method.getReturnType().getName())));
                    }
                } else {
                    Collection<?> collection = Collection.class.cast(value);
                    for (Object o : collection) {
                        if (!entityClasses.contains(o.getClass())) {
                            break;
                        }
                        Integer id = idFunction.apply(objectType.cast(o));
                        if (ids == null || !ids.contains(id)) {
                            result.putIfAbsent(id, new SimpleEntry<>(objectType.cast(o), new SimpleEntry<>(fieldName, method.getReturnType().getName())));
                        }
                    }
                }
            }

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // Class section

    static List<Class<?>> hierarchy(Class<?> objectClass) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> superClass = objectClass;
        while (superClass != null) {
            hierarchy.add(0, superClass);
            superClass = superClass.getSuperclass();
            if (superClass != null) {
                continue;
            } else {
                break;
            }
        }
        return hierarchy;
    }

    static Set<String> fieldNames(
        List<Class<?>> hierarchy,
        List<Class<?>> includeTypes,
        List<Class<?>> excludeTypes
    ) {
        Set<String> allFieldNames = new LinkedHashSet<>();
        Set<String> fieldNames = new LinkedHashSet<>();
        for (Class<?> c : hierarchy) {
            Set<String> classFieldNames = new LinkedHashSet<>();
            for (Field field : c.getDeclaredFields()) {
                String fieldName = field.getName();
                allFieldNames.add(fieldName);
                Class<?> fieldType = field.getType();
                boolean includePresent = true;
                boolean excludePresent = false;
                if (includeTypes != null && !includeTypes.isEmpty()) {
                    includePresent = includePresent && includeTypes.stream().filter(cl -> cl.isAssignableFrom(fieldType)).findAny().isPresent();
                }
                if (excludeTypes != null && !excludeTypes.isEmpty()) {
                    excludePresent = excludePresent || excludeTypes.stream().filter(cl -> cl.isAssignableFrom(fieldType)).findAny().isPresent();
                }
                if (excludePresent) {
                    fieldNames.remove(fieldName);
                    classFieldNames.remove(fieldName);
                } else if (includePresent) {
                    classFieldNames.add(fieldName);
                }
            }
            Map<String, String> methodFieldNameMap = Stream
            .concat(allFieldNames.stream(), classFieldNames.stream())
            .map(fieldName -> new SimpleEntry<>(getterName(fieldName), fieldName))
            .distinct()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            for (Method method : c.getDeclaredMethods()) {
                String fieldName = methodFieldNameMap.get(method.getName());
                if (fieldName == null) {
                    continue;
                }
                Class<?> methodReturnType = method.getReturnType();
                boolean includePresent = true;
                boolean excludePresent = false;
                if (includeTypes != null && !includeTypes.isEmpty()) {
                    includePresent = includePresent && includeTypes.stream().filter(cl -> cl.isAssignableFrom(methodReturnType)).findAny().isPresent();
                }
                if (excludeTypes != null && !excludeTypes.isEmpty()) {
                    excludePresent = excludePresent || excludeTypes.stream().filter(cl -> cl.isAssignableFrom(methodReturnType)).findAny().isPresent();
                }
                if (excludePresent) {
                    fieldNames.remove(fieldName);
                    classFieldNames.remove(fieldName);
                } else if (includePresent) {
                    classFieldNames.add(fieldName);
                }
            }
            fieldNames.addAll(classFieldNames);
        }
        return fieldNames;
    }

    static String getterName(String fieldName) {
        return toMethodName("get", fieldName);
    }

    static String setterName(String fieldName) {
        return toMethodName("set", fieldName);
    }

    static String toMethodName(String prefix, String fieldName) {
        return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
