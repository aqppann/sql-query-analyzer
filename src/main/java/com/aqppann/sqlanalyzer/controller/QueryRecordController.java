package com.aqppann.sqlanalyzer.controller;

import com.aqppann.sqlanalyzer.dto.QueryRecordRequestDto;
import com.aqppann.sqlanalyzer.dto.QueryRecordResponseDto;
import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import com.aqppann.sqlanalyzer.service.QueryRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.aqppann.sqlanalyzer.dto.QueryStatisticsDto;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/queries")
@RequiredArgsConstructor
@Tag(name = "Query Records", description = "API for managing and analyzing SQL queries")
public class QueryRecordController {
    private final QueryRecordService service;

    @PostMapping
    public ResponseEntity<QueryRecordResponseDto> create(
            @Valid @RequestBody QueryRecordRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<QueryRecordResponseDto>> findAll(
            @RequestParam(required = false) PerformanceStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueryRecordResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueryRecordResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody QueryRecordRequestDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/top-slow")
    public ResponseEntity<List<QueryRecordResponseDto>> findTopSlow() {
        return ResponseEntity.ok(service.findTopSlow());
    }

    @GetMapping("/statistics")
    public ResponseEntity<QueryStatisticsDto> getStatistics() {
        return ResponseEntity.ok(service.getStatistics());
    }
}