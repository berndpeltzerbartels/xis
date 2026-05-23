package one.xis.sql;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.RecordUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
class EntityRelationGraph {
    private final DatabaseMetaData metaData;

    EntitySQLMapping.Property primaryKey(EntitySQLMapping mapping) throws SQLException {
        List<EntitySQLMapping.Property> primaryKeys = primaryKeys(mapping);
        if (primaryKeys.isEmpty()) {
            throw new IllegalStateException("No primary key for table " + mapping.tableName());
        }
        if (primaryKeys.size() > 1) {
            throw new IllegalStateException("Composite primary keys are not supported for table " + mapping.tableName());
        }
        return primaryKeys.get(0);
    }

    List<EntitySQLMapping.Property> primaryKeys(EntitySQLMapping mapping) throws SQLException {
        List<String> primaryKeyColumns = primaryKeyColumns(mapping.tableName());
        if (primaryKeyColumns.isEmpty()) {
            throw new IllegalStateException("No primary key for table " + mapping.tableName());
        }
        var result = new ArrayList<EntitySQLMapping.Property>();
        for (String primaryKeyColumn : primaryKeyColumns) {
            EntitySQLMapping.Property property = mapping.propertyByColumn(primaryKeyColumn);
            if (property == null) {
                throw new IllegalStateException("Primary key column " + primaryKeyColumn
                        + " has no mapped property on " + mapping.tableName());
            }
            result.add(property);
        }
        return List.copyOf(result);
    }

    List<EntitySQLMapping.Property> saveProperties(EntitySQLMapping mapping) throws SQLException {
        var properties = new ArrayList<EntitySQLMapping.Property>();
        for (EntitySQLMapping.Property property : mapping.properties()) {
            if (!property.optionalColumn() || tableHasColumn(mapping.tableName(), property.columnName())) {
                properties.add(property);
            }
        }
        properties.addAll(relationProperties(mapping));
        return List.copyOf(properties);
    }

    List<CollectionRelation> collectionRelations(EntitySQLMapping mapping,
                                                 EntitySQLMapping.Property parentPrimaryKey) throws SQLException {
        var relations = new ArrayList<CollectionRelation>();
        for (Field field : FieldUtil.getFields(mapping.type(), field -> collectionField(mapping.type(), field))) {
            Class<?> elementType = FieldUtil.getGenericTypeParameter(field);
            relations.add(collectionRelation(mapping, parentPrimaryKey, new FieldCollectionAccessor(field), elementType));
        }
        if (mapping.type().isRecord()) {
            for (RecordComponent component : mapping.type().getRecordComponents()) {
                if (collectionComponent(component)) {
                    Class<?> elementType = RecordUtil.getGenericTypeParameter(component);
                    relations.add(collectionRelation(mapping, parentPrimaryKey,
                            new RecordCollectionAccessor(component), elementType));
                }
            }
        }
        return List.copyOf(relations);
    }

    private List<EntitySQLMapping.Property> relationProperties(EntitySQLMapping mapping) throws SQLException {
        var properties = new ArrayList<EntitySQLMapping.Property>();
        for (Field field : FieldUtil.getFields(mapping.type(), field -> relationField(mapping.type(), field))) {
            properties.add(relationProperty(mapping, field.getName(), field.getType(),
                    new FieldEntityAccessor(field)));
        }
        if (mapping.type().isRecord()) {
            for (RecordComponent component : mapping.type().getRecordComponents()) {
                if (relationComponent(component)) {
                    properties.add(relationProperty(mapping, component.getName(), component.getType(),
                            new RecordEntityAccessor(component)));
                }
            }
        }
        return properties;
    }

