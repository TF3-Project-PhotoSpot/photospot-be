package com.tf4.photospot.post.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMention is a Querydsl query type for Mention
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMention extends EntityPathBase<Mention> {

    private static final long serialVersionUID = -49792805L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMention mention = new QMention("mention");

    public final com.tf4.photospot.global.entity.QBaseEntity _super = new com.tf4.photospot.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.tf4.photospot.user.domain.QUser mentionedUser;

    public final QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMention(String variable) {
        this(Mention.class, forVariable(variable), INITS);
    }

    public QMention(Path<? extends Mention> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMention(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMention(PathMetadata metadata, PathInits inits) {
        this(Mention.class, metadata, inits);
    }

    public QMention(Class<? extends Mention> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mentionedUser = inits.isInitialized("mentionedUser") ? new com.tf4.photospot.user.domain.QUser(forProperty("mentionedUser")) : null;
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post"), inits.get("post")) : null;
    }

}

