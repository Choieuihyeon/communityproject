package com.zerobase.communityproject.post.service.impl;

import com.zerobase.communityproject.post.dto.PostDto;
import com.zerobase.communityproject.post.entity.Post;
import com.zerobase.communityproject.post.mapper.PostMapper;
import com.zerobase.communityproject.post.model.PostInput;
import com.zerobase.communityproject.post.model.PostParam;
import com.zerobase.communityproject.post.repository.PostRepository;
import com.zerobase.communityproject.post.service.PostService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final PostMapper postMapper;

	@Override
	public boolean add(PostInput parameter, Principal principal) {
		String userId = principal.getName();

		Post post = Post.builder()
			.id(parameter.getId())
			.userId(userId)
			.title(parameter.getTitle())
			.contents(parameter.getContents())
			.registeredAt(LocalDateTime.now())
			.modifiedAt(LocalDateTime.now())
			.build();

		postRepository.save(post);

		return true;
	}

	@Override
	public boolean set(PostInput parameter) {

		Optional<Post> optionalPost = postRepository.findById(parameter.getId());
		if (!optionalPost.isPresent()) {
			return false;
		}

		Post post = optionalPost.get();
		post.setTitle(parameter.getTitle());
		post.setContents(parameter.getContents());
		post.setModifiedAt(LocalDateTime.now());
		postRepository.save(post);

		return true;
	}

	@Override
	public List<PostDto> list(PostParam parameter) {

		long totalCount = postMapper.selectListCount(parameter);

		List<PostDto> list = postMapper.selectList(parameter);
		if (!CollectionUtils.isEmpty(list)) {
			int i = 0;
			for (PostDto x : list) {
				x.setTotalCount(totalCount);
				x.setSeq(totalCount - parameter.getPageStart() - i);
				i++;
			}
		}

		return list;
	}

	@Override
	public boolean delete(String idList) {

		if (idList != null && idList.length() > 0) {

			String[] ids = idList.split(",");
			for (String x : ids) {
				long id = 0L;
				try {
					id = Long.parseLong(x);
				} catch (Exception e) {
				}

				if (id > 0) {
					postRepository.deleteById(id);
				}
			}
		}
		return false;
	}

	@Override
	public PostDto frontDetail(long id) {

		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			return PostDto.of(optionalPost.get());
		}
		return null;
	}

	private Sort getSortBySortValueDesc() {
		return Sort.by(Sort.Direction.DESC, "sortValue");
	}

	@Override
	public List<PostDto> list() {
		List<Post> posts = postRepository.findAll(getSortBySortValueDesc());
		return PostDto.of(posts);
	}

	@Override
	public PostDto getById(long id) {

		return postRepository.findById(id).map(PostDto::of).orElse(null);
	}

	@Override
	public PostDto detail(String title) {

		Optional<Post> optionalPost = postRepository.findByTitle(title);
		if (!optionalPost.isPresent()) {
			return null;
		}

		Post post = optionalPost.get();

		return PostDto.of(post);
	}

	// 나의 정보만 볼 수 있는 것
	@Override
	public List<PostDto> myPost(String userId) {

		PostParam parameter = new PostParam();

		parameter.setUserId(userId);

		long totalCount = postMapper.selectListMyPostCount(parameter);

		List<PostDto> list = postMapper.selectListMyPost(parameter);

		return list;
	}

}

