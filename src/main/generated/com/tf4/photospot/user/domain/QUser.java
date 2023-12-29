package com.tf4.photospot.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1851913947L;

    public static final QUser user = new QUser("user");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final StringPath account = createString("account");

    public final ListPath<com.tf4.photospot.spot.domain.BookmarkFolder, com.tf4.photospot.spot.domain.QBookmarkFolder> bookmarkFolders = this.<com.tf4.photospot.spot.domain.BookmarkFolder, com.tf4.photospot.spot.domain.QBookmarkFolder>createList("bookmarkFolders", com.tf4.photospot.spot.domain.BookmarkFolder.class, com.tf4.photospot.spot.domain.QBookmarkFolder.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath profileUrl = createString("profileUrl");

    public final StringPath providerType = createString("providerType");

    public final StringPath role = createString("role");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