    private EntitySQLMapping.Property relationProperty(EntitySQLMapping mapping, String name, Class<?> relationType,
                                                       EntityAccessor accessor) throws SQLException {
        EntitySQLMapping referencedMapping = new EntitySQLMapping(relationType);
        ForeignKeyColumns foreignKey = importedForeignKey(mapping.tableName(), referencedMapping.tableName());
        if (foreignKey == null) {
            if (importedForeignKey(referencedMapping.tableName(), mapping.tableName()) != null) {
                throw new IllegalStateException("Cannot save inverse relation " + mapping.type().getName() + "."
                        + name + " because foreign key is on table " + referencedMapping.tableName());
            }
            throw new IllegalStateException("No foreign key from " + mapping.tableName() + " to "
                    + referencedMapping.tableName());
        }
        EntitySQLMapping.Property referencedPrimaryKey = referencedMapping.propertyByColumn(foreignKey.primaryColumn());
        if (referencedPrimaryKey == null) {
            throw new IllegalStateException("Foreign key target column " + foreignKey.primaryColumn()
                    + " has no mapped property on " + referencedMapping.tableName());
        }
        return new RelationProperty(name, foreignKey.foreignColumn(), accessor, referencedPrimaryKey);
    }

    private CollectionRelation collectionRelation(EntitySQLMapping parentMapping,
                                                  EntitySQLMapping.Property parentPrimaryKey,
                                                  CollectionAccessor accessor, Class<?> elementType) throws SQLException {
        if (!elementType.isAnnotationPresent(Entity.class)) {
            throw new IllegalStateException("Collection " + parentMapping.type().getName() + "."
                    + accessor.name() + " must contain @Entity elements");
        }
        EntitySQLMapping childMapping = new EntitySQLMapping(elementType);
        ForeignKeyColumns foreignKey = importedForeignKey(childMapping.tableName(), parentMapping.tableName());
        if (foreignKey == null) {
            throw new IllegalStateException("No foreign key from " + childMapping.tableName()
                    + " to " + parentMapping.tableName());
        }
        EntitySQLMapping.Property childPrimaryKey = primaryKey(childMapping);
        EntitySQLMapping.Property childForeignKeyProperty = childMapping.propertyByColumn(foreignKey.foreignColumn());
        List<EntitySQLMapping.Property> childProperties = childMapping.properties().stream()
                .filter(property -> property != childForeignKeyProperty)
                .toList();
        List<EntitySQLMapping.Property> nonPrimaryKeyChildProperties = childProperties.stream()
                .filter(property -> property != childPrimaryKey)
                .toList();
        return new CollectionRelation(accessor, childMapping, childPrimaryKey, childProperties,
                nonPrimaryKeyChildProperties, foreignKey.foreignColumn(), parentPrimaryKey,
                foreignKey.deleteRule() == DatabaseMetaData.importedKeyCascade);
    }

    private boolean relationField(Class<?> ownerType, Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !SQLAnnotationSupport.ignored(ownerType, field)
                && !Collection.class.isAssignableFrom(field.getType())
                && field.getType().isAnnotationPresent(Entity.class);
    }

    private boolean relationComponent(RecordComponent component) {
        return !SQLAnnotationSupport.ignored(component)
                && !Collection.class.isAssignableFrom(component.getType())
                && component.getType().isAnnotationPresent(Entity.class);
    }

    private boolean collectionField(Class<?> ownerType, Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !SQLAnnotationSupport.ignored(ownerType, field)
                && Collection.class.isAssignableFrom(field.getType())
                && collectionElementIsEntity(field);
    }

    private boolean collectionComponent(RecordComponent component) {
        return !SQLAnnotationSupport.ignored(component)
                && Collection.class.isAssignableFrom(component.getType())
                && collectionElementIsEntity(component);
    }

    private boolean collectionElementIsEntity(Field field) {
        Class<?> elementType = FieldUtil.getGenericTypeParameter(field);
        return elementType != null && elementType.isAnnotationPresent(Entity.class);
    }

    private boolean collectionElementIsEntity(RecordComponent component) {
        Class<?> elementType = RecordUtil.getGenericTypeParameter(component);
        return elementType != null && elementType.isAnnotationPresent(Entity.class);
    }

    private List<String> primaryKeyColumns(String tableName) throws SQLException {
        List<String> primaryKeys = primaryKeyColumnsExact(tableName);
        if (!primaryKeys.isEmpty()) {
            return primaryKeys;
        }
        return primaryKeyColumnsExact(tableName.toUpperCase(Locale.ROOT));
    }

    private boolean tableHasColumn(String tableName, String columnName) throws SQLException {
        return tableHasColumnExact(tableName, columnName)
                || tableHasColumnExact(tableName.toUpperCase(Locale.ROOT), columnName);
    }

