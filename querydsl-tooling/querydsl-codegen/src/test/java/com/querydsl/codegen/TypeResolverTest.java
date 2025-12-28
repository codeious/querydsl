/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.codegen.utils.model.ClassType;
import com.querydsl.codegen.utils.model.SimpleType;
import com.querydsl.codegen.utils.model.TypeCategory;
import com.querydsl.codegen.utils.model.TypeExtends;
import com.querydsl.codegen.utils.model.Types;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;

public class TypeResolverTest {

  @Test
  public void resolve_withNoSupertype_returnsResolvedType() {
    // Create an EntityType without any supertypes (getSuperType() returns null)
    var entityType = new EntityType(new ClassType(TypeCategory.ENTITY, String.class));

    // Create a declaring type with a type parameter
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Base",
            "com.example",
            "Base",
            false,
            false,
            new TypeExtends("T", Types.OBJECT));

    // Create a type with a varName that would need resolution
    var typeToResolve = new TypeExtends("T", Types.OBJECT);

    // When context has no supertype, should return the resolved type as-is
    var result = TypeResolver.resolve(typeToResolve, declaringType, entityType);
    assertThat(result).isEqualTo(typeToResolve);
  }

  @Test
  public void resolve_withNullEntityTypeInSupertype_returnsResolvedType() {
    // Create a Supertype without an EntityType set (getEntityType() returns null)
    var superTypeType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Parent",
            "com.example",
            "Parent",
            false,
            false,
            Types.STRING);
    var supertype = new Supertype(superTypeType);
    // Note: entityType is not set, so getEntityType() returns null

    Set<Supertype> superTypes = new LinkedHashSet<>();
    superTypes.add(supertype);
    var entityType = new EntityType(new ClassType(TypeCategory.ENTITY, String.class), superTypes);

    // Create a declaring type with a type parameter
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Base",
            "com.example",
            "Base",
            false,
            false,
            new TypeExtends("T", Types.OBJECT));

    // Create a type with a varName that would need resolution
    var typeToResolve = new TypeExtends("T", Types.OBJECT);

    // When entityType in supertype is null, should return resolved type as-is
    var result = TypeResolver.resolve(typeToResolve, declaringType, entityType);
    assertThat(result).isEqualTo(typeToResolve);
  }

  @Test
  public void resolve_withHierarchyNotContainingDeclaringType_returnsResolvedType() {
    // Create a hierarchy where declaringType is not found

    // First, create an entity type that will be in the supertype
    var parentType =
        new SimpleType(
            TypeCategory.ENTITY, "com.example.Parent", "com.example", "Parent", false, false);
    var parentEntityType = new EntityType(parentType);
    // Parent has no supertype (getSuperType() returns null for parent)

    // Create a Supertype with the parent entity
    var supertype = new Supertype(parentType, parentEntityType);

    Set<Supertype> superTypes = new LinkedHashSet<>();
    superTypes.add(supertype);
    var childEntityType =
        new EntityType(new ClassType(TypeCategory.ENTITY, String.class), superTypes);

    // Create a declaring type that is NOT in the hierarchy
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Unrelated",
            "com.example",
            "Unrelated",
            false,
            false,
            new TypeExtends("T", Types.OBJECT));

    // Create a type with a varName that would need resolution
    var typeToResolve = new TypeExtends("T", Types.OBJECT);

    // When declaringType is not found in hierarchy, should return resolved type as-is
    var result = TypeResolver.resolve(typeToResolve, declaringType, childEntityType);
    assertThat(result).isEqualTo(typeToResolve);
  }

  @Test
  public void resolve_withMatchingDeclaringTypeAndParameters_resolvesParameter() {
    // Create a declaring type with a type parameter
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Base",
            "com.example",
            "Base",
            false,
            false,
            new TypeExtends("T", Types.OBJECT));
    var declaringEntityType = new EntityType(declaringType);

    // Create a supertype that extends the declaring type with a concrete parameter
    var concreteSupertypeType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Base",
            "com.example",
            "Base",
            false,
            false,
            Types.STRING); // T is resolved to String
    var supertype = new Supertype(concreteSupertypeType, declaringEntityType);

    Set<Supertype> superTypes = new LinkedHashSet<>();
    superTypes.add(supertype);
    var childEntityType =
        new EntityType(new ClassType(TypeCategory.ENTITY, Object.class), superTypes);

    // Create a type with a varName that should be resolved
    var typeToResolve = new TypeExtends("T", Types.OBJECT);

    // Should resolve T to String
    var result = TypeResolver.resolve(typeToResolve, declaringType, childEntityType);
    assertThat(result).isEqualTo(Types.STRING);
  }

  @Test
  public void resolve_withRawType_returnsResolvedType() {
    // Create a declaring type with a type parameter
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY,
            "com.example.Base",
            "com.example",
            "Base",
            false,
            false,
            new TypeExtends("T", Types.OBJECT));
    var declaringEntityType = new EntityType(declaringType);

    // Create a supertype that extends the declaring type as raw type (no parameters)
    var rawSupertypeType =
        new SimpleType(
            TypeCategory.ENTITY, "com.example.Base", "com.example", "Base", false, false);
    var supertype = new Supertype(rawSupertypeType, declaringEntityType);

    Set<Supertype> superTypes = new LinkedHashSet<>();
    superTypes.add(supertype);
    var childEntityType =
        new EntityType(new ClassType(TypeCategory.ENTITY, Object.class), superTypes);

    // Create a type with a varName that would need resolution
    var typeToResolve = new TypeExtends("T", Types.OBJECT);

    // When supertype is raw (no parameters), should return resolved type as-is
    var result = TypeResolver.resolve(typeToResolve, declaringType, childEntityType);
    assertThat(result).isEqualTo(typeToResolve);
  }

  @Test
  public void resolve_nonGenericType_returnsOriginalType() {
    // Create an EntityType
    var entityType = new EntityType(new ClassType(TypeCategory.ENTITY, String.class));

    // Create a declaring type
    var declaringType =
        new SimpleType(
            TypeCategory.ENTITY, "com.example.Base", "com.example", "Base", false, false);

    // Non-generic type should be returned as-is
    var result = TypeResolver.resolve(Types.STRING, declaringType, entityType);
    assertThat(result).isEqualTo(Types.STRING);
  }
}
