package com.tf4.photospot.post.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = -109327537L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    public final ComparablePath<org.locationtech.jts.geom.Point> coord = createComparable("coord", org.locationtech.jts.geom.Point.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath detailAddress = createString("detailAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPrivate = createBoolean("isPrivate");

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final ListPath<Mention, QMention> mentions = this.<Mention, QMention>createList("mentions", Mention.class, QMention.class, PathInits.DIRECT2);

    public final com.tf4.photospot.photo.domain.QPhoto photo;

    public final ListPath<PostTag, QPostTag> postTags = this.<PostTag, QPostTag>createList("postTags", PostTag.class, QPostTag.class, PathInits.DIRECT2);

    public final com.tf4.photospot.spot.domain.QSpot spot;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.tf4.photospot.user.domain.QUser writer;

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.photo = inits.isInitialized("photo") ? new com.tf4.photospot.photo.domain.QPhoto(forProperty("photo"), inits.get("photo")) : null;
        this.spot = inits.isInitialized("spot") ? new com.tf4.photospot.spot.domain.QSpot(forProperty("spot")) : null;
        this.writer = inits.isInitialized("writer") ? new com.tf4.photospot.user.domain.QUser(forProperty("writer")) : null;
    }

}

