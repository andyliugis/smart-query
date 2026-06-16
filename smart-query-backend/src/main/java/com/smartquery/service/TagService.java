package com.smartquery.service;

import com.smartquery.model.TagDefinition;
import com.smartquery.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagDefinition> getAllTags() {
        return tagRepository.findAllTags();
    }

    public TagDefinition getTagById(Long id) {
        return tagRepository.findTagById(id);
    }

    public TagDefinition createTag(TagDefinition tag) {
        Long id = tagRepository.createTag(tag);
        tag.setId(id);
        return tag;
    }

    public void updateTag(TagDefinition tag) {
        tagRepository.updateTag(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteTag(id);
    }

    public List<TagDefinition> getTagsByTableId(Long tableId) {
        return tagRepository.findTagsByTableId(tableId);
    }

    public void setTagsForTable(Long tableId, List<Long> tagIds) {
        tagRepository.setTagsForTable(tableId, tagIds);
    }
}
