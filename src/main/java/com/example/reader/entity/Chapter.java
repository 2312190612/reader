package com.example.reader.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_chapter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer seq;        // 章节序号

    private String title;

    @Column(columnDefinition = "CLOB")
    private String htmlContent; // 整章 html

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
}