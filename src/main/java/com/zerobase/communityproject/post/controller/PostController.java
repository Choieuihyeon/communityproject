package com.zerobase.communityproject.post.controller;

import com.zerobase.communityproject.member.dto.MemberDto;
import com.zerobase.communityproject.member.service.MemberService;
import com.zerobase.communityproject.post.dto.PostDto;
import com.zerobase.communityproject.post.model.PostInput;
import com.zerobase.communityproject.post.model.PostParam;
import com.zerobase.communityproject.post.service.PostService;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PostController extends BaseController {

	private final PostService postService;
	private final MemberService memberService;

	// 구현 완료
	@GetMapping("/post/list")
	public String list(Model model, PostParam parameter) {

		parameter.init();
		List<PostDto> postList = postService.list(parameter);

		long totalCount = 0;
		if (!CollectionUtils.isEmpty(postList)) {
			totalCount = postList.get(0).getTotalCount();
		}

		String queryString = parameter.getQueryString();
		String pagerHtml = getPagerHtml(totalCount, parameter.getPageSize(),
			parameter.getPageIndex(), queryString);

		model.addAttribute("list", postList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pager", pagerHtml);

		List<MemberDto> memberList = memberService.listAll();
		model.addAttribute("memberList", memberList);

		return "post/list";
	}

	// 구현 완료
	@GetMapping("/post/add")
	public String add(Model model, HttpServletRequest request, PostInput parameter) {

		boolean editMode = request.getRequestURI().contains("/edit");
		PostDto detail = new PostDto();

		if (editMode) {
			long id = parameter.getId();
			PostDto exitPost = postService.getById(id);
			if (exitPost == null) {
				// error 처리
				model.addAttribute("message", "게시물정보가 존재하지 않습니다.");
				return "common/error";
			}
			detail = exitPost;
		}

		model.addAttribute("editMode", editMode);
		model.addAttribute("detail", detail);
		return "post/add";
	}

	// 구현 완료
	@PostMapping("/post/add")
	public String addSubmit(Model model, HttpServletRequest request
		, PostInput parameter) {

		boolean editMode = request.getRequestURI().contains("/edit");

		if (editMode) {
			long id = parameter.getId();
			PostDto existPost = postService.getById(id);
			if (existPost == null) {
				// error 처리
				model.addAttribute("message", "게시물정보가 존재하지 않습니다.");
				return "common/error";
			}
			boolean result = postService.set(parameter);
		} else {
			boolean result = postService.add(parameter, request.getUserPrincipal());
		}

		return "redirect:/post/list";
	}

	// 구현 완료
	@PostMapping("/post/delete")
	public String delete(Model model, HttpServletRequest request, PostInput parameter) {

		boolean result = postService.delete(parameter.getIdList());

		return "redirect:/post/list";
	}

	// 구현 완료
	@GetMapping("/post/detail/{id}")
	public String detail(Model model, PostParam parameter) {

		parameter.init();

		PostDto detail = postService.frontDetail(parameter.getId());

		model.addAttribute("post", detail);

		return "/post/detail";
	}

	// 구현 완료
	@GetMapping("/post/myPost")
	public String myPost(Model model, PostParam parameter, Principal principal) {

		String userId = principal.getName();

		List<PostDto> list = postService.myPost(userId);

		parameter.init();

		List<PostDto> postList = postService.list(parameter);

		long totalCount = 0;
		if (!CollectionUtils.isEmpty(list)) {
			totalCount = list.get(0).getTotalCount();
		}

		String queryString = parameter.getQueryString();
		String pagerHtml = getPagerHtml(totalCount, parameter.getPageSize(),
			parameter.getPageIndex(), queryString);

		model.addAttribute("list", postList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pager", pagerHtml);

		model.addAttribute("myPost", list);

		return "post/myPost";
	}

	// 구현 완료
	@GetMapping("/post/update/{id}")
	public String update(Model model, @PathVariable Long id) {

		PostDto post = postService.getById(id);
		model.addAttribute("post", post);

		return "post/update";
	}

	// 구현 완료
	@PostMapping("/post/update/{id}")
	public String updateSubmit(@PathVariable Long id, PostInput parameter) {

		parameter.setId(id);
		postService.set(parameter);

		return "redirect:/post/list";
	}
}
