package com.example.autocare360.service.sample;

import com.example.autocare360.entity.sample.SampleItem;
import com.example.autocare360.dto.sample.SampleItemRequest;
import com.example.autocare360.dto.sample.SampleItemResponse;
import com.example.autocare360.exception.ResourceNotFoundException;
import com.example.autocare360.repo.sample.SampleItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SampleItemService {

	private final SampleItemRepository repository;

	@Transactional
	public SampleItemResponse create(SampleItemRequest request) {
		SampleItem entity = SampleItem.builder()
				.name(request.getName())
				.description(request.getDescription())
				.build();
		SampleItem saved = repository.save(entity);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public SampleItemResponse getById(Long id) {
		SampleItem entity = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SampleItem not found: " + id));
		return toResponse(entity);
	}

	@Transactional(readOnly = true)
	public List<SampleItemResponse> list(String search) {
		List<SampleItem> items = (search == null || search.isBlank())
				? repository.findAll()
				: repository.findByNameContainingIgnoreCase(search);
		return items.stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional
	public SampleItemResponse update(Long id, SampleItemRequest request) {
		SampleItem entity = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SampleItem not found: " + id));
		entity.setName(request.getName());
		entity.setDescription(request.getDescription());
		SampleItem saved = repository.save(entity);
		return toResponse(saved);
	}

	@Transactional
	public void delete(Long id) {
		SampleItem entity = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SampleItem not found: " + id));
		repository.delete(entity);
	}

	private SampleItemResponse toResponse(SampleItem entity) {
		return SampleItemResponse.builder()
				.id(entity.getId())
				.name(entity.getName())
				.description(entity.getDescription())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}


