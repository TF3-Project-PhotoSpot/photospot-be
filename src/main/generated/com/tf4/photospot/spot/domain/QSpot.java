package com.tf4.photospot.spot.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSpot is a Querydsl query type for Spot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSpot extends EntityPathBase<Spot> {

    private static final long serialVersionUID = 1888946451L;

    public static final QSpot spot = new QSpot("spot");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final StringPath address = createString("address");

    public final ComparablePath<org.locationtech.jts.geom.Point> coord = createComparable("coord", org.locationtech.jts.geom.Point.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> postCount = createNumber("postCount", Long.class);

    public final ListPath<com.tf4.photospot.post.domain.Post, com.tf4.photospot.post.domain.QPost> posts = this.<com.tf4.photospot.post.domain.Post, com.tf4.photospot.post.domain.QPost>createList("posts", com.tf4.photospot.post.domain.Post.class, com.tf4.photospot.post.domain.QPost.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSpot(String variable) {
        super(Spot.class, forVariable(variable));
    }

    public QSpot(Path<? extends Spot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSpot(PathMetadata metadata) {
        super(Spot.class, metadata);
    }

}

