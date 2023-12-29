package com.tf4.photospot.spot.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookmarkFolder is a Querydsl query type for BookmarkFolder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookmarkFolder extends EntityPathBase<BookmarkFolder> {

    private static final long serialVersionUID = -632857579L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookmarkFolder bookmarkFolder = new QBookmarkFolder("bookmarkFolder");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final StringPath color = createString("color");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final ListPath<SpotBookmark, QSpotBookmark> spotBookmarks = this.<SpotBookmark, QSpotBookmark>createList("spotBookmarks", SpotBookmark.class, QSpotBookmark.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.tf4.photospot.user.domain.QUser user;

    public QBookmarkFolder(String variable) {
        this(BookmarkFolder.class, forVariable(variable), INITS);
    }

    public QBookmarkFolder(Path<? extends BookmarkFolder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookmarkFolder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookmarkFolder(PathMetadata metadata, PathInits inits) {
        this(BookmarkFolder.class, metadata, inits);
    }

    public QBookmarkFolder(Class<? extends BookmarkFolder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.tf4.photospot.user.domain.QUser(forProperty("user")) : null;
    }

}

