package tn.cyberious.compta.document.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.dto.TagRequest;
import tn.cyberious.compta.document.dto.TagResponse;
import tn.cyberious.compta.document.generated.tables.pojos.Tags;
import tn.cyberious.compta.document.repository.TagRepository;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  @Transactional
  public TagResponse create(TagRequest request) {
    log.info("Creating tag: {}", request.getName());

    if (tagRepository.existsByName(request.getName())) {
      throw new IllegalArgumentException(
          "Tag with name '" + request.getName() + "' already exists");
    }

    Tags tag = new Tags();
    tag.setName(request.getName());

    Tags saved = tagRepository.insert(tag);
    return toResponse(saved);
  }

  @Transactional
  public void delete(Long id) {
    log.info("Deleting tag: {}", id);

    if (!tagRepository.exists(id)) {
      throw new ResourceNotFoundException("Tag not found: " + id);
    }

    tagRepository.delete(id);
  }

  @Transactional(readOnly = true)
  public TagResponse getById(Long id) {
    log.debug("Getting tag: {}", id);

    Tags tag =
        tagRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id));

    return toResponse(tag);
  }

  @Transactional(readOnly = true)
  public List<TagResponse> getAll() {
    log.debug("Getting all tags");
    return tagRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<TagResponse> search(String query) {
    log.debug("Searching tags: {}", query);
    return tagRepository.searchByName(query).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public Tags findOrCreate(String name) {
    return tagRepository.findOrCreate(name);
  }

  public TagResponse toResponse(Tags tag) {
    return TagResponse.builder()
        .id(tag.getId())
        .name(tag.getName())
        .createdAt(tag.getCreatedAt())
        .build();
  }
}
