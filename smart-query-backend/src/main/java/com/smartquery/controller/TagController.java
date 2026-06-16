package com.smartquery.controller;

import com.smartquery.model.TagDefinition;
import com.smartquery.service.MetadataService;
import com.smartquery.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    private final MetadataService metadataService;

    public TagController(TagService tagService, MetadataService metadataService) {
        this.tagService = tagService;
        this.metadataService = metadataService;
    }

    @GetMapping
    public List<TagDefinition> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/{id}")
    public TagDefinition getTagById(@PathVariable Long id) {
        return tagService.getTagById(id);
    }

    @PostMapping
    public TagDefinition createTag(@RequestBody TagDefinition tag) {
        return tagService.createTag(tag);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateTag(@PathVariable Long id, @RequestBody TagDefinition tag) {
        tag.setId(id);
        tagService.updateTag(tag);
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Map.of("success", true);
    }

    @GetMapping("/tables/{tableId}")
    public List<TagDefinition> getTagsByTableId(@PathVariable Long tableId) {
        return tagService.getTagsByTableId(tableId);
    }

    @PutMapping("/tables/{tableId}")
    public Map<String, Object> setTagsForTable(@PathVariable Long tableId, @RequestBody List<Long> tagIds) {
        tagService.setTagsForTable(tableId, tagIds);
        return Map.of("success", true);
    }

    @GetMapping("/filter/{tagId}")
    public List<?> getTablesByTag(@PathVariable Long tagId) {
        if (tagId == 0) {
            return metadataService.getAllTables();
        }
        return metadataService.getTablesByTag(tagId);
    }
}
