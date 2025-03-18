package com.example.demo.repository;

import com.example.demo.entity.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParagraphRepository extends JpaRepository<Paragraph, Long> {

    List<Paragraph> findAllByUserId(Long id);
}