    private boolean tableHasColumnExact(String tableName, String columnName) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                if (normalizeColumn(rs.getString("COLUMN_NAME")).equals(normalizeColumn(columnName))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeColumn(String column) {
        return column == null ? "" : column.replace("_", "").replace("\"", "").toLowerCase(Locale.ROOT);
    }

    private List<String> primaryKeyColumnsExact(String tableName) throws SQLException {
        var primaryKeys = new ArrayList<String>();
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private ForeignKeyColumns importedForeignKey(String foreignTable, String primaryTable) throws SQLException {
        ForeignKeyColumns foreignKey = importedForeignKeyExact(foreignTable, primaryTable);
        if (foreignKey != null) {
            return foreignKey;
        }
        return importedForeignKeyExact(foreignTable.toUpperCase(Locale.ROOT), primaryTable);
    }

    private ForeignKeyColumns importedForeignKeyExact(String foreignTable, String primaryTable) throws SQLException {
        var matches = new ArrayList<ForeignKeyColumns>();
        try (ResultSet rs = metaData.getImportedKeys(null, null, foreignTable)) {
            while (rs.next()) {
                if (sameTable(rs.getString("PKTABLE_NAME"), primaryTable)
                        && sameTable(rs.getString("FKTABLE_NAME"), foreignTable)) {
                    matches.add(new ForeignKeyColumns(rs.getString("FKCOLUMN_NAME"), rs.getString("PKCOLUMN_NAME"),
                            rs.getShort("DELETE_RULE")));
                }
            }
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Composite foreign keys are not supported from "
                    + foreignTable + " to " + primaryTable);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private boolean sameTable(String actual, String expected) {
        return normalizeTable(actual).equals(normalizeTable(expected));
    }

    private String normalizeTable(String table) {
        return table == null ? "" : table.replace("\"", "").toLowerCase(Locale.ROOT);
    }

    record CollectionRelation(CollectionAccessor accessor, EntitySQLMapping childMapping,
                              EntitySQLMapping.Property childPrimaryKey,
                              List<EntitySQLMapping.Property> childProperties,
                              List<EntitySQLMapping.Property> nonPrimaryKeyChildProperties,
                              String foreignKeyColumn, EntitySQLMapping.Property parentPrimaryKey,
                              boolean deleteCascades) {
    }

    private record ForeignKeyColumns(String foreignColumn, String primaryColumn, short deleteRule) {
    }

    private record RelationProperty(String name, String columnName, EntityAccessor accessor,
                                    EntitySQLMapping.Property referencedPrimaryKey) implements EntitySQLMapping.Property {

        @Override
        public Object get(Object object) {
            Object referenced = accessor.get(object);
            return referenced == null ? null : referencedPrimaryKey.get(referenced);
        }

        @Override
        public Class<?> type() {
            return referencedPrimaryKey.type();
        }
    }

    private interface EntityAccessor {
        Object get(Object object);
    }

    private record FieldEntityAccessor(Field field) implements EntityAccessor {
        @Override
        public Object get(Object object) {
            return FieldUtil.getFieldValue(object, field);
        }
    }

    private record RecordEntityAccessor(RecordComponent component) implements EntityAccessor {
        @Override
        public Object get(Object object) {
            try {
                return component.getAccessor().invoke(object);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not read record component " + component.getName(), e);
            }
        }
    }

    interface CollectionAccessor {
        String name();

        Collection<?> get(Object object);
    }

    private record FieldCollectionAccessor(Field field) implements CollectionAccessor {
        @Override
        public String name() {
            return field.getName();
        }

        @Override
        public Collection<?> get(Object object) {
            return (Collection<?>) FieldUtil.getFieldValue(object, field);
        }
    }

    private record RecordCollectionAccessor(RecordComponent component) implements CollectionAccessor {
        @Override
        public String name() {
            return component.getName();
        }

        @Override
        public Collection<?> get(Object object) {
            try {
                return (Collection<?>) component.getAccessor().invoke(object);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not read record component " + component.getName(), e);
            }
        }
    }
}
