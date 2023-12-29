package com.tf4.photospot.photo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBubble is a Querydsl query type for Bubble
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBubble extends EntityPathBase<Bubble> {

    private static final long serialVersionUID = 506756487L;

    public static final QBubble bubble = new QBubble("bubble");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final ComparablePath<org.locationtech.jts.geom.Point> coord = createComparable("coord", org.locationtech.jts.geom.Point.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath text = createString("text");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBubble(String variable) {
        super(Bubble.class, forVariable(variable));
    }

    public QBubble(Path<? extends Bubble> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBubble(PathMetadata metadata) {
        super(Bubble.class, metadata);
    }

}

