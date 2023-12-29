package com.tf4.photospot.spot.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSpotBookmark is a Querydsl query type for SpotBookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSpotBookmark extends EntityPathBase<SpotBookmark> {

    private static final long serialVersionUID = -839626903L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSpotBookmark spotBookmark = new QSpotBookmark("spotBookmark");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final QBookmarkFolder bookmarkFolder;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSpot spot;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.tf4.photospot.user.domain.QUser user;

    public QSpotBookmark(String variable) {
        this(SpotBookmark.class, forVariable(variable), INITS);
    }

    public QSpotBookmark(Path<? extends SpotBookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSpotBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSpotBookmark(PathMetadata metadata, PathInits inits) {
        this(SpotBookmark.class, metadata, inits);
    }

    public QSpotBookmark(Class<? extends SpotBookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bookmarkFolder = inits.isInitialized("bookmarkFolder") ? new QBookmarkFolder(forProperty("bookmarkFolder"), inits.get("bookmarkFolder")) : null;
        this.spot = inits.isInitialized("spot") ? new QSpot(forProperty("spot")) : null;
        this.user = inits.isInitialized("user") ? new com.tf4.photospot.user.domain.QUser(forProperty("user")) : null;
    }

}

