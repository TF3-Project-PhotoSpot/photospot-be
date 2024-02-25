package com.tf4.photospot.support;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.album.domain.Album;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.spot.domain.BookmarkFolder;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotBookmark;
import com.tf4.photospot.user.domain.User;

public class TestFixture {
	private static final AtomicLong COORD_UNIQUE_KEY = new AtomicLong(1L);
	private static final Double COORD_UNIT_VALUE = 0.0001;
	private static final Random RANDOM = new Random();
	private static final int LIKE_COUNT_RANGE = 100;

	public static Spot createSpot(String address, Point coord, Long postCount) {
		return Spot.builder()
			.address(address)
			.coord(coord)
			.postCount(postCount)
			.build();
	}

	public static Spot createSpot() {
		return createSpot("주소", createPoint(), 0L);
	}

	public static Spot createSpot(CoordinateDto coord) {
		return Spot.builder()
			.address("주소")
			.coord(coord.toCoord())
			.postCount(0L)
			.build();
	}

	public static Post createPost(Spot spot, User user) {
		spot.incPostCount();
		return createPost(spot, user, 0L);
	}

	public static Post createPost(Spot spot, User user, boolean isPrivate) {
		spot.incPostCount();
		return createPost(spot, user, createPhoto(), 0L, isPrivate);
	}

	public static Post createPost(Spot spot, User user, Long likeCount) {
		spot.incPostCount();
		return createPost(spot, user, createPhoto(), likeCount, false);
	}

	public static Post createPost(Spot spot, User user, Photo photo) {
		spot.incPostCount();
		return createPost(spot, user, photo, 0L, false);
	}

	public static Post createPost(Spot spot, User user, Photo photo, Long likeCount, boolean isPrivate) {
		spot.incPostCount();
		return Post.builder()
			.spot(spot)
			.writer(user)
			.photo(photo)
			.detailAddress("디테일 주소")
			.likeCount(likeCount)
			.isPrivate(isPrivate)
			.build();
	}

	public static long createRandomLikeCount() {
		return RANDOM.nextInt(LIKE_COUNT_RANGE);
	}

	public static Photo createPhoto(String photoUrl) {
		return Photo.builder()
			.photoUrl(photoUrl)
			.coord(createPoint())
			.build();
	}

	public static Photo createPhoto() {
		return createPhoto("photoUrl");
	}

	public static User createUser(String nickname, String account, String providerType) {
		return User.builder()
			.nickname(nickname)
			.account(account)
			.providerType(providerType)
			.build();
	}

	public static User createUser(String nickname) {
		return createUser(nickname, null, null);
	}

	public static SpotBookmark createSpotBookmark(User user, Spot spot, BookmarkFolder bookmarkFolder) {
		return SpotBookmark.builder()
			.user(user)
			.spot(spot)
			.bookmarkFolder(bookmarkFolder)
			.description("기본 폴더")
			.build();
	}

	public static <T> List<T> createList(Supplier<T> construct, int total) {
		return IntStream.range(0, total)
			.mapToObj(i -> construct.get())
			.toList();
	}

	public static Point createPoint() {
		final double key = COORD_UNIQUE_KEY.getAndIncrement();
		return PointConverter.convert(
			124.000 + (key * COORD_UNIT_VALUE),
			33.000 + (key * COORD_UNIT_VALUE)
		);
	}

	public static List<Tag> createTags(String... tagNames) {
		return Arrays.stream(tagNames).map(TestFixture::createTag).toList();
	}

	public static Tag createTag(String tagName) {
		return Tag.builder()
			.name(tagName)
			.iconUrl("iconUrl")
			.build();
	}

	public static PostTag createPostTag(Spot spot, Post post, Tag tag) {
		return PostTag.builder()
			.spot(spot)
			.post(post)
			.tag(tag)
			.build();
	}

	public static List<PostTag> createPostTags(Spot spot, Post post, List<Tag> tags) {
		return tags.stream()
			.map(tag -> createPostTag(spot, post, tag))
			.toList();
	}

	public static PostLike createPostLike(Post post, User user) {
		return PostLike.builder()
			.post(post)
			.user(user)
			.build();
	}

	public static Mention createMention(Post post, User mentionedUser) {
		return new Mention(post, mentionedUser);
	}

	public static List<Mention> createMentions(Post post, List<User> mentionedUsers) {
		return mentionedUsers.stream()
			.map(user -> createMention(post, user))
			.toList();
	}

	public static Album createAlbum() {
		return new Album("album");
	}
}
