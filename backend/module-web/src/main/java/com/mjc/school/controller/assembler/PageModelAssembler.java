
package com.mjc.school.controller.assembler;

import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class PageModelAssembler {

    public <T> PageDtoResponse<T> addPaginationLinks(
            PageDtoResponse<T> page,
            ResourceSearchFilterRequestDTO searchRequest,
            String baseUrl) {

        int currentPage = page.getCurrentPage();
        int totalPages = page.getPageCount();

        page.add(createPageLink(baseUrl, searchRequest, currentPage).withSelfRel());

        page.add(createPageLink(baseUrl, searchRequest, 1).withRel("first"));

        page.add(createPageLink(baseUrl, searchRequest, totalPages).withRel("last"));

        if (currentPage > 1) {
            page.add(createPageLink(baseUrl, searchRequest, currentPage - 1).withRel("prev"));
        }

        if (currentPage < totalPages) {
            page.add(createPageLink(baseUrl, searchRequest, currentPage + 1).withRel("next"));
        }

        return page;
    }

    private Link createPageLink(String baseUrl, ResourceSearchFilterRequestDTO searchRequest, int pageNumber) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", pageNumber)
                .queryParam("pageSize", searchRequest.getPageSize());

        if (searchRequest.getSortByAndOrder() != null && !searchRequest.getSortByAndOrder().isEmpty()) {
            searchRequest.getSortByAndOrder().forEach(sort ->
                    builder.queryParam("sortByAndOrder", sort));
        }

        if (searchRequest.getSearchFilter() != null && !searchRequest.getSearchFilter().isEmpty()) {
            searchRequest.getSearchFilter().forEach(filter ->
                    builder.queryParam("searchCriteria", filter));
        }

        return Link.of(builder.toUriString());
    }
}