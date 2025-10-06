package com.example.reader.service;

import com.example.reader.entity.Book;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BookService {
    Book uploadEPUB(MultipartFile file) throws Exception;
}