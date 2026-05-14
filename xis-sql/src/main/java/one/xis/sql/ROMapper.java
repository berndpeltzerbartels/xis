package one.xis.sql;

import com.google.gson.Gson;
import lombok.NonNull;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.RecordUtil;
import one.xis.utils.lang.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class ROMapper {

    private final Gson gson = new Gson();
    private final Map<MappingKey, ObjectMapping> mappings = new ConcurrentHashMap<>();
    private final Map<RelationKey, ForeignKey> foreignKeys = new ConcurrentHashMap<>();

    <T> T toObject(@NonNull ResultSet rs, @NonNull Class<T> type) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        var columns = columns(rs);
        DatabaseMetaData metaData = databaseMetaData(rs);
        var mapping = mapping(type, columns, metaData);
        RowValues values = readRowValues(rs, mapping);
        return createObject(mapping, values);
    }

    <T> List<T> toObjects(@NonNull ResultSet rs, @NonNull Class<T> type) throws SQLException {
        var columns = columns(rs);
        DatabaseMetaData metaData = databaseMetaData(rs);
        var mapping = mapping(type, columns, metaData);
        var rows = new LinkedHashMap<RowKey, RowValues>();
        while (rs.next()) {
            RowKey key = rowKey(rs, mapping.simpleValues());
            RowValues values = rows.computeIfAbsent(key, ignored -> new RowValues());
            readRowInto(rs, mapping, values);
        }

        var result = new ArrayList<T>(rows.size());
        for (RowValues values : rows.values()) {
            result.add(createObject(mapping, values));
        }
        return result;
    }

    private RowValues readRowValues(ResultSet rs, ObjectMapping mapping) throws SQLException {
        var values = new RowValues();
        readRowInto(rs, mapping, values);
        return values;
    }

    private void readRowInto(ResultSet rs, ObjectMapping mapping, RowValues values) throws SQLException {
        for (ValueMapping value : mapping.simpleValues()) {
            values.put(value.name(), readColumn(rs, value.column(), value.type(), value.genericType()));
        }
        for (ComplexMapping complex : mapping.complexValues()) {
            Object value = readComplexValue(rs, complex);
            if (value != null) {
                values.put(complex.name(), value);
            }
        }
        for (CollectionMapping collection : mapping.collectionValues()) {
            Object value = collection.simpleElement()
                    ? readColumn(rs, collection.column(), collection.elementType(), collection.elementType())
                    : readComplexElement(rs, collection);
            if (value != null) {
                values.add(collection.name(), value);
            }
        }
    }

    private Object readComplexElement(ResultSet rs, CollectionMapping collection) throws SQLException {
        var values = readRowValues(rs, collection.elementMapping());
        return values.isEmpty() ? null : createObject(collection.elementMapping(), values);
    }

    private Object readComplexValue(ResultSet rs, ComplexMapping complex) throws SQLException {
        var values = readRowValues(rs, complex.mapping());
        return values.isEmpty() ? null : createObject(complex.mapping(), values);
    }

    private <T> T createObject(ObjectMapping mapping, RowValues values) {
        if (mapping.record()) {
            return createRecord(mapping, values);
        }
        @SuppressWarnings("unchecked")
        T object = (T) ClassUtils.newInstance(mapping.type());
        for (ValueMapping value : mapping.simpleValues()) {
            FieldUtil.setFieldValue(object, value.field(), valueOrDefault(value.type(), values.get(value.name())));
        }
        for (ComplexMapping complex : mapping.complexValues()) {
            FieldUtil.setFieldValue(object, complex.field(), values.get(complex.name()));
        }
        for (CollectionMapping collection : mapping.collectionValues()) {
            FieldUtil.setFieldValue(object, collection.field(), collectionInstance(collection, values.collection(collection.name())));
        }
        return object;
    }

    private <T> T createRecord(ObjectMapping mapping, RowValues values) {
        try {
            Object[] args = new Object[mapping.recordComponents().size()];
            for (int i = 0; i < args.length; i++) {
                RecordComponent component = mapping.recordComponents().get(i);
                CollectionMapping collection = mapping.collectionValue(component.getName());
                if (collection != null) {
                    args[i] = collectionInstance(collection, values.collection(collection.name()));
                    continue;
                }
                ComplexMapping complex = mapping.complexValue(component.getName());
                if (complex != null) {
                    args[i] = values.get(complex.name());
                    continue;
                }
                ValueMapping value = mapping.simpleValue(component.getName());
                args[i] = value == null ? defaultValue(component.getType()) : valueOrDefault(value.type(), values.get(value.name()));
            }
            @SuppressWarnings("unchecked")
            T result = (T) mapping.recordConstructor().newInstance(args);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Could not create record " + mapping.type().getName(), e);
        }
    }

    private Object collectionInstance(CollectionMapping mapping, List<Object> values) {
        return CollectionUtils.convertCollectionClass(values, mapping.collectionType());
    }

    private Object valueOrDefault(Class<?> type, Object value) {
        return value == null && type.isPrimitive() ? defaultValue(type) : value;
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    private ObjectMapping mapping(Class<?> type, List<ResultColumn> columns, DatabaseMetaData metaData) {
        return mappings.computeIfAbsent(new MappingKey(type, databaseId(metaData), columnLabels(columns)),
                ignored -> createMapping(type, columns, metaData));
    }

    private List<String> columnLabels(List<ResultColumn> columns) {
        return columns.stream()
                .map(column -> normalize(column.label()))
                .toList();
    }

    private ObjectMapping createMapping(Class<?> type, List<ResultColumn> columns, DatabaseMetaData metaData) {
        return type.isRecord() ? createRecordMapping(type, columns, metaData) : createClassMapping(type, columns, metaData);
    }

    private ObjectMapping createClassMapping(Class<?> type, List<ResultColumn> columns, DatabaseMetaData metaData) {
        var simpleValues = new ArrayList<ValueMapping>();
        var complexValues = new ArrayList<ComplexMapping>();
        var collectionValues = new ArrayList<CollectionMapping>();
        var unusedColumns = new ArrayList<>(columns);
        boolean allowUnmappedFields = allowUnmappedFields(type);

        for (Field field : fields(type)) {
            if (columnMappedField(type, field)) {
                ResultColumn column = findColumn(columnName(field), unusedColumns);
                if (column == null) {
                    if (allowUnmappedFields || SQLAnnotationSupport.optionalColumn(type, field)) {
                        continue;
                    }
                    throw missingColumn(type, field.getName(), columnName(field));
                }
                simpleValues.add(new ValueMapping(field.getName(), field.getType(), field.getGenericType(), column, field));
                unusedColumns.remove(column);
            }
        }
        for (Field field : fields(type)) {
            if (isComplexEntity(field.getType())) {
                if ((allowUnmappedFields || SQLAnnotationSupport.optionalColumn(type, field))
                        && noColumnForComplexEntity(field.getType(), unusedColumns)) {
                    continue;
                }
                complexValues.add(createComplexMapping(type, field.getName(), field.getType(), field, unusedColumns, metaData));
            }
        }
        for (Field field : fields(type)) {
            if (isCollection(field.getType()) && !SQLAnnotationSupport.jsonColumn(type, field)) {
                if ((allowUnmappedFields || SQLAnnotationSupport.optionalColumn(type, field))
                        && noColumnForCollection(field, unusedColumns)) {
                    continue;
                }
                collectionValues.add(createCollectionMapping(type, field.getName(), field.getType(),
                        FieldUtil.getGenericTypeParameter(field), field, unusedColumns, metaData));
            }
        }
        validateMappedFields(type, allowUnmappedFields);
        return new ObjectMapping(type, false, List.of(), null, simpleValues, complexValues, collectionValues);
    }

    private ObjectMapping createRecordMapping(Class<?> type, List<ResultColumn> columns, DatabaseMetaData metaData) {
        var simpleValues = new ArrayList<ValueMapping>();
        var complexValues = new ArrayList<ComplexMapping>();
        var collectionValues = new ArrayList<CollectionMapping>();
        var unusedColumns = new ArrayList<>(columns);
        var components = List.of(type.getRecordComponents());

        for (RecordComponent component : components) {
            if (ignored(component)) {
                continue;
            }
            if (columnMappedComponent(component)) {
                ResultColumn column = findColumn(columnName(component), unusedColumns);
                if (column == null) {
                    if (SQLAnnotationSupport.optionalColumn(component)) {
                        continue;
                    }
                    throw missingColumn(type, component.getName(), columnName(component));
                }
                simpleValues.add(new ValueMapping(component.getName(), component.getType(), component.getGenericType(), column, null));
                unusedColumns.remove(column);
            }
        }
        for (RecordComponent component : components) {
            if (ignored(component)) {
                continue;
            }
            if (isComplexEntity(component.getType())) {
                complexValues.add(createComplexMapping(type, component.getName(), component.getType(), null, unusedColumns, metaData));
            }
        }
        for (RecordComponent component : components) {
            if (ignored(component)) {
                continue;
            }
            if (isCollection(component.getType()) && !SQLAnnotationSupport.jsonColumn(component)) {
                collectionValues.add(createCollectionMapping(type, component.getName(), component.getType(),
                        RecordUtil.getGenericTypeParameter(component), null, unusedColumns, metaData));
            }
        }
        validateMappedRecordComponents(type, components);
        return new ObjectMapping(type, true, components, recordConstructor(type, components), simpleValues, complexValues, collectionValues);
    }

    private ComplexMapping createComplexMapping(Class<?> ownerType, String name, Class<?> fieldType,
                                                Field field, List<ResultColumn> unusedColumns, DatabaseMetaData metaData) {
        List<ResultColumn> fieldColumns = relationColumns(ownerType, fieldType, unusedColumns, metaData, RelationKind.REFERENCE);
        ObjectMapping fieldMapping = createMapping(fieldType, fieldColumns, metaData);
        removeColumns(unusedColumns, fieldMapping);
        return new ComplexMapping(name, field, fieldType, fieldMapping);
    }

    private CollectionMapping createCollectionMapping(Class<?> ownerType, String name, Class<?> collectionType, Class<?> elementType,
                                                      Field field, List<ResultColumn> unusedColumns, DatabaseMetaData metaData) {
        if (isSimpleType(elementType)) {
            ResultColumn column = findColumn(name, unusedColumns);
            if (column == null && unusedColumns.size() == 1) {
                column = unusedColumns.get(0);
            }
            if (column != null) {
                unusedColumns.remove(column);
            }
            if (column == null) {
                throw missingColumn(ownerType, name, name);
            }
            return new CollectionMapping(name, field, collectionType.asSubclass(Collection.class), elementType, true, column, null);
        }
        List<ResultColumn> elementColumns = relationColumns(ownerType, elementType, unusedColumns, metaData, RelationKind.COLLECTION);
        ObjectMapping elementMapping = createMapping(elementType, elementColumns, metaData);
        removeColumns(unusedColumns, elementMapping);
        return new CollectionMapping(name, field, collectionType.asSubclass(Collection.class), elementType, false, null, elementMapping);
    }

    private List<ResultColumn> relationColumns(Class<?> ownerType, Class<?> elementType, List<ResultColumn> unusedColumns,
                                         DatabaseMetaData metaData, RelationKind kind) {
        Entity owner = ownerType.getAnnotation(Entity.class);
        Entity element = elementType.getAnnotation(Entity.class);
        if (owner == null && element == null) {
            return unusedColumns;
        }
        if (owner == null || element == null) {
            throw new IllegalStateException("Collection relation " + ownerType.getName() + " -> " + elementType.getName()
                    + " needs @Entity on both types");
        }
        rejectBidirectionalModel(ownerType, elementType);
        ForeignKey foreignKey = foreignKey(owner.value(), element.value(), kind, metaData);
        List<ResultColumn> columns = unusedColumns.stream()
                .filter(column -> tableMatches(column.table(), element.value()))
                .toList();
        if (columns.isEmpty()) {
            throw new IllegalStateException("ResultSet has no columns for child table " + foreignKey.childTable());
        }
        return columns;
    }

    private void rejectBidirectionalModel(Class<?> ownerType, Class<?> elementType) {
        for (Field field : fields(elementType)) {
            if (field.getType() == ownerType || collectionElementType(field) == ownerType) {
                throw new IllegalStateException("Bidirectional relations are not supported: "
                        + ownerType.getName() + " <-> " + elementType.getName());
            }
        }
        if (elementType.isRecord()) {
            for (RecordComponent component : elementType.getRecordComponents()) {
                if (component.getType() == ownerType || collectionElementType(component) == ownerType) {
                    throw new IllegalStateException("Bidirectional relations are not supported: "
                            + ownerType.getName() + " <-> " + elementType.getName());
                }
            }
        }
    }

    private Class<?> collectionElementType(Field field) {
        return isCollection(field.getType()) ? FieldUtil.getGenericTypeParameter(field) : null;
    }

    private Class<?> collectionElementType(RecordComponent component) {
        return isCollection(component.getType()) ? RecordUtil.getGenericTypeParameter(component) : null;
    }

    private ForeignKey foreignKey(String ownerTable, String childTable, RelationKind kind, DatabaseMetaData metaData) {
        if (metaData == null) {
            throw new IllegalStateException("Cannot validate relation " + ownerTable + " -> " + childTable
                    + " because ResultSet has no database metadata");
        }
        RelationKey key = new RelationKey(databaseId(metaData), normalizeTable(ownerTable), normalizeTable(childTable), kind);
        ForeignKey cached = foreignKeys.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            ForeignKey foreignKey = findForeignKey(ownerTable, childTable, kind, metaData);
            if (foreignKey == null) {
                throw new IllegalStateException(missingForeignKeyMessage(ownerTable, childTable, kind));
            }
            foreignKeys.put(key, foreignKey);
            return foreignKey;
        } catch (SQLException e) {
            throw new RuntimeException("Could not read foreign keys for " + childTable, e);
        }
    }

    private ForeignKey findForeignKey(String ownerTable, String childTable, RelationKind kind, DatabaseMetaData metaData) throws SQLException {
        ForeignKey childToOwner = findImportedForeignKey(childTable, ownerTable, metaData);
        ForeignKey ownerToChild = findImportedForeignKey(ownerTable, childTable, metaData);
        if (kind == RelationKind.REFERENCE) {
            return singleReferenceForeignKey(ownerTable, childTable, ownerToChild, childToOwner);
        }
        ForeignKey forward = childToOwner;
        ForeignKey reverse = ownerToChild;
        if (forward != null && reverse != null) {
            throw new IllegalStateException("Bidirectional foreign keys are not supported between "
                    + ownerTable + " and " + childTable);
        }
        return forward;
    }

    private ForeignKey singleReferenceForeignKey(String ownerTable, String childTable, ForeignKey ownerToChild, ForeignKey childToOwner) {
        if (ownerToChild != null && childToOwner != null) {
            throw new IllegalStateException("Bidirectional foreign keys are not supported between "
                    + ownerTable + " and " + childTable);
        }
        return ownerToChild != null ? ownerToChild : childToOwner;
    }

    private ForeignKey findImportedForeignKey(String foreignTable, String primaryTable, DatabaseMetaData metaData) throws SQLException {
        ForeignKey foreignKey = findImportedForeignKeyExact(foreignTable, primaryTable, metaData);
        if (foreignKey != null) {
            return foreignKey;
        }
        return findImportedForeignKeyExact(foreignTable.toUpperCase(), primaryTable, metaData);
    }

    private ForeignKey findImportedForeignKeyExact(String foreignTable, String primaryTable, DatabaseMetaData metaData) throws SQLException {
        String owner = normalizeTable(primaryTable);
        String child = normalizeTable(foreignTable);
        try (ResultSet rs = metaData.getImportedKeys(null, null, foreignTable)) {
            return findForeignKey(rs, owner, child);
        }
    }

    private String missingForeignKeyMessage(String ownerTable, String childTable, RelationKind kind) {
        return switch (kind) {
            case COLLECTION -> "No foreign key from " + childTable + " to " + ownerTable;
            case REFERENCE -> "No foreign key between " + ownerTable + " and " + childTable;
        };
    }

    private ForeignKey findForeignKey(ResultSet rs, String ownerTable, String childTable) throws SQLException {
        while (rs.next()) {
            String primaryTable = rs.getString("PKTABLE_NAME");
            String foreignTable = rs.getString("FKTABLE_NAME");
            if (normalizeTable(primaryTable).equals(ownerTable) && normalizeTable(foreignTable).equals(childTable)) {
                return new ForeignKey(primaryTable, foreignTable);
            }
        }
        return null;
    }

    private boolean tableMatches(String actualTable, String expectedTable) {
        return !actualTable.isBlank() && normalizeTable(actualTable).equals(normalizeTable(expectedTable));
    }

    private String normalizeTable(String table) {
        return table == null ? "" : table.replace("\"", "").toLowerCase();
    }

    private String databaseId(DatabaseMetaData metaData) {
        if (metaData == null) {
            return "";
        }
        try {
            return metaData.getURL() + "|" + metaData.getUserName();
        } catch (SQLException e) {
            throw new RuntimeException("Could not read database identity", e);
        }
    }

    private void removeColumns(List<ResultColumn> columns, ObjectMapping mapping) {
        for (ValueMapping value : mapping.simpleValues()) {
            columns.remove(value.column());
        }
        for (ComplexMapping complex : mapping.complexValues()) {
            removeColumns(columns, complex.mapping());
        }
        for (CollectionMapping collection : mapping.collectionValues()) {
            if (collection.column() != null) {
                columns.remove(collection.column());
            }
            if (collection.elementMapping() != null) {
                removeColumns(columns, collection.elementMapping());
            }
        }
    }

    private Constructor<?> recordConstructor(Class<?> type, List<RecordComponent> components) {
        try {
            Class<?>[] parameterTypes = components.stream()
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find canonical constructor for " + type.getName(), e);
        }
    }

    private List<Field> fields(Class<?> type) {
        return FieldUtil.getFields(type, field -> !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !SQLAnnotationSupport.ignored(type, field));
    }

    private void validateMappedFields(Class<?> type, boolean allowUnmappedFields) {
        for (Field field : fields(type)) {
            if (SQLAnnotationSupport.jsonColumn(type, field)) {
                continue;
            }
            if (isCollection(field.getType()) && FieldUtil.getGenericTypeParameter(field) == null) {
                if (allowUnmappedFields) {
                    continue;
                }
                throw new IllegalStateException("No SQL mapping for field " + type.getName() + "." + field.getName());
            }
            if (!isSimpleType(field.getType()) && !isComplexEntity(field.getType()) && !isCollection(field.getType())) {
                if (allowUnmappedFields) {
                    continue;
                }
                throw new IllegalStateException("No SQL mapping for field " + type.getName() + "." + field.getName());
            }
        }
    }

    private boolean allowUnmappedFields(Class<?> type) {
        Entity entity = type.getAnnotation(Entity.class);
        return entity != null && entity.allowUnmappedFields();
    }

    private boolean noColumnForComplexEntity(Class<?> type, List<ResultColumn> columns) {
        for (Field field : fields(type)) {
            if (isSimpleType(field.getType()) && findColumn(columnName(field), columns) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean noColumnForCollection(Field field, List<ResultColumn> columns) {
        Class<?> elementType = FieldUtil.getGenericTypeParameter(field);
        if (elementType == null) {
            return true;
        }
        if (isSimpleType(elementType)) {
            return findColumn(field.getName(), columns) == null;
        }
        return noColumnForComplexEntity(elementType, columns);
    }

    private void validateMappedRecordComponents(Class<?> type, List<RecordComponent> components) {
        for (RecordComponent component : components) {
            if (ignored(component) || SQLAnnotationSupport.jsonColumn(component)) {
                continue;
            }
            if (isCollection(component.getType()) && RecordUtil.getGenericTypeParameter(component) == null) {
                throw new IllegalStateException("No SQL mapping for component " + type.getName() + "." + component.getName());
            }
            if (!isSimpleType(component.getType()) && !isComplexEntity(component.getType()) && !isCollection(component.getType())) {
                throw new IllegalStateException("No SQL mapping for component " + type.getName() + "." + component.getName());
            }
        }
    }

    private boolean ignored(RecordComponent component) {
        return SQLAnnotationSupport.ignored(component);
    }

    private IllegalStateException missingColumn(Class<?> type, String propertyName, String columnName) {
        return new IllegalStateException("No column for property " + type.getName() + "." + propertyName
                + " mapped to " + columnName);
    }

    private String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null ? field.getName() : column.value();
    }

    private String columnName(RecordComponent component) {
        Column column = component.getAnnotation(Column.class);
        return column == null ? component.getName() : column.value();
    }

    private ResultColumn findColumn(String propertyName, List<ResultColumn> columns) {
        String normalizedName = normalize(propertyName);
        for (ResultColumn column : columns) {
            if (normalize(column.name()).equals(normalizedName) || normalize(column.label()).equals(normalizedName)) {
                return column;
            }
        }
        for (ResultColumn column : columns) {
            if (endsWithProperty(column.name(), propertyName) || endsWithProperty(column.label(), propertyName)) {
                return column;
            }
        }
        return null;
    }

    private boolean endsWithProperty(String columnName, String propertyName) {
        return columnName != null && columnName.toLowerCase().endsWith("_" + propertyName.toLowerCase());
    }

    private RowKey rowKey(ResultSet rs, List<ValueMapping> values) throws SQLException {
        var keyValues = new ArrayList<>(values.size());
        for (ValueMapping value : values) {
            keyValues.add(rs.getObject(value.column().index()));
        }
        return new RowKey(keyValues);
    }

    private List<ResultColumn> columns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        var columns = new ArrayList<ResultColumn>(count);
        for (int i = 1; i <= count; i++) {
            columns.add(new ResultColumn(i, metaData.getColumnName(i), metaData.getColumnLabel(i), metaData.getTableName(i)));
        }
        return columns;
    }

    private DatabaseMetaData databaseMetaData(ResultSet rs) throws SQLException {
        if (rs.getStatement() == null || rs.getStatement().getConnection() == null) {
            return null;
        }
        return rs.getStatement().getConnection().getMetaData();
    }

    private Object readColumn(ResultSet rs, ResultColumn column, Class<?> targetType, Type genericType) throws SQLException {
        if (column == null) {
            return null;
        }
        Object value = rs.getObject(column.index());
        if (rs.wasNull()) {
            return null;
        }
        return toValue(value, targetType, genericType);
    }

    Object toValue(Object value, Class<?> targetType) {
        return toValue(value, targetType, targetType);
    }

    private Object toValue(Object value, Class<?> targetType, Type genericType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class && value instanceof Clob clob) {
            return clobToString(clob);
        }
        if (!isSimpleType(targetType)) {
            return gson.fromJson(value.toString(), genericType);
        }
        if (targetType == char[].class) {
            return value.toString().toCharArray();
        }
        if (targetType == Character.class || targetType == Character.TYPE) {
            String string = value.toString();
            return string.isEmpty() ? null : string.charAt(0);
        }
        if (targetType == UUID.class) {
            return UUID.fromString(value.toString());
        }
        if (targetType == Instant.class) {
            return toInstant(value);
        }
        if (targetType == Date.class) {
            return Date.from(toInstant(value));
        }
        if (targetType == Calendar.class) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(toInstant(value)));
            return calendar;
        }
        if (targetType == java.sql.Date.class) {
            return java.sql.Date.valueOf(toLocalDate(value));
        }
        if (targetType == java.sql.Time.class) {
            return java.sql.Time.valueOf(toLocalTime(value));
        }
        if (targetType == Timestamp.class) {
            return Timestamp.from(toInstant(value));
        }
        if (targetType == LocalDateTime.class) {
            return toLocalDateTime(value);
        }
        if (targetType == LocalDate.class) {
            return toLocalDate(value);
        }
        if (targetType == LocalTime.class) {
            return toLocalTime(value);
        }
        if (targetType == OffsetDateTime.class) {
            return toOffsetDateTime(value);
        }
        if (targetType == OffsetTime.class) {
            return toOffsetTime(value);
        }
        if (targetType == ZonedDateTime.class) {
            return toZonedDateTime(value);
        }
        if (targetType == Year.class) {
            return Year.parse(value.toString());
        }
        if (targetType == YearMonth.class) {
            return YearMonth.parse(value.toString());
        }
        if (targetType == MonthDay.class) {
            return MonthDay.parse(value.toString());
        }
        if (targetType == Month.class) {
            return value instanceof Number number ? Month.of(number.intValue()) : Month.valueOf(value.toString());
        }
        if (targetType == DayOfWeek.class) {
            return value instanceof Number number ? DayOfWeek.of(number.intValue()) : DayOfWeek.valueOf(value.toString());
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) targetType.asSubclass(Enum.class), value.toString());
            return enumValue;
        }
        if (targetType == Duration.class) {
            return Duration.parse(value.toString());
        }
        if (targetType == Period.class) {
            return Period.parse(value.toString());
        }
        return TypeUtils.convertSimple(value, targetType);
    }

    private String clobToString(Clob clob) {
        try {
            long length = clob.length();
            if (length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("CLOB value is too large to map to String");
            }
            return clob.getSubString(1, (int) length);
        } catch (SQLException e) {
            throw new RuntimeException("Could not read CLOB value", e);
        }
    }

    private Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().atDate(LocalDate.of(1970, 1, 1)).atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof LocalDate date) {
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toInstant();
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toInstant();
        }
        return Instant.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalDateTime();
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime time) {
            return time;
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalTime();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalTime();
        }
        if (value instanceof OffsetTime time) {
            return time.toLocalTime();
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalTime();
        }
        return LocalTime.parse(value.toString());
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toOffsetDateTime();
        }
        if (value instanceof CharSequence text) {
            return OffsetDateTime.parse(text);
        }
        return toInstant(value).atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetTime toOffsetTime(Object value) {
        if (value instanceof OffsetTime time) {
            return time;
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toOffsetTime();
        }
        if (value instanceof CharSequence text) {
            return OffsetTime.parse(text);
        }
        return toLocalTime(value).atOffset(OffsetDateTime.now().getOffset());
    }

    private ZonedDateTime toZonedDateTime(Object value) {
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toZonedDateTime();
        }
        if (value instanceof CharSequence text) {
            return ZonedDateTime.parse(text);
        }
        return toInstant(value).atZone(ZoneId.systemDefault());
    }

    private boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    private boolean isComplexEntity(Class<?> type) {
        return !isSimpleType(type) && !isCollection(type) && type.isAnnotationPresent(Entity.class);
    }

    private boolean columnMappedField(Class<?> ownerType, Field field) {
        return isSimpleType(field.getType()) || SQLAnnotationSupport.jsonColumn(ownerType, field);
    }

    private boolean columnMappedComponent(RecordComponent component) {
        return isSimpleType(component.getType()) || SQLAnnotationSupport.jsonColumn(component);
    }

    static boolean isSimpleType(Class<?> type) {
        return TypeUtils.isSimple(type)
                || type == Byte.class
                || type == Byte.TYPE
                || type == char[].class
                || type == UUID.class
                || type == Date.class
                || type == Calendar.class
                || type == LocalDate.class
                || type == LocalTime.class
                || type == LocalDateTime.class
                || type == Instant.class
                || type == OffsetDateTime.class
                || type == OffsetTime.class
                || type == ZonedDateTime.class
                || type == Year.class
                || type == YearMonth.class
                || type == MonthDay.class
                || type == Month.class
                || type == DayOfWeek.class
                || type == Duration.class
                || type == Period.class
                || type == java.sql.Date.class
                || type == java.sql.Time.class
                || type == Timestamp.class
                || type.isEnum();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replace("_", "");
    }

    private record ObjectMapping(Class<?> type, boolean record, List<RecordComponent> recordComponents,
                                 Constructor<?> recordConstructor, List<ValueMapping> simpleValues,
                                 List<ComplexMapping> complexValues, List<CollectionMapping> collectionValues) {

        private ValueMapping simpleValue(String name) {
            return simpleValues.stream()
                    .filter(value -> value.name().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        private CollectionMapping collectionValue(String name) {
            return collectionValues.stream()
                    .filter(value -> value.name().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        private ComplexMapping complexValue(String name) {
            return complexValues.stream()
                    .filter(value -> value.name().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    private record ValueMapping(String name, Class<?> type, Type genericType, ResultColumn column, Field field) {
    }

    private record ComplexMapping(String name, Field field, Class<?> type, ObjectMapping mapping) {
    }

    private record CollectionMapping(String name, Field field, Class<? extends Collection> collectionType,
                                     Class<?> elementType, boolean simpleElement, ResultColumn column,
                                     ObjectMapping elementMapping) {
    }

    private record ResultColumn(int index, String name, String label, String table) {
    }

    private record RowKey(List<Object> values) {
    }

    private record MappingKey(Class<?> type, String databaseId, List<String> columns) {
    }

    private record RelationKey(String databaseId, String ownerTable, String childTable, RelationKind kind) {
    }

    private record ForeignKey(String ownerTable, String childTable) {
    }

    private enum RelationKind {
        COLLECTION, REFERENCE
    }

    private static class RowValues {
        private final Map<String, Object> values = new LinkedHashMap<>();
        private final Map<String, List<Object>> collections = new LinkedHashMap<>();

        private void put(String name, Object value) {
            values.put(name, value);
        }

        private Object get(String name) {
            return values.get(name);
        }

        private void add(String name, Object value) {
            collections.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
        }

        private List<Object> collection(String name) {
            return collections.getOrDefault(name, List.of());
        }

        private boolean isEmpty() {
            return values.values().stream().allMatch(value -> value == null)
                    && collections.values().stream().allMatch(List::isEmpty);
        }
    }
}
