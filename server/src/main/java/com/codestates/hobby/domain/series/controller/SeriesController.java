package com.codestates.hobby.domain.series.controller;

import com.codestates.hobby.domain.member.entity.Member;
import com.codestates.hobby.domain.post.mapper.PostMapper;
import com.codestates.hobby.domain.series.dto.SeriesDto;
import com.codestates.hobby.domain.series.entity.Series;
import com.codestates.hobby.domain.series.mapper.SeriesMapper;
import com.codestates.hobby.domain.series.service.SeriesService;

import com.codestates.hobby.global.support.resolver.CustomPageRequest;
import com.codestates.hobby.global.dto.MultiResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class SeriesController {
    private final SeriesService seriesService;
    private final SeriesMapper seriesMapper;

    private final PostMapper postMapper;

    @PostMapping("/series")
    public ResponseEntity<?> post(@Valid @RequestBody SeriesDto.Post post,
                                  @AuthenticationPrincipal Member loginMember) {
        post.setMemberId(loginMember.getId());

        Series series =  seriesService.create(post);

        return new ResponseEntity(series.getId(), HttpStatus.CREATED);
    }

    @PatchMapping("/series/{series-id}")
    public ResponseEntity patch(@PathVariable("series-id") long seriesId,
                                @Valid @RequestBody SeriesDto.Patch patch,
                                @AuthenticationPrincipal Member loginMember) {
        patch.setSeriesId(seriesId);
        seriesService.edit(patch, loginMember.getId());

        return new ResponseEntity(seriesId, HttpStatus.OK);
    }

    @DeleteMapping("/series/{series-id}")
    public ResponseEntity delete(@PathVariable("series-id") long seriesId,
                                 @AuthenticationPrincipal Member loginMember) {
        seriesService.delete(seriesId, loginMember.getId());

        return new ResponseEntity(seriesId, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/series/{series-id}")
    public ResponseEntity get(@PathVariable("series-id") long seriesId,
                              CustomPageRequest pageRequest) {
        Series series = seriesService.get(seriesId);

        SeriesDto.SimpleResponse response = seriesMapper.SeriesToSimpleResponseDto(series);

        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/series")
    public ResponseEntity getAll(CustomPageRequest pageRequest) {
        Page<Series> series = seriesService.findAll(pageRequest.to());

        Page<SeriesDto.Response> responses = series.map(aSeries -> {
            SeriesDto.Response response = seriesMapper.SeriesToResponseDto(aSeries);
            response.setPost(aSeries.getPosts().isEmpty() ? null : postMapper.postToSimpleResponse(aSeries.getPosts().get(0)));
            return response;
        });

        return new ResponseEntity(new MultiResponseDto<>(responses), HttpStatus.OK);
    }

    @GetMapping("/categories/{category-name}/series")
    public ResponseEntity getAllByCategory(@PathVariable("category-name") String category,
                                           CustomPageRequest pageRequest) {
        Page<Series> series = seriesService.findAllByCategory(category, pageRequest.to());
        Page<SeriesDto.Response> responses = series.map(aSeries -> {
            SeriesDto.Response response = seriesMapper.SeriesToResponseDto(aSeries);
            response.setPost(aSeries.getPosts().isEmpty() ? null : postMapper.postToSimpleResponse(aSeries.getPosts().get(0)));
            return response;
        });

        return new ResponseEntity(new MultiResponseDto<>(responses), HttpStatus.OK);
    }

    @GetMapping("/members/{member-id}/series")
    public ResponseEntity getAllByMember(@PathVariable("member-id") long memberId,
                                         //@AuthenticationPrincipal Member loginMember,
                                         CustomPageRequest pageRequest) {
        //if(memberId != loginMember.getId()) throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        Page<Series> series = seriesService.findAllByMember(memberId, pageRequest.to());

        Page<SeriesDto.SimpleResponse> responses = series.map(seriesMapper::SeriesToSimpleResponseDto);

        return new ResponseEntity<>(new MultiResponseDto<>(responses), HttpStatus.OK);
    }

    @GetMapping("/series/search")
    public ResponseEntity<?> search(@RequestParam String query, CustomPageRequest pageRequest) {
        Page<Series> series = seriesService.search(query, pageRequest.to());

        Page<SeriesDto.SimpleResponse> responses = series.map(seriesMapper::SeriesToSimpleResponseDto);

        return new ResponseEntity<>(new MultiResponseDto<>(responses), HttpStatus.OK);
    }
}