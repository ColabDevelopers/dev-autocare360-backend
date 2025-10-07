package com.example.autocare360.repo.sample;

import com.example.autocare360.entity.sample.SampleItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleItemRepository extends JpaRepository<SampleItem, Long> {
	List<SampleItem> findByNameContainingIgnoreCase(String namePart);
}


