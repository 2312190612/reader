package com.example.reader.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;   // 示例字段，先保证能编译
}