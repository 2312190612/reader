package com.example.reader.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_book")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String coverPath;   // 封面磁盘路径

    private String filePath;    // 新增：EPUB 文件磁盘路径

    private LocalDateTime uploadTime;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<Chapter> chapters = new ArrayList<>();
}